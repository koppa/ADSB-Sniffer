package de.graeb.adsbsniffer.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.graeb.adsbsniffer.DatabaseHelper;
import de.graeb.adsbsniffer.Recorder;

/**
 * Fragment displaying stored recordings
 */
public class RecordingListFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final DecimalFormat FORMAT_1POINTS = new DecimalFormat("0.0");

    public static RecordingListFragment newInstance(int sectionNumber) {
        RecordingListFragment fragment = new RecordingListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RecordingListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recordinglist, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_recordings);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        RecordingAdapter adapter = new RecordingAdapter();
        listView.setAdapter(adapter);

        rootView.findViewById(R.id.button_send).setOnClickListener(adapter);
        rootView.findViewById(R.id.button_delete).setOnClickListener(adapter);

        return rootView;
    }

    private class RecordingAdapter extends BaseAdapter implements View.OnClickListener {
        private LinkedList<Entry> list;
        private List<Integer> selected;

        public RecordingAdapter() {
            updateList();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                view = layoutInflater.inflate(R.layout.item_recordinglist, null);
            } else {
                view = convertView;
            }

            TextView textLarge = (TextView) view.findViewById(R.id.text_item_large);
            TextView textSmall = (TextView) view.findViewById(R.id.text_item_small);

            textLarge.setText(list.get(position).title);
            textSmall.setText(list.get(position).stats);

            CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !selected.contains(position)) {
                        selected.add(position);
                    } else if (!isChecked) {
                        selected.remove(Integer.valueOf(position));
                    }
                }
            });
            checkBox.setChecked(selected.contains(position));

            view.setOnClickListener(new OnClickRename(position));
            return view;
        }


        public void updateList() {
            this.list = new LinkedList<>();

            LinkedList<File> dbFiles = new LinkedList<>();
            // internal
            for (String dbName : getActivity().databaseList()) {
                dbFiles.add(getActivity().getDatabasePath(dbName));
            }
            // external
            File external = new File(Environment.getExternalStorageDirectory(), "ADSB-Sniffer");
            if (external.exists()) {
                dbFiles.addAll(Arrays.asList(external.listFiles()));
            }

            for (File dbFile : dbFiles) {
                String fileName = dbFile.getName();
                if (!fileName.equals(Recorder.getInstance().getCurrentRecording()) && !fileName.endsWith("-journal")) {
                    DatabaseHelper dbHelper = new DatabaseHelper(getActivity(), dbFile.getAbsolutePath());

                    String customName = dbHelper.getName();

                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    long countMessages = DatabaseUtils.queryNumEntries(db, "messages");

                    // if no messages contained, delete recording
                    if (countMessages == 0) {
                        dbHelper.close();
                        dbFile.delete();
                        continue;
                    }

                    Cursor cursor = db.rawQuery("SELECT MIN(time) FROM messages", null);
                    cursor.moveToFirst();
                    long timeStart = (long) cursor.getInt(0);

                    cursor = db.rawQuery("SELECT MAX(time) FROM messages", null);
                    cursor.moveToFirst();
                    long timeEnd = (long) cursor.getInt(0);
                    dbHelper.close();

                    DateFormat dateFormat = DateFormat.getDateTimeInstance();
                    list.add(new Entry(dbFile,
                            customName != null?customName:dateFormat.format(new Date(timeStart * 1000)),
                            String.format(getActivity().getString(R.string.recordinglist_item_description),
                                    countMessages,
                                    FORMAT_1POINTS.format(((double) (timeEnd - timeStart)) / (60 * 60)),
                                    dbFile.length() / 1024)));
                }
            }

            notifyDataSetChanged();
            selected = new LinkedList<>();
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.button_delete) {
                boolean deleted = true;

                if (selected.size() == 0) {
                    Toast.makeText(getActivity(), "No entries selected", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                for (int i = selected.size() - 1; i >= 0; i--) {
                    File file = list.get(selected.get(i)).dbFile;
                    deleted &= file.delete();
                }
                updateList();

                if (deleted) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.files_deleted),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.files_not_deleted),
                            Toast.LENGTH_LONG).show();
                }
            } else if (v.getId() == R.id.button_send) {

                if (selected.size() == 0) {
                    Toast.makeText(getActivity(), "At least one entry has to be selected!", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                try {
                    ArrayList<Uri> uris = new ArrayList<>();
                    for (Integer pos : selected) {
                        File file = list.get(pos).dbFile;
                        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ADSB-Sniffer");

                        File externalFile;
                        // if stored on internal storage, move to external
                        if (!file.getParentFile().equals(directory)) {
                            if (!directory.exists()) {
                                directory.mkdir();
                            }
                            externalFile = new File(directory, file.getName());
                            copy(file, externalFile);
                            file.delete();
                        } else {
                            externalFile = file;
                        }

                        uris.add(Uri.fromFile(externalFile));
                    }
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    shareIntent.setType("*/*");
                    startActivity(Intent.createChooser(shareIntent, getActivity().getString(R.string.send_to)));

                    updateList();
                } catch (IOException e) {
                    Toast.makeText(getActivity(), R.string.couldnt_copy_file,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                throw new IllegalStateException("id not known");
            }
        }

        private class Entry {
            final File dbFile;
            final String title;
            final String stats;

            private Entry(File dbFile, String title, String stats) {
                this.dbFile = dbFile;
                this.title = title;
                this.stats = stats;
            }
        }

        private class OnClickRename implements View.OnClickListener {
            private final int position;

            public OnClickRename(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.rename_file);

                // Set up the input
                final EditText input = new EditText(getActivity());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString();
                        Toast.makeText(getActivity(),
                                getActivity().getString(R.string.renamed_to) + newName, Toast.LENGTH_LONG)
                                .show();

                        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), list.get(position).dbFile.getAbsolutePath());
                        databaseHelper.storeName(newName);
                        databaseHelper.close();

                        updateList();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        }
    }

    private static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
package de.graeb.adsbsniffer.status;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

import de.graeb.adsbsniffer.DatabaseHelper;
import de.graeb.adsbsniffer.R;
import de.graeb.adsbsniffer.Recorder;
import de.graeb.adsbsniffer.RecordingStatistics;
import de.graeb.adsbsniffer.ui.PlaneImages;

/**
 * Displays statistics of a single airplane
 */
public class SinglePlaneStatus extends Activity {
    public static final String EXTRA_ICAO24 = "EXTRA_ICAO24";
    private String icao24;
    private TextView textFirst;

    private TextView textLast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_single_plane_status);

        textFirst = (TextView) findViewById(R.id.text_plane_first);
        textLast = (TextView) findViewById(R.id.text_plane_last);

        TextView textIcao24 = (TextView) findViewById(R.id.text_icao24);
        icao24 = getIntent().getStringExtra(EXTRA_ICAO24);
        textIcao24.setText(icao24);

        ImageView imageFlag = (ImageView) findViewById(R.id.imageview_countryflag);
        imageFlag.setImageResource(PlaneImages.lookupAircraft(icao24));

        updateHandler.sendEmptyMessage(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateHandler.removeMessages(0);
    }


    public Handler updateHandler = new Handler () {
        @Override
        public void handleMessage(Message msg) {
            // query the flight
            SinglePlaneStatus context = SinglePlaneStatus.this;

            DatabaseHelper databaseHelper = new DatabaseHelper(context,
                    Recorder.getInstance().getCurrentRecording());
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String[] rows = {"first, last"};
            String[] args = {icao24};
            Cursor cursor = db.query("flights", rows, "icao24 == ?", args, null, null, "first DESC");

            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                Date firstTimestamp = new Date((long)cursor.getInt(0) * 1000);
                DateFormat dateFormat = DateFormat.getDateTimeInstance();
                textFirst.setText(dateFormat.format(firstTimestamp));

                if (cursor.isNull(1)) {
                    RecordingStatistics recordingStatistics = Recorder.getInstance().getRecordingStatistics();
                    RecordingStatistics.Entry entry = recordingStatistics.getEntry(icao24);

                    textLast.setText(dateFormat.format(entry.getLastPacket()));
                } else {
                    Date lastTimestamp = new Date((long)cursor.getInt(1) * 1000);
                    textLast.setText(dateFormat.format(lastTimestamp));
                }

                sendEmptyMessageDelayed(0, 1000);
            } else {
                Toast.makeText(context, context.getString(R.string.lookup_db_failed), Toast.LENGTH_LONG)
                        .show();
            }
        }
    };
}

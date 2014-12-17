package de.graeb.adsbsniffer.status;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import de.graeb.adsbsniffer.Recorder;
import de.graeb.adsbsniffer.RecordingStatistics;
import de.graeb.adsbsniffer.ui.PlaneImages;

/**
 * @author markus
 */
public class PlaneStatus extends Activity {
    private PlaneAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_status_planes);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        ListView listView = (ListView) findViewById(R.id.listView_planes);
        adapter = new PlaneAdapter(this);
        listView.setAdapter(adapter);

        TextView textPlanesLastMinutes = (TextView) findViewById(R.id.text_planes_last_minutes);
        textPlanesLastMinutes.setText(String.format(getString(R.string.planes_in_the_last_minutes),
                Recorder.getInstance().getRecordingStatistics().getTimeoutFlight()));

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

    private final Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            RecordingStatistics recordingStatistics = Recorder.getInstance().getRecordingStatistics();

            adapter.setContent(recordingStatistics.getIcao24s());

            this.sendEmptyMessageDelayed(0, 1000);
        }
    };

    private class PlaneAdapter extends BaseAdapter {
        private final Context context;
        private String[] textContent = new String[0];

        public PlaneAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return textContent.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = LayoutInflater.from(context).inflate(R.layout.item_icao24, null);
            }

            RecordingStatistics recordingStatistics = Recorder.getInstance().getRecordingStatistics();

            TextView textId = (TextView) view.findViewById(android.R.id.text1);
            TextView textRateAdsb = (TextView) view.findViewById(R.id.text_rate_adsb);
            TextView textRateModeS = (TextView) view.findViewById(R.id.text_rate_smode);
            textId.setText(textContent[position]);
            RecordingStatistics.Entry entry = recordingStatistics.getEntry(textContent[position]);
            textRateAdsb.setText(String.format("%d", entry.getPacketsAdsb()));
            textRateModeS.setText(String.format("%d", entry.getPacketsModeS()));

            ImageView imageView = (ImageView) view.findViewById(R.id.imageview_countryflag);
            imageView.setImageResource(PlaneImages.lookupAircraft(textContent[position]));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PlaneStatus.this, SinglePlaneStatus.class);
                    intent.putExtra(SinglePlaneStatus.EXTRA_ICAO24, textContent[position]);
                    startActivity(intent);
                }

            });

            return view;
        }

        public void setContent(String[] text) {
            this.textContent = text;
            notifyDataSetChanged();
        }
    }
}

package de.graeb.adsbsniffer.status;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.widget.TextView;

import de.graeb.adsbsniffer.R;
import de.graeb.adsbsniffer.Recorder;
import de.graeb.adsbsniffer.RecordingStatistics;

/**
 * @author markus
 */
public class AdsbStatus extends Activity {
    private TextView textCountAdsb;
    private TextView textCountModeS;
    private TextView textRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_status_adsb);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        textCountAdsb = (TextView) findViewById(R.id.text_cnt_adsb);
        textCountModeS = (TextView) findViewById(R.id.text_cnt_smode);
        textRate = (TextView) findViewById(R.id.text_rate);

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

            textCountModeS.setText(String.format("%d", recordingStatistics.getCountMessages()));

            int sum = 0;
            for (String icao24 : recordingStatistics.getIcao24s()) {
                RecordingStatistics.Entry entry = recordingStatistics.getEntry(icao24);
                sum += entry.getPacketsAdsb();
            }

            textCountAdsb.setText(String.format("%d", sum));
            textRate.setText(String.format(AdsbStatus.this.getString(R.string.format_packet_second),
                    recordingStatistics.getRate()));

            this.sendEmptyMessageDelayed(0, 1000);
        }
    };
}

package de.graeb.adsbsniffer.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.text.DecimalFormat;

import de.graeb.adsbsniffer.Recorder;
import de.graeb.adsbsniffer.RecordingStatistics;
import de.graeb.adsbsniffer.status.AdsbStatus;
import de.graeb.adsbsniffer.status.PlaneStatus;

/**
 * @author markus
 */
public class StatusFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Button buttonRate;
    private Button buttonCountPlanes;
    private Button buttonPosition;

    public static final DecimalFormat FORMAT_1POINTS = new DecimalFormat("0.0");
    public static final DecimalFormat FORMAT_2POINTS = new DecimalFormat("0.00");
    public static final DecimalFormat FORMAT_5POINTS = new DecimalFormat("0.00000");

    public static StatusFragment newInstance(int sectionNumber) {
        StatusFragment fragment = new StatusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public StatusFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

        buttonRate = (Button) rootView.findViewById(R.id.button_rate);
        buttonCountPlanes = (Button) rootView.findViewById(R.id.textView_countplanes);
        buttonPosition = (Button) rootView.findViewById(R.id.button_gpsstate);

        buttonRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AdsbStatus.class);
                getActivity().startActivity(intent);
            }
        });
        buttonCountPlanes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlaneStatus.class);
                getActivity().startActivity(intent);
            }
        });

        updateHandler.sendEmptyMessage(0);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        updateHandler.removeMessages(0);
        super.onDestroyView();
    }

    private Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Recorder recorder = Recorder.getInstance();
            RecordingStatistics recordingStatistics = recorder.getRecordingStatistics();
            String[] icao24s = recordingStatistics.getIcao24s();

            buttonCountPlanes.setText(String.format("%d", icao24s.length));
            buttonRate.setText(String.format(getActivity().getString(R.string.format_packet_second)
                                            , FORMAT_1POINTS.format(recordingStatistics.getRate())));

            Location location = recorder.getLastLocation();
            buttonPosition.setText(location != null ? formatLocation(location)
                    : getActivity().getText(R.string.gps_disabled));

            sendEmptyMessageDelayed(0, 1000);
        }
    };

    private String formatLocation(Location location) {
        return String.format(
                getActivity().getString(R.string.location_format),
                FORMAT_5POINTS.format(location.getLongitude()),
                FORMAT_5POINTS.format(location.getLatitude()),
                FORMAT_2POINTS.format(location.getSpeed()))
                + (location.hasAltitude()?String.format(getActivity().getString(R.string.location_altitude), (int)location.getAltitude()):"");
    }
}

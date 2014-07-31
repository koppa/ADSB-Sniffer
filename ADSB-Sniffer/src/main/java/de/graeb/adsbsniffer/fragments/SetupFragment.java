package de.graeb.adsbsniffer.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.graeb.adsbsniffer.R;

/**
 * @author markus
 */
public class SetupFragment extends PreferenceFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    public static SetupFragment newInstance(int sectionNumber) {
        SetupFragment fragment = new SetupFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SetupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_setup, container, false);
        rootView.addView(view, 0);
        return rootView;
    }
}

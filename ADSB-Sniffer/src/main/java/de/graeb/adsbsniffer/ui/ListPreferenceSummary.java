package de.graeb.adsbsniffer.ui;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Shows the current value in the summary field
 *
 * Code from http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
 */
public class ListPreferenceSummary extends ListPreference {
    public ListPreferenceSummary(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListPreferenceSummary(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference arg0, Object arg1) {
                arg0.setSummary(getEntry());
                return true;
            }
        });
    }

    @Override
    public CharSequence getSummary() {
        return super.getEntry();
    }
}
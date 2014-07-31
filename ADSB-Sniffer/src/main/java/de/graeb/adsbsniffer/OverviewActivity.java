package de.graeb.adsbsniffer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;

import de.graeb.adsbsniffer.fragments.AboutFragment;
import de.graeb.adsbsniffer.fragments.NavigationDrawerFragment;
import de.graeb.adsbsniffer.fragments.RecordingListFragment;
import de.graeb.adsbsniffer.fragments.SetupFragment;
import de.graeb.adsbsniffer.fragments.StatusFragment;

/**
 * Main activity contains the NavigationDrawer
 */
public class OverviewActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE) != null) {
            Recorder.getInstance().reattach();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (position) {
            case 0:
                fragmentTransaction = fragmentTransaction
                        .replace(R.id.container, SetupFragment.newInstance(position + 1));
                break;
            case 1:
                fragmentTransaction = fragmentTransaction
                        .replace(R.id.container, RecordingListFragment.newInstance(position + 1));
                break;
            case 2:
                fragmentTransaction = fragmentTransaction
                        .replace(R.id.container, StatusFragment.newInstance(position + 1));
                break;
            case 3:
                fragmentTransaction = fragmentTransaction
                        .replace(R.id.container, AboutFragment.newInstance(position + 1));
                break;
            default:
                throw new IllegalArgumentException("position unknown: " + position);
        }
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overview, menu);

        Switch switchRecording = (Switch) menu.findItem(R.id.menu_switch_streaming)
                .getActionView();
        switchRecording.setOnCheckedChangeListener(new RecordingSwitchHandler());
        switchRecording.setChecked(Recorder.getInstance().isRecording());
        switchRecording.setEnabled(Recorder.getInstance().isAvailable());

        return true;
    }

    private class RecordingSwitchHandler implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Recorder recorder = Recorder.getInstance();
            if (isChecked && !recorder.isRecording()) {
                recorder.startRecording();
            } else if (!isChecked && recorder.isRecording()) {
                recorder.stopRecording();

                Fragment fragment = getFragmentManager().findFragmentById(R.id.container);
                if (fragment instanceof RecordingListFragment) {
                    onNavigationDrawerItemSelected(1);
                }
            }
        }
    }
}

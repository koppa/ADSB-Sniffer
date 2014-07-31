package de.graeb.adsbsniffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import de.graeb.adsbsniffer.adbsreceiver.GNSReceiver;
import de.graeb.adsbsniffer.adbsreceiver.Packet;
import de.graeb.adsbsniffer.adbsreceiver.exceptions.NoUsbDeviceFound;

/**
 * Singleton used for Recording ADS-B Messages
 */
public class Recorder implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static Recorder instance = null;
    private final Context context;

    private int timeoutFlight;

    private GNSReceiver gns = null;
    private DatabaseHelper recordingDb = null;
    private String currentRecording = null;
    private RecordingStatistics recordingStatistics;

    private boolean positionTracking;

    private Location lastLocation = null;
    private LocationTracking locationTracking = null;
    private boolean storeDatabaseExtern;
    private LinkedList<Packet> packetBuffer1;
    private LinkedList<Long> packetBuffer2;

    Recorder(final Context context) {
        this.context = context;
        instance = this;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(this);

        reattach();
        recordingStatistics = new RecordingStatistics(timeoutFlight, recordingDb);
    }

    public static Recorder getInstance() {
        return instance;
    }

    public void startRecording() {
        setUp();

        packetBuffer1 = new LinkedList<>();
        packetBuffer2 = new LinkedList<>();

        // create filename
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
        String date = dateFormat.format(new Date());

        String prefix = null;
        if (storeDatabaseExtern) {
            File directory = new File(Environment.getExternalStorageDirectory(), "ADSB-Sniffer");

            if (!directory.exists()) {
                directory.mkdirs();
            }
            prefix = directory.getAbsolutePath();
        }

        currentRecording = (prefix == null?"":prefix + "/") + date + ".sqlite3";
        recordingDb = new DatabaseHelper(context, currentRecording);
        recordingStatistics = new RecordingStatistics(timeoutFlight, recordingDb);

        // check if gps tracking enabled
        if (positionTracking) {
            int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
            if (resp == ConnectionResult.SUCCESS) {
                locationTracking = new LocationTracking(context, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        lastLocation = location;
                        if (recordingDb != null) {
                            recordingDb.storePosition(location);
                        }
                    }
                });
            } else {
                Toast.makeText(context, "Google Play Service Error " + resp, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    /**
     * Tries to connect to a usb receiver
     * <p />
     * The USB device lost the connection and was inserted again
     * The current recording will be continued.
     */
    public void reattach () {
        try {
            gns = new GNSReceiver(context, new GNSReceiver.PacketReceivedHandler() {
                @Override
                public void incoming(Packet packet) {
                    synchronized (Recorder.this) {
                        if (recordingDb != null) {
                            long id = recordingStatistics.add(packet);
                            if (packet.isAdsb) {
                                packetBuffer1.add(packet);
                                packetBuffer2.add(id);
                                if (packetBuffer1.size() > 100) {
                                    flushPacketBuffer();
                                }
                            }
                        }
                    }
                }
            });
            setUp();
        } catch (NoUsbDeviceFound e) {
            Toast.makeText(context, context.getText(R.string.no_device_found), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private synchronized void flushPacketBuffer() {
        final Iterator<Packet> iterator1 = packetBuffer1.iterator();
        final Iterator<Long> iterator2 = packetBuffer2.iterator();

        final SQLiteDatabase db= recordingDb.getWritableDatabase();
        db.beginTransaction();
        while (iterator1.hasNext()) {
            recordingDb.storePacket(db, iterator1.next(), iterator2.next());
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        packetBuffer1.clear();
        packetBuffer2.clear();
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void stopRecording() {
        currentRecording = null;
        recordingStatistics.writeBack();

        if (recordingDb != null) {
            synchronized (this) {
                flushPacketBuffer();
                recordingDb.createIndex();
                recordingDb.close();
                recordingDb = null;
            }
        }

        if (locationTracking != null) {
            locationTracking.stop();
            locationTracking = null;
        }

        setUp();
    }

    public boolean isRecording() {
        return currentRecording != null;
    }

    /**
     * Read preference settings and configure the receiver
     */
    private void setUp() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int mode = Integer.valueOf(sharedPreferences.getString("settings_gns_mode", "0"));
        boolean timestamp = sharedPreferences.getBoolean("settings_gns_timestamp", false);
        boolean heartbeat = sharedPreferences.getBoolean("settings_gns_heartbeat", false);

        if (gns != null) {
            gns.setUp(mode, heartbeat, timestamp);
        }

        timeoutFlight = sharedPreferences.getInt("settings_flight_timeout", 10);
        positionTracking = sharedPreferences.getBoolean("settings_position_tracking", false);
        storeDatabaseExtern = sharedPreferences.getBoolean("settings_store_extern", false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("Recorder", "onSharedPreferenceChanged()");
        setUp();
    }

    public RecordingStatistics getRecordingStatistics() {
        return recordingStatistics;
    }

    public boolean isAvailable() {
        return gns != null;
    }

    public String getCurrentRecording() {
        return currentRecording;
    }

}

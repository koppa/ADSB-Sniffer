package de.graeb.adsbsniffer;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

/**
 * Class handles location updates
 *
 * Uses the GooglePlay Services
 */
public class LocationTracking {
    public static final int INTERVAL_UPDATE = 1500;
    private final LocationClient locationclient;

    /**
     * Subscribes for location updates
     *
     * The updates will be received with the interval INTERVAL_UPDATE (ms)
     * @param context current context
     * @param locationListener Callback
     */
    public LocationTracking(final Context context, final LocationListener locationListener) {
        locationclient = new LocationClient(context, new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Toast.makeText(context, "Connected to GPS!!", Toast.LENGTH_LONG)
                        .show();

                locationclient.requestLocationUpdates(
                        LocationRequest.create().setInterval(INTERVAL_UPDATE)
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                        locationListener);
            }

            @Override
            public void onDisconnected() {
                Toast.makeText(context, "Disconnected from GPS!!", Toast.LENGTH_LONG)
                        .show();
            }
        }, new GooglePlayServicesClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Toast.makeText(context, "Failed to connect to GPS!!", Toast.LENGTH_LONG)
                        .show();
            }
        }
        );
        locationclient.connect();

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context,
                    "Please enable gps to get a more accurate location tracking", Toast.LENGTH_LONG)
                    .show();
            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);
        }
    }

    /**
     * Stop receiving location updates
     */
    public void stop() {
        locationclient.disconnect();
    }
}

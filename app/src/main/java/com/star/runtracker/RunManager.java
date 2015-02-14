package com.star.runtracker;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class RunManager {

    private static final String TAG = "RunManager";

    private static final String PREFS_FILE = "runs";
    private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";

    public static final String ACTION_LOCATION =
            "com.star.runtracker.ACTION_LOCATION";

    private static final String TEST_PROVIDER = "TEST_PROVIDER";

    private static RunManager sRunManager;

    private Context mAppContext;

    private LocationManager mLocationManager;

    private RunDatabaseHelper mRunDatabaseHelper;
    private SharedPreferences mSharedPreferences;
    private long mCurrentRunId;

    private RunManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager) mAppContext.getSystemService(Context.LOCATION_SERVICE);

        mRunDatabaseHelper = new RunDatabaseHelper(mAppContext);
        mSharedPreferences = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    public static RunManager getInstance(Context context) {
        if (sRunManager == null) {
            synchronized (RunManager.class) {
                if (sRunManager == null) {
                    sRunManager = new RunManager(context);
                }
            }
        }
        return sRunManager;
    }

    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;

        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }

    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;

        if (mLocationManager.getProvider(TEST_PROVIDER) != null &&
                mLocationManager.isProviderEnabled(TEST_PROVIDER)) {
            provider = TEST_PROVIDER;
        }

        Log.d(TAG, "Using provider " + provider);

        PendingIntent pi = getLocationPendingIntent(true);

        mLocationManager.requestLocationUpdates(provider, 0, 0, pi);

        Location lastKnown = mLocationManager.getLastKnownLocation(provider);

        if (lastKnown != null) {
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }
    }

    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);

        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }

    public boolean isTrackingRun() {
        return getLocationPendingIntent(false) != null;
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);
    }

    public Run startTrackingRun() {
        Run run = insertRun();
        mCurrentRunId = run.getId();
        mSharedPreferences.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentRunId).commit();
        startLocationUpdates();

        return run;
    }

    private Run insertRun() {
        Run run = new Run();
        run.setId(mRunDatabaseHelper.insertRun(run));
        return run;
    }

    public void stopTrackingRun() {
        stopLocationUpdates();
        mSharedPreferences.edit().remove(PREF_CURRENT_RUN_ID).commit();
    }

    public void insertLocation(Location location) {
        if (isTrackingRun()) {
            mRunDatabaseHelper.insertLocation(mCurrentRunId, location);
        } else {
            Log.e(TAG, "Location received with no tracking run; ignoring.");
        }
    }
}

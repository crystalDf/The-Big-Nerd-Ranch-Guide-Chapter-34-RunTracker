package com.star.runtracker;


import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class RunDatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "runs.sqlite";
    private static final int VERSION = 1;

    private static final String TABLE_RUN = "run";
    private static final String COLUMN_RUN_START_DATE = "start_date";

    private static final String TABLE_LOCATION = "location";
    private static final String COLUMN_LOCATION_TIMESTAMP = "timestamp";
    private static final String COLUMN_LOCATION_LATITUDE = "latitude";
    private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
    private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
    private static final String COLUMN_LOCATION_PROVIDER = "provider";
    private static final String COLUMN_LOCATION_RUN_ID = "run_id";

    private static final String CREATE_TABLE_RUN =
            "CREATE TABLE " + TABLE_RUN + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "start_date INTEGER" + ")";

    private static final String CREATE_TABLE_LOCATION =
            "CREATE TABLE " + TABLE_LOCATION + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp INTEGER, " +
                    "latitude REAL, " +
                    "longitude REAL, " +
                    "altitude REAL, " +
                    "provider VARCHAR(100), " +
                    "run_id INTEGER REFERENCES run(_id)" + ")";

    public RunDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_RUN);
        db.execSQL(CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertRun(Run run) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());

        return getWritableDatabase().insert(TABLE_RUN, null, contentValues);
    }

    public long insertLocation(long runId, Location location) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LOCATION_TIMESTAMP, location.getTime());
        contentValues.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
        contentValues.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
        contentValues.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
        contentValues.put(COLUMN_LOCATION_PROVIDER, location.getProvider());
        contentValues.put(COLUMN_LOCATION_RUN_ID, runId);

        return getWritableDatabase().insert(TABLE_LOCATION, null, contentValues);
    }
}

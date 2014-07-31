package de.graeb.adsbsniffer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.util.Date;

import de.graeb.adsbsniffer.adbsreceiver.Packet;

/**
 * @author markus
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 2;

    private static final String CREATE_MESSAGES = "CREATE TABLE IF NOT EXISTS messages(" +
            "id        INTEGER NOT NULL PRIMARY KEY," +
            "flight    INTEGER NOT NULL," +
            "timestamp INTEGER," +
            "time      INTEGER NOT NULL," +
            "icao24    TEXT NOT NULL," +
            "format    INTEGER NOT NULL," +
            "message   TEXT NOT NULL," +
            "checksum  INTEGER NOT NULL," +
            "FOREIGN KEY(flight) REFERENCES flights(id)" +
            ");";

    private static final String CREATE_FLIGHT_STATE = "CREATE TABLE IF NOT EXISTS flight_state(" +
            "id        INTEGER NOT NULL PRIMARY KEY," +
            "flight    INTEGER NOT NULL," +
            "time      INTEGER NOT NULL," +
            "adsb_cnt  INTEGER NOT NULL," +
            "smode_cnt INTEGER NOT NULL," +
            "FOREIGN KEY(flight) REFERENCES flights(id)" +
            ");";

    private static final String CREATE_FLIGHTS = "CREATE TABLE IF NOT EXISTS flights (" +
            "id     INTEGER NOT NULL PRIMARY KEY," +
            "first  INTEGER NOT NULL," +
            "last   INTEGER," +
            "icao24 TEXT NOT NULL" +
            ");";

    private static final String CREATE_POSITION = "CREATE TABLE IF NOT EXISTS position(" +
            "time      INTEGER NOT NULL PRIMARY KEY," +
            "latitude  REAL NOT NULL," +
            "longitude REAL NOT NULL," +
            "altitude  REAL," +
            "speed     REAL," +
            "direction REAL" +
            ");";

    private static final String CREATE_METADATA = "CREATE TABLE IF NOT EXISTS metadata(" +
            "name TEXT" +
            ");";

    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_FLIGHT_STATE = "flight_state";
    public static final String TABLE_FLIGHTS = "flights";
    public static final String TABLE_POSITION = "position";
    public static final String TABLE_METADATA = "metadata";

    public DatabaseHelper(Context context, String databaseName) {
        super(context, databaseName, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FLIGHTS);
        db.execSQL(CREATE_FLIGHT_STATE);
        db.execSQL(CREATE_MESSAGES);
        db.execSQL(CREATE_POSITION);
        db.execSQL(CREATE_METADATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on new Version just create the new tables
        onCreate(db);
    }

    public long storePacket(Packet packet, long flightId) {
        SQLiteDatabase db = getWritableDatabase();
        return storePacket(db, packet, flightId);
    }

    public long storePacket(SQLiteDatabase db, Packet packet, long flightId) {
        ContentValues values = new ContentValues(5);

        values.put("flight", flightId);

        if (packet.externalTimestamp > 0) {
            values.put("timestamp", packet.externalTimestamp);
        }

        values.put("time", packet.internalTimestamp.getTime() / 1000);
        values.put("icao24", packet.icao24);
        values.put("format", packet.format);
        values.put("message", packet.message);
        values.put("checksum", packet.checksumCorrect.value);

        return db.insertOrThrow(TABLE_MESSAGES, null, values);
    }

    public long storePosition(Location location) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(6);
        values.put("time", location.getTime() / 1000);
        values.put("latitude", location.getLatitude());
        values.put("longitude", location.getLongitude());
        if (location.hasAltitude()) {
            values.put("altitude", location.getAltitude());
        }
        if (location.hasSpeed()) {
            values.put("speed", location.getSpeed());
        }
        if (location.hasBearing()) {
            values.put("direction", location.getBearing());
        }

        long id;
        try {
            id = db.insertOrThrow(TABLE_POSITION, null, values);
        } catch (SQLiteConstraintException e) {
            Log.e("DatabaseHelper", "storePosition, ConstraintException");
            id = -1;
        }
        return id;
    }

    public long storeFlightState(long flightId, Date date, int adsbCnt, int smodeCnt) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(4);
        values.put("flight", flightId);
        values.put("time", date.getTime() / 1000);
        values.put("adsb_cnt", adsbCnt);
        values.put("smode_cnt", smodeCnt);

        return db.insert(TABLE_FLIGHT_STATE, null, values);
    }

    public long storeFlight(Date first, String icao24) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(6);
        values.put("first", first.getTime() / 1000);
        values.putNull("last");
        values.put("icao24", icao24);

        return db.insert(TABLE_FLIGHTS, null, values);
    }

    /**
     * Update the last value in the flight table
     * @param id   of the flight
     * @param last the value
     * @return id
     */
    public long updateFlight(long id, Date last) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(1);
        values.put("last", last.getTime() / 1000);
        return db.update(TABLE_FLIGHTS, values, "id = " + id, null);
    }

    /**
     * Creates an index "index_time" in the messages table
     */
    public void createIndex() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("CREATE INDEX IF NOT EXISTS index_time ON messages (time);");
    }

    /**
     * Stores the name of the recording
     * @param name name of the recording
     * @return  1 if successful
     */
    public long storeName(String name) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues(2);
        values.put("name", name);
        values.put("rowid", 1);
        return db.replace(TABLE_METADATA, null, values);
    }

    /**
     * Get the name, set by storeName()
     * @return name or null
     */
    public String getName() {
        SQLiteDatabase db = getWritableDatabase();
        String[] columns = {"name"};
        Cursor cursor = db.query(TABLE_METADATA, columns, null, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getCount() > 0 ? cursor.getString(0) : null;
    }
}

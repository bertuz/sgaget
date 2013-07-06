package org.altervista.bertuz83.sgaget.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.altervista.bertuz83.sgaget.helper.MyApplication;

/**
 * User: bertuz
 * Project: sgaget
 *
 * helper class per l'utilizzo di SQLite
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION= 1;
    public static final String DATABASE_NAME= "sgaget.db";

    public static final String TABLE_HOTPOINTS= "hotpoints";
    public static final String HOTPOINTS_COLUMN_NAME = "name";
    public static final String HOTPOINTS_COLUMN_LATITUDE = "latitude";
    public static final String HOTPOINTS_COLUMN_LONGITUDE = "longitude";

    public static final String TABLE_HOTPOINTS_CREATE= "CREATE TABLE " +
            TABLE_HOTPOINTS + "(" +
            HOTPOINTS_COLUMN_NAME + " TEXT NOT NULL PRIMARY KEY, " +
            HOTPOINTS_COLUMN_LATITUDE + " REAL, " +
            HOTPOINTS_COLUMN_LONGITUDE + " REAL);";

    public static final String TABLE_TRACKRECORD= "trackrecord";
    public static final String TRACKRECORD_COLUMN_CREATIONDATE = "creationdate";
    public static final String TRACKRECORD_COLUMN_COMPLETED = "completed";
    public static final String TRACKRECORD_COLUMN_ELAPSEDTIME = "elapsedTime";
    public static final String TRACKRECORD_COLUMN_TRANSPORTATIONTYPE = "transportationtype";
    public static final String TRACKRECORD_COLUMN_DAY = "day";
    public static final String TRACKRECORD_COLUMN_STARTHOUR = "starthour";
    public static final String TRACKRECORD_COLUMN_STARTHOTPOINT = "starthotpoint";
    public static final String TRACKRECORD_COLUMN_FINISHHOUR = "finishour";
    public static final String TRACKRECORD_COLUMN_FINISHHOTPOINT = "finishhotpoint";
    public static final String TRACKRECORD_COLUMN_TRAFFICRATE = "trafficrate";
    public static final String TRACKRECORD_COLUMN_QUALITYRATE = "qualityrate";
    public static final String TRACKRECORD_COLUMN_NOTES = "notes";
    public static final String TRACKRECORD_COLUMN_SENT = "sent";


    public static final String[] TRACKRECORD_ALL_COLUMNS = {TRACKRECORD_COLUMN_CREATIONDATE,
            TRACKRECORD_COLUMN_COMPLETED,
            TRACKRECORD_COLUMN_ELAPSEDTIME,
            TRACKRECORD_COLUMN_TRANSPORTATIONTYPE,
            TRACKRECORD_COLUMN_DAY,
            TRACKRECORD_COLUMN_STARTHOUR,
            TRACKRECORD_COLUMN_STARTHOTPOINT,
            TRACKRECORD_COLUMN_FINISHHOUR,
            TRACKRECORD_COLUMN_FINISHHOTPOINT,
            TRACKRECORD_COLUMN_TRAFFICRATE,
            TRACKRECORD_COLUMN_QUALITYRATE,
            TRACKRECORD_COLUMN_NOTES,
            TRACKRECORD_COLUMN_SENT};

    public static final String TABLE_TRACKRECORD_CREATE= "CREATE TABLE " +
            TABLE_TRACKRECORD + "(" +
            TRACKRECORD_COLUMN_CREATIONDATE + " INTEGER NOT NULL PRIMARY KEY, " +
            TRACKRECORD_COLUMN_COMPLETED + " INTEGER, " +
            TRACKRECORD_COLUMN_ELAPSEDTIME + " INTEGER, " +
            TRACKRECORD_COLUMN_TRANSPORTATIONTYPE + " TEXT, " +
            TRACKRECORD_COLUMN_DAY + " TEXT, " +
            TRACKRECORD_COLUMN_STARTHOUR + " INTEGER, " +
            TRACKRECORD_COLUMN_STARTHOTPOINT + " TEXT, " +
            TRACKRECORD_COLUMN_FINISHHOUR + " INTEGER, " +
            TRACKRECORD_COLUMN_FINISHHOTPOINT + " TEXT, " +
            TRACKRECORD_COLUMN_TRAFFICRATE + " INTEGER, " +
            TRACKRECORD_COLUMN_QUALITYRATE + " INTEGER, " +
            TRACKRECORD_COLUMN_NOTES + " TEXT, " +
            TRACKRECORD_COLUMN_SENT + " INTEGER );";

    public static final String TABLE_CANDIDATESTARTFINISH= "candidatestartfinish";
    public static final String CANDIDATESTARTFINISH_TIME= "time";
    public static final String CANDIDATESTARTFINISH_HOTPOINTNAME= "hotpointname";
    public static final String CANDIDATESTARTFINISH_LATITUDE= "latitude";
    public static final String CANDIDATESTARTFINISH_LONGITUDE= "longitude";
    public static final String CANDIDATESTARTFINISH_RATE= "rate";


    public static final String TABLE_CANDIDATESTARTFINISH_CREATE= "CREATE TABLE " +
            TABLE_CANDIDATESTARTFINISH + "(" +
            CANDIDATESTARTFINISH_TIME + " INTEGER, " +
            CANDIDATESTARTFINISH_HOTPOINTNAME + " TEXT NOT NULL, " +
            CANDIDATESTARTFINISH_LATITUDE + " REAL NOT NULL, " +
            CANDIDATESTARTFINISH_LONGITUDE + " REAL NOT NULL, " +
            CANDIDATESTARTFINISH_RATE + " REAL NOT NULL, " +
            "PRIMARY KEY (" + CANDIDATESTARTFINISH_TIME + ", " + CANDIDATESTARTFINISH_HOTPOINTNAME + "));";

    public static final String TABLE_STARTEDIN= "startedin";
    public static final String STARTEDIN_TRACKRECORDID= "trackrecordid";
    public static final String STARTEDIN_CANDIDATEIDTIME= "candidateidtime";
    public static final String STARTEDIN_CANDIDATEIDNAME= "candidateidname";

    public static final String TABLE_STARTEDIN_CREATE= "CREATE TABLE " +
            TABLE_STARTEDIN + "(" +
            STARTEDIN_TRACKRECORDID + " INTEGER, " +
            STARTEDIN_CANDIDATEIDTIME + " INTEGER, " +
            STARTEDIN_CANDIDATEIDNAME + " TEXT);";

    public static final String TABLE_FINISHEDIN= "finishedin";
    public static final String FINISHEDIN_TRACKRECORDID= "trackrecordid";
    public static final String FINISHEDIN_CANDIDATEIDTIME= "candidateidtime";
    public static final String FINISHEDIN_CANDIDATEIDNAME= "candidateidname";

    public static final String TABLE_FINISHEDIN_CREATE= "CREATE TABLE " +
            TABLE_FINISHEDIN + "(" +
            FINISHEDIN_TRACKRECORDID + " INTEGER, " +
            FINISHEDIN_CANDIDATEIDTIME + " INTEGER, " +
            FINISHEDIN_CANDIDATEIDNAME + " TEXT);";


    public MySQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void createDB(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL(TABLE_HOTPOINTS_CREATE);
        sqLiteDatabase.execSQL(TABLE_TRACKRECORD_CREATE);
        sqLiteDatabase.execSQL(TABLE_CANDIDATESTARTFINISH_CREATE);
        sqLiteDatabase.execSQL(TABLE_STARTEDIN_CREATE);
        sqLiteDatabase.execSQL(TABLE_FINISHEDIN_CREATE);
    }

    private void eraseDB(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_HOTPOINTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CANDIDATESTARTFINISH);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FINISHEDIN);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_STARTEDIN);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TRACKRECORD);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        this.createDB(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        this.eraseDB(sqLiteDatabase);
        this.createDB(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.eraseDB(db);
        this.createDB(db);
    }


}

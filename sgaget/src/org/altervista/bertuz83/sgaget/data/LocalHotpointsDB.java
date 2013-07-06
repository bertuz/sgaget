package org.altervista.bertuz83.sgaget.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.altervista.bertuz83.sgaget.business.Hotpoint;
import org.altervista.bertuz83.sgaget.helper.MyApplication;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Classe implementante l'interfaccia DAO per lettura e scrittura dei punti di interesse su DB locale.
 */
public class LocalHotpointsDB implements LocalHotpointsDAO{
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private String[] allColumns= {MySQLiteHelper.HOTPOINTS_COLUMN_NAME, MySQLiteHelper.HOTPOINTS_COLUMN_LATITUDE, MySQLiteHelper.HOTPOINTS_COLUMN_LONGITUDE};

    public void open() throws SQLException{
        if(dbHelper == null){
            this.dbHelper= new MySQLiteHelper(MyApplication.getAppContext());
            this.database= this.dbHelper.getWritableDatabase();
        }
    }

    public void openSecure() throws SQLException{
        this.open();
        database.beginTransaction();
    }

    public void closeSecure(){
        database.setTransactionSuccessful();
        database.endTransaction();
        this.close();
    }

    public void close(){
        this.dbHelper.close();
    }

    public boolean insertHotpoint(Hotpoint hotpoint){
        if(database.insert(MySQLiteHelper.TABLE_HOTPOINTS, null, hotpointToValues(hotpoint)) == -1)
            return false;

        return true;
    }

    private ContentValues hotpointToValues(Hotpoint hotpoint) {
        ContentValues cv= new ContentValues();
        cv.put(MySQLiteHelper.HOTPOINTS_COLUMN_NAME, hotpoint.getName());
        cv.put(MySQLiteHelper.HOTPOINTS_COLUMN_LATITUDE, hotpoint.getLatitude());
        cv.put(MySQLiteHelper.HOTPOINTS_COLUMN_LONGITUDE, hotpoint.getLongitude());

        return cv;
    }

    private Hotpoint cursorToHotpoint(Cursor cursor){
        String name= cursor.getString(0);
        double latitude= cursor.getDouble(1);
        double longitude= cursor.getDouble(2);

        Hotpoint hotpoint= new Hotpoint(latitude, longitude, name);
        return hotpoint;
    }

    public void eraseHotpoints(){
        database.delete(MySQLiteHelper.TABLE_HOTPOINTS, null, null);
    }

    @Override
    public ArrayList<Hotpoint> getHotpointsList() {
        ArrayList<Hotpoint> hotpointsToReturn= new ArrayList<Hotpoint>();

        Cursor hotpointsSet= database.query(MySQLiteHelper.TABLE_HOTPOINTS, allColumns, null, null, null, null, null, null);
        hotpointsSet.moveToFirst();
        while(!hotpointsSet.isAfterLast()){
            hotpointsToReturn.add(cursorToHotpoint(hotpointsSet));
            hotpointsSet.moveToNext();
        }
        hotpointsSet.close();

        return hotpointsToReturn;
    }

    @Override
    public int getNrHotpoints() {
        Cursor nrHotpoints= database.rawQuery("SELECT COUNT(*) FROM " + MySQLiteHelper.TABLE_HOTPOINTS, null);
        if(!nrHotpoints.isAfterLast()){
            nrHotpoints.moveToNext();
            return nrHotpoints.getInt(0);
        }

        return 0;
    }


}

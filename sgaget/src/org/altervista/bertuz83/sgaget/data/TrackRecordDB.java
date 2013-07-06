package org.altervista.bertuz83.sgaget.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.altervista.bertuz83.sgaget.R;
import org.altervista.bertuz83.sgaget.business.*;
import org.altervista.bertuz83.sgaget.helper.MyApplication;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Classe implementante l'interfaccia DAO per lettura e scrittura dei punti degli spostamenti su DB locale.
 */
public class TrackRecordDB implements TrackRecordDAO{
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private static final String queryNrTrackRecordToSend= "SELECT COUNT(*) FROM " + MySQLiteHelper.TABLE_TRACKRECORD + " "
            + "WHERE " + MySQLiteHelper.TRACKRECORD_COLUMN_SENT + " = 0 AND " + MySQLiteHelper.TRACKRECORD_COLUMN_COMPLETED + " = 1;";

    private static final String queryAvgTimeByTransportation= "SELECT DISTINCT AVG(" + MySQLiteHelper.TRACKRECORD_COLUMN_ELAPSEDTIME + "), " +
            MySQLiteHelper.TRACKRECORD_COLUMN_TRANSPORTATIONTYPE + " " +
            "FROM " + MySQLiteHelper.TABLE_TRACKRECORD + " " +
            "WHERE " + MySQLiteHelper.TRACKRECORD_COLUMN_STARTHOTPOINT + " =? " +
            "AND " + MySQLiteHelper.TRACKRECORD_COLUMN_FINISHHOTPOINT + " =? " +
            "AND " + MySQLiteHelper.TRACKRECORD_COLUMN_COMPLETED + " = 1 " +
            "GROUP BY " + MySQLiteHelper.TRACKRECORD_COLUMN_TRANSPORTATIONTYPE;

    private static final String getHotpointsStartFinishInTrackRecord= "SELECT DISTINCT " + MySQLiteHelper.TRACKRECORD_COLUMN_STARTHOTPOINT + ", " + MySQLiteHelper.TRACKRECORD_COLUMN_FINISHHOTPOINT + " " +
            "FROM " + MySQLiteHelper.TABLE_TRACKRECORD + " WHERE " + MySQLiteHelper.TRACKRECORD_COLUMN_COMPLETED + "=1;";

    @Override
    public void openWrite() throws SQLException {
        if(dbHelper == null){
            this.dbHelper= new MySQLiteHelper(MyApplication.getAppContext());
            this.database= this.dbHelper.getWritableDatabase();
        }
    }


    public void beginAtomicOperations(){
        database.beginTransaction();
    }



    public void endAtomicOperations(){
        database.beginTransaction();
    }


    @Override
    public void openRead() throws SQLException {
        if(dbHelper == null){
            this.dbHelper= new MySQLiteHelper(MyApplication.getAppContext());
            this.database= this.dbHelper.getReadableDatabase();
        }
    }


    @Override
    public void close() {
        this.dbHelper.close();
        dbHelper= null;
    }


    public void openSecure() throws SQLException{
        this.openWrite();
        database.beginTransaction();
    }


    public void closeSecure(boolean successful){
        if(successful)
            database.setTransactionSuccessful();

        database.endTransaction();
        this.close();
    }


    private ContentValues trackRecordToValues(TrackRecord trackRecord){
        ContentValues cv= new ContentValues();
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_CREATIONDATE, trackRecord.getCreationDate());
        int completed= (trackRecord.isCompleted())? 1 : 0;
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_COMPLETED, completed);
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_DAY, trackRecord.getDay());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_FINISHHOTPOINT, trackRecord.getFinishHotpoint());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_FINISHHOUR, trackRecord.getFinishTime());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_NOTES, trackRecord.getNotes());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_QUALITYRATE, trackRecord.getQualityRate());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_STARTHOTPOINT, trackRecord.getStartHotpoint());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_STARTHOUR, trackRecord.getStartTime());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_TRAFFICRATE, trackRecord.getTrafficRate());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_TRANSPORTATIONTYPE, trackRecord.getTransportationType());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_ELAPSEDTIME, trackRecord.getElapsedTimeInMs());
        cv.put(MySQLiteHelper.TRACKRECORD_COLUMN_SENT, trackRecord.isSent());
        return cv;
    }


    private ArrayList<ContentValues> trackRecordToCandidatesStartValues(TrackRecord trackRecord){
        ArrayList<ContentValues> candidatesToReturn= new ArrayList<ContentValues>();

        for(HotpointCandidate candidate : trackRecord.getStartCandidates()){
            candidatesToReturn.add(hotpointCandidateToValues(candidate, false));
        }

        return candidatesToReturn;
    }


    private ArrayList<ContentValues> trackRecordToCandidatesFinishValues(TrackRecord trackRecord){
        ArrayList<ContentValues> candidatesToReturn= new ArrayList<ContentValues>();

        for(HotpointCandidate candidate : trackRecord.getFinishCandidates()){
            /*
            if(MyApplication.DEBUG)
                candidatesToReturn.add(hotpointCandidateToValues(candidate, true));
            else
            */
                candidatesToReturn.add(hotpointCandidateToValues(candidate, true));
        }

        return candidatesToReturn;
    }


    /*
        add1 è utilizzato per fare in modo che gli start e finishcandidate non siano uguali nel tempo.
        Questo infatti può causare problemi con la chiave degli startfinishcandidates (duplicate primary key).
        Aggiungendo un millisecondo, questo non influenza le nostre statistiche in modo significativo.
     */
    private ContentValues hotpointCandidateToValues(HotpointCandidate candidate, boolean add1){
        ContentValues cv= new ContentValues();
        if(add1)
            cv.put(MySQLiteHelper.CANDIDATESTARTFINISH_TIME, candidate.getTime()+1);
        else
            cv.put(MySQLiteHelper.CANDIDATESTARTFINISH_TIME, candidate.getTime());

        cv.put(MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME, candidate.getHotpointName());
        cv.put(MySQLiteHelper.CANDIDATESTARTFINISH_LATITUDE, candidate.getHotpoint().getLatitude());
        cv.put(MySQLiteHelper.CANDIDATESTARTFINISH_LONGITUDE, candidate.getHotpoint().getLongitude());
        cv.put(MySQLiteHelper.CANDIDATESTARTFINISH_RATE, candidate.getRate());

        return cv;
    }


    private HotpointCandidate cursorToHotpointCandidate(Cursor cursor){
        HotpointCandidate candidateReturn= new HotpointCandidate();
        Hotpoint hotpointToBind= new Hotpoint(cursor.getDouble(3), cursor.getDouble(4), cursor.getString(1));
        candidateReturn.setTime(cursor.getLong(0));
        candidateReturn.setRate(cursor.getDouble(2));
        candidateReturn.setHotpoint(hotpointToBind);

        return candidateReturn;
    }


    private TrackRecord cursorToTrackRecord(Cursor cursor){
        /*
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
        */
        TrackRecord trackRecordRet= new TrackRecord();
        trackRecordRet.setCreationDate(cursor.getLong(0));
        trackRecordRet.setCompleted((cursor.getInt(1) == 1) ? true : false);
        trackRecordRet.setElapsedTime(cursor.getLong(2));
        trackRecordRet.setTransportationType(cursor.getString(3));
        trackRecordRet.setDay(cursor.getString(4));
        trackRecordRet.setStartTime(cursor.getLong(5));
        trackRecordRet.setStartHotpoint(cursor.getString(6));
        trackRecordRet.setFinishTime(cursor.getLong(7));
        trackRecordRet.setFinishHotpoint(cursor.getString(8));
        trackRecordRet.setTrafficRate(cursor.getInt(9));
        trackRecordRet.setQualityRate(cursor.getInt(10));
        trackRecordRet.setNotes(cursor.getString(11));
        trackRecordRet.setSent((cursor.getInt(12) == 1) ? true : false);

        //candidates start
        trackRecordRet.setStartCandidates(getStartOrEndCandidates(trackRecordRet.getCreationDate(), true));
        trackRecordRet.setFinishCandidates(getStartOrEndCandidates(trackRecordRet.getCreationDate(), false));

        return trackRecordRet;
    }



    @Override
    public long insertTrackRecord(TrackRecord trackRecord) {
        long trackRecordID= -1;

        try{
            trackRecordID= database.insert(MySQLiteHelper.TABLE_TRACKRECORD, null, trackRecordToValues(trackRecord));

            if(trackRecordID == -1){
                return -1;
            }



        }catch(Exception e){
            return -1;
        }

        ArrayList<ContentValues> candidatesToInsert= trackRecordToCandidatesStartValues(trackRecord);
        for(ContentValues cv : candidatesToInsert ){
            //candidatestartend insertion
            if(database.insert(MySQLiteHelper.TABLE_CANDIDATESTARTFINISH, null, cv) == -1){
                return -1;
            }
            //startedin insertion
            ContentValues cvForStartedTable= new ContentValues();
            cvForStartedTable.put(MySQLiteHelper.STARTEDIN_CANDIDATEIDTIME, cv.getAsLong(MySQLiteHelper.CANDIDATESTARTFINISH_TIME));
            cvForStartedTable.put(MySQLiteHelper.STARTEDIN_CANDIDATEIDNAME, cv.getAsString(MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME));
            cvForStartedTable.put(MySQLiteHelper.STARTEDIN_TRACKRECORDID, trackRecord.getCreationDate());
            if(database.insert(MySQLiteHelper.TABLE_STARTEDIN, null, cvForStartedTable) == -1){
                return -1;
            }
        }

        candidatesToInsert= trackRecordToCandidatesFinishValues(trackRecord);
        for(ContentValues cv : candidatesToInsert ){
            //candidatestartend insertion
            if(database.insert(MySQLiteHelper.TABLE_CANDIDATESTARTFINISH, null, cv) == -1){
                return -1;
            }

            //finishedin table insertion
            ContentValues cvForFinishTable= new ContentValues();
            cvForFinishTable.put(MySQLiteHelper.FINISHEDIN_CANDIDATEIDTIME, cv.getAsLong(MySQLiteHelper.CANDIDATESTARTFINISH_TIME));
            cvForFinishTable.put(MySQLiteHelper.FINISHEDIN_CANDIDATEIDNAME, cv.getAsString(MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME));
            cvForFinishTable.put(MySQLiteHelper.FINISHEDIN_TRACKRECORDID, trackRecord.getCreationDate());
            if(database.insert(MySQLiteHelper.TABLE_FINISHEDIN, null, cvForFinishTable) == -1){
                return -1;
            }
        }

        return trackRecordID;
    }


    @Override
    public ArrayList<TrackRecord> getTrackRecordsToComplete() {
        ArrayList<TrackRecord> trackRecordsToReturn= new ArrayList<TrackRecord>();

        Cursor trackRecordsSet= database.query(MySQLiteHelper.TABLE_TRACKRECORD, MySQLiteHelper.TRACKRECORD_ALL_COLUMNS, MySQLiteHelper.TRACKRECORD_COLUMN_COMPLETED+"=0", null, null, null, MySQLiteHelper.TRACKRECORD_COLUMN_CREATIONDATE, null);
        trackRecordsSet.moveToFirst();

        while(!trackRecordsSet.isAfterLast()){
            trackRecordsToReturn.add(cursorToTrackRecord(trackRecordsSet));
            trackRecordsSet.moveToNext();
        }

        return trackRecordsToReturn;
    }


    @Override
    public ArrayList<TrackRecord> getTrackRecordsToSend() {
        ArrayList<TrackRecord> trackRecordsToReturn= new ArrayList<TrackRecord>();
        String whereClause= MySQLiteHelper.TRACKRECORD_COLUMN_COMPLETED+ "=1 AND " + MySQLiteHelper.TRACKRECORD_COLUMN_SENT + "=0";
        Cursor trackRecordsSet= database.query(MySQLiteHelper.TABLE_TRACKRECORD, MySQLiteHelper.TRACKRECORD_ALL_COLUMNS, whereClause, null, null, null, MySQLiteHelper.TRACKRECORD_COLUMN_CREATIONDATE, null);
        trackRecordsSet.moveToFirst();
        while(!trackRecordsSet.isAfterLast()){
            trackRecordsToReturn.add(cursorToTrackRecord(trackRecordsSet));
            trackRecordsSet.moveToNext();
        }

        return trackRecordsToReturn;
    }


    @Override
    public int getNrTrackRecordToSend() {
        Cursor trackRecordsSet= database.rawQuery(TrackRecordDB.queryNrTrackRecordToSend, null);
        trackRecordsSet.moveToFirst();
        return trackRecordsSet.getInt(0);
    }


    @Override
    public TrackRecord getTrackRecord(long creationDate) {
        TrackRecord trackRecordToReturn= null;

        Cursor trackRecordsSet= database.query(MySQLiteHelper.TABLE_TRACKRECORD, MySQLiteHelper.TRACKRECORD_ALL_COLUMNS, MySQLiteHelper.TRACKRECORD_COLUMN_CREATIONDATE+"="+creationDate, null, null, null, null, null);
        trackRecordsSet.moveToFirst();
        while(!trackRecordsSet.isAfterLast()){
            trackRecordToReturn= (cursorToTrackRecord(trackRecordsSet));
            break;
        }

        return trackRecordToReturn;
    }


    private void deleteCandidates(long creationDate){
        String []args= {""+creationDate};

        String queryDeleteCadidates= "EXISTS "
                + "(SELECT * FROM " + MySQLiteHelper.TABLE_FINISHEDIN + " "
                + "WHERE " + MySQLiteHelper.TABLE_FINISHEDIN + "." + MySQLiteHelper.FINISHEDIN_CANDIDATEIDTIME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_TIME + " "
                + "AND " + MySQLiteHelper.TABLE_FINISHEDIN + "." + MySQLiteHelper.FINISHEDIN_CANDIDATEIDNAME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME + " "
                + "AND " + MySQLiteHelper.TABLE_FINISHEDIN + "." + MySQLiteHelper.FINISHEDIN_TRACKRECORDID + " = " + creationDate + " "
                + "UNION "
                + "SELECT * FROM " + MySQLiteHelper.TABLE_STARTEDIN + " "
                + "WHERE " + MySQLiteHelper.TABLE_STARTEDIN + "." + MySQLiteHelper.STARTEDIN_CANDIDATEIDTIME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_TIME + " "
                + "AND " + MySQLiteHelper.TABLE_STARTEDIN + "." + MySQLiteHelper.STARTEDIN_CANDIDATEIDNAME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME + " "
                + "AND " + MySQLiteHelper.TABLE_STARTEDIN + "." + MySQLiteHelper.FINISHEDIN_TRACKRECORDID + " = " + creationDate + ")";

        //DELETE IN STARTEDIN E FINISHEDIN MISSING
        database.delete(MySQLiteHelper.TABLE_FINISHEDIN, MySQLiteHelper.FINISHEDIN_TRACKRECORDID+"=?", args);
        database.delete(MySQLiteHelper.TABLE_STARTEDIN, MySQLiteHelper.STARTEDIN_TRACKRECORDID+"=?", args);
    }


    @Override
    public boolean deleteTrackRecord(long creationDate) {
        String []args= {""+creationDate};
        int ret= database.delete(MySQLiteHelper.TABLE_TRACKRECORD, MySQLiteHelper.TRACKRECORD_COLUMN_CREATIONDATE+"=?", args);

        deleteCandidates(creationDate);

        if(ret==0)
            return false;
        return true;
    }


    @Override
    public boolean completeTrackRecord(TrackRecord trackRecord) {
        try{
            ContentValues values= trackRecordToValues(trackRecord);
            String whereClause= MySQLiteHelper.TRACKRECORD_COLUMN_CREATIONDATE + " = " + trackRecord.getCreationDate();
            int nrAffected= database.update(MySQLiteHelper.TABLE_TRACKRECORD, values, whereClause, null);

            if(nrAffected == 0)
                return false;

            deleteCandidates(trackRecord.getCreationDate());
        }catch(Exception e){
            return false;
        }

        return true;
    }


    public boolean updateTrackRecord(TrackRecord trackRecord){
        try{
            ContentValues values= trackRecordToValues(trackRecord);
            String whereClause= MySQLiteHelper.TRACKRECORD_COLUMN_CREATIONDATE + " = " + trackRecord.getCreationDate();
            int nrAffected= database.update(MySQLiteHelper.TABLE_TRACKRECORD, values, whereClause, null);

            if(nrAffected == 0)
                return false;
        }catch(Exception e){
            return false;
        }

        return true;
    }


    private ArrayList<HotpointCandidate> getStartOrEndCandidates(long trackRecordCreationDate, boolean startCandidates){
        ArrayList<HotpointCandidate> candidatesToReturn= new ArrayList<HotpointCandidate>();

        String queryStart= "SELECT " + MySQLiteHelper.CANDIDATESTARTFINISH_TIME + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_RATE + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_LATITUDE + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_LONGITUDE + " "
                + " FROM " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + " "
                + "WHERE EXISTS "
                + "(SELECT * FROM " + MySQLiteHelper.TABLE_STARTEDIN + " "
                + "WHERE " + MySQLiteHelper.TABLE_STARTEDIN + "." + MySQLiteHelper.STARTEDIN_CANDIDATEIDTIME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_TIME + " "
                + "AND " + MySQLiteHelper.TABLE_STARTEDIN + "." + MySQLiteHelper.STARTEDIN_CANDIDATEIDNAME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME + " "
                + "AND " + MySQLiteHelper.TABLE_STARTEDIN + "." + MySQLiteHelper.STARTEDIN_TRACKRECORDID + " = " + trackRecordCreationDate + ") " +
                "ORDER BY "+ MySQLiteHelper.CANDIDATESTARTFINISH_RATE + " DESC;";

        String queryEnd= "SELECT " + MySQLiteHelper.CANDIDATESTARTFINISH_TIME + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_RATE + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_LATITUDE + ", " + MySQLiteHelper.CANDIDATESTARTFINISH_LONGITUDE + " "
                + " FROM " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + " "
                + "WHERE EXISTS "
                + "(SELECT * FROM " + MySQLiteHelper.TABLE_FINISHEDIN + " "
                + "WHERE " + MySQLiteHelper.TABLE_FINISHEDIN + "." + MySQLiteHelper.FINISHEDIN_CANDIDATEIDTIME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_TIME + " "
                + "AND " + MySQLiteHelper.TABLE_FINISHEDIN + "." + MySQLiteHelper.FINISHEDIN_CANDIDATEIDNAME + " = " + MySQLiteHelper.TABLE_CANDIDATESTARTFINISH + "." + MySQLiteHelper.CANDIDATESTARTFINISH_HOTPOINTNAME + " "
                + "AND " + MySQLiteHelper.TABLE_FINISHEDIN + "." + MySQLiteHelper.FINISHEDIN_TRACKRECORDID + " = " + trackRecordCreationDate + ") " +
                "ORDER BY "+ MySQLiteHelper.CANDIDATESTARTFINISH_RATE + " DESC;";

        Cursor set;

        if(startCandidates)
            set= database.rawQuery(queryStart, null);
        else
            set= database.rawQuery(queryEnd, null);

        set.moveToFirst();
        while(!set.isAfterLast()){
            candidatesToReturn.add(cursorToHotpointCandidate(set));
            set.moveToNext();
        }

        return candidatesToReturn;
    }


    private FinishHotpointsForStart cursorToFinishHotpointsForStart(Cursor cursor){
        FinishHotpointsForStart objReturn= new FinishHotpointsForStart();

        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            objReturn.addFinishHotpoint(cursor.getString(0), cursor.getString(1));
            cursor.moveToNext();
        }

        return objReturn;
    }


    private TransportationStatistics cursorToTransportationStatistics(Cursor cursor){
        Context context= MyApplication.getAppContext();
        TransportationStatistics objReturn= new TransportationStatistics();

        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            if(cursor.getString(1).startsWith(context.getString(R.string.transportation_type_other)))
                objReturn.setAltro(cursor.getDouble(0));
            else if(cursor.getString(1).equals(context.getString(R.string.transportation_type_bus)))
                objReturn.setAutobus(cursor.getDouble(0));
            else if(cursor.getString(1).equals(context.getString(R.string.transportation_type_bycicle)))
                objReturn.setBici(cursor.getDouble(0));
            else if(cursor.getString(1).equals(context.getString(R.string.transportation_type_car)))
                objReturn.setAuto(cursor.getDouble(0));
            else if(cursor.getString(1).equals(context.getString(R.string.transportation_type_feet)))
                objReturn.setPiedi(cursor.getDouble(0));

            cursor.moveToNext();
        }

        return objReturn;
    }


    public FinishHotpointsForStart getStartFinishHotpoints(){
        Cursor set= database.rawQuery(getHotpointsStartFinishInTrackRecord, null);
        FinishHotpointsForStart objReturn=  cursorToFinishHotpointsForStart(set);
        return objReturn;
    }


    public TransportationStatistics getStartFinishHotpointStatistics(String startHotpoint, String finishHotpoint){
        String[] args= {startHotpoint, finishHotpoint};
        Cursor set= database.rawQuery(queryAvgTimeByTransportation, args);

        TransportationStatistics statReturn= cursorToTransportationStatistics(set);
        statReturn.setDa(startHotpoint);
        statReturn.setA(finishHotpoint);

        return statReturn;
    }


    public void eraseDB(){
        database.delete(MySQLiteHelper.TABLE_CANDIDATESTARTFINISH, null, null);
        database.delete(MySQLiteHelper.TABLE_FINISHEDIN, null, null);
        database.delete(MySQLiteHelper.TABLE_STARTEDIN, null, null);
        database.delete(MySQLiteHelper.TABLE_TRACKRECORD, null, null);
    }

}

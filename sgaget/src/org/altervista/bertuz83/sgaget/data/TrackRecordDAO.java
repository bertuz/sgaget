package org.altervista.bertuz83.sgaget.data;


import org.altervista.bertuz83.sgaget.business.FinishHotpointsForStart;
import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.altervista.bertuz83.sgaget.business.TransportationStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: bertuz
 * Project: sgaget
 */
public interface TrackRecordDAO {
    public void openWrite() throws SQLException;
    public void openRead() throws SQLException;
    public void close();
    public void openSecure() throws SQLException;
    public void closeSecure(boolean successful);

    public void beginAtomicOperations();
    public void endAtomicOperations();


    //returns true if the insertion was successful
    public long insertTrackRecord(TrackRecord trackRecord);
    public ArrayList<TrackRecord> getTrackRecordsToComplete();
    public TrackRecord getTrackRecord(long creationDate);
    public boolean deleteTrackRecord(long creationDate);
    public boolean completeTrackRecord(TrackRecord trackRecord);
    public boolean updateTrackRecord(TrackRecord trackRecord);
    public ArrayList<TrackRecord> getTrackRecordsToSend();
    public int getNrTrackRecordToSend();
    public FinishHotpointsForStart getStartFinishHotpoints();
    public TransportationStatistics getStartFinishHotpointStatistics(String startHotpoint, String finishHotpoint);
    public void eraseDB();
}

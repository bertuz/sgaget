package org.altervista.bertuz83.sgaget;

import org.altervista.bertuz83.sgaget.business.TrackRecord;

/**
 * User: bertuz
 * Project: sgaget
 *
 * interfaccia di comunicazione tra l'activity CompleteTrack e i suoi fragments (passi del completamento del tragitto)
 */
public interface IActCompleteFragsComm{
    public void setTrackRecord(TrackRecord trackRecord);
    public boolean saveSettings();
}

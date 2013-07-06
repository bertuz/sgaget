package org.altervista.bertuz83.sgaget.receiver;

/**
 * User: bertuz
 * Project: sgaget
 * Creatuion date: 05/06/13, 00:27
 */
public interface ControlReceiverNewTrackRecordToComplete {
    public void onNewTrackRecordToComplete(long creationTime);
    public void onTrackRecordCompleted(long creationTime);
}

package org.altervista.bertuz83.sgaget.receiver;

/**
 * User: bertuz
 * Project: sensor
 * Creatuion date: 19/05/13, 16:48
 * Interfaccia che deve implementare per ricevere gli eventi del broadcast receiver TrackingStatus
 */
public interface ControlReceiverTracking{
    public void onTrackingStopped();
    public void onTrackingStartingError();
    public void onTrackingStartingNoHotpoints();
    public void onTrackingNoWiFi();
}

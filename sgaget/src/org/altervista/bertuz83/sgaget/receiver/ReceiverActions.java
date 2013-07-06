package org.altervista.bertuz83.sgaget.receiver;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Tutte le action identifier utilizzate da sender e receiver interni all'applicazione sono definite qui
 * per praticità e per evitare action identifier identici per receiver diversi.
 *
 * Le action identifier qui listate sono pensate SOLO per un uso tramite LOCAL BROADCAST (supportlibrary)
 */
public class ReceiverActions {
    public static final String ACT_REC_HOTPOINTSNEARBY_LOCATION_UPDATED = "updateLoc";

    public static final String ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_TO_COMPLETE = "newTrackRecordToComplete";
    public static final String ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_COMPLETED = "trackRecordCompleted";

    public static final String ACT_REC_TRACKINGSTATUS_STOPPED = "trackingStopped";
    public static final String ACT_REC_TRACKINGSTATUS_ERROR_START= "trackingErrorStart";
    public static final String ACT_REC_TRACKINGSTATUS_ERROR_START_NOPOINTS= "trackingErrorNohopoints";
    public static final String ACT_REC_TRACKINGSTATUS_ACTION_NO_WIFI= "trackingNoWiFi";



}

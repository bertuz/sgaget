package org.altervista.bertuz83.sgaget.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Receiver per local broadcast notificanti un nuovo spostamento da completare disponibile.
 * Utilizzato quando il tracciamento di uno spostamento è stato portato a termine con successo, quindi
 * la lista degli spostamenti da compilare deve essere aggiornata con quest'ultimo tracciamento.
 *
 * @see org.altervista.bertuz83.sgaget.ActTrackingCompleted
 * @see org.altervista.bertuz83.sgaget.FTabCompletareActHome
 */
public class ReceiverNewTrackRecordToComplete extends BroadcastReceiver {
    public static final String EXTRA_CREATIONDATE = "creationDate";

    ControlReceiverNewTrackRecordToComplete controllerToCall;

    public ReceiverNewTrackRecordToComplete(ControlReceiverNewTrackRecordToComplete controller){
        this.controllerToCall= controller;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();
        long creationDate= intent.getLongExtra(EXTRA_CREATIONDATE, -1);

        if(action == ReceiverActions.ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_TO_COMPLETE){
            this.controllerToCall.onNewTrackRecordToComplete(creationDate);
            return;
        }
        if(action.equals( ReceiverActions.ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_COMPLETED )){
            this.controllerToCall.onTrackRecordCompleted(creationDate);
        }
    }
}

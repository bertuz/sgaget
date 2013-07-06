package org.altervista.bertuz83.sgaget.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * User: bertuz
 * Project: sgaget
 *
 * receiver utilizzato per la ricezione dei nuovi stati riguardo il motore di tracciamento
 *
 * @see org.altervista.bertuz83.sgaget.service.Locator
 * @see org.altervista.bertuz83.sgaget.FTabSpostamentoActHome
 */
public class ReceiverTrackingStatus extends BroadcastReceiver {
    ControlReceiverTracking controllerToCall;

    public ReceiverTrackingStatus(ControlReceiverTracking controlReceiverTracking){
        this.controllerToCall= controlReceiverTracking;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action= intent.getAction();

        if(action == ReceiverActions.ACT_REC_TRACKINGSTATUS_STOPPED){
            this.controllerToCall.onTrackingStopped();
            return;
        }
        if(action== ReceiverActions.ACT_REC_TRACKINGSTATUS_ERROR_START){
            this.controllerToCall.onTrackingStartingError();
            return;
        }
        if(action==  ReceiverActions.ACT_REC_TRACKINGSTATUS_ERROR_START_NOPOINTS){
            this.controllerToCall.onTrackingStartingNoHotpoints();
            return;
        }
        if(action== ReceiverActions.ACT_REC_TRACKINGSTATUS_ACTION_NO_WIFI){
            this.controllerToCall.onTrackingNoWiFi();
            return;
        }
    }
}

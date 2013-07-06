package org.altervista.bertuz83.sgaget.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import org.altervista.bertuz83.sgaget.business.Hotpoint;
import org.altervista.bertuz83.sgaget.service.Locator;
import org.altervista.bertuz83.sgaget.service.ServiceTracking;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * User: bertuz
 * Project: sensor
 *
 * Receiver per local broadcast notificanti nuovi punti di interesse in prossimità.
 * Utilizzato come feedback di buon funzionamento del tracciamento all'utilizzatore.
 *
 * @see org.altervista.bertuz83.sgaget.service.Locator
 * @see org.altervista.bertuz83.sgaget.FTabSpostamentoActHome
 */
public class ReceiverHotpointsNearby extends BroadcastReceiver{
    private ServiceTracking boundService;
    private ArrayList<String> locationsList;
    private ArrayAdapter adapter;

    public ReceiverHotpointsNearby(ArrayList<String> locationsList, ArrayAdapter adapter, ServiceTracking boundService){
        this.locationsList= locationsList;
        this.adapter= adapter;
        this.boundService= boundService;
    }

    public void onReceive(Context context, Intent intent){
        locationsList.clear();
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");
        String time= sdfDate.format(boundService.getLastTrackingUpdate().getLocation().getTime());

        for(Hotpoint hotpoint: boundService.getLastTrackingUpdate().getHotpoints()){
            locationsList.add(time + " " + hotpoint.getName());
        };
        adapter.notifyDataSetChanged();
    }


}

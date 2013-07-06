package org.altervista.bertuz83.sgaget.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import org.altervista.bertuz83.sgaget.*;
import org.altervista.bertuz83.sgaget.business.HotpointCandidate;
import org.altervista.bertuz83.sgaget.helper.MyApplication;
import org.altervista.bertuz83.sgaget.receiver.ControlReceiverWiFiChanged;
import org.altervista.bertuz83.sgaget.receiver.ReceiverActions;
import org.altervista.bertuz83.sgaget.receiver.ReceiverWiFiChanged;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Service del tracciamento degli spostamenti.
 *
 * DO NOT USE LIKE A SERVICE TO BE EXPOSED IN THE MANIFEST. It has been thought like an internal service
 */
public class ServiceTracking extends Service implements ControlReceiverWiFiChanged {
    private Locator locator;
    // This is the object that receives interactions from clients.
    private final IBinder mBinder= new LocalBinder();
    private ReceiverWiFiChanged receiver;
    private boolean abortedForWiFiDisabled = false;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        public ServiceTracking getService() {
            return ServiceTracking.this;
        }

        public LocalBinder(){
            super();
        }
    }


    @Override
    public void onWiFiDisabled() {
        if( !MyApplication.DEBUG ){
            locator.abortTracking();
            this.stopService();

            /*
                questo potrebbe non essere letto (app in background, eccetto il service) e un local sticky broadcast non
                e' ancora stato implementato da google. Quindi utilizzo la variabile abortedForNoWifi che leggero'
                nel caso rieffettui il binding in tabtragitto una volta  l'activity home sia in foreground.
             */
            MyApplication.getAppContext().unregisterReceiver(this.receiver);
            abortedForWiFiDisabled= true;
            Intent wifiDisabledIntent= new Intent(ReceiverActions.ACT_REC_TRACKINGSTATUS_ACTION_NO_WIFI);
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(wifiDisabledIntent);
        }
    }


    public boolean isAbortedForWiFiDisabled(){
        return abortedForWiFiDisabled;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        abortedForWiFiDisabled= false;
        this.locator.startTracking();

        Intent notifyIntent= new Intent(MyApplication.getAppContext(), ActHome.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notifyIntent.putExtra("homeTab", true);
        PendingIntent intentHome= PendingIntent.getActivity(MyApplication.getAppContext(),2,notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notificationToSend= new Notification(R.drawable.icon_notification, "Tracciamento avviato", System.currentTimeMillis());
        notificationToSend.setLatestEventInfo(MyApplication.getAppContext(), "Tracciamento sgàget!","Seleziona per tornare al tracciamento", intentHome);
        notificationToSend.flags |= Notification.DEFAULT_SOUND;
        notificationToSend.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(MyApplication.NOTIFICATION_TRACKING, notificationToSend);

        receiver= new ReceiverWiFiChanged(this);
        IntentFilter iff= new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        MyApplication.getAppContext().registerReceiver(receiver, iff);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void newTracking(){
        this.abortedForWiFiDisabled= false;
        this.locator= new Locator((LocationManager) MyApplication.getAppContext().getSystemService(MyApplication.getAppContext().LOCATION_SERVICE));
    }


    public int getNrHotpointUpdates(){
        if(this.locator == null)
            return -1;

        return this.locator.getNrHotpointUpdatesTracked();
    }


    public boolean areUpdatesEnough(){
        if ((this.locator.getHotpointsHistory()!= null)&&(this.locator.getHotpointsHistory().size() < 3))
            return false;

        return true;
    }


    public Collection<HotpointCandidate> getHotpointCandidatesSortedForUpdate(int nrUpdate) throws LocatorException{
        if(this.locator == null)
            return null;

        this.locator.sortHotpoints(nrUpdate);

        return (this.locator.getHotpointsCandidates());
    }


    public void stopTracking() throws LocatorException {
        MyApplication.getAppContext().unregisterReceiver(this.receiver);
        locator.stopTracking();
    }


    public void stopService(){
        stopForeground(true);
        this.stopSelf();
        abortedForWiFiDisabled= false;
    }


    public int getTrackingState(){
        if(this.locator == null)
            return -1;

        return this.locator.getState();
    }


    public Locator.PositionUpdate getLastTrackingUpdate(){
        if (locator== null)
            return null;

        return locator.getActualUpdate();
    }


    public void abortTracking(){
        locator.abortTracking();
        this.stopService();
        MyApplication.getAppContext().unregisterReceiver(this.receiver);
    }
}

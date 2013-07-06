package org.altervista.bertuz83.sgaget;

import android.content.*;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.altervista.bertuz83.sgaget.dialogs.DialogInfo;
import org.altervista.bertuz83.sgaget.helper.MyApplication;
import org.altervista.bertuz83.sgaget.service.Locator;
import org.altervista.bertuz83.sgaget.receiver.*;
import org.altervista.bertuz83.sgaget.service.ServiceTracking;

import java.util.*;

/**
 * User: o bertuz
 * Project: sgàget
 *
 * tab di tracciamento dello spostamento
 *
 * @see org.altervista.bertuz83.sgaget.ActHome
 */
public class FTabSpostamentoActHome extends Fragment implements ControlReceiverTracking, ControlReceiverBoundService {
    public static final String TAGNAME="tabSpostamento";

    public static final String PERSISTENCE_HOTPOINTS= FTabSpostamentoActHome.class.getName()+"Hotpoints";
    public static final String PERSISTENCE_STARTTIME= FTabSpostamentoActHome.class.getName()+"StartTime";

    private Timer timerTrace;
    private long startTime= 0;
    private LocationManager lm= (LocationManager) MyApplication.getAppContext().getSystemService(MyApplication.getAppContext().LOCATION_SERVICE);
    private ArrayList<String> hotpoints= new ArrayList<String>();
    private ArrayAdapter<String> hotpointsAdapter;
    private ListView hotpointsViewList;

    //my receivers
    private ReceiverHotpointsNearby hotpointsReceiver;
    private ReceiverTrackingStatus trackingStatusReceiver;

    //tracking service properties
    private ServiceTracking boundService;
    private boolean onStartCalled= false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService= ((ServiceTracking.LocalBinder)service).getService();
            onTrackingServiceBound();
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService= null;
        }
    };


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(FTabSpostamentoActHome.PERSISTENCE_STARTTIME, this.startTime);
        outState.putStringArrayList(FTabSpostamentoActHome.PERSISTENCE_HOTPOINTS, this.hotpoints);
        //setUserVisibleHint(true);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //gui preparation (pristine session)
        hotpoints= new ArrayList<String>();
        this.hotpointsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, hotpoints);
        this.hotpointsViewList= ((ListView)getView().findViewById(R.id.tab_spostamento_hotpoints_around));
        this.hotpointsViewList.setAdapter(hotpointsAdapter);

        //gui (restoring last session)
        if(savedInstanceState != null){
            this.startTime= savedInstanceState.getLong(FTabSpostamentoActHome.PERSISTENCE_STARTTIME, 0);
            hotpointsAdapter.addAll(savedInstanceState.getStringArrayList(FTabSpostamentoActHome.PERSISTENCE_HOTPOINTS));
            hotpointsAdapter.notifyDataSetChanged();
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundler){
        View fragmentView= (inflater.inflate(R.layout.tab_spostamento, container, false));
        return fragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();

        /*
            bound effettuato nella onstart. Nel caso vada semplicemente in pause, non avrò una callback di bound effettuato
            dove effettuo tutte le operazioni all'avvio del fragment che necessitano del binding al service.
            Per questo, avendo per forza il bound, chiamo manualmente la callback di bound effettuato.

            Nel caso il fragment sia stato appena crato, onstart sia stata chiamata e una richiesta di binding sia stata effettuata,
            MA IL BINDING NON È STATO ANCORA EFFETTUATO (stato di boundservice a null e boundcalled false)

            => aspetterò il binding al service prima di chiamare la callback
         */
        if(!onStartCalled)
            onTrackingServiceBound();
    }


    private void startTrackingDisplay(){
        if(timerTrace!=null){
            timerTrace.cancel();
            timerTrace.purge();
        }

        //se dopo 30 secondi non ho ricevuto l'ultimo aggiornamento, chiudo comunque il tracking con le informazioni che ho ricevuto.
        timerTrace= new Timer();
        TimerTask timerTask= new MyTimerTaskFTabSpostamento(this, startTime);
        timerTrace.schedule(timerTask, 0, 1000);

        IntentFilter iff= new IntentFilter(ReceiverActions.ACT_REC_HOTPOINTSNEARBY_LOCATION_UPDATED);
        this.hotpointsReceiver= new ReceiverHotpointsNearby(this.hotpoints, this.hotpointsAdapter, this.boundService);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(hotpointsReceiver, iff);

        if((boundService.getTrackingState() == Locator.TRACKING)||(boundService.getTrackingState() == Locator.HASTOSTART)){
            getView().findViewById(R.id.tab_spostamento_startStop).setBackgroundDrawable(getResources().getDrawable(R.drawable.grad_red));
            ((Button)getView().findViewById(R.id.tab_spostamento_startStop)).setText(R.string.home_start_stop_end);
            getView().findViewById(R.id.tab_spostamento_logoISF).setVisibility(View.GONE);
            getView().findViewById(R.id.tab_spostamento_bottone).setLayoutParams(
                    new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));
        }
        else{
            getView().findViewById(R.id.tab_spostamento_startStop).setBackgroundDrawable(getResources().getDrawable(R.drawable.grad_yellow));
            ((Button)getView().findViewById(R.id.tab_spostamento_startStop)).setText(R.string.home_start_stop_endind);
            getView().findViewById(R.id.tab_spostamento_logoISF).setVisibility(View.GONE);
            getView().findViewById(R.id.tab_spostamento_bottone).setLayoutParams(
                    new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));
        }
    }


    private void resetTrackingDisplay(){
        //
        //ACTUALIZAÇÕES DA GUI
        //
        /*
            timer podria estar no estado do null:
            1. fragment ricreato nello stesso momento in cui il tracking si ferma
            2. tracking invia broadcast di terminazione tracking. Fragment in creazione, nessuno ricevera' il broadcast fino a fragment ricreato.
            3. fragment ricreato. Effetto binding al servizio di trcking
            4. binding effettuato: controllo se c'e' tracking in corso. NO=> non avvio né timers, né display etc (quindi non chiamo startDisplaytimer)
            5. INIZIO  SOLO ORA ASCOLTO EVENTI DAL TRACKING: ricevo quindi il broadcast di terminazione
            6. Azioni di terminazione tracking + ripristino display e timer. Ma startTrackingDisplay mai chiamata: sarà null.
            É preciso, portanto, verifica-lo (timer deve ter uma inicialização)
         */
        this.hotpoints.clear();
        this.hotpointsAdapter.notifyDataSetChanged();

        getView().findViewById(R.id.tab_spostamento_startStop).setBackgroundDrawable(getResources().getDrawable(R.drawable.grad_green));
        ((Button)getView().findViewById(R.id.tab_spostamento_startStop)).setText(R.string.home_start_stop_default);
        getView().findViewById(R.id.tab_spostamento_logoISF).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.tab_spostamento_bottone).setLayoutParams(
                new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        this.enableButtonStartStopTracking();

        if(timerTrace !=null){
            timerTrace.cancel();
            timerTrace.purge();
        }
        ((TextView)getView().findViewById(R.id.tab_spostamento_time)).setText("00:00:00");
        getView().findViewById(R.id.tab_spostamento_display_tracking).setVisibility(View.GONE);
    }


    public void startStopTrackingPressed(View viewClicked){
        if((boundService.getTrackingState() == Locator.COMPLETED)||(boundService.getTrackingState() == -1)){
            boundService.newTracking();
        }

        if(boundService.getTrackingState() == Locator.HASTOSTART){
            //check if everething is ok before tracking
            if(!lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                DialogInfo dialog= DialogInfo.newInstance("Sgàget ha bisogno di funzionalità che questo telefono sembra non supportare.", true, "Network provider assente");
                dialog.show(getFragmentManager(), "errorNowifi");
                return;
            }

            WifiManager wifi = (WifiManager) getActivity().getSystemService(getActivity().WIFI_SERVICE);
            if(!wifi.isWifiEnabled()){
                //I use a deprecated method because I need to run on api-10-driven appliances
                getActivity().showDialog(ActHome.DIALOG_ACTIVATE_WIFI);
            }
            else{
                LocationManager gps= (LocationManager)getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                if(!gps.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    getActivity().showDialog(ActHome.DIALOG_ACTIVATE_GPS);

                getView().findViewById(R.id.tab_spostamento_display_tracking).setVisibility(View.VISIBLE);
                startTime= Calendar.getInstance().getTimeInMillis();

                Intent serviceTracking= new Intent(getActivity(), ServiceTracking.class);
                getActivity().startService(serviceTracking);

                this.startTrackingDisplay();
            }
        }
        else if(boundService.getTrackingState() == Locator.TRACKING){
            try{
                this.disableButtonStartStopTracking();
                getActivity().findViewById(R.id.tab_spostamento_startStop).setBackgroundDrawable(getResources().getDrawable(R.drawable.grad_yellow));
                ((Button)getActivity().findViewById(R.id.tab_spostamento_startStop)).setText(R.string.home_start_stop_endind);

                LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.hotpointsReceiver);
                hotpointsReceiver= null;

                boundService.stopTracking();
            }catch(Exception e){ }
        }
    }


    @Override
    public void onStop(){
        if(mConnection!=null)
            MyApplication.getAppContext().unbindService(mConnection);
        super.onStop();
    }


    @Override
    public void onPause(){
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.trackingStatusReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.hotpointsReceiver);

        if(timerTrace!=null){
            timerTrace.cancel();
            timerTrace.purge();

        }

        onStartCalled= false;
        super.onPause();
    }


    @Override
    public void onStart() {
        Log.d("debugCheck", "chiamataa¡¡");
        super.onStart();
        onStartCalled= true;
        MyApplication.getAppContext().bindService(new Intent(MyApplication.getAppContext(), ServiceTracking.class), mConnection, Context.BIND_AUTO_CREATE);
    }


    public void disableButtonStartStopTracking() {
        getView().findViewById(R.id.tab_spostamento_startStop).setOnClickListener(null);
    }


    public void enableButtonStartStopTracking(){
        getView().findViewById(R.id.tab_spostamento_startStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopTrackingPressed(view);
            }
        });
    }


    @Override
    public void onTrackingStartingError() {
        try{
            boundService.stopService();
        }catch(Exception e){ }

        DialogInfo dialog= DialogInfo.newInstance("Sgàget ha riscontrato un problema nel motore di tracciamento dello spostamento.", true, null);
        dialog.show(getFragmentManager(), "trackproblem");
        resetTrackingDisplay();
    }


    @Override
    public void onTrackingStartingNoHotpoints() {
        try{
            boundService.stopService();
        }catch(Exception e){ }

        ((ActHome)getActivity()).showDialog(ActHome.DIALOG_NO_HOTPOINTS);
        resetTrackingDisplay();
    }


    @Override
    public void onTrackingNoWiFi() {
        if(! MyApplication.DEBUG){
            boundService.stopService();
            resetTrackingDisplay();
            DialogInfo dialog= DialogInfo.newInstance("Pare che il WiFi sia stato disabilitato mentre sgàget tracciava lo spostamento. ", true, null);
            dialog.show(getFragmentManager(), "nowifiproblem");
        }
    }


    @Override
    public void onTrackingStopped(){
        /*
            fermo il servizio (oltre che il tracking del Locator) a ricezione della cessazione del tracking del locator perche' .
            nel caso
            1. cliccassi stop
            2. sia il tracking che il servizio vengono stoppati
            3. nello stesso momento ruoto lo schermo (quindi perdendo il binding al servizio)

            mi troverei senza nessuna istanza al puntatore per un momento. Quindi il servizio, con annesso LOCATOR, verrebbe rilasciato.
            Fermando il servizio qui, ho comunque la certezza di aver riacquisito un binding e non verra' scartato (con Locator annesso)
         */
        if(!boundService.areUpdatesEnough()){
            if(! MyApplication.DEBUG){
                getActivity().showDialog(ActHome.DIALOG_WARNING_POOR_POINTS);
                DialogInfo dialog= DialogInfo.newInstance("Sgàget non ha rilevato abbastanza aree durante il percorso.\nSicur* d'esserti spostat*?", true, "Spostamento troppo corto");
                dialog.show(getFragmentManager(), "errorpoorpoints");
            }
        }
        else{
            Intent activityCompleted= new Intent(getActivity(), ActTrackingCompleted.class);
            activityCompleted.putExtra(ActTrackingCompleted.MSG_TIME, ((TextView)getView().findViewById(R.id.tab_spostamento_time)).getText());
            startActivity(activityCompleted);
        }

        this.boundService.stopService();
        resetTrackingDisplay();
    }


    @Override
    public void onTrackingServiceBound() {
        if(boundService.isAbortedForWiFiDisabled())
            this.onTrackingNoWiFi();

        if((boundService.getTrackingState() == Locator.TRACKING) || (boundService.getTrackingState() == Locator.TERMINATING)){
            getView().findViewById(R.id.tab_spostamento_display_tracking).setVisibility(View.VISIBLE);
            this.startTrackingDisplay();

            /*
                simulo la ricezione di nuove punti di interesse (hotpoints) che viene notificata tramite local broadcasts
                Il metodo chiamato fa parte de receiver incaricato di gestire appunto questo tipo di broadcast per conto di
                chi l'ha istanziato (in questo caso, questo fragment)
                Nel caso il tracking sia avviato, ma nessun punto è stato ancora ricevuto (non si è stati in prossimità di nessun punto di interesse),
                allora non richiedo nessun punto.
             */
            if(boundService.getLastTrackingUpdate() != null)
                hotpointsReceiver.onReceive(null, null);
        }
        if(boundService.getTrackingState() != Locator.TERMINATING)
            this.enableButtonStartStopTracking();


        //solo dopo che ho ripristinato il contatto con il tracking service, mi occupo del fatto che potrei aver terminato il tracking durante l'assenza del fragment
        IntentFilter iff= new IntentFilter(ReceiverActions.ACT_REC_TRACKINGSTATUS_STOPPED);
        iff.addAction(ReceiverActions.ACT_REC_TRACKINGSTATUS_ERROR_START);
        iff.addAction(ReceiverActions.ACT_REC_TRACKINGSTATUS_ERROR_START_NOPOINTS);
        iff.addAction(ReceiverActions.ACT_REC_TRACKINGSTATUS_ACTION_NO_WIFI);

        this.trackingStatusReceiver = new ReceiverTrackingStatus(this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this.trackingStatusReceiver, iff);
    }

}

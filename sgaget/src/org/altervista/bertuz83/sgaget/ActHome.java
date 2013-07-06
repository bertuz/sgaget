package org.altervista.bertuz83.sgaget;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.altervista.bertuz83.sgaget.data.*;
import org.altervista.bertuz83.sgaget.business.Hotpoint;
import org.altervista.bertuz83.sgaget.dialogs.DialogExit;
import org.altervista.bertuz83.sgaget.dialogs.DialogInfo;
import org.altervista.bertuz83.sgaget.dialogs.DialogSendTrackRecords;
import org.altervista.bertuz83.sgaget.dialogs.WaitDialog;
import org.altervista.bertuz83.sgaget.helper.AppPreferences;
import org.altervista.bertuz83.sgaget.helper.MyApplication;
import org.altervista.bertuz83.sgaget.receiver.ControlReceiverBoundService;
import org.altervista.bertuz83.sgaget.service.Locator;

import org.altervista.bertuz83.sgaget.service.ServiceTracking;

import java.lang.reflect.Field;
import java.util.*;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Activity principale.
 */
public class ActHome extends SherlockFragmentActivity implements ActionBar.TabListener,
                                                                    ControlReceiverBoundService,
                                                                    IFragActHomeComm,
                                                                    FragTaskSendTrack.TaskCallbacks,
                                                                    DialogExit.DialogInfoComm{
    public static final int DIALOG_ACTIVATE_WIFI= 0;
    public static final int DIALOG_WARNING_NO_NETOWRK= 1;
    public static final int DIALOG_UPDATE_HOTPOINTS= 2;
    public static final int DIALOG_UPDATE_HOTPOINTS_FAILED= 3;
    public static final int DIALOG_WARNING_POOR_POINTS= 4;
    public static final int DIALOG_NO_HOTPOINTS= 5;
    public static final int DIALOG_TRACKING_ERROR= 6;
    public static final int DIALOG_TRACKING_NO_WIFI= 7;
    public static final int DIALOG_ACTIVATE_GPS= 8;

    public static final String MSG_ENTRANCE = "entrance";
    public static final int MSG_ENTRANCE_TRACKING_SAVED = 0;
    public static final int MSG_ENTRANCE_TRACKING_COMPLETED = 1;
    public static final String MSG_ENTRANCE_TRACKING_COMPLETED_EXTRA_TRACKID = "trackid";

    private static final int REQ_DB_CHANGED= 1;

    private boolean isRunning= false;
    private boolean fragmentSendTrackTaskToDismiss= false;
    private boolean fragmentSendTrackTaskToDismissSuccess;
    private int fragmentSendTrackTaskToDismissNrSent;

    private ViewPager pagerTabs;
    private MyPagerAdapterActHome pagerAdapter;

    private ActHome.DownloadHotpointsTask taskDownloadHotpoints;
    private ProgressDialog updateHotpointsDialog;

    private AppPreferences preferences= new AppPreferences();

    private DialogFragment uploadDialogFrament;
    private FragTaskSendTrack fragmentSendTrackTask;
    private Long sendTrackRecordsToCall= null;

    private MenuItem sendTrackRecordsItem;
    private MenuItem sortButton;

    private int nrTrackRecordsToSend= 0;

    //tracking service properties
    private ServiceTracking boundService;

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
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent i;

        switch(item.getItemId()){
            case R.id.actionbar_send_trackrecords:
                sendTrackRecords(Long.MAX_VALUE);
                break;
            case R.id.actionbar_sort_first_new:
                pagerAdapter.getTabCompletare().onChangeSortList(true);
                break;
            case R.id.actionbar_sort_first_old:
                pagerAdapter.getTabCompletare().onChangeSortList(false);
                break;
            case R.id.actionbar_settings:
                i= new Intent(this, ActPreferences.class);
                startActivityForResult(i, REQ_DB_CHANGED);
                break;
            case R.id.actionbar_statistics:
                i= new Intent(this, ActCharts.class);
                startActivity(i);
                break;
        }

        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == REQ_DB_CHANGED ){
            if ( resultCode == RESULT_OK ){
                boolean dbChanged=data.getBooleanExtra("dbChanged", false);
                if ( dbChanged ){
                    this.sendTrackRecordsItem.setEnabled(false);
                    this.sendTrackRecordsItem.setIcon(R.drawable.send_data_disabled);
                    pagerAdapter.getTabCompletare().deleteAllItems();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu, menu);

        sendTrackRecordsItem= menu.findItem(R.id.actionbar_send_trackrecords);
        sortButton= menu.findItem(R.id.actionbar_sort_button);
        this.updateSendTrackRecordsButton(0);

        updateActionbar();
        return true;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        //se non esistono le impostazioni iniziali dovute, vai alla schermata di benvenuto
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email = prefs.getString(ActPreferences.SETTINGS_EMAIL, "");

        if( email.length()==0 ){
            Intent goWelcome= new Intent(this, ActWelcome.class);
            goWelcome.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(goWelcome);
            finish();
        }

        /*
            tabs (FRAGMENTS) creati (non visualizzati). Ora mi lego al servizio di tracking che serve al tab di tracciamento.
            una volta effettuato il binding (asincrono), verra' chiamata una callback (onTrackingServiceBound) su tale fragment per avvertirlo del
            possibile uso del servizio. Saranno quindi effettuati sul fragment controlli come tracciamento in corso per visualizzare il display, etc..
        */
        bindService(new Intent(this, ServiceTracking.class), mConnection, Context.BIND_AUTO_CREATE);

        if(getLastCustomNonConfigurationInstance() != null)
            this.taskDownloadHotpoints= (DownloadHotpointsTask) getLastCustomNonConfigurationInstance();

        //
        //GUI
        //
        setContentView(R.layout.home);

        //enabling the overflow actionbar button even for D-hw-button driven appliances
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        //
        //FRAGMENTS (tabs) AND PAGER
        //
        pagerAdapter= new MyPagerAdapterActHome(getSupportFragmentManager(), R.id.home_pager);
        pagerTabs= (ViewPager) findViewById(R.id.home_pager);
        pagerTabs.setAdapter(pagerAdapter);
        pagerTabs.setCurrentItem(1);
        //mantengo sempre tutti i fragment che voglio visualizzare (maggiore velocita' nella visualizzazione dopo la prima creazione)
        pagerTabs.setOffscreenPageLimit(2);

        pagerTabs.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });


        //adding the actionbar tabs
        getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Punti di interesse").setTabListener(this));
        getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Nuovo spostamento").setTabListener(this));
        getSupportActionBar().addTab(getSupportActionBar().newTab().setText("Compila spotamenti").setTabListener(this));
        goToSpostamentoTab();

        //trackrecords upload managements
        uploadDialogFrament= (DialogFragment) getSupportFragmentManager().findFragmentByTag("sendDialog");
        fragmentSendTrackTask= (FragTaskSendTrack) fm.findFragmentByTag("sendTask");


        if(savedInstanceState == null){
            TrackRecordDAO dbDAO= new TrackRecordDB();
            try{
                dbDAO.openRead();
            }catch (Exception e){ }

            this.nrTrackRecordsToSend= dbDAO.getNrTrackRecordToSend();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        //não mudar este bloque de código na onStart(). onRetainCustomNonConfigurationInstance é chamada somente ANTES da onResume. Por isso DEVE FICAR A CÁ.
        //ripristina, dopo l'eventuale ricreazione della activity, il dialog
        if( (this.taskDownloadHotpoints != null) && (this.taskDownloadHotpoints.hasFinished() == false) )
            this.taskDownloadHotpoints.setDialog(updateHotpointsDialog);

        if(fragmentSendTrackTaskToDismiss){
            fragmentSendTrackTaskToDismiss= false;
            onTerminated(fragmentSendTrackTaskToDismissSuccess, fragmentSendTrackTaskToDismissNrSent);
        }

        if(sendTrackRecordsToCall != null){
            sendTrackRecords(sendTrackRecordsToCall);
            sendTrackRecordsToCall= null;
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //selecting the right tab in case someone called this activity with an intent
        if (intent.getBooleanExtra("homeTab", false))
            this.pagerTabs.setCurrentItem(1, true);

        Toast toastToShow;

        //actions to do
        switch (intent.getIntExtra(MSG_ENTRANCE, -1)){
            case MSG_ENTRANCE_TRACKING_SAVED:
                toastToShow= Toast.makeText(MyApplication.getAppContext(), "Spostamento salvato", Toast.LENGTH_SHORT);
                toastToShow.setGravity(Gravity.BOTTOM, 0, 130);
                toastToShow.show();
                break;
            case MSG_ENTRANCE_TRACKING_COMPLETED:
                updateSendTrackRecordsButton(+1);

                long creationDate= intent.getLongExtra(MSG_ENTRANCE_TRACKING_COMPLETED_EXTRA_TRACKID, -1);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean sendNow= prefs.getBoolean(ActPreferences.SETTINGS_SEND_AT_ONCE, false);
                if(!sendNow){
                    toastToShow= Toast.makeText(MyApplication.getAppContext(), "Informazioni sullo spostamento completate", Toast.LENGTH_SHORT);
                    toastToShow.setGravity(Gravity.BOTTOM, 0, 130);
                    toastToShow.show();
                }
                else
                    sendTrackRecordsToCall= creationDate;

                break;
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        isRunning= true;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("nrTrackRecordsToSend", nrTrackRecordsToSend);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null){
            nrTrackRecordsToSend= savedInstanceState.getInt("nrTrackRecordsToSend");
            updateSendTrackRecordsButton(0);
        }
    }


    private void updateSendTrackRecordsButton(int trackrecordsUpdating){
        nrTrackRecordsToSend += trackrecordsUpdating;

        if(nrTrackRecordsToSend > 0){
            this.sendTrackRecordsItem.setEnabled(true);
            this.sendTrackRecordsItem.setIcon(R.drawable.send_data);
        }
        else{
            this.sendTrackRecordsItem.setEnabled(false);
            this.sendTrackRecordsItem.setIcon(R.drawable.send_data_disabled);
        }

    }


    @Override
    protected void onStop() {
        super.onStop();

        //nel caso questa activity venga messa in stop, non continuo l'upload degli trackrecord in background. (eccezion fatta per ricreaz. immediata dell'activity)
        if ( (!isChangingConfigurations()) && (fragmentSendTrackTask != null) ){
            fragmentSendTrackTask.abort();
        }

        isRunning= false;
    }


    private void sendTrackRecords(long creationDate){
        /*
            1. elimino un eventuale dialog precedente (la creazione e' gestita dalle callback del fragment task)
            in reakta' questa situazione non dovrebbe mai succedere: questo tipo di fragment, se esistente, mostra un dialog. Quest'ultimo e' visualizzato *solo* se
            il task e' in esecuzione. Alla callback di terminazione del task, il dialog viene dismesso (e di conseguenza il suo fragment)
        */
        Fragment prev= getSupportFragmentManager().findFragmentByTag("sendDialog");
        if (prev != null)
            getSupportFragmentManager().beginTransaction().remove(prev).commit();

        //2. creo il task (in un fragment) che gestirà l'upload
        FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
        fragmentSendTrackTask= new FragTaskSendTrack();
        ft.add(fragmentSendTrackTask, "sendTask").commit();
        getSupportFragmentManager().executePendingTransactions();

        //3. avvio il task che gestisce l'upload
        if(creationDate!= Long.MAX_VALUE)
            fragmentSendTrackTask.setSingleSendToPerform(creationDate);

        fragmentSendTrackTask.startSend();
    }

    /*
      METODO DEPRECATO
       questo sistema per creare e mantenere dialogs anche durante la ricreazione di activity (es: cambio di configurazione)
       è stato deprecato, perciò non dovrebbe essere usato. In sostanza non è una questione di migliori prestazioni, bensì di
       un nuovo approccio all'uso di svariate componenti tramite l'uso dei Fragment a partire da android 3.0 (apo 10 o 11... non ricordo esattamente).

       Ho inizialmente implementato alcuni dialog con questo metodo poiché è stata la metodologia mostrata a lezione,
       e perché volevo un software compatibile con android 2.3.3 (api 10). Sperimentando poi la libreria di supporto v4 di google
       sono passato al nuovo approccio.
       Per problemi di tempo non ho potuto tramutare tutti i dialog attraverso il nuovo approccio, che è possibile vedere
       nel sottopackage "dialogs".
       Alcuni approcci utilizzati in tale package possono risultare strani: ad esempio si salvano gli stati dei dialog
       detenuti dai vari fragment. Questo non dovrebbe essere necessario, ma a causa dell'enorme numero di bugs che
       la support library contiene, ho dovuto realizzare tale workaround.

       È possibile seguire una delle discussione che ho aperto in merito qui:
       http://stackoverflow.com/questions/17035090/dialogfragments-dialog-loses-set-properties-after-a-conf-change/17035455#17035455
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        switch(id){
            case DIALOG_ACTIVATE_WIFI :
                builder.setTitle("WiFi necessario");
                builder.setMessage("Sgàget ha bisogno del supporto WiFi attivato per una corretta localizzazione.");
                builder.setCancelable(false);
                builder.setPositiveButton("Attiva", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Chiudi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog= builder.create();
                break;
            case DIALOG_ACTIVATE_GPS :
                builder.setTitle("GPS disattivato o assente");
                builder.setMessage("È consigliato attivare il supporto GPS per un miglior tracciamento possibile.");
                builder.setCancelable(false);
                builder.setPositiveButton("Attiva", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Ignora", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog= builder.create();
                break;
            case DIALOG_UPDATE_HOTPOINTS:
                updateHotpointsDialog=new ProgressDialog(this);
                updateHotpointsDialog.setTitle("Aggiornamento aree");
                updateHotpointsDialog.setIndeterminate(false);
                updateHotpointsDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                updateHotpointsDialog.setMessage("Scaricamento della nuova lista di aree in corso");
                updateHotpointsDialog.setIndeterminate(true);
                updateHotpointsDialog.setCancelable(false);
                dialog= updateHotpointsDialog;
                break;
            case DIALOG_UPDATE_HOTPOINTS_FAILED:
                builder.setTitle("Aggiornamento aree fallito");
                builder.setMessage("Sgàget ha riscontrato problemi nello scaricare le nuove aree.\nSaranno mantenute le aree attuali.");
                builder.setCancelable(true);
                builder.setPositiveButton("Chiudi", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.dismiss();
                    }
                });
                dialog= builder.create();
                break;
            case DIALOG_NO_HOTPOINTS:
                builder.setTitle("Nessun punto di interesse");
                builder.setMessage("Sgàget non ha nessun punto di interesse di cui ha bisogno per continuare.");
                builder.setCancelable(true);
                builder.setPositiveButton("Aggiorna i punti", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.dismiss();
                        preferences.setHotpointsVersion(0);
                        preferences.savePreferences();
                        taskDownloadHotpoints= new DownloadHotpointsTask();
                        taskDownloadHotpoints.execute();
                    }
                });
                builder.setNegativeButton("Chiudi", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.dismiss();
                    }
                });
                dialog= builder.create();
                break;
            default:
                dialog= null;
        }
        return dialog;
    }


    @Override
    protected void onDestroy() {
        if(mConnection!=null)
            unbindService(mConnection);

        //nel caso l'activity non venga ricreata, annullo il download degli hotpoints che stava effettuando
        if(isFinishing()){
            if(this.taskDownloadHotpoints!= null)
                this.taskDownloadHotpoints.cancel(true);
        }
        super.onDestroy();
    }


    @Override
    public Object onRetainCustomNonConfigurationInstance(){
        if((this.taskDownloadHotpoints!=null)&&(!taskDownloadHotpoints.hasFinished()))
            return this.taskDownloadHotpoints;

        return null;
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        this.pagerTabs.setCurrentItem(tab.getPosition());

        //all'avvio dell'activity ontabselected puo essere chiamato prima della creazione dell'actionbar. Quindi la chiamata all'update è da controllare.
        if(this.sortButton == null)
            return;
        updateActionbar();
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) { }


    private void updateActionbar(){
        ActionBar.Tab tab= getSupportActionBar().getSelectedTab();

        switch(tab.getPosition()){
            case 0:
                this.sortButton.setVisible(false);
                break;
            case 1:
                this.sortButton.setVisible(false);
                break;
            case 2:
                this.sortButton.setVisible(true);
                break;
        }
    }


    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) { }


    @Override
    public void onTrackingServiceBound() {
        /*
            condições por linha
            0. se nenhuma entrega das gravações dos trajectos for em execução
            1. se needsupdate for true e então um dialog pra um update já está em execução
            2. se nessun tracking e' in atto
            3. se já há mais de 24 horas que não temos controlado os novos pontos.
            =>prova a scaricare eventuali nuovi hotpoints di ISF
        */
        if(    ((getSupportFragmentManager().findFragmentByTag("sendDialog") == null) && (sendTrackRecordsToCall == null))
            && (this.taskDownloadHotpoints == null)||(this.taskDownloadHotpoints.hasFinished() == true) ){
            if((boundService.getTrackingState()!= Locator.TRACKING) && (boundService.getTrackingState()!= Locator.TERMINATING)){
                long updateTime= preferences.getHotpointsDate();
                long actualTime= new Date().getTime();
                if((actualTime - updateTime) >= 86400000){
                    //la possibilta' di fare tracking e' momentaneamente disabilitata (bottone senza listener), finche' non sono certo di avere una lista degli hotpoints "stabile"
                    //hotpoints update (if more than a day has passed)
                    this.taskDownloadHotpoints= new DownloadHotpointsTask();
                    this.taskDownloadHotpoints.execute();
                }
            }
        }

    }


    @Override
    public void goToSpostamentoTab() {
        this.pagerTabs.setCurrentItem(1, true);
    }


    @Override
    public void onUploadStarted(int trackRecordSize) {
        FragmentManager fm= getSupportFragmentManager();
        uploadDialogFrament= (DialogFragment) getSupportFragmentManager().findFragmentByTag("sendDialog");
        if(uploadDialogFrament!=null)
            uploadDialogFrament.dismiss();

        //adding e aggiunte nello fragment backstack sono automaticamente e opportunamente gestite dal DialogFragment al momento dello show
        FragmentTransaction ft= fm.beginTransaction();

        uploadDialogFrament= DialogSendTrackRecords.newInstance();
        uploadDialogFrament.show(ft,"sendDialog");
        fm.executePendingTransactions();

        if( trackRecordSize > 1 ){
            ((DialogSendTrackRecords)uploadDialogFrament).setMax(trackRecordSize);
            ((DialogSendTrackRecords)uploadDialogFrament).setIndeterminate(false);
        }
    }


    @Override
    public boolean onTrackSent() {
        ((DialogSendTrackRecords)uploadDialogFrament).incrementProgress(1);

        return ((DialogSendTrackRecords)uploadDialogFrament).isAbortPressed();
    }


    @Override
    public void onTerminated(boolean success, int nrTrackRecordsSent) {
        //per colpa di svariati bug della v4supportlibrary, devo controllare se la mia activity e' in foreground o comunque attiva,
        //altrimenti queste operazioni le dovro' fare solo alla resume della stessa (in caso contrario: pena eccezioni)
        if(isRunning)
            uploadDialogFrament.dismiss();
        else{
            fragmentSendTrackTaskToDismiss= true;
            fragmentSendTrackTaskToDismissSuccess= success;
            fragmentSendTrackTaskToDismissNrSent= nrTrackRecordsSent;
        }

        if(success){
            Toast toastToShow= Toast.makeText(this, "Grazie! Statistiche inviate a ISF. ", Toast.LENGTH_LONG);
            toastToShow.setGravity(Gravity.BOTTOM, 0, 130);
            toastToShow.show();
        }
        else
            DialogInfo.newInstance("Errore durante l'invio dei dati a ISF. Controlla la tua connessione e ritenta.", true, null).show(getSupportFragmentManager(), "errorDialog");

        //il fragment contenente il task di invio dei trackrecord ha completato il suo compito. Possiamo eliminarlo (il fragment non sara' piu' "ritenuto").
        FragmentManager fm= getSupportFragmentManager();
        fm.beginTransaction().remove(fragmentSendTrackTask);
        fragmentSendTrackTask= null;

        this.updateSendTrackRecordsButton( (-1 * nrTrackRecordsSent) );
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if ( (boundService!= null)  && ((boundService.getTrackingState() == Locator.TRACKING) || (boundService.getTrackingState() == Locator.TERMINATING)) ){
                DialogExit dialog= DialogExit.newInstance();
                dialog.setExitListener(this);
                dialog.show(getSupportFragmentManager(), "exitDialog");
            }
            else
                finish();

            return true;
        }
        return super.onKeyDown(keyCode, event);

    }


    @Override
    public void onExitSelected() {
        if(boundService != null)
            boundService.abortTracking();

        finish();
    }


    public class DownloadHotpointsTask extends AsyncTask<Void, String, Boolean>{
        private ProgressDialog dialogToHandle;
        private int reached=0;
        private int max=0;
        private boolean finished= false;
        private LocalHotpointsDAO dbDAO;

        //to update the dialog in case the activity is recreated
        protected void setDialog(ProgressDialog newDialog){
            this.dialogToHandle= newDialog;
        }

        protected boolean hasFinished(){
            return this.finished;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_UPDATE_HOTPOINTS);
            this.dialogToHandle= updateHotpointsDialog;
        }

        @Override
        protected Boolean doInBackground(Void... unused) {
            WebHotpointsDAO remotePoints= new WebHotpointsHTTPTextFile();
            Hotpoint newHotpoint;

            try{
                if(remotePoints.getVersion() <= preferences.getHotpointsVersion()){
                    //updating the version in the sharedpreferecens of the app
                    preferences.setHotpointsDate(new Date().getTime());
                    preferences.savePreferences();

                    return true;
                }

                //downloading
                max= remotePoints.getNrHotpoints();
                List<Hotpoint> hotpoints= new ArrayList();
                publishProgress("Scaricamento delle nuove aree");
                do{
                    if(isCancelled())
                        return false;

                    newHotpoint= remotePoints.getNextHotpoint();
                    if(newHotpoint!=null){
                        hotpoints.add(newHotpoint);
                        publishProgress();
                    }
                }while(newHotpoint!=null);

                //db updating
                publishProgress("Salvataggio delle nuove aree");
                dbDAO= new LocalHotpointsDB();
                dbDAO.openSecure();
                dbDAO.eraseHotpoints();
                boolean insertionOK;

                if(isCancelled())
                    return false;

                for(Hotpoint hotpointToAdd : hotpoints){
                    if(isCancelled())
                        return false;

                    insertionOK= dbDAO.insertHotpoint(hotpointToAdd);
                    if(!insertionOK)
                        throw new Exception();

                    publishProgress();
                }
                dbDAO.closeSecure();

                //updating the version in the sharedpreferecens of the app
                preferences.setHotpointsVersion(remotePoints.getVersion());
                preferences.setHotpointsDate(new Date().getTime());

                preferences.savePreferences();
            }catch(Exception e){
                return false;
            }
            return true;
        }


        @Override
        protected void onCancelled() {
            dialogToHandle.cancel();
            dialogToHandle= null;

            if(dbDAO != null)
                dbDAO.closeSecure();

            this.finished= true;
            super.onCancelled();
        }


        @Override
        protected void onProgressUpdate(String... message) {
            if(message.length == 0){
                reached++;
                this.dialogToHandle.setMax(max*2);
                this.dialogToHandle.setIndeterminate(false);
                this.dialogToHandle.setProgress(reached);
            }
            else{
                this.dialogToHandle.setMessage(message[0]);
                this.dialogToHandle.setIndeterminate(true);
            }
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                dialogToHandle.cancel();

                Toast toastToShow= Toast.makeText(MyApplication.getAppContext(), "Aree di interesse aggiornate", Toast.LENGTH_SHORT);
                toastToShow.setGravity(Gravity.BOTTOM, 0, 130);
                toastToShow.show();

                pagerAdapter.getTabHotpoints().updatePoints();
            }
            else{
                updateHotpointsDialog.cancel();
                showDialog(DIALOG_UPDATE_HOTPOINTS_FAILED);
            }

            //taskDownloadHotpoints= null;
            this.finished= true;
        }
    }


    public void goIsfWebsite(View caller){
        Intent i= new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.isf-modena.org/"));
        startActivity(i);
    }

}
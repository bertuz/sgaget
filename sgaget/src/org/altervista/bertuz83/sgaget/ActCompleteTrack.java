package org.altervista.bertuz83.sgaget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageButton;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.altervista.bertuz83.sgaget.data.TrackRecordDAO;
import org.altervista.bertuz83.sgaget.data.TrackRecordDB;
import org.altervista.bertuz83.sgaget.dialogs.DialogInfo;
import org.altervista.bertuz83.sgaget.helper.MyApplication;
import org.altervista.bertuz83.sgaget.receiver.ReceiverActions;
import org.altervista.bertuz83.sgaget.receiver.ReceiverNewTrackRecordToComplete;

import java.lang.reflect.Field;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Activity di completamento delle statistiche. L'activity e' in realta' realizzata dai vari fragment-passi
 * di completamento che comunicano con l'activity padre e vice versa tramite interfacce.
 *
 * @see org.altervista.bertuz83.sgaget.Frag1ActCompleteFragsCommTrack
 * @see org.altervista.bertuz83.sgaget.Frag2ActCompleteFragsCommTrack
 * @see org.altervista.bertuz83.sgaget.Frag3ActCompleteFragsCommTrack
 * @see org.altervista.bertuz83.sgaget.Frag4ActCompleteFragsCommTrack
 *
 */
public class ActCompleteTrack extends SherlockFragmentActivity implements DialogInfo.DialogInfoComm{
    public static final String EXTRA_TRACK_ID = "trackID";
    public static final int DIALOG_REQ_NOSAVE= 0;
    public static final int DIALOG_REQ_NODBOPEN= 1;

    private long track_ID;
    private TrackRecord trackRecord;

    private TrackRecordDAO dbDAO;

    private int onStepNr= -1;
    private int maxStepReached= 0;
    private final int[] breadcrumbIDs= {R.id.act_complete_track_btn_1, R.id.act_complete_track_btn_2, R.id.act_complete_track_btn_3, R.id.act_complete_track_btn_4};
    private final int[] breadcrumbImages= {R.drawable.i1, R.drawable.i2, R.drawable.i3, R.drawable.i4};
    private final int[] breadcrumbImagesSelected= {R.drawable.i1s, R.drawable.i2s, R.drawable.i3s, R.drawable.i4s};
    private final int[] breadcrumbDescriptions= {R.id.act_complete_track_breadcrumb_description_1, R.id.act_complete_track_breadcrumb_description_2, R.id.act_complete_track_breadcrumb_description_3, R.id.act_complete_track_breadcrumb_description_4};


    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        track_ID= getIntent().getLongExtra(EXTRA_TRACK_ID, -1);
        if(track_ID == -1)
            finish();

        setContentView(R.layout.act_complete_track);

        //enabling the overflow actionbar button even for menu-hw-button driven appliances
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) { }

        final ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        try{
            //DB aperto in insertion
            dbDAO= new TrackRecordDB();
            dbDAO.openRead();

            trackRecord= dbDAO.getTrackRecord(track_ID);
        }catch (Exception e){
            DialogInfo dialog= DialogInfo.newInstance("Sono sorti problemi con il reperimento dei dati. Se il problema persiste, contatta ISF Modena", false, null);
            dialog.setCloseListener(this, DIALOG_REQ_NODBOPEN);
            dialog.show(getSupportFragmentManager(), "errorOpenSave");
        }

        trackRecord= (TrackRecord)getLastCustomNonConfigurationInstance();

        if(trackRecord==null)
            trackRecord= dbDAO.getTrackRecord(track_ID);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (onStepNr == 1){
                finish();
                return true;
            }
            else{
                gotoStep(onStepNr - 1);
            }
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return trackRecord;
    }

    @Override
    protected void onDestroy() {
        dbDAO.close();
        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_complete_track, menu);

        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                Intent back= new Intent(this, ActHome.class);
                back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(back);
                break;
        }

        return true;
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.onStepNr= savedInstanceState.getInt("onStepNr");
        this.maxStepReached= savedInstanceState.getInt("maxStepReached");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("onStepNr",onStepNr);
        outState.putInt("maxStepReached",maxStepReached);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Fragment step= new Frag1ActCompleteFragsCommTrack();
        ((IActCompleteFragsComm)step).setTrackRecord(trackRecord);

        if(onStepNr == -1)
            gotoStep(1);
        else
            updateBreadcrumbs(onStepNr);

    }


    private void completeInsertion(){
        IActCompleteFragsComm fragmentToSave= (IActCompleteFragsComm)getSupportFragmentManager().findFragmentById(R.id.act_complete_track_frameLayout);
        fragmentToSave.saveSettings();

        trackRecord.setCompleted(true);
        boolean success= false;
        try{
            TrackRecordDAO dbDAO= new TrackRecordDB();
            dbDAO.openSecure();
            dbDAO.completeTrackRecord(trackRecord);
            dbDAO.closeSecure(true);
            success= true;
        }catch (Exception e){
            DialogInfo dialog= DialogInfo.newInstance("Il salvataggio del tragitto sembra non essere riuscito. Se il problema persiste, contatta ISF Modena", false, null);
            dialog.setCloseListener(this, DIALOG_REQ_NOSAVE);
            dialog.show(getSupportFragmentManager(), "errorDBsave");
        }

        if(success){
            //torno alla  home
            Intent i= new Intent(this, ActHome.class);
            i.putExtra(ActHome.MSG_ENTRANCE, ActHome.MSG_ENTRANCE_TRACKING_COMPLETED);
            i.putExtra(ActHome.MSG_ENTRANCE_TRACKING_COMPLETED_EXTRA_TRACKID, trackRecord.getCreationDate());
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            //informo che trackrecord e' stato completato (togliere dalla lista)
            Intent intent= new Intent(ReceiverActions.ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_COMPLETED);
            intent.putExtra(ReceiverNewTrackRecordToComplete.EXTRA_CREATIONDATE, trackRecord.getCreationDate());
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
        }
    }


    private void gotoStep(int i){
        if (i== onStepNr)
            return;

        if(maxStepReached < i)
            maxStepReached= i;

        if( i > this.breadcrumbIDs.length ){
            completeInsertion();
            return;
        }

        IActCompleteFragsComm fragmentToRetain= (IActCompleteFragsComm)getSupportFragmentManager().findFragmentById(R.id.act_complete_track_frameLayout);
        boolean actualFragmentOK= true;
        if(fragmentToRetain!=null)
            actualFragmentOK= fragmentToRetain.saveSettings();

        if(actualFragmentOK)
            onStepNr= i;
        else
            return;


        boolean firstAttach= false;
        Fragment step=  getSupportFragmentManager().findFragmentByTag(""+onStepNr);

        //se non ancora arrivato a tale step, lo creo
        if (step == null) {
            firstAttach= true;
            switch(i){
                case 1:
                    step= new Frag1ActCompleteFragsCommTrack();
                    break;
                case 2:
                    step= new Frag2ActCompleteFragsCommTrack();
                    break;
                case 3:
                    step= new Frag3ActCompleteFragsCommTrack();
                    break;
                case 4:
                    step= new Frag4ActCompleteFragsCommTrack();
                    break;
            }
            ((IActCompleteFragsComm) step).setTrackRecord(trackRecord);
        }

        replaceWithStep(step, onStepNr, firstAttach);
        updateBreadcrumbs(onStepNr);
    }


    private void replaceWithStep(Fragment f, int step, boolean firstAttach){
        Fragment fragmentToRetain= getSupportFragmentManager().findFragmentById(R.id.act_complete_track_frameLayout);

        /*
            COMMENTO LASCIATO PER RAGIONI "ESAUSTIVE"
            Questo commento non ha piu' senso, ora che la nuova revisione della supportlibrary sembra funzionare a dovere.
            Lascio comunque il commento in modo da far capire quanto possa essere farraginoso e fuorviante l'utilizzo di questa libreria
            per fini didattici.
            ------------------------------------
            gli nested fragments producono grandi problemi con support-v4-library + actionbarsherlock.
            l'unica maniera per gestirli - non potendo utilizzare getchildfragmentmanager nei fragment - e' eliminarli dai fragment padri in modo che siano
            poi ricreati ogni volta. In pratica non possiamo mantenere gli nested fragment senza ricaricarli.
        */
        if(fragmentToRetain!=null){
            //non lo sostituisco con se stesso
            if (fragmentToRetain.getTag().equals(""+step))
                return;

            FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.act_complete_track_frameLayout, f, ""+step);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                if(firstAttach)
                    ft.addToBackStack(null);
            ft.commit();
        }
        else{
            FragmentTransaction ft= getSupportFragmentManager().beginTransaction();
            ft.add(R.id.act_complete_track_frameLayout, f, ""+step);
            if(firstAttach)
                ft.addToBackStack(null);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

            ft.commit();
        }
    }


    private void updateBreadcrumbs(int i){
        ImageButton button;

        //reset
        for(int j=0; j< this.breadcrumbIDs.length; j++){
            button= (ImageButton)findViewById(this.breadcrumbIDs[j]);
            button.setImageResource(this.breadcrumbImages[j]);
            button.setVisibility(View.VISIBLE);
            findViewById(this.breadcrumbDescriptions[j]).setVisibility(View.GONE);
        }

        //hidden breadcrumb
        for(int j=maxStepReached; j< breadcrumbIDs.length; j++){
            button= (ImageButton)findViewById(this.breadcrumbIDs[j]);
            button.setVisibility(View.GONE);
        }

        //set selected image
        button= (ImageButton)findViewById(this.breadcrumbIDs[i-1]);
        button.setImageResource(this.breadcrumbImagesSelected[i-1]);
        button.setVisibility(View.VISIBLE);
        findViewById(this.breadcrumbDescriptions[i-1]).setVisibility(View.VISIBLE);

        //final step
        if(i == this.breadcrumbIDs.length)
            ((ImageButton)findViewById(R.id.act_complete_track_btn_next)).setImageResource(R.drawable.i_completed_white);
        else
            ((ImageButton)findViewById(R.id.act_complete_track_btn_next)).setImageResource(R.drawable.i_next_white);
    }


    public void next(View view){
        gotoStep(onStepNr+1);
    }


    public void goTo(View view){
        switch (view.getId()){
            case R.id.act_complete_track_btn_1:
                gotoStep(1);
                break;
            case R.id.act_complete_track_btn_2:
                gotoStep(2);
                break;
            case R.id.act_complete_track_btn_3:
                gotoStep(3);
                break;
            case R.id.act_complete_track_btn_4:
                gotoStep(4);
                break;
        }
    }


    @Override
    public void onInfoDialogClosed(int requestCode) {
        switch (requestCode){
            case DIALOG_REQ_NOSAVE:
                finish();
                break;
            case DIALOG_REQ_NODBOPEN:
                finish();
                break;
        }

    }
}
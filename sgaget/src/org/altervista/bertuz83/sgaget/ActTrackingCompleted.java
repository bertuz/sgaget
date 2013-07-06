package org.altervista.bertuz83.sgaget;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RadioGroup;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.altervista.bertuz83.sgaget.business.HotpointCandidate;
import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.altervista.bertuz83.sgaget.data.TrackRecordDAO;
import org.altervista.bertuz83.sgaget.data.TrackRecordDB;
import org.altervista.bertuz83.sgaget.dialogs.DialogInfo;
import org.altervista.bertuz83.sgaget.helper.MyApplication;
import org.altervista.bertuz83.sgaget.receiver.ControlReceiverBoundService;
import org.altervista.bertuz83.sgaget.receiver.ReceiverActions;
import org.altervista.bertuz83.sgaget.receiver.ReceiverNewTrackRecordToComplete;
import org.altervista.bertuz83.sgaget.service.ServiceTracking;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: bertuz
 * Project: sgaget
 */
public class ActTrackingCompleted extends SherlockFragmentActivity implements ControlReceiverBoundService, DialogInfo.DialogInfoComm {
    public static final String MSG_TIME= "elapsedTime";

    private ServiceTracking boundService;
    private boolean completedToCall= true;
    private long trackRecordID= -1;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            boundService= ((ServiceTracking.LocalBinder)service).getService();
            onTrackingServiceBound();
        }

        public void onServiceDisconnected(ComponentName className) {
            boundService= null;
        }
    };



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        //GUI
        //
        setContentView(R.layout.act_tracking_completed);

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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_tracking_completed, menu);
        menu.removeItem(R.id.actionbar_send_trackrecords);

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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("completedToCall", completedToCall);
        outState.putLong("trackRecordID", trackRecordID);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.completedToCall= savedInstanceState.getBoolean("completedToCall");
        this.trackRecordID= savedInstanceState.getLong("trackRecordID");
    }




    public void trashTracking(View button){
        Intent back= new Intent(this, ActHome.class);
        back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(back);
    }


    private boolean createTrackRecord(){
        completedToCall= false;
        String elapsedTime= getIntent().getCharSequenceExtra(MSG_TIME).toString();

        //finish
        try{
            ArrayList<HotpointCandidate> endCandidates= new ArrayList<HotpointCandidate>(boundService.getHotpointCandidatesSortedForUpdate(0));
            ArrayList<HotpointCandidate> startCandidates= new ArrayList<HotpointCandidate>(boundService.getHotpointCandidatesSortedForUpdate(boundService.getNrHotpointUpdates() - 1));

            long time= new Date().getTime();
            TrackRecord trackToAdd= new TrackRecord(time, elapsedTime, startCandidates, endCandidates);

            //DB insertion
            TrackRecordDAO dbDAO= new TrackRecordDB();
            dbDAO.openSecure();
            this.trackRecordID= dbDAO.insertTrackRecord(trackToAdd);

            if(this.trackRecordID != -1){
                dbDAO.closeSecure(true);

                //update complete listview in home
                Intent intent= new Intent(ReceiverActions.ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_TO_COMPLETE);
                intent.putExtra(ReceiverNewTrackRecordToComplete.EXTRA_CREATIONDATE, this.trackRecordID);
                LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);

                return true;
            }
            else{
                dbDAO.closeSecure(false);
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }


    private void dialogError(){
        DialogInfo dialog= DialogInfo.newInstance("Dei problemi imprevisti non hanno permesso di salvare questo tragitto. Se il problema persiste, contatta ISF Modena per risolvere il problema!", false, null);
        dialog.setCloseListener(this, 0);
        dialog.show(getSupportFragmentManager(), "errorInfo");
    }

    @Override
    protected void onStart() {
        super.onStart();

        //!attenzione. Nel caso non si utilizzi questa activity subito dopo l'activity home, il service potrebbe
        //essere chiuso definitivamente e scartato dal sistema, perdendo di conseguenza il locator con l'ultimo tracciamento
        bindService(new Intent(this, ServiceTracking.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(mConnection);
        super.onStop();
    }


    @Override
    public void onTrackingServiceBound() {
        findViewById(R.id.tracking_completed_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int idSelected= ((RadioGroup) findViewById(R.id.tracking_completed_radioComplete)).getCheckedRadioButtonId();

                if((completedToCall)&&(!createTrackRecord())){
                    dialogError();
                    return;
                }

                if(idSelected == R.id.tacking_completed_button_later){
                    Intent i= new Intent(ActTrackingCompleted.this, ActHome.class);
                    i.putExtra(ActHome.MSG_ENTRANCE, ActHome.MSG_ENTRANCE_TRACKING_SAVED);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                else{
                    Intent i= new Intent(ActTrackingCompleted.this, ActCompleteTrack.class);
                    i.putExtra(ActCompleteTrack.EXTRA_TRACK_ID, ActTrackingCompleted.this.trackRecordID);
                    startActivity(i);
                    finish();
                }
            }
        });
    }


    @Override
    public void onInfoDialogClosed(int requestCode) {
        finish();
    }
}
package org.altervista.bertuz83.sgaget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.altervista.bertuz83.sgaget.data.TrackRecordDAO;
import org.altervista.bertuz83.sgaget.data.TrackRecordDB;
import org.altervista.bertuz83.sgaget.receiver.ControlReceiverNewTrackRecordToComplete;
import org.altervista.bertuz83.sgaget.receiver.ReceiverActions;
import org.altervista.bertuz83.sgaget.receiver.ReceiverNewTrackRecordToComplete;

import java.util.ArrayList;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Tab con lista degli spostamenti da completare
 *
 * @see org.altervista.bertuz83.sgaget.ActHome
 */
public class FTabCompletareActHome extends ListFragment implements IActHomeFragTabCompletareComm, ControlReceiverNewTrackRecordToComplete{
    public static final String TAGNAME="tabCompletare";

    private IFragActHomeComm activityComm;
    private ArrayAdapterTrackingCompleted adapter;

    private ArrayList<TrackRecord> trackRecords;

    TrackRecordDAO dbDAO= new TrackRecordDB();
    BroadcastReceiver newTrackRecordReceiver= null;

    private int firstVisiblePosition= -1;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundler) {
        return (inflater.inflate(R.layout.tab_completare, container, false));
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        try{
            dbDAO.openRead();
            trackRecords= dbDAO.getTrackRecordsToComplete();
        }catch(Exception e){
            //non dovrebbe mai succedere, per questo non esiste nessuna gestione dell'eccezione
        }

        if(newTrackRecordReceiver==null){
            newTrackRecordReceiver= new ReceiverNewTrackRecordToComplete(this);
            IntentFilter iff= new IntentFilter(ReceiverActions.ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_TO_COMPLETE);
            iff.addAction(ReceiverActions.ACT_REC_NEWTRACKRECORDTOCOMPLETE_TRACKRECORD_COMPLETED);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(newTrackRecordReceiver, iff);
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter= new ArrayAdapterTrackingCompleted(getActivity(), trackRecords, this);
        setListAdapter(adapter);

        adapter.setNotifyOnChange(true);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activityComm= (IFragActHomeComm) activity;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("listPosition", this.firstVisiblePosition);

        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }

    @Override
    public void onStop() {
        firstVisiblePosition= getListView().getFirstVisiblePosition();
        super.onStop();
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if((savedInstanceState != null) && (getListView() != null))
            getListView().setSelection(savedInstanceState.getInt("listPosition"));
    }


    @Override
    public void onResume() {
        super.onResume();
        /*
            Sebbene sia gia' chiamata in onCreate, nella onPause chiudiamo la connessione ma non distruggiamo l'oggetto.
            Inoltre openRead riapre la connessione solo nel caso non ce ne sia gia' una aperta, altrimenti non fa nulla.
            Per questo motivo e' sicuro chiamare un'altra volta openRead, sfruttando ben pochi cicli macchina in piu'.
        */
        try{
            dbDAO.openRead();
        }catch(Exception e){ }

    }


    @Override
    public void onDeleteTrackrecord(long creationDate) {
        TrackRecordDB dbDAO;
        try{
            //DB insertion
            dbDAO= new TrackRecordDB();
            dbDAO.openSecure();
            dbDAO.deleteTrackRecord(creationDate);
            dbDAO.closeSecure(true);
        }catch(Exception e){ }

        if(getListAdapter().getCount() == 0)
            activityComm.goToSpostamentoTab();
    }


    @Override
    public void onPause() {
        dbDAO.close();
        super.onPause();
    }


    @Override
    public void onTrackrecordClicked(long creationDate) {
        Intent i = new Intent(getActivity(), ActCompleteTrack.class);
        i.putExtra(ActCompleteTrack.EXTRA_TRACK_ID, creationDate);
        getActivity().startActivity(i);
    }


    @Override
    public void onChangeSortList(boolean newOnTop) {
            this.adapter.changeOrder(newOnTop);
    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.newTrackRecordReceiver);
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
    public void onNewTrackRecordToComplete(long creationDate) {
        try{
            dbDAO.openRead();
        }catch(Exception e){ }

        TrackRecord trackRecordToInsert= dbDAO.getTrackRecord(creationDate);

        if(trackRecordToInsert!=null){
            ((ArrayAdapterTrackingCompleted)getListAdapter()).addItem(trackRecordToInsert);
        }

        dbDAO.close();
    }


    @Override
    public void onTrackRecordCompleted(long creationTime) {
        ((ArrayAdapterTrackingCompleted)getListAdapter()).removeItem(creationTime);

        if(getListAdapter().getCount() == 0)
            activityComm.goToSpostamentoTab();
    }


    public void deleteAllItems(){
        ((ArrayAdapterTrackingCompleted)getListAdapter()).removeAllItems();
    }
}

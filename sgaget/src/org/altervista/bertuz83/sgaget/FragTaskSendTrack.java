package org.altervista.bertuz83.sgaget;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.altervista.bertuz83.sgaget.data.StatisticsGoogleDocs;
import org.altervista.bertuz83.sgaget.data.TrackRecordDAO;
import org.altervista.bertuz83.sgaget.data.TrackRecordDB;
import org.altervista.bertuz83.sgaget.data.TrackRecordStatisticsDAO;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.List;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Task di invio degli spostamenti completati a Google.
 * Il task e' wrappato in un fragment secondo una best practice di design dei task tramite fragment.
 *
 * http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-changes.html
 */
public class FragTaskSendTrack extends Fragment{
    private TaskCallbacks mCallbacks;
    private SendTask mTask;

    private long creationDate= Long.MAX_VALUE;
    private boolean toAbort= false;

    private int nrTrackRecordsSent= 0;

    static interface TaskCallbacks{
        void onUploadStarted(int trackRecordSize);
        boolean onTrackSent();
        void onTerminated(boolean success, int nrTrackRecordsSent);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        mTask= new SendTask();
    }


    //if MIN, all the trackrecords that are ready to be sent will be sent
    public void setSingleSendToPerform(long creationDate){
        this.creationDate= creationDate;
    }

    public void startSend(){
        mTask.execute();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mCallbacks= (TaskCallbacks) activity;
    }


    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks= null;
    }


    public void abort(){
        toAbort= true;
    }


    private class SendTask extends AsyncTask<Void, Void, Boolean>{
        TrackRecordDAO dbDAO;
        ArrayList<TrackRecord> trackRecords;
        String emailToSend;

        protected void onPreExecute() {
            super.onPreExecute();
            nrTrackRecordsSent= 0;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            emailToSend = prefs.getString(ActPreferences.SETTINGS_EMAIL, "");

            try{
                dbDAO= new TrackRecordDB();
                dbDAO.openRead();
            }catch (Exception e){
                abort();
            }

            if( creationDate!= Long.MAX_VALUE ){
                trackRecords= new ArrayList<TrackRecord>();
                trackRecords.add(dbDAO.getTrackRecord(creationDate));
            }
            else
                trackRecords= dbDAO.getTrackRecordsToSend();

            dbDAO.close();

            if(mCallbacks != null)
                mCallbacks.onUploadStarted(trackRecords.size());
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                dbDAO= new TrackRecordDB();
                dbDAO.openWrite();
            }catch (Exception e){
                abort();
            }

            try{
                for(TrackRecord sendingRecord : trackRecords){
                    if ( toAbort ){
                        dbDAO.close();
                        return true;
                    }

                    TrackRecordStatisticsDAO trs= new StatisticsGoogleDocs(emailToSend);
                    trs.sendStatisticItem(sendingRecord);

                    sendingRecord.setSent(true);
                    dbDAO.updateTrackRecord(sendingRecord);

                    publishProgress();
                }
            }catch(Exception e){
                dbDAO.close();
                return false;
            }

            dbDAO.close();
            return true;
        }


        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            nrTrackRecordsSent++;

            if( (mCallbacks != null) && (trackRecords.size() > 1) )
                toAbort= mCallbacks.onTrackSent();
        }


        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            mCallbacks.onTerminated(success, nrTrackRecordsSent);
        }
    }
}

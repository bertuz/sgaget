package org.altervista.bertuz83.sgaget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.*;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import org.achartengine.GraphicalView;
import org.altervista.bertuz83.sgaget.business.FinishHotpointsForStart;
import org.altervista.bertuz83.sgaget.business.TransportationStatistics;
import org.altervista.bertuz83.sgaget.charts.ChartEngine;
import org.altervista.bertuz83.sgaget.data.TrackRecordDAO;
import org.altervista.bertuz83.sgaget.data.TrackRecordDB;
import org.altervista.bertuz83.sgaget.dialogs.DialogInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;

/**
 * User: bertuz
 * Project: sgaget
 */
public class ActCharts extends SherlockFragmentActivity implements DialogInfo.DialogInfoComm{
    private static final int dialogErrorToTerminate= 0;

    private TrackRecordDAO dbDAO;
    private FinishHotpointsForStart hotpointsSF;
    private Spinner from;
    private Spinner to;
    private FrameLayout fl;
    private ArrayAdapter<String> adapterFrom;
    private ArrayAdapter<String> adapterTo;
    private ChartEngine chart;
    private GraphicalView shareGraph;

    private Intent intentShareChart;

    private RetainFragActCharts retainFragment;

    private File fileShareChart;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fileShareChart= new File( getCacheDir(), "chart.png" );
        fileShareChart.setReadable(true, false);

        setContentView(R.layout.act_statistics);
        from= (Spinner) findViewById(R.id.act_statistics_spinner_from);
        to= (Spinner) findViewById(R.id.act_statistics_spinner_to);
        fl= (FrameLayout) findViewById(R.id.act_statistics_chart);
        fl.setDrawingCacheEnabled(true);

        chart= new ChartEngine(ActCharts.this);

        try{
            dbDAO= new TrackRecordDB();
            dbDAO.openRead();
        }catch(Exception e){
            DialogInfo dialog= DialogInfo.newInstance("Durante l'apertura del DB. Contatta ISF Modena per ottenere supporto!", true, null);
            dialog.setCloseListener(this, ActCharts.dialogErrorToTerminate);
            dialog.show(getSupportFragmentManager(), "errorDialog");
        }

        FragmentManager fm= getSupportFragmentManager();
        retainFragment= (RetainFragActCharts) fm.findFragmentByTag("persistence");
        if(retainFragment == null){
            hotpointsSF= dbDAO.getStartFinishHotpoints();
            ArrayList<String> arrayStartHotpoints= hotpointsSF.getStartHotpoints();

            if(arrayStartHotpoints.size() == 0){
                DialogInfo dialog= DialogInfo.newInstance("Non esiste ancora nessuna statistica completata per poter mostrare informazioni.", false, null);
                dialog.setCloseListener(this, ActCharts.dialogErrorToTerminate);
                dialog.show(getSupportFragmentManager(), "errorDialog");
            }

            adapterFrom= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayStartHotpoints);

            retainFragment= new RetainFragActCharts();
            retainFragment.setListFrom(arrayStartHotpoints);
            retainFragment.setHotpointsSF(hotpointsSF);

            FragmentTransaction ft= fm.beginTransaction();
            ft.add(retainFragment, "persistence");
            ft.commit();
        }
        else{
            adapterFrom= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, retainFragment.getListFrom());
            this.hotpointsSF= retainFragment.getHotpointsSF();
        }

        adapterTo= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        adapterFrom.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        from.setAdapter(adapterFrom);
        adapterTo.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        to.setAdapter(adapterTo);

        from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                adapterTo.clear();
                adapterTo.addAll(hotpointsSF.getFinishHotpoints((String) adapterView.getItemAtPosition(i)));
                to.setSelection(0, true);
                requestChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                requestChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        final ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    private void requestChart(){
        String hotpointFrom= (String)from.getSelectedItem();
        String hotpointTo;

        hotpointTo= (String)to.getSelectedItem();

        if(hotpointTo != null){
            fl.removeAllViews();
            TransportationStatistics statistics= dbDAO.getStartFinishHotpointStatistics(hotpointFrom, hotpointTo);
            GraphicalView graph=  chart.getChart(statistics, false);
            graph.setTag("graph");
            fl.addView(graph);

            shareGraph= chart.getChart(statistics, true);
            saveChartImage();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }


    private void saveChartImage(){
        try {
            FileOutputStream fos= new FileOutputStream(fileShareChart);
            shareGraph.toBitmap().compress(Bitmap.CompressFormat.PNG, 80, fos);

            fos.flush();
            fos.close();
        } catch (Exception e) {
            DialogInfo dialog= DialogInfo.newInstance("Sgàget sembra aver incontrato alcuni problemi. La condivisione del grafico potrebbe essere compromessa", true, null);
            dialog.show(getSupportFragmentManager(), "errorSaveChart");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_statistics, menu);

        ShareActionProvider mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.actionbar_share_chart).getActionProvider();

        intentShareChart = new Intent(Intent.ACTION_SEND);
        intentShareChart.setType("image/png");

        Uri screenshotUri = Uri.fromFile(fileShareChart);
        intentShareChart.putExtra(Intent.EXTRA_STREAM, screenshotUri);

        mShareActionProvider.setShareIntent(this.intentShareChart);

        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();

        try{
            dbDAO= new TrackRecordDB();
            dbDAO.openRead();
        }catch(Exception e){
            DialogInfo dialog= DialogInfo.newInstance("Durante l'apertura del DB. Contatta ISF Modena per ottenere supporto!", true, null);
            dialog.setCloseListener(this, ActCharts.dialogErrorToTerminate);
            dialog.show(getSupportFragmentManager(), "errorDialog");
        }
    }


    @Override
    protected void onStop() {
        dbDAO.close();
        super.onStop();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("positionFrom", from.getSelectedItemPosition());
        outState.putInt("positionTo", to.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final int fromPos= savedInstanceState.getInt("positionFrom");

        //nel caso ruoti lo schermo, ma ancora nessuna statistica e' presente
        if(adapterFrom.getCount()!=0)
            adapterTo.addAll(hotpointsSF.getFinishHotpoints(this.adapterFrom.getItem(from.getSelectedItemPosition())) );
        final int toPos= savedInstanceState.getInt("positionTo");

        /*
            yet another google bug. Questo ha dell'incredibile: setSelection per uno spinner NON FUNZIONA, se non portato all'interno del looper del UI thread
            tramite un handler.
            COME SI PUO' FARE DIDATTICA CON QUESTA PORCHERIA MI DOMANDO? Santi studenti, santi docenti. Beata Apple. Amen.
            http://stackoverflow.com/questions/1484528/android-setselection-having-no-effect-on-spinner
         */
        new Handler().postDelayed(new Runnable() {
            public void run() {
                to.setSelection(toPos);
                from.setSelection(fromPos);
            }
        }, 100);
    }


    @Override
    public void onInfoDialogClosed(int requestCode) {
        switch(requestCode){
            case ActCharts.dialogErrorToTerminate:
                finish();
                break;
        }
    }
}
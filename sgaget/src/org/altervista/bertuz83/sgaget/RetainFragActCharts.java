package org.altervista.bertuz83.sgaget;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import org.altervista.bertuz83.sgaget.business.FinishHotpointsForStart;
import org.altervista.bertuz83.sgaget.charts.ChartEngine;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Fragment utilizzato per la persistenza dei dati dell'activity Charts
 * @see org.altervista.bertuz83.sgaget.ActCharts
 */
public class RetainFragActCharts extends Fragment {
    private FinishHotpointsForStart hotpointsSF;
    private ArrayList<String> listFrom;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null){
            listFrom= savedInstanceState.getStringArrayList("listFrom");
            hotpointsSF= savedInstanceState.getParcelable("hotpointsSF");
        }

        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!getActivity().isChangingConfigurations()){
            outState.putStringArrayList("listFrom", listFrom);
            outState.putParcelable("hotpointsSF", hotpointsSF);
        }

        super.onSaveInstanceState(outState);
    }

    public FinishHotpointsForStart getHotpointsSF(){
        return hotpointsSF;
    }

    public void setHotpointsSF(FinishHotpointsForStart hotpointsSF){
        this.hotpointsSF= hotpointsSF;
    }

    public void setListFrom(ArrayList<String> listFrom){
        this.listFrom= listFrom;
    }

    public ArrayList<String> getListFrom(){
        return this.listFrom;
    }


}

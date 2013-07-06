package org.altervista.bertuz83.sgaget.business;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Descrive i punti di interesse finali relativi a spostamenti con punto di partenza in comune.
 * Parcellizzabile per non eseguire eccessive query
 */
public class FinishHotpointsForStart implements Parcelable{
    private HashMap finishHotpoints= new HashMap<String, ArrayList<String>>();


    public FinishHotpointsForStart(Parcel data){
        finishHotpoints= (HashMap<String, ArrayList<String>>) data.readBundle().getSerializable(null);
    }

    public FinishHotpointsForStart(){ }

    public void addFinishHotpoint(String startHotpoint, String finishHotpoint){
        if(finishHotpoints.containsKey(startHotpoint))
            ((ArrayList<String>)finishHotpoints.get(startHotpoint)).add(finishHotpoint);
        else{
            ArrayList<String> hotpointsToAdd= new ArrayList<String>();
            hotpointsToAdd.add(finishHotpoint);
            finishHotpoints.put(startHotpoint, hotpointsToAdd);
        }
    }

    public ArrayList<String> getFinishHotpoints(String startHotpoints){
        return ((ArrayList<String>)finishHotpoints.get(startHotpoints));
    }

    public ArrayList<String> getStartHotpoints(){
        ArrayList<String> arrayRet= new ArrayList<String>(finishHotpoints.keySet());
        return (arrayRet);
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle finishSave= new Bundle();
        finishSave.putSerializable(null, finishHotpoints);
        parcel.writeBundle(finishSave);
    }


    public static final Parcelable.Creator<FinishHotpointsForStart> CREATOR = new Creator<FinishHotpointsForStart>() {
        public FinishHotpointsForStart createFromParcel(Parcel source) {
            return new FinishHotpointsForStart(source);
        }
        public FinishHotpointsForStart[] newArray(int size) {
            return new FinishHotpointsForStart[0];
        }
    };
}

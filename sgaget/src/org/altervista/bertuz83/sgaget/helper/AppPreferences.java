package org.altervista.bertuz83.sgaget.helper;

import android.content.SharedPreferences;

/**
 * User: bertuz
 * Project: sgaget
 *
 * dati "preference" non direttamente correlati a scelte dell'utente.
 */
public class AppPreferences{
    public static final String preferenceFilename= "GlobalPreferences";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public AppPreferences(){
        preferences= MyApplication.getAppContext().getSharedPreferences(
                AppPreferences.preferenceFilename,
                MyApplication.getAppContext().MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor(){
        if(editor==null)
            this.editor= preferences.edit();

        return this.editor;
    }

    public void setHotpointsVersion(int version){
        SharedPreferences.Editor editor= getEditor();
        editor.putInt("hotpointsVersion", version);
    }

    public int getHotpointsVersion(){
        return preferences.getInt("hotpointsVersion", 0);
    }

    public void setHotpointsDate(long date){
        SharedPreferences.Editor editor= getEditor();
        editor.putLong("hotpointsDate", date);
    }

    public long getHotpointsDate(){
        return preferences.getLong("hotpointsDate", 0);
    }


    public void savePreferences(){
        if(editor!=null)
            editor.commit();
    }


}

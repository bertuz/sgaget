package org.altervista.bertuz83.sgaget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.view.KeyEvent;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.altervista.bertuz83.sgaget.data.TrackRecordDAO;
import org.altervista.bertuz83.sgaget.data.TrackRecordDB;
import org.altervista.bertuz83.sgaget.helper.UtilityFunctions;


/**
 * User: bertuz
 * Project: sgaget
 */
public class ActPreferences extends SherlockPreferenceActivity{
    private static final int DIALOG_VERSION_INFO= 0;
    private static final int DIALOG_CONFIRM_DELETE= 1;
    private static final int DIALOG_ERROR_DELETE= 2;

    public static final String SETTINGS_EMAIL= "email";
    public static final String SETTINGS_SEND_AT_ONCE= "at_once";


    private boolean dbChanged= false;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
            Ebbene sì! Al giorno d'oggi non esiste ancora nella support v4 library una implementazione del nuovo approccio
            alla preferenceactivity (disponibile dalla api 11). Quindi utilizzerò il vecchio approccio, chiamando metodi deprecati.
            Mah.
            http://stackoverflow.com/questions/9783368/alternatives-to-preferencefragment-with-android-support-v4
        */
        addPreferencesFromResource(R.xml.prefs);

        final ActionBar actionBar= getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        PreferenceScreen pref= getPreferenceScreen();

        Preference myPref= findPreference("deleteDB");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showDialog(DIALOG_CONFIRM_DELETE);
                return true;
            }
        });

        myPref = findPreference("about");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showDialog(DIALOG_VERSION_INFO);
                return true;
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String email= prefs.getString(SETTINGS_EMAIL, "");
        Preference emailPref= findPreference("email");
        emailPref.setDefaultValue(email);
        emailPref.setSummary(email);

        emailPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(preference.getKey().equals("email")){
                    if(UtilityFunctions.isEmailValid((String)newValue))
                        preference.setSummary((String) newValue);
                    else{
                        Toast.makeText(ActPreferences.this, "Email non valida, mantenuta precedente", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                return true;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                terminateActivity();
                break;
            case R.id.actionbar_save_trackrecords:
                terminateActivity();
                break;
        }
        return true;
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        switch(id){
            case DIALOG_VERSION_INFO :
                builder.setTitle("Sgàget!");
                builder.setMessage("Realizzato per ISF Modena da Matteo Bertamini\nhttp://bertuz83.altervista.org");
                builder.setCancelable(true);
                builder.setPositiveButton("Chiudi", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.dismiss();
                    }
                });
                dialog= builder.create();
                break;
            case DIALOG_CONFIRM_DELETE :
                builder.setTitle("Cancella tutti i dati");
                builder.setMessage("Sei sicur* di cancellare tutti gli spostamenti?");
                builder.setCancelable(false);
                builder.setPositiveButton("No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Sì", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        eraseDB();
                        dialog.dismiss();
                    }
                });

                dialog= builder.create();
                break;
            case DIALOG_ERROR_DELETE :
                builder.setTitle("Oh, ooh...");
                builder.setMessage("Un errore ha impedito di cancellare tutti i dati dal database. Contattare ISF Modena se il problema persiste.");
                builder.setCancelable(true);
                builder.setNegativeButton("Chiudi", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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

    private void eraseDB(){
        try{
            TrackRecordDAO dbDAO= new TrackRecordDB();
            dbDAO.openSecure();
            dbDAO.eraseDB();
            dbDAO.closeSecure(true);
        }catch(Exception e){ }

        dbChanged= true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
            terminateActivity();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void terminateActivity(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("dbChanged", dbChanged);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
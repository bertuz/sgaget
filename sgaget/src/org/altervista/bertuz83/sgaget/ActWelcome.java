package org.altervista.bertuz83.sgaget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.actionbarsherlock.app.SherlockActivity;
import org.altervista.bertuz83.sgaget.helper.UtilityFunctions;

public class ActWelcome extends SherlockActivity{
    private String mailInserted= "";


    @Override
    public void onCreate(Bundle savedInstanceState){
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_welcome);

        findViewById(R.id.main_mail).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    //hide the keyboard
                    InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(findViewById(R.id.main_mail).getWindowToken(), 0);

                    iniziaClicked(view);
                    findViewById(R.id.loseFocus).requestFocus();
                }

                return false;
            }
        });

        findViewById(R.id.main_mail).setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View view, boolean gainFocus) {
                if(!gainFocus)
                    return;

                ((EditText)findViewById(R.id.main_mail)).setTextColor(Color.BLACK);
                ((EditText)findViewById(R.id.main_mail)).setText(mailInserted);
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mailInserted", this.mailInserted);
        outState.putInt("nameInsertedColor", ((EditText) findViewById(R.id.main_mail)).getCurrentTextColor());
    }


    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);    //To change body of overridden methods use File | Settings | File Templates.

        this.mailInserted = state.getString("mailInserted");
        ((EditText)findViewById(R.id.main_mail)).setTextColor(state.getInt("nameInsertedColor"));
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public void iniziaClicked(View viewClicked){
        //in case the text is not OK and it has not modified yet, nothing needs to be done.
        if(Color.RED == ((EditText)findViewById(R.id.main_mail)).getCurrentTextColor())
            return;

        findViewById(R.id.loseFocus).requestFocus();


        this.mailInserted = ((EditText)findViewById(R.id.main_mail)).getText().toString();
        mailInserted = mailInserted.trim();


        if(!UtilityFunctions.isEmailValid(mailInserted)){
            ((EditText)findViewById(R.id.main_mail)).setTextColor(Color.RED);
            ((EditText)findViewById(R.id.main_mail)).setText("Email non valida");
            return;
        }


        //if everythins is ok, I save the name and I go further
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor= prefs.edit();
        editor.putString(ActPreferences.SETTINGS_EMAIL, this.mailInserted);
        editor.commit();

        this.goHome();
    }


    private void goHome(){
        Intent goHome= new Intent(this, ActHome.class);
        goHome.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goHome);
        finish();
    }
}

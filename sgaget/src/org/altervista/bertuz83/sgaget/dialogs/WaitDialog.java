package org.altervista.bertuz83.sgaget.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * User: bertuz
 * Project: sgaget
 *
 * al momento inutilizzato. Vedi commenti
 */

/*
    ho creato questo dialog come palliativo  al caricamento molto lento nel caso ci debba caricare un map fragment (mappe)
    purtropo non serve a nulla. La questione e' ben nota e per ora non sono disponibili workaround:
    http://stackoverflow.com/questions/14418927/setcontentview-slow-with-map-fragment

 */
public class WaitDialog extends SherlockDialogFragment{
    private DialogWaitComm callBackEntity;

    public static interface DialogWaitComm{
        public void onWaitialogCreated();
    }

    public void setListener(DialogWaitComm entity){
        callBackEntity= entity;
    }

    public static WaitDialog newInstance() {
        WaitDialog frag = new WaitDialog();
        return frag;
    }

    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        this.setCancelable(false);
        final ProgressDialog dialog= new ProgressDialog(getActivity());
        dialog.setMessage("Caricamento");

        callBackEntity.onWaitialogCreated();
        return dialog;
    }

}

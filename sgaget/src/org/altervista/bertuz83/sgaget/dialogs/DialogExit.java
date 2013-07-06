package org.altervista.bertuz83.sgaget.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockDialogFragment;


/**
 * User: bertuz
 * Project: sgaget
 */
public class DialogExit extends SherlockDialogFragment {
    private DialogInfoComm callBackEntity;

    public static interface DialogInfoComm{
        public void onExitSelected();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setExitListener(DialogInfoComm entity){
        callBackEntity= entity;
    }


    public static DialogExit newInstance() {
        DialogExit frag = new DialogExit();

        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getSherlockActivity());
        this.setCancelable(true);
        builder.setMessage("Sgàget! sta tracciando uno spostamento. Uscendo interromperai l'operazione in corso.");
        builder.setTitle("Uscita Sgàget!");

        builder.setPositiveButton("Esci", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                dialog.dismiss();
                if(callBackEntity != null)
                    callBackEntity.onExitSelected();
            }
        });

        builder.setNegativeButton("Cancella", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                dialog.dismiss();
            }
        });

        return (builder.create());
    }

    /*
    [italian soh]
    http://stackoverflow.com/questions/12433397/android-dialogfragment-disappears-after-orientation-change
    thanks, google. Thanks.
    [/italian soh]
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

}
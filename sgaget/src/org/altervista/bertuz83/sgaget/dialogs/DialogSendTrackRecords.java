package org.altervista.bertuz83.sgaget.dialogs;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

/**
 * User: bertuz
 * Project: sgaget
 * Dialog utilizzato dal task di invio degli spostamenti completati
 *
 * @see org.altervista.bertuz83.sgaget.FragTaskSendTrack
 * @see org.altervista.bertuz83.sgaget.ActHome
 */
public class DialogSendTrackRecords extends DialogFragment {
    private boolean indeterminate= true;
    private int progress= 0;
    private int max= 0;

    private boolean aborted= false;


    public static DialogSendTrackRecords newInstance() {
        DialogSendTrackRecords frag = new DialogSendTrackRecords();
        return frag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if( savedInstanceState != null ){
            indeterminate= savedInstanceState.getBoolean("indeterminate", true);
            progress= savedInstanceState.getInt("progress", 0);
            max= savedInstanceState.getInt("max", 0);
        }
        setRetainInstance(true);
    }

    /*
        workaround: nel caso sia in atto un cambio di configurazione, tutte le informazioni di stato
        vengono perse. Devo salvare e ripristinare tutto esplicitamente
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("indeterminate", indeterminate);
        outState.putInt("progress", progress);
        outState.putInt("max", max);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onResume() {
        super.onResume();

        if( indeterminate == false ){
            ((ProgressDialog)getDialog()).setIndeterminate(indeterminate);
            ((ProgressDialog)getDialog()).setProgress(progress);
            ((ProgressDialog)getDialog()).setMax(max);
        }
    }


    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        this.setCancelable(false);
        final ProgressDialog dialog= new ProgressDialog(getActivity());
        dialog.setTitle("Caricamento spostamenti");
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Interrompi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //onclick overriden dal dialog (vedi sotto). In questo modo non chiudo il dialog fino a quando non voglio io esplicitamente
            }
        });
        dialog.setMessage("Invio delle statistiche sugli spostamenti completate a ISF Modena in corso");
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b= dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        aborted= true;
                    }
                });
            }
        });
        return dialog;
    }


    public void setIndeterminate(boolean isIndeterminate){
        ((ProgressDialog)getDialog()).setIndeterminate(isIndeterminate);
        indeterminate= isIndeterminate;
    }


    public void incrementProgress(int progress){
        this.progress+= progress;
        ((ProgressDialog)getDialog()).incrementProgressBy(progress);
    }


    public void setMax(int max){
        this.max= max;
        ((ProgressDialog)getDialog()).setMax(max);
    }


    /*
        [italian soh]
        http://stackoverflow.com/questions/12433397/android-dialogfragment-disappears-after-orientation-change
        GRAZIE, google. GRAZIE per questo pomeriggio e sera persi in tuo onore.
        [/italian soh]
     */
    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }


    public boolean isAbortPressed(){
        return aborted;
    }
}
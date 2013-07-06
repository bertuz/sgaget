package org.altervista.bertuz83.sgaget.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockDialogFragment;


/**
 * User: bertuz
 * Project: sgaget
 *
 * Dialog flessibile e riutilizzabile utilizzante il nuovo approccio tramite fragments.
 * Utilizzabile con supportlibrary.
 */
public class DialogInfo extends SherlockDialogFragment {
    private int requestCode= -1;
    private DialogInfoComm callBackEntity;

    public static interface DialogInfoComm{
        public void onInfoDialogClosed(int requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setCloseListener(DialogInfoComm entity, int requestCode){
        callBackEntity= entity;
        this.requestCode= requestCode;
    }


    public static DialogInfo newInstance(String alertMessage, boolean cancelable, String title) {
        DialogInfo frag = new DialogInfo();

        Bundle args = new Bundle();
        args.putString("message", alertMessage);
        args.putBoolean("cancelable", cancelable);
        if(title == null)
            args.putString("title", "Oh, ooh...");
        else
            args.putString("title", title);

        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getSherlockActivity());
        this.setCancelable(getArguments().getBoolean("cancelable"));
        builder.setMessage(getArguments().getString("message"));
        builder.setTitle(getArguments().getString("title"));
        builder.setPositiveButton("Chiudi", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id){
                dialog.dismiss();
                if(callBackEntity != null)
                    callBackEntity.onInfoDialogClosed(requestCode);
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
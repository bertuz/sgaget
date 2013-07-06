package org.altervista.bertuz83.sgaget.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;
import org.altervista.bertuz83.sgaget.R;
import org.altervista.bertuz83.sgaget.helper.MyApplication;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Dialog utilizzante il nuovo approccio tramite fragments e personalizzato con proprio layout. Utilizzabile con supportlibrary.
 **/
public class DialogInfoHotpoints extends SherlockDialogFragment {

    /*
        scrivendo qui il contenuto che il dialog dovrebbe avere, e' possibile visualizzare cio'
        in un fullscreen frame anziche' in un dialog. Basta aggiungere in un fragmenttransaction questo frame.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View returnView= inflater.inflate(R.layout.dialog_help_hotpoints, container, false);


        TextView tv = (TextView) returnView.findViewById(R.id.dialog_help_hotpoints_description);
        SpannableString ss= new SpannableString(MyApplication.getAppContext().getString(R.string.help_hotpoints_description));
        ImageSpan is = new ImageSpan(MyApplication.getAppContext(), R.drawable.i_marker_red);
        ss.setSpan(is, 0, 1, 0);

        tv.setText(ss);

        returnView.findViewById(R.id.dialog_help_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInfoHotpoints.this.dismiss();
            }
        });


        return returnView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.setCancelable(true);

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        //dialog.requestWindowFeature(new Long(Window.FEATURE_NO_TITLE).intValue());
        dialog.setTitle("Punti di interesse");
        dialog.setCancelable(true);

        return dialog;
    }
}

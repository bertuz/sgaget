package org.altervista.bertuz83.sgaget.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockDialogFragment;


/**
 * User: bertuz
 * Project: sgaget
 * Dialog di scelta dell'ora utilizzato nel completamento delle statistiche.
 *
 * @see org.altervista.bertuz83.sgaget.Frag1ActCompleteFragsCommTrack
 * @see org.altervista.bertuz83.sgaget.Frag2ActCompleteFragsCommTrack
 */
public class DialogTimePicker extends SherlockDialogFragment{
    private TimePickerDialog.OnTimeSetListener listener= null;


    public void setListener(TimePickerDialog.OnTimeSetListener listener){
        this.listener= listener;
    }

    public static DialogTimePicker newInstance(int hour, int minute, TimePickerDialog.OnTimeSetListener listener) {
        DialogTimePicker frag = new DialogTimePicker();
        Bundle args = new Bundle();
        args.putInt("hour", hour);
        args.putInt("minute", minute);
        frag.setArguments(args);
        frag.setListener(listener);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimePickerDialog dialog= new TimePickerDialog(getActivity(), listener, getArguments().getInt("hour"), getArguments().getInt("minute"), true);
        return dialog;
    }
}
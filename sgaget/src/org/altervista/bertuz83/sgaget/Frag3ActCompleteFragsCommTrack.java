package org.altervista.bertuz83.sgaget;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.altervista.bertuz83.sgaget.helper.MyApplication;

/**
 * User: bertuz
 * Project: sgaget
 *
 * @see org.altervista.bertuz83.sgaget.ActCompleteTrack
 */
public class Frag3ActCompleteFragsCommTrack extends Fragment implements IActCompleteFragsComm {
    private TrackRecord trackRecord;

    private RadioGroup radiogroup;
    private EditText textAltro;
    private RadioButton buttonAltro;

    private boolean alertShown= false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView= (inflater.inflate(R.layout.act_complete_track_f3, container, false));
        radiogroup= (RadioGroup) fragmentView.findViewById(R.id.act_complete_track_f3_radiogroup);
        textAltro= (EditText) fragmentView.findViewById(R.id.act_complete_track_f3_txt_altro);
        buttonAltro= (RadioButton) fragmentView.findViewById(R.id.act_complete_track_f3_btn_altro);

        return fragmentView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                buttonAltro.setChecked(false);
                radioGroup.requestFocus();
                textAltro.clearFocus();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });


        buttonAltro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radiogroup.clearCheck();
                buttonAltro.setChecked(true);
                textAltro.requestFocus();

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.showSoftInput(textAltro, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        textAltro.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                radiogroup.clearCheck();
                buttonAltro.setChecked(true);
                if(alertShown){
                    textAltro.setText("");
                    alertShown= false;
                    textAltro.setTextColor(Color.BLACK);
                }
                return false;
            }
        });

        textAltro.addTextChangedListener(new TextWatcher() {
            int start=0;
            int after=0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if(alertShown){
                    start= i;
                    after= i3;
                    textAltro.setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(alertShown){
                    alertShown= false;
                    textAltro.setText(editable.subSequence(start, start+after));
                    textAltro.setSelection(textAltro.getText().length());
                }
            }
        });
    }

    @Override
    public void onPause() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        super.onPause();
    }

    @Override
    public void setTrackRecord(TrackRecord trackRecord) {
        this.trackRecord= trackRecord;
    }

    @Override
    public boolean saveSettings() {
        if((radiogroup.getCheckedRadioButtonId() == -1) && (!buttonAltro.isChecked()))
            return false;

        if(((this.buttonAltro.isChecked()) && (this.textAltro.getText().length()==0)) || (alertShown)){
            textAltro.setText("Inserire mezzo");
            textAltro.setTextColor(Color.RED);
            alertShown= true;
            return false;
        }

        if(radiogroup.getCheckedRadioButtonId() != -1){
            RadioButton button= (RadioButton) radiogroup.findViewById(radiogroup.getCheckedRadioButtonId());

            trackRecord.setTransportationType(button.getText().toString());
        }
        else
            trackRecord.setTransportationType(MyApplication.getAppContext().getString(R.string.transportation_type_other) + textAltro.getText().toString());

        return true;
    }
}

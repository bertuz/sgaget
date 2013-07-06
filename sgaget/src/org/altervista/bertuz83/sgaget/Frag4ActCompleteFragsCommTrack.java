package org.altervista.bertuz83.sgaget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import org.altervista.bertuz83.sgaget.business.TrackRecord;

import java.util.Calendar;
import java.util.Date;

/**
 * User: bertuz
 * Project: sgaget
 *
 * @see org.altervista.bertuz83.sgaget.ActCompleteTrack
 */
public class Frag4ActCompleteFragsCommTrack extends Fragment implements IActCompleteFragsComm {
    private TrackRecord trackRecord;
    private RatingBar qualityTraffic;
    private RatingBar qualityTrack;
    private EditText notes;
    private CheckBox isFestiveCheckBox;

    RatingBar.OnRatingBarChangeListener ratingListener=  new RatingBar.OnRatingBarChangeListener() {
        @Override public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            if(rating < 1)
                ratingBar.setRating(1);

        }
    };


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
        View layoutReturn= inflater.inflate(R.layout.act_complete_track_f4,container, false);

        qualityTrack= (RatingBar) layoutReturn.findViewById(R.id.act_complete_track_f4_rating_qualita);
        qualityTraffic= (RatingBar) layoutReturn.findViewById(R.id.act_complete_track_f4_rating_traffico);
        notes= (EditText) layoutReturn.findViewById(R.id.act_complete_track_f4_note);
        isFestiveCheckBox= (CheckBox) layoutReturn.findViewById(R.id.act_complete_track_f4_isFestive);

        //não permito um voto que seja menor que uma estrela
        //non permetto un voto che sia minore di una stella.
        qualityTrack.setOnRatingBarChangeListener(ratingListener);
        qualityTraffic.setOnRatingBarChangeListener(ratingListener);

        return layoutReturn;
    }

    @Override
    public void setTrackRecord(TrackRecord trackRecord) {
        this.trackRecord= trackRecord;
    }

    @Override
    public boolean saveSettings() {
        trackRecord.setQualityRate(new Double(qualityTrack.getRating()).intValue());
        trackRecord.setTrafficRate(new Double(qualityTraffic.getRating()).intValue());

        String notes= this.notes.getText().toString();
        if(notes==null)
            trackRecord.setNotes("");
        else
            trackRecord.setNotes(notes);

        if(isFestiveCheckBox.isChecked())
            trackRecord.setDay("Festivo");
        else{
            Calendar c= Calendar.getInstance();
            c.setTime(new Date(trackRecord.getCreationDate()));
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            String dayName;

            switch(dayOfWeek){
                case Calendar.MONDAY:
                    dayName= "Lunedì";
                    break;
                case Calendar.TUESDAY:
                    dayName= "Martedì";
                    break;
                case Calendar.WEDNESDAY:
                    dayName= "Mercoledì";
                    break;
                case Calendar.THURSDAY:
                    dayName= "Giovedì";
                    break;
                case Calendar.FRIDAY:
                    dayName= "Venerdì";
                    break;
                case Calendar.SATURDAY:
                    dayName= "Sabato";
                    break;
                default:
                    dayName= "Domenica";
                    break;
            }

            trackRecord.setDay(dayName);
        }

        return true;
    }
}

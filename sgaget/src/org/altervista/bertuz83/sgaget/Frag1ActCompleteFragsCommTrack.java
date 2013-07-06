package org.altervista.bertuz83.sgaget;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.altervista.bertuz83.sgaget.business.HotpointCandidate;
import org.altervista.bertuz83.sgaget.business.TrackRecord;
import org.altervista.bertuz83.sgaget.dialogs.DialogTimePicker;
import org.altervista.bertuz83.sgaget.helper.MyApplication;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * User: bertuz
 * Project: sgaget
 *
 * @see org.altervista.bertuz83.sgaget.ActCompleteTrack
 */
public class Frag1ActCompleteFragsCommTrack extends Fragment implements IActCompleteFragsComm {
    private TrackRecord trackRecord;
    private TextView tvDisplayTime;
    private ListView lstPlaces;
    private int hour;
    private int minute;
    private ArrayAdapter arrayAdapter;

    private SupportMapFragment mapFrag;
    private GoogleMap mMap;
    private Marker marker;


    private TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                    hour = selectedHour;
                    minute = selectedMinute;

                    // set current time into textview
                    tvDisplayTime.setText(new StringBuilder().append(pad(hour))
                            .append(":").append(pad(minute)));
                }
            };


    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        ArrayList<String> startList = new ArrayList();
        for(HotpointCandidate candidateStart : trackRecord.getStartCandidates()){
            startList.add("" + candidateStart.getHotpointName());
        }

        arrayAdapter= new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_single_choice,startList);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);    //To change body of overridden methods use File | Settings | File Templates.
    }


    private void setHourMinute(long time){
        GregorianCalendar calendar= new GregorianCalendar();
        calendar.setTimeInMillis(time);

        String str=  pad(calendar.get(GregorianCalendar.HOUR_OF_DAY)) +
                ":" + pad(calendar.get(GregorianCalendar.MINUTE));
        hour= calendar.get(GregorianCalendar.HOUR_OF_DAY);
        minute= calendar.get(GregorianCalendar.MINUTE);
        tvDisplayTime.setText(str);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView= (inflater.inflate(R.layout.act_complete_track_f1, container, false));
        this.lstPlaces= (ListView)fragmentView.findViewById(R.id.act_complete_track_f1_list_places);
        this.tvDisplayTime= (TextView) fragmentView.findViewById(R.id.act_complete_track_f1_hour);

        if(getChildFragmentManager().findFragmentByTag("map") == null){
            GoogleMapOptions options= new GoogleMapOptions();
            LatLng latlng= new LatLng(MyApplication.CENTERMAP_LAT, MyApplication.CENTERMAP_LONG);
            options.camera(new CameraPosition(latlng, 17, 30, 0));
            mapFrag= SupportMapFragment.newInstance(options);

            FragmentTransaction ft= getChildFragmentManager().beginTransaction();

            ft.add(R.id.act_complete_track_f1_map, mapFrag, "map");
            mapFrag.setRetainInstance(true);

            ft.commit();
        }

        //la scelta di setitemchecked viene poi eventualmente spostata sull'ultimo item selezionato grazie a standard onviewstaterestore di android
        //(dovro' quindi solo impostare l'ora relativa alla selezione)
        lstPlaces.setAdapter(arrayAdapter);
        lstPlaces.setItemChecked(0, true);

        fragmentView.findViewById(R.id.act_complete_track_f1_btn_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeDialog();
            }
        });

        lstPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setHourMinute(trackRecord.getStartCandidates().get(i).getTime());
                updateMap(trackRecord.getStartCandidates().get(i).getHotpoint().getLatitude(), trackRecord.getStartCandidates().get(i).getHotpoint().getLongitude());
            }
        });

        return fragmentView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();

        mMap= mapFrag.getMap();
        if(mMap == null)
            getView().findViewById(R.id.act_complete_track_f1_map).setVisibility(View.GONE);

        //a questo punto il precedente punto e' eventualmente stato richeckato
        int positionSelected= ((ListView)getView().findViewById(R.id.act_complete_track_f1_list_places)).getCheckedItemPosition();

        HotpointCandidate candidate= (trackRecord.getStartCandidates().get(positionSelected));
        updateMap(candidate.getHotpoint().getLatitude(), candidate.getHotpoint().getLongitude());
        if(tvDisplayTime.getText().length() == 0)
            setHourMinute(trackRecord.getStartCandidates().get(positionSelected).getTime());
    }


    private void updateMap(Double latitude, Double longitude){
        if(mMap == null)
            return;

        LatLng place= new LatLng(latitude, longitude);

        if(marker != null)
            marker.remove();

        marker= this.mMap.addMarker(new MarkerOptions()
                .position(place));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(place)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private void showTimeDialog(){
        // Create and show the dialog.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("timedialog");
        if (prev != null)
            ft.remove(prev);

        ft.addToBackStack(null);
        DialogFragment newFragment= DialogTimePicker.newInstance(hour, minute, timePickerListener);
        newFragment.show(ft, "timedialog");
    }


    @Override
    public void setTrackRecord(TrackRecord trackRecord) {
        this.trackRecord= trackRecord;
    }


    @Override
    public boolean saveSettings() {
        trackRecord.setStartHotpoint(trackRecord.getStartCandidates().get(this.lstPlaces.getCheckedItemPosition()).getHotpointName());
        trackRecord.setStarthour(tvDisplayTime.getText().toString());
        return true;
    }
}
package org.altervista.bertuz83.sgaget;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.altervista.bertuz83.sgaget.business.Hotpoint;
import org.altervista.bertuz83.sgaget.data.LocalHotpointsDAO;
import org.altervista.bertuz83.sgaget.data.LocalHotpointsDB;
import org.altervista.bertuz83.sgaget.dialogs.DialogInfo;
import android.view.ViewGroup.LayoutParams;
import org.altervista.bertuz83.sgaget.dialogs.DialogInfoHotpoints;
import org.altervista.bertuz83.sgaget.helper.MyApplication;

import java.util.ArrayList;

/**
 * User: bertuz
 * Project: sgaget
 *
 * tab con mappa dei punti di interesse attuali
 *
 * @see org.altervista.bertuz83.sgaget.ActHome
 */
public class FTabPuntiInteresse extends Fragment implements DialogInfo.DialogInfoComm{
    private SupportMapFragment mapFrag;

    private ArrayAdapter<String> hotpointsAdapter;

    private View mapFrame;
    private TextView warningHotpoints;
    private ListView listHotpoints;

    private int mapFrameVisibility= View.GONE;
    private int warningHotpointsVisibility= View.VISIBLE;
    private int listHotpointsVisibility= View.GONE;

    private ArrayList<Hotpoint> hotpointsList;
    private boolean pointsAdded= false;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    public void updatePoints(){
        LocalHotpointsDAO hotpoints= new LocalHotpointsDB();
        try{
            hotpoints.open();
        }catch(Exception e){
            DialogInfo dialog= DialogInfo.newInstance("Un problema nel reperimento dei dati ha impedito sgàget di continaure l'esecuzione. Cancella i dati e/o reinstalla Sgàget.", false, null);
            dialog.setCloseListener(this, 0);
        }

        hotpointsList= hotpoints.getHotpointsList();
        hotpoints.close();

        pointsAdded= true;

        if(hotpointsList.size() != 0){
            this.warningHotpoints.setVisibility(View.GONE);
            warningHotpointsVisibility= View.GONE;
        }
        else
            return;

        GoogleMap mMap= mapFrag.getMap();
        if(mMap != null) {
            mapFrame.setVisibility(View.VISIBLE);
            mapFrameVisibility= View.VISIBLE;
            mMap.clear();

            for(Hotpoint hotpoint : hotpointsList){
                LatLng place= new LatLng(hotpoint.getLatitude(), hotpoint.getLongitude());
                mMap.addMarker(new MarkerOptions().position(place).title(hotpoint.getName()));
            }
        }
        else {
            this.listHotpoints.setVisibility(View.VISIBLE);
            listHotpointsVisibility= View.VISIBLE;

            hotpointsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
            for(Hotpoint hotpoint : hotpointsList){
                hotpointsAdapter.add(hotpoint.getName());
            }
            listHotpoints.setAdapter(hotpointsAdapter);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        if(!pointsAdded)
            updatePoints();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragReturn= inflater.inflate(R.layout.tab_punti_interesse, container, false);

        /*
            nel caso sia stata distrutta la view del frame per problemi di spazio (es: switch su altre applicazioni e ritorno dopo svariate attivita')
            ricreo la view e riottengo i riferimenti ai vari elementi grafici. Il frame delle mappe non ha bisogno di essere ricreato E TANTOMENO
            DEVE ESSERE RINIZIALIZZATO (es: se ho aggiunto dei marker, il supportmapfragment e' stato programmato in modo da salvare tutte le informazioni del caso).
            Dovro' comunque riottenere la referenza a esso, in modo che la mia variabile di referenza al fragment sia di nuovo utilizzabile.
        */
        if(getChildFragmentManager().findFragmentByTag("hotpointsmap") == null){
            GoogleMapOptions options= new GoogleMapOptions();
            LatLng latlng= new LatLng(MyApplication.CENTERMAP_LAT, MyApplication.CENTERMAP_LONG);
            options.camera(new CameraPosition(latlng, 13, 30, 0));
            mapFrag= SupportMapFragment.newInstance(options);
            mapFrag.setRetainInstance(true);

            FragmentTransaction ft= getChildFragmentManager().beginTransaction();
            ft.add(R.id.tab_punti_interesse_map, mapFrag, "hotpointsmap");
            ft.commit();
            getChildFragmentManager().executePendingTransactions();
        }
        else
            mapFrag= (SupportMapFragment) getChildFragmentManager().findFragmentByTag("hotpointsmap");

        this.mapFrame= fragReturn.findViewById(R.id.tab_punti_interesse_map);
        this.warningHotpoints = (TextView) fragReturn.findViewById(R.id.tab_punti_interesse_warning_nopunti);
        this.listHotpoints= (ListView) fragReturn.findViewById(R.id.tab_punti_interesse_list);

        if(hotpointsAdapter != null)
            listHotpoints.setAdapter(hotpointsAdapter);

        mapFrame.setVisibility(mapFrameVisibility);
        listHotpoints.setVisibility(listHotpointsVisibility);
        warningHotpoints.setVisibility(warningHotpointsVisibility);

        fragReturn.findViewById(R.id.tab_punti_interesse_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInfoHotpoints infoHotpoints= new DialogInfoHotpoints();
                infoHotpoints.show(getFragmentManager(), "dialog");
                if ( infoHotpoints.getDialog() != null )
                    infoHotpoints.getDialog().setCanceledOnTouchOutside(true);

                //FragmentTransaction transaction = getFragmentManager().beginTransaction();
                //transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                //aggiungo dialog
                //transaction.add(android.R.id.content, infoHotpoints).addToBackStack(null).commit();
            }
        });

        return fragReturn;
    }

    @Override
    public void onInfoDialogClosed(int requestCode) {
        if(getActivity() != null)
            getActivity().finish();
    }
}

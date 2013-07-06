package org.altervista.bertuz83.sgaget.service;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import org.altervista.bertuz83.sgaget.business.Hotpoint;
import org.altervista.bertuz83.sgaget.business.HotpointCandidate;
import org.altervista.bertuz83.sgaget.data.LocalHotpointsDB;
import org.altervista.bertuz83.sgaget.helper.MyApplication;
import org.altervista.bertuz83.sgaget.receiver.ReceiverActions;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.*;
import static java.lang.Math.asin;
import static java.lang.Math.sqrt;

/**
 * motore del tracciamento degli spostamenti.
 */
public class Locator implements LocationListener {
    public static final int HASTOSTART= 0;
    public static final int TRACKING= 1;
    public static final int TERMINATING= 2;
    public static final int COMPLETED= 3;

    private int state= HASTOSTART;

    private LocationManager locationManager;

    private ArrayList<Hotpoint> hotpoints;

    private List<PositionUpdate> hotpointsHistory= new ArrayList<PositionUpdate>();
    private Map<String, HotpointCandidate> hotpointsRate;

    /**
     * A reference to the next update that will be stored in the hotpointsHistory. It still needs a check with a new update for completing the phase 2
     * (see the enclosed documentation to understand the tracking phases)
     * */
    private PositionUpdate previousUpdate;

    /**
     * used when someone stops the tracking
     */
    private boolean pushLastUpdate= false;

    //expressed in meters
    private final static double radiusHotPointArea= 200.0;
    //expressed in km per hour. Used in phase 2 (see the enclosed documentation for further details)
    private final static double maxSpeed= 70.0;
    private final static double equatorialRadius= 6378.1370;
    private final static double polarRadius= 6356.7523;

    //final results
    public List<HotpointCandidate> hotpointsCandidates;


    public Locator(LocationManager locationManager){
        this.locationManager = locationManager;
    }

    public void startTracking(){
        //non sono all'ultimo update richiesto per fermare il tutto.
        if(this.pushLastUpdate == false){
            try{
                LocalHotpointsDB dbDAO= new LocalHotpointsDB();
                dbDAO.open();
                this.hotpoints= dbDAO.getHotpointsList();
            }catch(Exception e){
                Intent intent= new Intent(ReceiverActions.ACT_REC_TRACKINGSTATUS_ERROR_START);
                intent.putExtra("errorMessage", "problemi all' apertura del database");
                LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
                return;
            }

            if(this.hotpoints.size()==0){
                Intent intent= new Intent(ReceiverActions.ACT_REC_TRACKINGSTATUS_ERROR_START_NOPOINTS);
                intent.putExtra("errorMessage", "nessun punto di interesse presente. Aggiornare i punti di interesse per poter tracciare il tragitto.");
                LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
                return;
            }

            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 7000, 30, this);
            this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 100, this);
            this.state= TRACKING;
            return;
        }
        else
            return;

    }

    public int getState(){
        return this.state;
    }

    @Override
    public void onLocationChanged(Location location){
        if((this.state != TRACKING) && (this.state != TERMINATING))
            return;

        /*
            nel caso abbia chiamato stoptracking e stia aspettando l'ultimo aggiornamento, ma questo non arriva,
            potrei aver chiamato manualmente questa callback passando location null.
            Cio' significa che completo comunque l'azione, ma senza controllare gli eventuali nuovi hotpoint raggiunti
            (come se ritornasse null la getHotpointsAreIn).
        */
        //I create a new update with my new location information that has just arrived
        PositionUpdate update= null;
        if(location != null)
            update= getHotpointsAreIn(location);

        if(MyApplication.DEBUG){
            Log.d("tracciamento", "----------------------------------- UPDATE -----------------------------------");
            if(previousUpdate!=null){
                SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
                String time= sdfDate.format(previousUpdate.getLocation().getTime());

                Log.d("tracciamento", "PREVIOUS UPDATE");
                Log.d("tracciamento", "ora: " + time);

                for(Hotpoint hotpointTrace: previousUpdate.getHotpoints()){
                    Log.d("tracciamento", hotpointTrace.getName());
                }
            }
        }

        if(update!=null){
            //first update with worth regarding hotspots arrived. No previous updates, this is the first one (or maybe the last update was null)
            if(previousUpdate == null)
                previousUpdate= update;

                //i also check if the new update returned is the same of before (in case a new update has the same hotpoints, we disregard this new update)
            else{
                if(previousUpdate.getNumberOfHotpoints() != 0){
                    //I finally add my previous update
                    hotpointsHistory.add(0, previousUpdate);
                }
                //the new generated update will be the previous update next time.
                previousUpdate= update;
            }

            Intent intent= new Intent(ReceiverActions.ACT_REC_HOTPOINTSNEARBY_LOCATION_UPDATED);
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
        }

        if(MyApplication.DEBUG){
            if(update!=null){
                SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
                String time= sdfDate.format(update.getLocation().getTime());
                Log.d("trackDEBUG", "----------------------------------- NEW UPDATE ------------------------------");
                Log.d("trackDEBUG", "ora: " + time);
                Log.d("trackDEBUG", "long lat: " + update.getLocation().getLongitude() + " " + update.getLocation().getLatitude() + " " + update.getLocation().getAccuracy());

                for(Hotpoint hotpointTrace: update.getHotpoints()){
                    Log.d("trackDEBUG", hotpointTrace.getName());
                }
            }
            Log.d("trackDEBUG", "----------------------------------------------------------------------");
        }

        /*
             push the last previousUpdate if we have terminated (the second phase's last check it should need won't be performed)
             Con l'ultimo update (requestsingleupdate richiesto da stoptracking) abbiamo eventualmente inserito il previousupdate nello storico updates, opportunamente controllato
             e filtrato con questo nuovo update.
             Ora aggiungiamo anche questo update, il quale non sara' controllato e filtrato "all'indietro" da un prossimo update.
         */
        if(pushLastUpdate){
            if(previousUpdate!=null)
                hotpointsHistory.add(0, previousUpdate);

            //callback to whom stopped the tracking. Not it can read the hotpoints estimates safely
            this.state= COMPLETED;
            Intent intent= new Intent(ReceiverActions.ACT_REC_TRACKINGSTATUS_STOPPED);
            LocalBroadcastManager.getInstance(MyApplication.getAppContext()).sendBroadcast(intent);
        }
    }

    private PositionUpdate getHotpointsAreIn(Location locationToCheck){
        PositionUpdate newUpdate= new PositionUpdate(locationToCheck);
        double radius= getRadius(locationToCheck.getLongitude());
        Location previousLocation= null;
        double distance;

        if(previousUpdate!=null)
            previousLocation= previousUpdate.getLocation();

        for(Hotpoint hotpoint: this.hotpoints){
            distance= getDistance(locationToCheck.getLatitude(),
                    locationToCheck.getLongitude(),
                    radius,
                    hotpoint.getLatitude(),
                    hotpoint.getLongitude());

            //
            // 1st PHASE (see documentation)
            // Adding all the possible hotpoints
            //
            if(distance - locationToCheck.getAccuracy() <= radiusHotPointArea){
                //if this is the first update, no further phases are needed.
                if(this.previousUpdate == null){
                    newUpdate.addHotpoint(hotpoint);
                    continue;
                }

                //
                // 2nd PHASE
                //
                // comparing the last position (if the new location, taking into account the maximum speed, is too far away from the last point, it is maybe not worth adding it)
                //check the documentation to figure out when a new point is disregarded.
                double distanceLastPoint= getDistance(previousLocation.getLatitude(),
                        previousLocation.getLongitude(),
                        radius,
                        hotpoint.getLatitude(),
                        hotpoint.getLongitude());
                double distanceWithMaxSpeed= (maxSpeed * (1000.0 / 3600.0)) * ((locationToCheck.getTime() - previousLocation.getTime())/1000.0);

                if(distanceLastPoint - previousLocation.getAccuracy() - distanceWithMaxSpeed <= radiusHotPointArea)
                    newUpdate.addHotpoint(hotpoint);
            }
        }

        //in case no hotspots are bound with this location update, I have already finished (this update is useless)
        if(newUpdate.getNumberOfHotpoints()==0)
            return null;

        //
        // 3rd PHASE
        //
        //I filter hotpoints list of the previous and new location update (see the documentation for more info)
        if(previousUpdate ==null)
            return newUpdate;

        //if the previous update's hotpoint is too far away from the new update (the user could not get there by travelling at the most speed possible), we disregard that hotpoint
        List<String> hotpointIDsToRemove= new ArrayList<String>();

        for(Hotpoint hotpointToCheck : previousUpdate.getHotpoints()){
            double distanceNewLocationToHotpoint= getDistance(locationToCheck.getLatitude(), locationToCheck.getLongitude(), radius, hotpointToCheck.getLatitude(), hotpointToCheck.getLongitude());
            double distanceWithMaxSpeed= (maxSpeed * (1000.0 / 3600.0)) * ((locationToCheck.getTime() - previousUpdate.getLocation().getTime())/1000.0);

            if(distanceNewLocationToHotpoint - distanceWithMaxSpeed - locationToCheck.getAccuracy() > radiusHotPointArea)
                hotpointIDsToRemove.add(hotpointToCheck.getName());
        }
        previousUpdate.filterHotpointsByIDs(hotpointIDsToRemove);


        //if both the previous and the new location update have the same hotpoints, it disregards this new update (it is useless)
        if(previousUpdate.getHotpointsIds().equals(newUpdate.getHotpointsIds()))
            return null;

        return newUpdate;
    }

    /**
     * It returns the actual positions update containing the list of the possible hotpoint areas we coule be in.
     */
    public PositionUpdate getActualUpdate(){
        return this.previousUpdate;
    }

    public List<PositionUpdate> getHotpointsHistory(){
        return this.hotpointsHistory;
    }

    //see the enclosed documentation "EMISENOVERSO"
    private double getRadius(double latitude){
        double a2= pow(equatorialRadius,2);
        double b2= pow(polarRadius,2);
        double cosLat= cos(latitude);
        double sinLat= sin(latitude);

        double numerator= pow(a2 * cosLat,2) + pow(b2 * sinLat,2);
        double denominator= pow(equatorialRadius * cosLat, 2) + pow(polarRadius * sinLat, 2);

        double radius= sqrt(numerator / denominator);

        return radius;
    }

    //see the enclosed documentation "EMISENOVERSO"
    private double getDistance(double latPosition, double longPosition, double radius, double latPoint, double longPoint){
        latPosition= latPosition * Math.PI / 180;
        longPosition= longPosition * Math.PI / 180;
        latPoint= latPoint * Math.PI / 180;
        longPoint= longPoint * Math.PI / 180;


        double part1= pow(sin((latPosition - latPoint)/2),2);
        double part2= cos(latPoint)*cos(latPosition);
        double part3= pow(sin((longPosition - longPoint) / 2), 2);

        double distance= 2 * radius * asin(sqrt((part1 + (part2 * part3))));

        //from km to meters
        return (distance*1000);
    }

    public class PositionUpdate{
        private Map<String, Hotpoint> hotPoints= new HashMap<String, Hotpoint>();
        private Location locationUpdate;

        public PositionUpdate(Location locationUpdate){
            this.locationUpdate= locationUpdate;
        }

        public Collection<Hotpoint> getHotpoints(){
            return hotPoints.values();
        }

        public Set<String> getHotpointsIds(){
            return hotPoints.keySet();
        }

        public void addHotpoint(Hotpoint hotpoint){
            this.hotPoints.put(hotpoint.getName(), hotpoint);
        }

        public int getNumberOfHotpoints(){
            return this.hotPoints.size();
        }

        public Location getLocation(){
            return this.locationUpdate;
        }

        public void filterHotpointsByIDs(List<String> ids){
            for(String key : ids){
                this.hotPoints.remove(key);
            }
        }
    }

    public void abortTracking(){
        this.state= COMPLETED;
        locationManager.removeUpdates(this);
    }

    public void stopTracking() throws LocatorException{
        if(this.state == TRACKING){
            this.state= TERMINATING;
            locationManager.removeUpdates(this);
            this.pushLastUpdate = true;

            final LocationManager manager = (LocationManager) MyApplication.getAppContext().getSystemService(MyApplication.getAppContext().LOCATION_SERVICE);
            if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ))
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            else
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);

            //nel caso non riceva l'ultimo aggiornamento richiesto (es: gps non riesce ad ottenere il fixing o network provider non ha connessione internet)
            //cancello la richiesta e termino senza un ultimo update.
            TimerTask task= new LastUpdateTimeout(this);
            Timer timer= new Timer();
            timer.schedule(task, 0, 1000);
        }
        else
            throw new LocatorException(LocatorException.ERROR_ALREADY_STOPPED);
    }


    //posizione 0 = update piu nuovo, piu' si scorre l'array piu' si scorreranno updates vicini alla partenza del tracking
    public void sortHotpoints(int startHotpointPosition) throws LocatorException{
        hotpointsRate = new HashMap<String, HotpointCandidate>();

        if((startHotpointPosition < 0)||(startHotpointPosition >= this.hotpointsHistory.size()))
            throw new LocatorException(LocatorException.ERROR_POSITION_OUT_OF_RANGE);

        if((this.state == TRACKING)|| (this.state == TERMINATING))
            throw new LocatorException(LocatorException.ERROR_STILL_TRACKING);

        if(! MyApplication.DEBUG){
            if(this.hotpointsHistory.size() < 3)
                throw new LocatorException(LocatorException.ERROR_TOO_FEW_POINTS);
        }
        //
        //local var definitions
        //
        double maxAccuracy= Double.MIN_VALUE;
        double minAccuracy= Double.MAX_VALUE;
        double accuracyRate;
        double maxUpdateVal= ((1.0/(Math.log10((0 / 5.0) + 1.2)))*2.0)-2.0;


        //Double avgWeightRate= 1.0 / (hotpointsHistory.size() + (hotpointsHistory.size()*5.0/3.0));
        Double rateWeightForAccuracy= Math.pow(hotpointsHistory.size()-1, 3);
        rateWeightForAccuracy= 0.2 / rateWeightForAccuracy;


        //calcolo il massimo dell'accuracy e il minimo per calcoli successivi
        //reverse perche' allo zero abbiamo i piu nuovi (quindi adatti all'arrivo), in fondo quelli vecchi (adatti a partenza)
        for(PositionUpdate updateToCheck : this.hotpointsHistory){
            if(updateToCheck.getLocation().getAccuracy()< minAccuracy)
                minAccuracy= updateToCheck.getLocation().getAccuracy();

            if(updateToCheck.getLocation().getAccuracy()> maxAccuracy)
                maxAccuracy= updateToCheck.getLocation().getAccuracy();
        }

        //anche l'update con meno accuratezza conterà qualcosa, sebbene molto poco
        //+1.0 inc ase max and min are the same
        maxAccuracy= maxAccuracy+((maxAccuracy+1.0 - minAccuracy) / 5.0);
        accuracyRate= 1.0 / (maxAccuracy-minAccuracy);

        //central check
        PositionUpdate updateToCheck= this.hotpointsHistory.get(startHotpointPosition);
        Double updateWeight= this.calculateWeight(updateToCheck, 0, maxAccuracy, accuracyRate, maxUpdateVal, rateWeightForAccuracy);
        Log.d("locatorDebug", "update CENTRO posizione:" + 0 + " weight " + updateWeight);
        updateHotpointsRate(updateToCheck, updateWeight);

        int positionUpdate= 0;
        //Left check (towards the newests/ end of the track). At most 3 updates
        for(int i=startHotpointPosition-1; (i>=0 && positionUpdate<=3); i--){
            positionUpdate++;
            updateToCheck= this.hotpointsHistory.get(i);

            updateWeight= this.calculateWeight(updateToCheck, positionUpdate, maxAccuracy, accuracyRate, maxUpdateVal, rateWeightForAccuracy);
            Log.d("locatorDebug", "update SX posizione:" + positionUpdate + " weight " + updateWeight);
            updateHotpointsRate(updateToCheck, updateWeight);
        }

        positionUpdate= 0;
        //Right check (towards the oldests/ start of the track). At most 3 updates
        for(int i=startHotpointPosition+1; (i<hotpointsHistory.size() && positionUpdate<=3); i++){
            positionUpdate++;
            updateToCheck= this.hotpointsHistory.get(i);
            updateWeight= this.calculateWeight(updateToCheck, positionUpdate, maxAccuracy, accuracyRate, maxUpdateVal, rateWeightForAccuracy);
            Log.d("locatorDebug", "update DX posizione:" + positionUpdate + " weight " + updateWeight + " " + hotpointsHistory.get(i).getHotpoints());
            updateHotpointsRate(updateToCheck, updateWeight);
        }

        //scorrimento degli updates e assegnazione dei pesi ad ogni hotpoint riscontrato effettuata.
        //Ora ordino semplicemente gli hotpoint per peso e li restituisco
        MyComparator<String> ratesComparator= new MyComparator<String>(hotpointsRate);
        TreeMap tm= new TreeMap<String, HotpointCandidate>(ratesComparator);
        tm.putAll(this.hotpointsRate);

        this.hotpointsCandidates= new ArrayList(tm.values());

        if(MyApplication.DEBUG){
            Log.d("trackDEBUG", "-------------------------------- SORTED " + startHotpointPosition+ " ------------------------------------");
            for(HotpointCandidate candidate : hotpointsCandidates){
                Log.d("trackDEBUG", "" + candidate.getHotpoint().getName() + "  " + candidate.getRate() );

            }
            Log.d("trackDEBUG", "----------------------------------------------------------------------");
        }
    }

    private void updateHotpointsRate(PositionUpdate updateToCheck, double updateWeight){
        //aggiorno le statistiche degli hotspot in base al nuovo rate (se nelle statistiche l'hotspot non c'era lo aggiungo)
        for(Hotpoint hotpointToAdd: updateToCheck.getHotpoints()){
            String nameHtpointToChange= hotpointToAdd.getName();
            if(hotpointsRate.containsKey(nameHtpointToChange)){
                HotpointCandidate hotpointToUpdate= hotpointsRate.get(nameHtpointToChange);

                SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");
                String time= sdfDate.format(updateToCheck.getLocation().getTime());

                hotpointToUpdate.addRate(updateWeight, updateToCheck.getLocation().getTime());
            }
            else
                hotpointsRate.put(nameHtpointToChange,
                        new HotpointCandidate(hotpointToAdd,
                                updateWeight,
                                updateToCheck.getLocation().getTime()));
        }
    }

    private double calculateWeight(PositionUpdate updateToCheck, int positionUpdate, double maxAccuracy, double accuracyRate, double maxUpdateVal, double rateWeightForAccuracy){
        double updateWeight= ((1.0/(Math.log10((positionUpdate / 5.0) + 1.2)))*2.0)-2.0;
        updateWeight= updateWeight/maxUpdateVal;

        double accuracyWeight= accuracyRate * (maxAccuracy - updateToCheck.getLocation().getAccuracy());

        //now I calculate the final weight by calculating a weighted average between accuracy and update number
        //Double avgWeightAccuracy= avgWeightRate * (positionUpdateWeight + (hotpointsHistory.size()/2.0));
        Double avgWeightAccuracy= ((rateWeightForAccuracy * Math.pow(positionUpdate, 3))+ 0.3);

        //prova
        //Double totalweight= new Double((avgWeightAccuracy * accuracyWeight) + ((1.0 - avgWeightAccuracy) * updateWeight));
        Double totalweight= new Double(updateWeight + ((avgWeightAccuracy * accuracyWeight) + ((1.0 - avgWeightAccuracy) * updateWeight)));

        return (totalweight);
    }

    /**
       it gives back the candidates ordered from the most probable to the least one
     */
    public Collection<HotpointCandidate> getHotpointsCandidates(){
        Collections.reverse(this.hotpointsCandidates);
        return this.hotpointsCandidates;
    }

    public int getNrHotpointUpdatesTracked(){
        return this.getHotpointsHistory().size();
    }

    private class MyComparator<String> implements Comparator<String> {
        private Map<String, HotpointCandidate> hotpointCandidates;

        public MyComparator(Map<String, HotpointCandidate> hotpointCandidates){
            this.hotpointCandidates= hotpointCandidates;
        }

        @Override
        public int compare(String hotpointName1, String hotpointName2){
            Double hotpointRate1= hotpointCandidates.get(hotpointName1).getRate();
            Double hotpointRate2= hotpointCandidates.get(hotpointName2).getRate();

            int comparation= hotpointRate1.compareTo(hotpointRate2);
            if(comparation == 0)
                return -1;
            else
                return comparation;
        }

        @Override
        public boolean equals(Object o){
            return false;
        }
    };
    ;

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }
}

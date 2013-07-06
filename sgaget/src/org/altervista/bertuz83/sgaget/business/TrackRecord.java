package org.altervista.bertuz83.sgaget.business;

import org.altervista.bertuz83.sgaget.helper.UtilityFunctions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * User: bertuz
 * Project: sgaget
 *
 * java bean per la rappresentazione OOP di uno spostamento
 */
public class TrackRecord {
    private long creationDate= 0;
    private boolean completed= false;
    private String transportationType= "";
    private String day= "";
    private long startTime= -1;
    private String startHotpoint= "";
    private long finishTime= -1;
    private String finishHotpoint= "";
    private int trafficRate= 0;
    private int qualityRate= 0;
    private String notes= "";
    private long elapsedTime= -1;
    private boolean sent= false;


    //first candidate= most possible candidate
    private ArrayList<HotpointCandidate> startCandidates= new ArrayList<HotpointCandidate>();
    private ArrayList<HotpointCandidate> finishCandidates= new ArrayList<HotpointCandidate>();

    public TrackRecord(){ }

    public TrackRecord(long creationDate,
                       boolean completed,
                       String transportationType,
                       String day,
                       long startTime,
                       String startHotpoint,
                       long finishTime,
                       String finishHotpoint,
                       int trafficRate,
                       int qualityRate,
                       String notes,
                       boolean sent){
        this.creationDate= creationDate;
        this.completed= completed;
        this.transportationType= transportationType;
        this.day= day;
        this.startTime= startTime;
        this.startHotpoint= startHotpoint;
        this.finishTime= finishTime;
        this.finishHotpoint= finishHotpoint;
        this.trafficRate= trafficRate;
        this.qualityRate= qualityRate;
        this.notes= notes;
        this.sent= sent;
    }

    public TrackRecord(long creationDate, String elapsedTime, ArrayList<HotpointCandidate> startCandidates, ArrayList<HotpointCandidate> finishCandidates){
        this.creationDate= creationDate;
        this.elapsedTime= UtilityFunctions.getLongFromStrHHmmss(elapsedTime);
        this.startCandidates= startCandidates;
        this.finishCandidates= finishCandidates;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }


    /**
     *
     * @return if both starttime and finishtime are set, it returns the real elapsed time, otherwise it returns
     * the indicative elapsed time set with setElapsedTime
     * @see org.altervista.bertuz83.sgaget.business.TrackRecord.setElapsedTime
     */
    public String getElapsedTime() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) -
            TimeUnit.HOURS.toMinutes(minutes);
        String minutesStr = (minutes < 10) ? "0" + minutes : "" + minutes;
        String secondsStr = (seconds < 10) ? "0" + seconds : "" + seconds;
        return (minutesStr + ":" + secondsStr);
    }


    public long getElapsedTimeInMs(){
        return this.elapsedTime;
    }


    private long calculateRealElapsedTime(){
        long finishTime= (this.finishTime >= startTime)? this.finishTime : this.finishTime + this.startTime;

        return ( finishTime - startTime );
    }


    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime= UtilityFunctions.getLongFromStrHHmmss(elapsedTime);
    }


    public void setElapsedTime(long elapsedTime){
        this.elapsedTime= elapsedTime;
    }


    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getTransportationType() {
        return transportationType;
    }

    public void setTransportationType(String transportationType) {
        this.transportationType = transportationType;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStarthour() {
        return UtilityFunctions.getHourFromLong(startTime);
    }

    public long getStartTime(){
        return this.startTime;
    }

    public long getFinishTime(){
        return this.finishTime;
    }

    public void setStarthour(String starthour) {
        setStartTime(UtilityFunctions.getLongFromStrHHmm(starthour));
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;

        if(finishTime!= -1)
            this.elapsedTime= calculateRealElapsedTime();
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;

        if(startTime!= -1)
            this.elapsedTime= calculateRealElapsedTime();
    }

    public String getStartHotpoint() {
        return startHotpoint;
    }

    public void setStartHotpoint(String startHotpoint) {
        this.startHotpoint = startHotpoint;
    }

    public String getFinishhour() {
        return UtilityFunctions.getHourFromLong(finishTime);
    }

    public void setFinishhour(String finishhour) {
        setFinishTime(UtilityFunctions.getLongFromStrHHmm(finishhour));
    }


    public String getFinishHotpoint() {
        return finishHotpoint;
    }

    public void setFinishHotpoint(String finishHotpoint) {
        this.finishHotpoint = finishHotpoint;
    }

    public int getTrafficRate() {
        return trafficRate;
    }

    public void setTrafficRate(int trafficRate) {
        this.trafficRate = trafficRate;
    }

    public int getQualityRate() {
        return qualityRate;
    }

    public void setQualityRate(int qualityRate) {
        this.qualityRate = qualityRate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ArrayList<HotpointCandidate> getStartCandidates() {
        return startCandidates;
    }

    public void setStartCandidates(ArrayList<HotpointCandidate> startCandidates) {
        this.startCandidates = startCandidates;
    }

    public ArrayList<HotpointCandidate> getFinishCandidates() {
        return finishCandidates;
    }

    public void setFinishCandidates(ArrayList<HotpointCandidate> finishCandidates) {
        this.finishCandidates = finishCandidates;
    }
}

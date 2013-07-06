package org.altervista.bertuz83.sgaget.business;

/**
 * User: bertuz
 * Project: sgaget
 *
 * java bean per rappresentazione OOP di un punto di interesse candidato alla partenza o arrivo di uno spostamento
*/
public class HotpointCandidate {
    private double rate= 0.0;
    private long time;
    private Hotpoint hotpoint;

    //per i nostri usi valori <=0 non esistono. Quindi 0= non usata.
    private double estimateTime= 0;

    public HotpointCandidate(Hotpoint hotpoint, Double rate, long time){
        this.hotpoint= hotpoint;
        this.rate= rate;
        this.estimateTime= rate * (time+0.0);
    }

    public HotpointCandidate() { }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getEstimateTime() {
        return estimateTime/rate;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setHotpoint(Hotpoint hotpoint) {
        this.hotpoint = hotpoint;
    }

    public void addRate(double rateToAdd, long time){
        this.rate+= rateToAdd;
        this.estimateTime+= rateToAdd * (time+0.0);
    }

    public long getTime(){
        //nel caso abbia estimatetime, ho appena creato questo hotpointcandidate dal locator. uso quella stime. Altrimenti uso il tempo caricato dal DB
        if(estimateTime!=0)
            return new Double(getEstimateTime()).longValue();

        return this.time;
    }


    public Hotpoint getHotpoint(){
        return this.hotpoint;
    }

    public String getHotpointName(){
        return this.hotpoint.getName();
    }

    public Double getRate(){
        return this.rate;
    }
}

package org.altervista.bertuz83.sgaget.business;

/**
 * User: bertuz
 * Project: sgaget
 *
 * java bean per rappresentazione OOP di un punto di interesse
 */
public class Hotpoint{
    private double latitude;
    private double longitude;
    private String name;

    public Hotpoint(double latitude, double longitude, String name){
        this.latitude= latitude;
        this.longitude= longitude;

        this.name= name;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public String getName(){
        return(this.name);
    }

    public void setName(String name){
        this.name= name;
    }

    public void setLatitude(double latitude){
        this.latitude= latitude;
    }

    public void setLongitude(double longitude){
        this.longitude= longitude;
    }
}
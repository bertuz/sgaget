package org.altervista.bertuz83.sgaget.business;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * User: bertuz
 * Project: sgaget
 *
 * java bean per rappresentazione OOP delle statistiche di spostamento personali da un punto di interesse a un altro punto.
 */
public class TransportationStatistics {
    private String da="";
    private String a="";

    private double bici= 0.0;
    private double autobus= 0.0;
    private double auto= 0.0;
    private double piedi= 0.0;
    private double altro= 0.0;

    private int nrAltro= 0;


    public double getMax() {
        double max= 0.0;

        if(bici > max)
            max= bici;
        if(autobus > max)
            max= autobus;
        if(auto > max)
            max= auto;
        if(piedi > max)
            max= piedi;
        if(getAltro() > max)
            max= getAltro();

        return max;
    }

    /**
     * @return nr of ms
     */
    public double getBici() {
        return bici;
    }

    public void setBici(double bici) {
        this.bici = bici;
    }

    public double getAutobus() {
        return autobus;
    }

    public void setAutobus(double autobus) {
        this.autobus = autobus;
    }

    public double getAuto() {
        return auto;
    }

    public void setAuto(double auto) {
        this.auto = auto;
    }

    public double getPiedi() {
        return piedi;
    }

    public void setPiedi(double piedi) {
        this.piedi = piedi;
    }

    public double getAltro() {
        if(nrAltro == 0)
            return 0;

        return ((0.0 + this.altro) / (nrAltro + 0.0));
    }

    public void setAltro(double altro) {
        nrAltro++;
        this.altro += altro;
    }

    public String getDa() {
        return da;
    }

    public void setDa(String da) {
        this.da = da;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }
}

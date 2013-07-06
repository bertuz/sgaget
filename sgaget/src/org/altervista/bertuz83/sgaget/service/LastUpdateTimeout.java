package org.altervista.bertuz83.sgaget.service;

import android.util.Log;

import java.util.Date;
import java.util.TimerTask;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Task su thread non UI che permette di aspettare 10 secondi dopo la richiesta di terminare il tracciamento.
 * Utile per lasciare un lasso di tempo ragionevole al GPS di ottenere il fixing dei satelliti e ottenere
 * un ultimo aggiornamento della locazione molto preciso.
 *
 * @see org.altervista.bertuz83.sgaget.service.Locator
 * @see org.altervista.bertuz83.sgaget.service.ServiceTracking
 */
public class LastUpdateTimeout extends TimerTask {
    Locator locator;
    long startTime;


    public LastUpdateTimeout(Locator locator) {
        this.locator= locator;
        this.startTime= new Date().getTime();
    }


    @Override
    public void run() {
        long actualTime= new Date().getTime();
        if(actualTime - startTime > 10000){
            if(locator.getState() == Locator.TERMINATING)
                locator.onLocationChanged(null);

            this.cancel();
        }
    }
}

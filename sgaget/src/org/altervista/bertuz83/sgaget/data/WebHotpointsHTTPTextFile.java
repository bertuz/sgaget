package org.altervista.bertuz83.sgaget.data;

import android.util.Log;
import org.altervista.bertuz83.sgaget.business.Hotpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Classe implementante l'interfaccia DAO per la lettura dei punti di interesse dal web.
 */
public class WebHotpointsHTTPTextFile implements WebHotpointsDAO{
    public static final int STEPVERSION= 0;
    public static final int GETNRHOTPOINTS= STEPVERSION + 1;
    public static final int ACQUIRINGHOTPOINTS= GETNRHOTPOINTS + 1;
    public static final int ENDACQUIRING= ACQUIRINGHOTPOINTS + 1;
    private int step= -1;

    private BufferedReader in;

    private int version, nrHotpoints;


    private void open() throws IOException{
        try{
            URL url= new URL("https://dl.dropboxusercontent.com/u/1889847/hotpoints.txt");
            in= new BufferedReader(new InputStreamReader(url.openStream()));
        }catch(MalformedURLException e){
            throw new IOException("Apertura file fallita");
        }
    }

    //in.close();

    @Override
    public int getVersion() throws IOException{
        if(this.step >= STEPVERSION)
            return version;

        this.step++;

        this.open();

        this.version= Integer.parseInt(in.readLine());
        return version;
    }

    @Override
    public int getNrHotpoints() throws IOException{
        if(this.step >= GETNRHOTPOINTS)
            return nrHotpoints;

        if(this.step < STEPVERSION)
            throw new IOException("chiedere prima la versione (getVersion)");

        this.step ++;
        this.nrHotpoints= Integer.parseInt(in.readLine());

        return nrHotpoints;
    }

    @Override
    public Hotpoint getNextHotpoint() throws IOException{
        if(this.step < GETNRHOTPOINTS)
            throw new IOException("chiamare prima getNrHotpoints");

        if(this.step == GETNRHOTPOINTS)
            this.step= ACQUIRINGHOTPOINTS;


        String tmp;
        if(this.step < ENDACQUIRING){
            if((tmp = in.readLine()) != null){
                double lat= Double.parseDouble(tmp);
                double lon= Double.parseDouble(in.readLine());
                String name= in.readLine();

                Hotpoint newHotpoint= new Hotpoint(lat,lon, name);

                return newHotpoint;
            }
            else{
                in.close();
                this.step++;
            }
        }

        return null;
    }
}

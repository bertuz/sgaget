package org.altervista.bertuz83.sgaget.helper;

import android.app.Application;
import android.content.Context;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Helper class per informazioni globali e per l'ottenimento del contesto dell'applicazione
 */
public class MyApplication extends Application {
    public static final boolean DEBUG= false;
    public static final int NOTIFICATION_TRACKING= 1;

    public static final double CENTERMAP_LAT= 46.069692;
    public static final double CENTERMAP_LONG= 11.121089;


    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApplication.context= getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

}

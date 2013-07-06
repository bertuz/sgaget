package org.altervista.bertuz83.sgaget;

import android.widget.TextView;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * User: bertuz
 * Project: sgaget
 */
public class MyTimerTaskFTabSpostamento extends TimerTask {
    private FTabSpostamentoActHome fragment;
    private long startTime= 0;


    public MyTimerTaskFTabSpostamento(FTabSpostamentoActHome fragment, long startTime){
        this.fragment= fragment;
        this.startTime= startTime;
    }


    @Override
    public void run() {
        fragment.getView().post(new Runnable() {
            @Override
            public void run() {
                long elapsedTime = new Date().getTime() - startTime;
                long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) -
                        TimeUnit.HOURS.toMinutes(hours);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) -
                        TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);
                String hoursStr = (hours < 10) ? "0" + hours : "" + hours;
                String minutesStr = (minutes < 10) ? "0" + minutes : "" + minutes;
                String secondsStr = (seconds < 10) ? "0" + seconds : "" + seconds;
                if(fragment.getView()!=null)
                    ((TextView) fragment.getView().findViewById(R.id.tab_spostamento_time)).setText(hoursStr + ":" + minutesStr + ":" + secondsStr);
            }
        });
    }
}

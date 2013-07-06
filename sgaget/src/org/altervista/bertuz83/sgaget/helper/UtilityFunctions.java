package org.altervista.bertuz83.sgaget.helper;

import android.widget.TextView;
import org.altervista.bertuz83.sgaget.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: bertuz
 * Project: sgaget
 *
 * Funzioni helper varie
 */
public class UtilityFunctions {

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }


    public static long getMinutesFromLong(long time){
        return TimeUnit.MILLISECONDS.toMinutes(time);
    }


    public static String getHourFromLong(long time){
        long hours = TimeUnit.MILLISECONDS.toHours(time);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) -
                TimeUnit.HOURS.toMinutes(hours);
        String hoursStr = (hours < 10) ? "0" + hours : "" + hours;
        String minutesStr = (minutes < 10) ? "0" + minutes : "" + minutes;
        return (hoursStr + ":" + minutesStr);
    }

    /**
     * in forma HH:mm:ss
     * @param time
     * @return ms
     */
    public static long getLongFromStrHHmmss(String time){
        long timeReturn;
        SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try{
            timeReturn= sdf.parse(time).getTime();
        }catch(Exception e){
            timeReturn= 0;
        }

        return timeReturn;
    }


    public static long getLongFromStrHHmm(String time){
        long timeReturn;
        SimpleDateFormat sdf= new SimpleDateFormat("HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try{
            timeReturn= sdf.parse(time).getTime();
        }catch(Exception e){
            timeReturn= 0;
        }

        return timeReturn;
    }

}

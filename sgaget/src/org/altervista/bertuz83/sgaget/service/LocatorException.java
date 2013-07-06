package org.altervista.bertuz83.sgaget.service;

/**
 * User: bertuz
 * Project: sgaget
 */
public class LocatorException extends Exception{
    public static final int ERROR_STILL_TRACKING= 0;
    public static final int ERROR_TOO_FEW_POINTS= 1;
    public static final int ERROR_ALREADY_STOPPED= 2;
    public static final int ERROR_POSITION_OUT_OF_RANGE= 3;
    public static final int UNEXPECTED= 4;

    private int errorCode;

    public LocatorException(int errorCode){
        super();
        this.errorCode= errorCode;
    }

    public int getErrorCode(){
        return this.errorCode;
    }
}

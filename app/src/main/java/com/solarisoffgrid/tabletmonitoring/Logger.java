package com.solarisoffgrid.tabletmonitoring;

import android.util.Log;


public class Logger {

    public static void debug(Class<?> cls, String message) {
        if(BuildConfig.DEBUG)
            Log.d(cls.getSimpleName(), "--------" + message);
    }

    public static void info(Class<?> cls, String message) {
        Log.e(cls.getSimpleName(), "--------" + message);
    }

    public static void error(Class<?> cls, String message, Exception e) {
        Log.e(cls.getSimpleName(), "--------" + message, e);
    }

    public static void warn(Class<?> cls, String message) {
        Log.w(cls.getName(), "--------" + message);
    }

    public static void error(Class<?> cls, CharSequence message) {
        Log.e(cls.getSimpleName(), "--------" + message);
    }
}
package com.avapira.bobroreader;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class DebugTimer {

    public static final String TAG = DebugTimer.class.getSimpleName();

    private static List<String> names = new LinkedList<>();
    private static List<Double> laps  = new LinkedList<>();
    static double lastLap;
    static double start;

    public static void start() {
        names.clear();
        laps.clear();
        lastLap = start = System.nanoTime();
    }

    public static void lap() {
        lap("");
    }

    public static void lap(String s) {
        laps.add(System.nanoTime() - lastLap);
        names.add(s);
        lastLap = System.nanoTime();
    }

    public static void logLaps() {
        for (int i = 0; i < laps.size() && i < names.size(); i++) {
            Log.d(TAG, String.format("%s : %s", names.get(i), laps.get(i) / 10e5));
        }
    }

}

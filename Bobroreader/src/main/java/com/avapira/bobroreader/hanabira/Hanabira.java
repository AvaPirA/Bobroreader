package com.avapira.bobroreader.hanabira;

/**
 *
 */
public class Hanabira {

    private static Hanabira flower = new Hanabira();

    public static Hanabira getFlower() {
        return flower;
    }

    public static HanabiraCache getCache() {
        return getFlower().cacheImpl;
    }

    private final HanabiraCache cacheImpl;

    private Hanabira() {
        cacheImpl = new ActiveCache();
    }
}

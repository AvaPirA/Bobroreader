package com.avapira.bobroreader.hanabira;

/**
 *
 */
public class Hanabira {

    private static Hanabira flower = new Hanabira();

    public static Hanabira getFlower() {
        return flower;
    }


    private Hanabira() {
    }
}

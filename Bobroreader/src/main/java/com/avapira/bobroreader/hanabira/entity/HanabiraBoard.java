package com.avapira.bobroreader.hanabira.entity;

import java.util.TreeSet;

/**
 *
 */
public class HanabiraBoard extends HanabiraEntity {

    private String          boardKey;
    private TreeSet<Thread> threads;
    private int             pages;
    private Object          capabilities;

}

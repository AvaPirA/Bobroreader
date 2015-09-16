package com.avapira.bobroreader.hanabira.entity;

/**
 * Mne jopa ot etih media-typov polihaet prosto!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
public enum HanabiraMediaType {
    CODE, VECTOR, MUSIC, PDF, TEXT, IMAGE, FLASH, ARCHIVE, VIDEO;

    public HanabiraMediaType parse(String s) {
        switch (s) {
            case "code":
                return CODE;
            case "vector":
                return VECTOR;
            case "music":
                return MUSIC;
            case "pdf":
                return PDF;
            case "text":
                return TEXT;
            case "image":
                return IMAGE;
            case "flash":
                return FLASH;
            case "archive":
                return ARCHIVE;
            case "video":
                return VIDEO;
            default:
                throw new IllegalArgumentException("Unsupported Hanabira media type");
        }
    }
}

package com.avapira.bobroreader.hanabira.entity;

/**
 *
 */
public class HanabiraFile {

    public enum Rating {
        SFW, R15, R18, R18G;
    }

    public enum Type {
        CODE, VECTOR, MUSIC, PDF, TEXT, IMAGE, FLASH, ARCHIVE, VIDEO;
    }

    private final String metadata;
    private final String src;
    private final int    thumbHeight;
    private final int    fileId;
    private final int    thumbWidth;
    private final Rating rating;
    private final int    size;
    private final Type   type;
    private final String thumb;

    public HanabiraFile(String metadata,
                        String src,
                        int thumbHeight,
                        int fileId,
                        int thumbWidth,
                        String rating,
                        int size,
                        String type,
                        String thumb) {
        this.metadata = metadata;
        this.src = src;
        this.thumbHeight = thumbHeight;
        this.fileId = fileId;
        this.thumbWidth = thumbWidth;
        this.rating = Rating.valueOf(rating.toUpperCase());
        this.size = size;
        this.type = Type.valueOf(type.toUpperCase());
        this.thumb = thumb;
    }

    public static abstract class Metadata {}
}

package com.avapira.bobroreader;

/**
 *
 */
public class AmbiguousId {
    private final int id;
    private final int displayId;
    private final String board;

    public AmbiguousId(int id) {
        this.id = id;
        this.displayId = 0;
        this.board = null;
    }

    public AmbiguousId(String board, int displayId) {
        this.id = 0;
        this.displayId = displayId;
        this.board = board;
    }

    public boolean isDisplay() {
        return displayId > 0;
    }

    public int getId() {
        return id;
    }

    public int getDisplayId() {
        return displayId;
    }

    public String getBoard() {
        return board;
    }

}

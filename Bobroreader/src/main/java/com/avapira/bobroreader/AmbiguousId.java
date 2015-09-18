package com.avapira.bobroreader;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */
public class AmbiguousId implements Parcelable {

    private final int    id;
    private final int    displayId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(board);
        if (isDisplay()) {
            dest.writeInt(-1 * displayId);
        } else {
            dest.writeInt(id);
        }
    }

    public AmbiguousId(Parcel in) {
        board = in.readString();
        int aid = in.readInt();
        if (aid > 0) {
            id = aid;
            displayId = 0;
        } else {
            id = 0;
            displayId = aid;
        }
    }
}

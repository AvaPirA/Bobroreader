/*
 * Bobroreader is open source software, created, maintained, and shared under
 * the MIT license by Avadend Piroserpen Arts. The project includes components
 * from other open source projects which remain under their existing licenses,
 * detailed in their respective source files.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015. Avadend Piroserpen Arts Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */
package com.avapira.bobroreader.hanabira.entity;

import com.google.gson.annotations.SerializedName;
import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class HanabiraPost extends HanabiraEntity {

    static class ModificationDateComparator implements Comparator<HanabiraPost> {

        @Override
        public int compare(HanabiraPost lhs, HanabiraPost rhs) {
            return lhs.modifiedDate.compareTo(rhs.modifiedDate);
        }
    }

    @SerializedName("display_id")
    private int           displayId;
    //    private List<File>    files;
    @SerializedName("last_modified")
    private LocalDateTime modifiedDate;
    @SerializedName("date")
    private LocalDateTime createdDate;
    @SerializedName("post_id")
    private int           postId;
    private String        message;
    private String        subject;
    @SerializedName("board_id")
    private int           boardId;
    private String        name;
    @SerializedName("thread_id")
    private int           threadId;
    private boolean       op;

    public int getBoardId() {
        return boardId;
    }

    public String getMessage() {
        return message;
    }

    public boolean equals(Object o) {
        return o instanceof HanabiraPost && ((HanabiraPost) o).postId == this.postId;
    }

    public boolean deepEquals(HanabiraPost p) {
        return displayId == p.displayId && postId == p.postId && boardId == p.boardId &&
                modifiedDate.equals(p.modifiedDate) &&
                createdDate.equals(p.createdDate) && message.equals(p.message) && subject.equals(p.subject) &&
                name == null ? p.name == null : name.equals(p.name) && threadId == p.threadId && op == p.op;
    }

    public String getName() {
        return name;
    }

    public boolean isOp() {
        return op;
    }

    public LocalDateTime getDate() {
        return createdDate;
    }

    public List getFiles() {
        return null;
    }

    public int getDisplayId() {
        return displayId;
    }
}



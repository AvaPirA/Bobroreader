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

    @SerializedName("display_id")
    private final int           displayId;
    @SerializedName("date")
    private final LocalDateTime createdDate;
    @SerializedName("post_id")
    private final int           postId;
    @SerializedName("board_id")
    private final int           boardId;
    @SerializedName("thread_id")
    private final int           threadId;
    private final boolean       op;
    //    private List<File>    files;
    @SerializedName("last_modified")
    private       LocalDateTime modifiedDate;
    private       String        message;
    private       String        subject;
    private       String        name;

    HanabiraPost(int displayId,
                 LocalDateTime modifiedDate,
                 LocalDateTime createdDate,
                 int postId,
                 String message,
                 String subject,
                 int boardId,
                 String name,
                 int threadId,
                 boolean op) {
        this.displayId = displayId;
        this.modifiedDate = modifiedDate;
        this.createdDate = createdDate;
        this.postId = postId;
        this.message = message;
        this.subject = subject;
        this.boardId = boardId;
        this.name = name;
        this.threadId = threadId;
        this.op = op;
    }

    private static class ModificationDateComparator implements Comparator<HanabiraPost> {

        @Override
        public int compare(HanabiraPost lhs, HanabiraPost rhs) {
            return lhs.modifiedDate.compareTo(rhs.modifiedDate);
        }
    }

    public int getBoardId() {
        return boardId;
    }

    public String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
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

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public int getPostId() {
        return postId;
    }

    public String getSubject() {
        return subject;
    }

    void setSubject(String subject) {
        this.subject = subject;
    }

    public int getThreadId() {
        return threadId;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public boolean isOp() {
        return op;
    }

    public List getFiles() {
        return null;
    }

    public int getDisplayId() {
        return displayId;
    }

    public boolean isUpToDate(LocalDateTime modifiedDate) {
        return modifiedDate.isEqual(modifiedDate);
    }

}



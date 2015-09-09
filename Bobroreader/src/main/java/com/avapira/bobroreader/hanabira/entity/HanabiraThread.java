package com.avapira.bobroreader.hanabira.entity;

import com.google.gson.annotations.SerializedName;
import org.joda.time.LocalDateTime;

import java.util.*;

/**
 *
 */
public class HanabiraThread extends HanabiraEntity {

    HanabiraThread(int dispayId,
                   int threadId,
                   LocalDateTime modifiedDate,
                   int postsCount,
                   int filesCount,
                   int boardId,
                   boolean archived,
                   String title,
                   boolean autosage,
                   LocalDateTime lastHit) {
        this.dispayId = dispayId;
        this.threadId = threadId;
        this.modifiedDate = modifiedDate;
        this.postsCount = postsCount;
        this.filesCount = filesCount;
        this.boardId = boardId;
        this.archived = archived;
        this.title = title;
        this.autosage = autosage;
        this.lastHit = lastHit;
        posts = new TreeMap<>();
    }

    @SerializedName("display_id")
    private final int           dispayId;
    @SerializedName("thread_id")
    private final int           threadId;
    @SerializedName("last_modified")
    private       LocalDateTime modifiedDate;
    @SerializedName("created")
    private       LocalDateTime createdDate;
    @SerializedName("posts_count")
    private       int           postsCount;
    @SerializedName("files_count")
    private       int           filesCount;
    @SerializedName("board_id")
    private final int           boardId;
    private       boolean       archived;
    private       String        title;
    private final boolean       autosage;
    @SerializedName("last_hit")
    private       LocalDateTime lastHit;

    private final TreeMap<LocalDateTime, Integer> posts;


    public boolean isUpToDate(LocalDateTime modifiedDate) {
        return modifiedDate.isEqual(modifiedDate);
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public void setFilesCount(int filesCount) {
        this.filesCount = filesCount;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void setLastHit(LocalDateTime lastHit) {
        this.lastHit = lastHit;
    }

    public int getDispayId() {
        return dispayId;
    }

    public int getThreadId() {
        return threadId;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public int getBoardId() {
        return boardId;
    }

    public boolean isArchived() {
        return archived;
    }

    public String getTitle() {
        return title;
    }

    public boolean isAutosage() {
        return autosage;
    }

    public LocalDateTime getLastHit() {
        return lastHit;
    }

    public TreeMap<LocalDateTime, Integer> getPosts() {
        return posts;
    }

    public List<Integer> getLastN(int want) {
        if (postsCount < 2) { return Collections.emptyList(); }
        int have = posts.size() - 1;
        int skip = want > have ? 0 : have - want;
        List<Integer> returnList = new ArrayList<>(Math.min(want, have));
        Iterator<Map.Entry<LocalDateTime, Integer>> read = posts.entrySet().iterator();
        while (skip-- >= 0) {
            read.next();
        }
        while (read.hasNext()) {
            returnList.add(read.next().getValue());
        }
        return returnList;
    }
}

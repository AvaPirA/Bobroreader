package com.avapira.bobroreader.hanabira.entity;

import com.google.gson.annotations.SerializedName;
import org.joda.time.LocalDateTime;

import java.util.*;

/**
 *
 */
public class HanabiraThread extends HanabiraEntity {

    @SerializedName("display_id")
    private final int                             displayId;
    @SerializedName("thread_id")
    private final int                             threadId;
    @SerializedName("board_id")
    private final int                             boardId;
    private final boolean                         autosage;
    /**
     * Maps date of post creation to it's (internal) ID
     */
    private final TreeMap<LocalDateTime, Integer> posts;
    @SerializedName("last_modified")
    private       LocalDateTime                   modifiedDate;
    @SerializedName("created")
    private       LocalDateTime                   createdDate;
    @SerializedName("posts_count")
    private       int                             postsCount;
    @SerializedName("files_count")
    private       int                             filesCount;
    private       boolean                         archived;
    private       String                          title;
    @SerializedName("last_hit")
    private       LocalDateTime                   lastHit;

    HanabiraThread(int displayId,
                   int threadId,
                   LocalDateTime modifiedDate,
                   int postsCount,
                   int filesCount,
                   int boardId,
                   boolean archived,
                   String title,
                   boolean autosage,
                   LocalDateTime lastHit) {
        this.displayId = displayId;
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

    public boolean isUpToDate(LocalDateTime modifiedDate) {
        return modifiedDate.isEqual(modifiedDate);
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public int getDisplayId() {
        return displayId;
    }

    public int getThreadId() {
        return threadId;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public void setFilesCount(int filesCount) {
        this.filesCount = filesCount;
    }

    public int getBoardId() {
        return boardId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAutosage() {
        return autosage;
    }

    public LocalDateTime getLastHit() {
        return lastHit;
    }

    public void setLastHit(LocalDateTime lastHit) {
        this.lastHit = lastHit;
    }

    public TreeMap<LocalDateTime, Integer> getPosts() {
        return posts;
    }

    /**
     * @return list of IDs of last {@code want} posts on this thread
     */
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

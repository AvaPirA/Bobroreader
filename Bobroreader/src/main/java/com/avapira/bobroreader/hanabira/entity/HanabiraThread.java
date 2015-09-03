package com.avapira.bobroreader.hanabira.entity;

import com.google.gson.annotations.SerializedName;
import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class HanabiraThread extends HanabiraEntity{

    static class ModificationDateComparator implements Comparator<HanabiraThread> {

        @Override
        public int compare(HanabiraThread lhs, HanabiraThread rhs) {
            return lhs.modifiedDate.compareTo(rhs.modifiedDate);
        }
    }

    @SerializedName("display_id")
    private int dispayId;   @SerializedName("thread_id")
    private int threadId;
    @SerializedName("last_modified")
    private LocalDateTime modifiedDate;
    @SerializedName("created")
    private LocalDateTime createdDate;
    @SerializedName("posts_count")
    private int           postsCount;
    @SerializedName("files_count")
    private int           filesCount;
    @SerializedName("board_id")
    private int           boardId;
    private boolean       archived;
    private String        title;
    private boolean       autosage;
    @SerializedName("last_hit")
    private LocalDateTime lastHit;

    private List<HanabiraPost> posts;


}

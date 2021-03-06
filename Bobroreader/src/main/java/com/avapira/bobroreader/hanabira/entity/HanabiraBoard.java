package com.avapira.bobroreader.hanabira.entity;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.avapira.bobroreader.Bober;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class HanabiraBoard extends HanabiraEntity {

    private final String                      boardKey;

    /**
     * Thread global IDs (not display)
     */
    private final Map<Integer, List<Integer>> pages;
    private       int                         pagesCount;
    private       Object                      capabilities;

    public HanabiraBoard(String boardKey, int pagesCount, Object capabilities) {
        this.capabilities = capabilities;
        this.pagesCount = pagesCount;
        this.boardKey = boardKey;
        this.pages = new HashMap<>();
    }

    public static class Info {

        private static final Map<String, Info>  boardToInfo   = new HashMap<>();
        private static final Map<Integer, Info> idToInfo      = new HashMap<>();
        private static final Pattern            removeSlashes = Pattern.compile("^/([a-z]{1,4})/$");
        private static List<Info>              boardsInfoStorage;
        @SerializedName("allow_names")
        public         boolean                 allowNames;
        @SerializedName("require_thread_file")
        public         boolean                 reqThreadFile;
        @SerializedName("description")
        public         String                  description;
        @SerializedName("require_captcha")
        public         boolean                 reqCaptcha;
        @SerializedName("restrict_read")
        public         boolean                 restrictRead;
        @SerializedName("allow_files")
        public         boolean                 allowFiles;
        @SerializedName("require_post_file")
        public         boolean                 reqPostFile;
        @SerializedName("allowed_filetypes")
        public         List<HanabiraMediaType> allowedFiletypes;
        @SerializedName("remember_name")
        public         boolean                 rememberName;
        @SerializedName("require_new_file")
        public         boolean                 reqNewFile;
        @SerializedName("allow_OP_moderation")
        public         boolean                 allowOpModeration;
        @SerializedName("allow_custom_restricts")
        public         boolean                 allowCustomRestricts;
        @SerializedName("id")
        public         int                     id;
        @SerializedName("files_max_qty")
        public         int                     filesMaxQty;
        @SerializedName("restrict_trip")
        public         boolean                 restrictTrip;
        @SerializedName("delete_thread_post_limit")
        public         int                     deleteThreadPostLimit;
        @SerializedName("title")
        public         String                  title;
        @SerializedName("file_max_res")
        public         int                     fileMaxRes;
        @SerializedName("archive")
        public         boolean                 archive;
        @SerializedName("restrict_new_reply")
        public         boolean                 restrictNewReply;
        @SerializedName("allow_delete_threads")
        public         boolean                 allowDeleteThreads;
        @SerializedName("file_max_size")
        public         int                     fileMaxSize;
        @SerializedName("restrict_new_thread")
        public         boolean                 restrictNewThread;
        @SerializedName("bump_limit")
        public         int                     bumpLimit;
        @SerializedName("keep_filenames")
        public         boolean                 keepFilenames;
        @SerializedName("board")
        public         String                  boardKey;

        public static void loadBoardsInfo(Resources res, int id) {
            if (isLoaded()) { return; }
            // read json
            String json = Bober.rawJsonToString(res, id);
            boardsInfoStorage = new Gson().fromJson(json, new TypeToken<List<Info>>() {}.getType());
            // index data
            for (Info info : boardsInfoStorage) {
                boardToInfo.put(info.boardKey, info);
                idToInfo.put(info.id, info);
            }
        }

        public static boolean isLoaded() {
            return boardsInfoStorage != null;
        }

        public static Info getForId(int id) {
            return idToInfo.get(id);
        }

        public static Info getForKey(@NonNull String s) {
            return boardToInfo.get(s);
        }

        public static Info getForSlashed(@NonNull String s) {
            return boardToInfo.get(cutSlashes(s));
        }

        public static int getIdForKey(@NonNull String boardKey) {
            return getForKey(boardKey).id;
        }

        public static String getKeyForId(int id) {
            return getForId(id).boardKey;
        }

        public static String cutSlashes(@NonNull String slashed) {
            Matcher m = removeSlashes.matcher(slashed);
            if (m.find()) {
                return m.group(1);
            } else { throw new IllegalStateException("Wrong slashed board name"); }
        }
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void update(int pagesCount, Object capabilities) {
        this.pagesCount = pagesCount;
        this.capabilities = capabilities;
    }

    public Info getInfo() {
        return Info.getForKey(boardKey);
    }

    Map<Integer, List<Integer>> getPages() {
        return pages;
    }

    public List<Integer> getPage(int num) {
        return pages.get(num);
    }

    public List<HanabiraThread> getPageThreads(int num) {
        List<HanabiraThread> threads = new ArrayList<>();
        List<Integer> page = getPage(num);
        if (page == null) {
            return Collections.emptyList();
        }
        for (Integer threadId : page) {
            threads.add(Hanabira.getStem().findThreadById(threadId));
        }
        return threads;
    }

    public List<Integer> updatePage(int page, List<Integer> threadsOnPage) {
        return pages.put(page, threadsOnPage);
    }

    public String getKey() {
        return boardKey;
    }
}

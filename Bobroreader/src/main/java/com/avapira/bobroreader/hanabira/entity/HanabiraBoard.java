package com.avapira.bobroreader.hanabira.entity;

import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class HanabiraBoard {

    private static List<HanabiraBoard> boards;
    private static final Map<String, HanabiraBoard>  boardToBoard = new HashMap<>();
    private static final Map<Integer, HanabiraBoard> idToBoard    = new HashMap<>();
//    private static final Map<String, HanabiraBoard> boardToBoard = new HashMap<>();
//    private static final Map<String, HanabiraBoard> boardToBoard = new HashMap<>();
//    private static final Map<String, HanabiraBoard> boardToBoard = new HashMap<>();

    public static void loadBoards(String json) {
        if (isLoaded()) {
            throw new IllegalStateException("Boards already loaded");
        }
        List<HanabiraBoard> boards = new Gson().fromJson(json, new TypeToken<List<HanabiraBoard>>() {}.getType());
        for (HanabiraBoard board : boards) {
            boardToBoard.put(board.board, board);
            idToBoard.put(board.id, board);
        }
    }

    public static boolean isLoaded() {
        return boards != null;
    }

    public static HanabiraBoard getForId(int id) {
        return idToBoard.get(id);
    }

    public static HanabiraBoard getForBoard(@NonNull String s) {
        return boardToBoard.get(s);
    }

    public static HanabiraBoard getForSlashed(@NonNull String s) {
        return boardToBoard.get(cutSlashes(s));
    }

    private static final Pattern removeSlashes = Pattern.compile("^/([a-z]{1,4})/$");

    public static String cutSlashes(@NonNull String slashed) {
        Matcher m = removeSlashes.matcher(slashed);
        if (m.find()) {
            return m.group(1);
        } else { throw new IllegalStateException("Wrong slashed board name"); }
    }

    @SerializedName("allow_names")
    private boolean                 allowNames;
    @SerializedName("require_thread_file")
    private boolean                 reqThreadFile;
    @SerializedName("description")
    private String                  description;
    @SerializedName("require_captcha")
    private boolean                 reqCaptcha;
    @SerializedName("restrict_read")
    private boolean                 restrictRead;
    @SerializedName("allow_files")
    private boolean                 allowFiles;
    @SerializedName("require_post_file")
    private boolean                 reqPostFile;
    @SerializedName("allowed_filetypes")
    private List<HanabiraMediaType> allowedFiletypes;
    @SerializedName("remember_name")
    private boolean                 rememberName;
    @SerializedName("require_new_file")
    private boolean                 reqNewFile;
    @SerializedName("allow_OP_moderation")
    private boolean                 allowOpModeration;
    @SerializedName("allow_custom_restricts")
    private boolean                 allowCustomRestricts;
    @SerializedName("id")
    private int                     id;
    @SerializedName("files_max_qty")
    private int                     filesMaxQty;
    @SerializedName("restrict_trip")
    private boolean                 restrictTrip;
    @SerializedName("delete_thread_post_limit")
    private int                     deleteThreadPostLimit;
    @SerializedName("title")
    private String                  title;
    @SerializedName("file_max_res")
    private int                     fileMaxRes;
    @SerializedName("archive")
    private boolean                 archive;
    @SerializedName("restrict_new_reply")
    private boolean                 restrictNewReply;
    @SerializedName("allow_delete_threads")
    private boolean                 allowDeleteThreads;
    @SerializedName("file_max_size")
    private int                     fileMaxSize;
    @SerializedName("restrict_new_thread")
    private boolean                 restrictNewThread;
    @SerializedName("bump_limit")
    private int                     bumpLimit;
    @SerializedName("keep_filenames")
    private boolean                 keepFilenames;
    @SerializedName("board")
    private String                  board;

    public String getBoard() {
        return board;
    }
}

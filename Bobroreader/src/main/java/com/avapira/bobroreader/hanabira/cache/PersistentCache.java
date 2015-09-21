package com.avapira.bobroreader.hanabira.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.CallSuper;
import android.util.Log;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class PersistentCache implements HanabiraCache {

    public static final String TAG = PersistentCache.class.getSimpleName();
    private SQLiteDatabase db;

    public PersistentCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    db = new Dam(Hanabira.getFlower()).getWritableDatabase();
                } catch (SQLiteException e) {
                    Log.w("PersistentCache", "Can not connect to the database");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public class Dam extends SQLiteOpenHelper {

        private static final String DB_NAME                   = "VeryStronkBeawerDam";
        private static final int    DB_VERSION                = 1;
        private static final String TAG                       = "DB controller";
        private static final String CREATE_TABLE_BOARDS_PAGES = "CREATE TABLE BoardsPages (" +
                "id INTEGER," +
                "page_num INTEGER," +
                "threads TEXT);";
        private static final String CREATE_TABLE_BOARDS       = "CREATE TABLE Boards (" +
                "id INTEGER PRIMARY KEY," +
                "pages_count INTEGER";
        private static final String CREATE_TABLE_THREADS      = "CREATE TABLE Threads (" +
                "display_id INTEGER," +
                "thread_id INTEGER PRIMARY_KEY," +
                "board_id INTEGER," +
                "autosage INTEGER," +
                "last_modified INTEGER," +
                "created INTEGER," +
                "posts_count INTEGER," +
                "files_count INTEGER," +
                "archived INTEGER," +
                "title TEXT," +
                "last_hit INTEGER );";
        private static final String CREATE_TABLE_POSTS        = "CREATE TABLE Posts (" +
                "display_id INTEGER," +
                "date INTEGER," +
                "post_id INTEGER PRIMARY_KEY," +
                "board_id INTEGER," +
                "thread_id INTEGER," +
                "op INTEGER," +
                "files INTEGER," +
                "last_modified INTEGER," +
                "message TEXT," +
                "subject TEXT," +
                "name TEXT);";
        private static final String CREATE_TABLE_FILES        = "CREATE TABLE Files (" +
                "metadata TEXT," +
                "src TEXT," +
                "thumb_height INTEGER," +
                "file_id INTEGER," +
                "thumb_width INTEGER," +
                "rating INTEGER," +
                "size INTEGER," +
                "type INTEGER," +
                "thumb TEXT";

        public Dam(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            db.execSQL(CREATE_TABLE_BOARDS);
            db.execSQL(CREATE_TABLE_THREADS);
            db.execSQL(CREATE_TABLE_POSTS);
            db.execSQL(CREATE_TABLE_FILES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Database to be upgraded");
            clearCache(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Database to be downgraded");
            clearCache(db);
        }

        public void clearCache(SQLiteDatabase db) {
            Log.w(TAG, "CLEARING ALL PERSISTENT CACHES");
            db.execSQL("drop table Boards");
            db.execSQL("drop table Threads");
            db.execSQL("drop table Posts");
            db.execSQL("drop table Files");
        }
    }

    private static String boardPagesToDb(Map<Integer, List<Integer>> pages)

    @Override
    @CallSuper
    public HanabiraBoard findBoardByKey(String boardKey) {
        if (db == null) { return null; }
        int boardId = HanabiraBoard.Info.getIdForKey(boardKey);
        int pagesCount = 0; // todo db.getPagesCount()
        HanabiraBoard board = new HanabiraBoard(boardKey, pagesCount, null);
        //todo board.setPages
        return board;
    }

    @Override
    @CallSuper
    public HanabiraThread findThreadById(int threadId) {
        if (db == null) { return null; }
    }

    @Override
    @CallSuper
    public HanabiraPost findPostById(int postId) {
        if (db == null) { return null; }
    }

    @Override
    public HanabiraThread findThreadByDisplayId(String boardKey, int threadDisplayId) {
        if (db == null) { return null; }
    }

    @Override
    public HanabiraPost findPostByDisplayId(String boardKey, int postDisplayId) {
        if (db == null) { return null; }
    }

    @Override
    @CallSuper
    public void saveBoard(HanabiraBoard board) {
        if (db == null) { return; }
    }

    @Override
    @CallSuper
    public void saveThread(HanabiraThread thread, String boardKey) {
        if (db == null) { return; }
    }

    @Override
    @CallSuper
    public void savePost(HanabiraPost cachedPost, String boardKey) {
        if (db == null) { return; }
    }

    @Override
    @CallSuper
    public void savePost(HanabiraPost cachedPost, String boardKey) {
        if (db == null) { return; }
    }
}
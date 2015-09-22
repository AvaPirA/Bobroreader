package com.avapira.bobroreader.hanabira.cache;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.CallSuper;
import android.util.Log;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Override
    @CallSuper
    public HanabiraBoard findBoardByKey(String boardKey) {
        if (db == null) { return null; }
        String[] idArg = new String[]{Integer.toString(HanabiraBoard.Info.getIdForKey(boardKey))};
        Cursor c = db.rawQuery("SELECT (pages_count) FROM Boards WHERE ?=id", idArg);
        if (c.getCount() != 1) {
            throw new SQLiteException("Wrong return cursor size for <select page_num>: " + c.getCount());
        }
        final int pageCount = c.getInt(0);
        c.close();
        HanabiraBoard retBoard = new HanabiraBoard(boardKey, pageCount, null);

        c = db.rawQuery("SELECT (page_num, threads) FROM BoardPages WHERE ?=id", idArg);
        if (c.moveToFirst()) {
            do {
                List<Integer> threadsList = new ArrayList<>();
                int page = c.getInt(0);
                String[] threads = c.getString(1).split(";");
                for (String s : threads) {
                    threadsList.add(Integer.parseInt(s));
                }
                retBoard.updatePage(page, threadsList);
            } while (!c.isLast());
        }
        return retBoard;
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
        Set<Integer> pageKeys = board.getPages().keySet();
        for (int pageKey : pageKeys) {
            List<Integer> page = board.getPage(pageKey);
            StringBuilder sb = new StringBuilder();
            for (int threadId : page) {
                sb.append(threadId);
                sb.append(';');
            }
            sb.deleteCharAt(sb.length() - 1);
            db.execSQL(String.format("INSERT (id, page_num, threads) INTO BoardPages VALUES (%s,%s,%s)", board.getKey(),
                                     pageKey, sb.toString()));
        }
    }

    @Override
    @CallSuper
    public void saveThread(HanabiraThread thread, String boardKey) {
        if (db == null) { return; }
    }

}
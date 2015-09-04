package com.avapira.bobroreader.hanabira;

import android.util.Log;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Cache {

    private static final String TAG = Cache.class.getSimpleName();

    public static HanabiraBoard findBoardByKey(String boardKey) {
        HanabiraBoard retVal = indexedBoards.get(boardKey);
        if (retVal != null) {
            Log.d(TAG, "board found by id=" + boardKey);
            return retVal;
        } else {
            Log.d(TAG, "board NOT FOUND by id=" + boardKey);
            return null;
        }
    }

    public static HanabiraThread findThreadById(int threadId) {
        HanabiraThread retVal = indexedThreads.get(threadId);
        if (retVal != null) {
            Log.d(TAG, "thread found by id=" + threadId);
            return retVal;
        } else {
            Log.d(TAG, "thread NOT FOUND by id=" + threadId);
            return null;
        }
    }

    public static HanabiraPost finPostById(int postId) {
        HanabiraPost retVal = indexedPosts.get(postId);
        if (retVal != null) {
            Log.d(TAG, "post found by id=" + postId);
            return retVal;
        } else {
            Log.d(TAG, "post NOT FOUND by id=" + postId);
            return null;
        }
    }

    public static HanabiraThread findThreadByDisplayId(int threadDisplayId) {
        HanabiraThread retVal = indexedThreadsDisplay.get(threadDisplayId);
        if (retVal != null) {
            Log.d(TAG, "thread found by display_id=" + threadDisplayId);
            return retVal;
        } else {
            Log.d(TAG, "thread NOT FOUND by display_id=" + threadDisplayId);
            return null;
        }
    }

    public static HanabiraPost finPostByDisplayId(int postDisplayId) {
        HanabiraPost retVal = indexedPostsDisplay.get(postDisplayId);
        if (retVal != null) {
            Log.d(TAG, "post found by display_id=" + postDisplayId);
            return retVal;
        } else {
            Log.d(TAG, "post NOT FOUND by display_id=" + postDisplayId);
            return null;
        }
    }


    public static void saveBoard(HanabiraBoard board) {
        indexedBoards.put(board.getKey(), board);
    }

    public static void saveThread(HanabiraThread thread) {
        indexedThreads.put(thread.getThreadId(), thread);
        indexedThreadsDisplay.put(thread.getDispayId(), thread);
    }

    public static void savePost(HanabiraPost cachedPost) {
        indexedPosts.put(cachedPost.getPostId(), cachedPost);
        indexedPostsDisplay.put(cachedPost.getDisplayId(), cachedPost);
    }


    private static final Map<String, HanabiraBoard>   indexedBoards  = new HashMap<>();
    private static final Map<Integer, HanabiraThread> indexedThreads = new HashMap<>();
    private static final Map<Integer, HanabiraPost>   indexedPosts   = new HashMap<>();

    private static final Map<Integer, HanabiraThread> indexedThreadsDisplay = new HashMap<>();
    private static final Map<Integer, HanabiraPost>   indexedPostsDisplay   = new HashMap<>();

}

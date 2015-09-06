package com.avapira.bobroreader.hanabira.cache;

import android.content.Context;
import android.util.Log;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ActiveCache extends PersistentCache implements HanabiraCache {

    private static final String TAG = ActiveCache.class.getSimpleName();

    public ActiveCache(Context context) {

    }

    public HanabiraBoard findBoardByKey(String boardKey) {
        HanabiraBoard retVal = indexedBoards.get(boardKey);
        if (retVal != null) {
            Log.d(TAG, "board found by id=" + boardKey);
            return retVal;
        } else {
            Log.d(TAG, "board NOT FOUND by id=" + boardKey);
            return super.findBoardByKey(boardKey);
        }
    }

    public HanabiraThread findThreadById(int threadId) {
        HanabiraThread retVal = indexedThreads.get(threadId);
        if (retVal != null) {
            Log.d(TAG, "thread found by id=" + threadId);
            return retVal;
        } else {
            Log.d(TAG, "thread NOT FOUND by id=" + threadId);
            return super.findThreadById(threadId);
        }
    }

    public HanabiraPost findPostById(int postId) {
        HanabiraPost retVal = indexedPosts.get(postId);
        if (retVal != null) {
            Log.d(TAG, "post found by id=" + postId);
            return retVal;
        } else {
            Log.d(TAG, "post NOT FOUND by id=" + postId);
            return super.findPostById(postId);
        }
    }

    public HanabiraThread findThreadByDisplayId(int threadDisplayId) {
        HanabiraThread retVal = indexedThreadsDisplay.get(threadDisplayId);
        if (retVal != null) {
            Log.d(TAG, "thread found by display_id=" + threadDisplayId);
            return retVal;
        } else {
            Log.d(TAG, "thread NOT FOUND by display_id=" + threadDisplayId);
            return super.findThreadByDisplayId(threadDisplayId);
        }
    }

    public HanabiraPost findPostByDisplayId(int postDisplayId) {
        HanabiraPost retVal = indexedPostsDisplay.get(postDisplayId);
        if (retVal != null) {
            Log.d(TAG, "post found by display_id=" + postDisplayId);
            return retVal;
        } else {
            Log.d(TAG, "post NOT FOUND by display_id=" + postDisplayId);
            return super.findPostByDisplayId(postDisplayId);
        }
    }


    public void saveBoard(HanabiraBoard board) {
        indexedBoards.put(board.getKey(), board);
        super.saveBoard(board);
    }

    public void saveThread(HanabiraThread thread) {
        indexedThreads.put(thread.getThreadId(), thread);
        indexedThreadsDisplay.put(thread.getDispayId(), thread);
        super.saveThread(thread);
    }

    public void savePost(HanabiraPost cachedPost) {
        indexedPosts.put(cachedPost.getPostId(), cachedPost);
        indexedPostsDisplay.put(cachedPost.getDisplayId(), cachedPost);
        findThreadById(cachedPost.getThreadId()).getPosts().put(cachedPost.getCreatedDate(), cachedPost.getDisplayId());
        super.savePost(cachedPost);
    }


    private final Map<String, HanabiraBoard>   indexedBoards  = new HashMap<>();
    private final Map<Integer, HanabiraThread> indexedThreads = new HashMap<>();
    private final Map<Integer, HanabiraPost>   indexedPosts   = new HashMap<>();

    private final Map<Integer, HanabiraThread> indexedThreadsDisplay = new HashMap<>();
    private final Map<Integer, HanabiraPost>   indexedPostsDisplay   = new HashMap<>();

}

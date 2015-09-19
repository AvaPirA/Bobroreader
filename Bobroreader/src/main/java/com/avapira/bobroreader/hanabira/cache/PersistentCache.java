package com.avapira.bobroreader.hanabira.cache;

import android.support.annotation.CallSuper;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public class PersistentCache implements HanabiraCache {

    public static final String TAG = PersistentCache.class.getSimpleName();

    @Override
    public void asyncParse(Iterable<Integer> threads, int recentDepth) {
        //do nothing
    }

    @Override
    public void asyncParse(Iterable<Integer> threads) {
        //do nothing
    }

    @Override
    @CallSuper
    public HanabiraBoard findBoardByKey(String boardKey) {
        return null;
    }

    @Override
    @CallSuper
    public HanabiraThread findThreadById(int threadId) {
        return null;
    }

    @Override
    @CallSuper
    public HanabiraPost findPostById(int postId) {
        return null;
    }

    @Override
    public HanabiraThread findThreadByDisplayId(String boardKey, int threadDisplayId) {
        return null;
    }

    @Override
    public HanabiraPost findPostByDisplayId(String boardKey, int postDisplayId) {
        return null;
    }

    @Override
    public CharSequence getParsedPost(int postDisplayId) {
        return null;
    }

    @Override
    @CallSuper
    public void saveBoard(HanabiraBoard board) {

    }

    @Override
    @CallSuper
    public void saveThread(HanabiraThread thread, String boardKey) {

    }

    @Override
    @CallSuper
    public void savePost(HanabiraPost cachedPost, String boardKey) {

    }
}

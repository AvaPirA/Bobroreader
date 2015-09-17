package com.avapira.bobroreader.hanabira.cache;

import android.support.annotation.CallSuper;
import android.util.Log;
import com.avapira.bobroreader.AmbiguousId;
import com.avapira.bobroreader.Castor;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public class PersistentCache extends HanabiraCache {

    public static final String TAG = PersistentCache.class.getSimpleName();

    public PersistentCache(Castor castor) {
        super(castor);
        Log.d(TAG, "Create");
    }

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
    public HanabiraThread findThread(AmbiguousId id) {
        return null;
    }

    @Override
    @CallSuper
    public HanabiraPost findPostById(int postId) {
        return null;
    }

    @Override
    public HanabiraPost findPost(AmbiguousId id) {
        return null;
    }

    @Override
    @CallSuper
    public HanabiraThread findThreadByDisplayId(int threadDisplayId) {
        return null;
    }

    @Override
    @CallSuper
    public HanabiraPost findPostByDisplayId(int postDisplayId) {
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
    public void saveThread(HanabiraThread thread) {

    }

    @Override
    @CallSuper
    public void savePost(HanabiraPost cachedPost) {

    }
}

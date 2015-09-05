package com.avapira.bobroreader.hanabira;

import android.support.annotation.CallSuper;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public class PersistentCache implements HanabiraCache {
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

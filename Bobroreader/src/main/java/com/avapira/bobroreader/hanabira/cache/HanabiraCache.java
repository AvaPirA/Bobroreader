package com.avapira.bobroreader.hanabira.cache;

import com.avapira.bobroreader.Castor;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public abstract class HanabiraCache {

    protected final Castor castor;

    public HanabiraCache(Castor castor) {this.castor = castor;}

    public abstract void asyncParse(Iterable<Integer> threads, int recentDepth);
    public abstract void asyncParse(Iterable<Integer> threads);
    public abstract HanabiraBoard findBoardByKey(String boardKey);
    public abstract HanabiraThread findThreadById(int threadId);
    public abstract HanabiraPost findPostById(int postId);
    /**
     * Should be used only if internal id is unable or hard to obtain
     */
    public abstract HanabiraThread findThreadByDisplayId(String boardKey, int threadDisplayId);
    /**
     * Should be used only if internal id is unable or hard to obtain
     */
    public abstract HanabiraPost findPostByDisplayId(String boardKey, int postDisplayId);
    public abstract CharSequence getParsedPost(int postDisplayId);
    public abstract void saveBoard(HanabiraBoard board);
    public abstract void saveThread(HanabiraThread thread, String boardKey);
    public abstract void savePost(HanabiraPost cachedPost, String boardKey);
}

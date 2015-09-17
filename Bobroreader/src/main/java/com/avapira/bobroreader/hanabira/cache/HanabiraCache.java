package com.avapira.bobroreader.hanabira.cache;

import com.avapira.bobroreader.AmbiguousId;
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
    public abstract HanabiraThread findThread(AmbiguousId id);
    public abstract HanabiraPost findPostById(int postId);
    public abstract HanabiraPost findPost(AmbiguousId id);
    public abstract HanabiraThread findThreadByDisplayId(int threadDisplayId);
    public abstract HanabiraPost findPostByDisplayId(int postDisplayId);
    public abstract CharSequence getParsedPost(int postDisplayId);
    public abstract void saveBoard(HanabiraBoard board);
    public abstract void saveThread(HanabiraThread thread);
    public abstract void savePost(HanabiraPost cachedPost);
}

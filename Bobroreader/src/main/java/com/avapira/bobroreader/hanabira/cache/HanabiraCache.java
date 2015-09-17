package com.avapira.bobroreader.hanabira.cache;

import android.content.Context;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public abstract class HanabiraCache {

    protected final Context context;

    public HanabiraCache(Context context) {this.context = context;}

    public abstract void asyncParse(Iterable<Integer> threads, int recentDepth);
    public abstract void asyncParse(Iterable<Integer> threads);
    public abstract HanabiraBoard findBoardByKey(String boardKey);
    public abstract HanabiraThread findThreadById(int threadId);
    public abstract HanabiraPost findPostById(int postId);
    public abstract HanabiraThread findThreadByDisplayId(int threadDisplayId);
    public abstract HanabiraPost findPostByDisplayId(int postDisplayId);
    public abstract CharSequence getParsedPost(int postDisplayId);
    public abstract void saveBoard(HanabiraBoard board);
    public abstract void saveThread(HanabiraThread thread);
    public abstract void savePost(HanabiraPost cachedPost);
}

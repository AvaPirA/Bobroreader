package com.avapira.bobroreader.hanabira.cache;

import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public interface HanabiraCache {

    HanabiraBoard findBoardByKey(String boardKey);
    HanabiraThread findThreadById(int threadId);
    HanabiraPost findPostById(int postId);
    /**
     * Should be used only if internal id is unable or hard to obtain
     */
    HanabiraThread findThreadByDisplayId(String boardKey, int threadDisplayId);
    /**
     * Should be used only if internal id is unable or hard to obtain
     */
    HanabiraPost findPostByDisplayId(String boardKey, int postDisplayId);
    void saveBoard(HanabiraBoard board);
    void saveThread(HanabiraThread thread, String boardKey);
    void savePost(HanabiraPost cachedPost, String boardKey);
}

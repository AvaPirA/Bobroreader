package com.avapira.bobroreader.hanabira;

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
    HanabiraThread findThreadByDisplayId(int threadDisplayId);
    HanabiraPost findPostByDisplayId(int postDisplayId);
    void saveBoard(HanabiraBoard board);
    void saveThread(HanabiraThread thread);
    void savePost(HanabiraPost cachedPost);
}

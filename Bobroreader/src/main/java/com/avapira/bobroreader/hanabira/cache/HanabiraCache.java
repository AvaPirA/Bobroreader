package com.avapira.bobroreader.hanabira.cache;

import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public interface HanabiraCache {
    void asyncParse(Iterable<Integer> threads, int recentDepth);
    void asyncParse(Iterable<Integer> threads);
    HanabiraBoard findBoardByKey(String boardKey);
    HanabiraThread findThreadById(int threadId);
    HanabiraPost findPostById(int postId);
    HanabiraThread findThreadByDisplayId(int threadDisplayId);
    HanabiraPost findPostByDisplayId(int postDisplayId);
    CharSequence getParsedPost(int postDisplayId);
    void saveBoard(HanabiraBoard board);
    void saveThread(HanabiraThread thread);
    void savePost(HanabiraPost cachedPost);
}

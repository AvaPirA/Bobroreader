package com.avapira.bobroreader.hanabira;

import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public class Cache {

    public static HanabiraThread findThreadById(int thread_id) {
        throw new UnsupportedOperationException();
    }

    public static HanabiraPost finPostById(int postId) {
        throw new UnsupportedOperationException();
    }

    public static void savePost(HanabiraPost cachedPost) {
        // todo Save to index and putIfAbsent to parent thread
        // todo {if (post.isOP) thread.setCreatedDate(post.createdDate();)}
        throw new UnsupportedOperationException();
    }

    public static void saveThread(HanabiraThread thread) {
        throw new UnsupportedOperationException();
    }
}

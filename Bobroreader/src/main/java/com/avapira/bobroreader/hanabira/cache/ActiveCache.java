package com.avapira.bobroreader.hanabira.cache;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.HanabiraParser;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ActiveCache extends PersistentCache implements HanabiraCache {

    private static final String                                    TAG                   = "RAM cache";
    private final        Map<Integer, CharSequence>                cachedParsedPosts     = new HashMap<>();
    private final        Map<String, HanabiraBoard>                indexedBoards         = new HashMap<>();
    private final        Map<Integer, HanabiraThread>              indexedThreads        = new HashMap<>();
    private final        Map<Integer, HanabiraPost>                indexedPosts          = new HashMap<>();
    private final        Map<String, Map<Integer, HanabiraThread>> indexedThreadsDisplay = new HashMap<>();
    private final        Map<String, Map<Integer, HanabiraPost>>   indexedPostsDisplay   = new HashMap<>();

    private class AsyncHanabiraParser implements Runnable {

        private final Iterable<Integer> postsToParse;
        private final Iterable<Integer> threadsToParse;
        private final int               previewsToParse;

        private AsyncHanabiraParser(Iterable<Integer> threadsToParse, int previewsToParse) {
            this.postsToParse = null;
            this.threadsToParse = threadsToParse;
            this.previewsToParse = previewsToParse;
        }

        private AsyncHanabiraParser(Iterable<Integer> postsToParse) {
            this.postsToParse = postsToParse;
            threadsToParse = null;
            previewsToParse = -1;
        }

        public void run() {
            Log.w(TAG, Thread.currentThread().toString() + " started parsing");
            double start = System.nanoTime();
            if (postsToParse != null) {
                for (int pdi : postsToParse) {
                    cachePost(pdi);
                }
            }
            if (threadsToParse != null) {
                for (int threadId : threadsToParse) {
                    cachePost(Hanabira.getStem().findThreadById(threadId).getPosts().firstEntry().getValue());
                    // OP posts is first priority
                }
                for (int threadId : threadsToParse) {
                    for (Integer postIds : findThreadById(threadId).getLastN(previewsToParse)) {
                        cachePost(postIds);
                    }
                    // previews posts is second priority
                }
            }
            Log.w(TAG, Thread.currentThread().toString() + " completed parsing for " +
                    ((System.nanoTime() - start) / 10e5) + "ms");
        }

        private void cachePost(int postId) {
            synchronized (cachedParsedPosts) { // not way 'if(contains) return;' because mby it's edited reloaded post
                HanabiraPost post = findPostById(postId);
                cachedParsedPosts.put(post.getPostId(), new HanabiraParser(post).getFormatted());
            }
        }
    }

    public void asyncParse(Iterable<Integer> threads, int recentDepth) {
        new Thread(new AsyncHanabiraParser(threads, recentDepth)).start();
    }

    public void asyncParse(Iterable<Integer> threads) {
        new Thread(new AsyncHanabiraParser(threads)).start();
    }

    public HanabiraBoard findBoardByKey(String boardKey) {
        HanabiraBoard retVal = indexedBoards.get(boardKey);
        if (retVal != null) {
            return retVal;
        } else {
            retVal = super.findBoardByKey(boardKey);
            activeCacheInternalSaveBoard(retVal);
            return retVal;
        }
    }

    @Override
    public HanabiraThread findThreadByDisplayId(@NonNull String boardKey, int threadDisplayId)
    throws NullPointerException {
        return indexedThreadsDisplay.get(boardKey).get(threadDisplayId);
    }

    @Override
    public HanabiraPost findPostByDisplayId(@NonNull String boardKey, int postDisplayId) throws NullPointerException {
        return indexedPostsDisplay.get(boardKey).get(postDisplayId);
    }

    public HanabiraThread findThreadById(int threadId) {
        HanabiraThread retVal = indexedThreads.get(threadId);
        if (retVal != null) {
            return retVal;
        } else {
            retVal = super.findThreadById(threadId);
            activeCacheInternalSaveThread(retVal, HanabiraBoard.Info.getKeyForId(retVal.getBoardId()));
            return retVal;
        }
    }

    public HanabiraPost findPostById(int postId) {
        HanabiraPost retVal = indexedPosts.get(postId);
        if (retVal != null) {
            return retVal;
        } else {
            retVal = super.findPostById(postId);
            activeCacheInternalSavePost(retVal, HanabiraBoard.Info.getKeyForId(retVal.getBoardId()));
            return retVal;
        }
    }

    @Override
    public CharSequence getParsedPost(int postId) {
        synchronized (cachedParsedPosts) {
            CharSequence seq = cachedParsedPosts.get(postId);
            if (seq == null) {
                Log.w(TAG, "Parsed cache miss for post " + postId);
                seq = new HanabiraParser(findPostById(postId)).getFormatted();
                cachedParsedPosts.put(postId, seq);
            }
            return seq;
        }
    }

    @SuppressLint("UseSparseArrays")
    public void saveBoard(HanabiraBoard board) {
        activeCacheInternalSaveBoard(board);
        super.saveBoard(board);
    }

    private void activeCacheInternalSaveBoard(HanabiraBoard board) {
        indexedBoards.put(board.getKey(), board);
        indexedPostsDisplay.put(board.getKey(), new HashMap<Integer, HanabiraPost>());
        indexedThreadsDisplay.put(board.getKey(), new HashMap<Integer, HanabiraThread>());
    }

    public void saveThread(HanabiraThread thread, @Nullable String boardKey) {
        boardKey = boardKey == null ? HanabiraBoard.Info.getKeyForId(thread.getBoardId()) : boardKey;
        activeCacheInternalSaveThread(thread, boardKey);
        super.saveThread(thread, boardKey);
    }

    private void activeCacheInternalSaveThread(HanabiraThread thread, @Nullable String boardKey) {
        indexedThreads.put(thread.getThreadId(), thread);
        indexedThreadsDisplay.get(boardKey).put(thread.getDisplayId(), thread);
    }

    public void savePost(HanabiraPost cachedPost, @Nullable String boardKey) {
        boardKey = boardKey == null ? HanabiraBoard.Info.getKeyForId(cachedPost.getBoardId()) : boardKey;
        activeCacheInternalSavePost(cachedPost, boardKey);
        super.savePost(cachedPost, boardKey);
    }

    private void activeCacheInternalSavePost(HanabiraPost cachedPost, @Nullable String boardKey) {
        indexedPosts.put(cachedPost.getPostId(), cachedPost);
        indexedPostsDisplay.get(boardKey).put(cachedPost.getDisplayId(), cachedPost);
        findThreadById(cachedPost.getThreadId()).getPosts().put(cachedPost.getCreatedDate(), cachedPost.getPostId());
    }

}

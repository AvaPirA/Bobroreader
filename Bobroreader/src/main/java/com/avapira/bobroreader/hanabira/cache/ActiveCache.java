package com.avapira.bobroreader.hanabira.cache;

import android.util.Log;
import com.avapira.bobroreader.AmbiguousId;
import com.avapira.bobroreader.Castor;
import com.avapira.bobroreader.hanabira.HanabiraParser;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ActiveCache extends PersistentCache {

    private static final String                       TAG                   = ActiveCache.class.getSimpleName();
    private final        Map<Integer, CharSequence>   cachedParsedPosts     = new HashMap<>();
    private final        Map<String, HanabiraBoard>   indexedBoards         = new HashMap<>();
    private final        Map<Integer, HanabiraThread> indexedThreads        = new HashMap<>();
    private final        Map<Integer, HanabiraPost>   indexedPosts          = new HashMap<>();
    private final        Map<Integer, HanabiraThread> indexedThreadsDisplay = new HashMap<>();
    private final        Map<Integer, HanabiraPost>   indexedPostsDisplay   = new HashMap<>();

    public ActiveCache(Castor castor) {
        super(castor);
        Log.d(TAG, "Create");
    }

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
                for (int tdi : threadsToParse) {
                    cachePost(tdi);     // first priority
                }
                for (int tdi : threadsToParse) {
                    for (Integer pdi : findThreadByDisplayId(tdi).getLastN(previewsToParse)) {
                        cachePost(pdi);             // second priority
                    }
                }
            }
            Log.w(TAG, Thread.currentThread().toString() + " completed parsing for " +
                    ((System.nanoTime() - start) / 10e5) + "ms");
        }

        private void cachePost(int postDisplayId) {
            synchronized (cachedParsedPosts) { // not way 'if(contains) return;' because mby it's edited reloaded post
                HanabiraPost post = findPostByDisplayId(postDisplayId);
                cachedParsedPosts.put(post.getDisplayId(), new HanabiraParser(post, castor).getFormatted());
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
            return super.findBoardByKey(boardKey);
        }
    }

    @Override
    public HanabiraThread findThread(AmbiguousId id) {
        if (id.isDisplay()) {
            return findThreadByDisplayId(id.getDisplayId());
        } else {
            return findThreadById(id.getId());
        }
    }

    @Override
    public HanabiraPost findPost(AmbiguousId id) {
        if (id.isDisplay()) {
            return findPostByDisplayId(id.getDisplayId());
        } else {
            return findPostById(id.getId());
        }
    }

    public HanabiraThread findThreadById(int threadId) {
        HanabiraThread retVal = indexedThreads.get(threadId);
        if (retVal != null) {
            return retVal;
        } else {
            return super.findThreadById(threadId);
        }
    }

    public HanabiraPost findPostById(int postId) {
        HanabiraPost retVal = indexedPosts.get(postId);
        if (retVal != null) {
            return retVal;
        } else {
            return super.findPostById(postId);
        }
    }

    public HanabiraThread findThreadByDisplayId(int threadDisplayId) {
        HanabiraThread retVal = indexedThreadsDisplay.get(threadDisplayId);
        if (retVal != null) {
            return retVal;
        } else {
            return super.findThreadByDisplayId(threadDisplayId);
        }
    }

    public HanabiraPost findPostByDisplayId(int postDisplayId) {
        HanabiraPost retVal = indexedPostsDisplay.get(postDisplayId);
        if (retVal != null) {
            return retVal;
        } else {
            return super.findPostByDisplayId(postDisplayId);
        }
    }

    @Override
    public CharSequence getParsedPost(int postDisplayId) {
        synchronized (cachedParsedPosts) {
            CharSequence seq = cachedParsedPosts.get(postDisplayId);
            if (seq == null) {
                Log.w(TAG, "Parsed cache miss for post " + postDisplayId);
                seq = new HanabiraParser(findPostByDisplayId(postDisplayId), castor).getFormatted();
                cachedParsedPosts.put(postDisplayId, seq);
            }
            return seq;
        }
    }

    public void saveBoard(HanabiraBoard board) {
        indexedBoards.put(board.getKey(), board);
        super.saveBoard(board);
    }

    public void saveThread(HanabiraThread thread) {
        indexedThreads.put(thread.getThreadId(), thread);
        indexedThreadsDisplay.put(thread.getDisplayId(), thread);
        super.saveThread(thread);
    }

    public void savePost(HanabiraPost cachedPost) {
        indexedPosts.put(cachedPost.getPostId(), cachedPost);
        indexedPostsDisplay.put(cachedPost.getDisplayId(), cachedPost);
        findThreadById(cachedPost.getThreadId()).getPosts().put(cachedPost.getCreatedDate(), cachedPost.getDisplayId());
        super.savePost(cachedPost);
    }

}

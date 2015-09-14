package com.avapira.bobroreader.hanabira.networking;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.HanabiraException;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;

/**
 *
 */
public class HanabiraRequestBuilder {

    public static final String TAG = "HanabiraRequest";

    private static final String FLOWER = "http://dobrochan.ru";

    private static HanabiraRequestBuilder instance;

    public static HanabiraRequestBuilder init(Context context) {
        if (instance == null) {
            return instance = new HanabiraRequestBuilder(context);
        } else {
            throw new IllegalStateException("Only one instance per session");
        }
    }

    private final RequestQueue volleyQueue;

    private HanabiraRequestBuilder(Context context) {
        volleyQueue = Volley.newRequestQueue(context);
    }

    private static StringRequest createRequest(String url, Response.Listener<String> listener) {
        return createRequest(url, listener, errorListener);
    }

    private static StringRequest createRequest(String url,
                                               Response.Listener<String> listener,
                                               Response.ErrorListener errorListener) {
        Log.d(TAG.concat("#createRequest"), url);
        return new StringRequest(url, listener, errorListener);
    }

    private static Response.ErrorListener errorListener = new Response.ErrorListener() {
        public void onErrorResponse(VolleyError e) {
            e.printStackTrace();
        }
    };


    public class HanabiraRequest {
        private String url;

        public HanabiraRequest(String cookedUrl) {
            url = cookedUrl;
        }

        public void doRequest(Response.Listener<String> listener) {
            doRequest(listener, errorListener);
        }

        public void doRequest(Response.Listener<String> listener, Response.ErrorListener errorListener) {
            volleyQueue.add(createRequest(url, listener, errorListener));
        }

    }

    @SuppressWarnings("unchecked")
    private static abstract class RequestBuilder<B extends RequestBuilder> {
        protected StringBuilder flower = new StringBuilder(FLOWER);
        boolean withNewFormat   = false;
        boolean withMessageHtml = false;
        boolean withRaw         = false;
        boolean boardInfo;
        boolean threadInfo;
        boolean userThreads;

        public B withParamNewFormat() {
            withNewFormat = true;
            return (B) this;
        }

        public B withParamMessageHtml() {
            withMessageHtml = true;
            return (B) this;
        }

        public B withParamRaw() {
            withRaw = true;
            return (B) this;
        }

        public B withParamThreadInfo() {
            threadInfo = true;
            return (B) this;
        }

        public B withParamBoardInfo() {
            boardInfo = true;
            return (B) this;
        }

        public B withParamUserThreads() {
            userThreads = true;
            return (B) this;
        }

        protected void tryAddAmp(StringBuilder sb) {
            if (sb.length() > 0) { sb.append('&'); }
        }

        public abstract HanabiraRequest build();

        protected final StringBuilder basicParams() {
            StringBuilder res = new StringBuilder();
            if (withMessageHtml) {
                tryAddAmp(res);
                res.append("new_format");
            }
            if (withMessageHtml) {
                tryAddAmp(res);
                res.append("message_html");
            }
            if (withRaw) {
                tryAddAmp(res);
                res.append("message_raw");
            }
            if (threadInfo) {
                tryAddAmp(res);
                res.append("thread");
            }
            if (boardInfo) {
                tryAddAmp(res);
                res.append("board");
            }
            if (userThreads) {
                tryAddAmp(res);
                res.append("threads");
            }
            tryAddParamsStarter(res);
            return res;
        }

        protected void tryAddParamsStarter(StringBuilder res) {
            if (res.length() > 0 && res.charAt(0) != '?') {
                res.insert(0, '?');
            }
        }
    }

    public class BoardRequestBuilder extends RequestBuilder<BoardRequestBuilder> {
        String key;
        int page = -1; // -1 <=> not set manually

        public BoardRequestBuilder forKey(@NonNull String key) {
            if (!"".equals(key)) {
                this.key = key;
            } else {
                throw new HanabiraException("Board key may not be empty");
            }
            return this;
        }

        public BoardRequestBuilder atPage(int page) {
            tryCheckBoardPage(page);
            this.page = page;
            return this;
        }

        @Override
        public HanabiraRequest build() {
            page = page == -1 ? 0 : page; // request for 0 if not set manually
            if (key != null) {
                return new HanabiraRequest(
                        flower.append(String.format("/%s/%d.json", key, page)).append(basicParams()).toString());
            } else {
                throw new HanabiraException("Board key is required parameter for this request");
            }
        }

        private void tryCheckBoardPage(int page) {
            if (key != null) {
                HanabiraBoard board = Hanabira.getCache().findBoardByKey(key);
                if (board != null) {
                    int actualPagesCount = board.getPagesCount();
                    if (actualPagesCount >= page) {
                        if (page < 0) {
                            throw new HanabiraException("Page number can not be less than zero");
                        }
                    } else {
                        // may produce fail-throws when board grew and local info is not up to date
                        throw new HanabiraException(
                                String.format("Board %s has only %d pages while requested %d", key, actualPagesCount,
                                        page));
                    }
                }
            }
        }
    }

    public class ThreadRequestBuilder extends RequestBuilder<ThreadRequestBuilder> {

        ThreadRequestType type;
        int threadId = -1;
        boolean displayId;
        String  boardKey;
        private int last  = -1;
        private int count = -1;

        public ThreadRequestBuilder get(@NonNull ThreadRequestType type) {
            switch (type) {
                case ALL:
                    if (last != -1) {
                        throw new HanabiraException(String.format("Already defined \"last_post\"=%d param", last));
                    }
                    if (count != -1) {
                        throw new HanabiraException(String.format("Already defined \"count\"=%d param", count));
                    }
            }
            this.type = type;
            return this;
        }

        public ThreadRequestBuilder forId(int threadId) {
            if (threadId < 1) {
                throw new HanabiraException("Wrong thread id: " + threadId);
            }
            displayId = false;
            this.threadId = threadId;
            return this;
        }

        public ThreadRequestBuilder forDisplayId(int threadDisplayId) {
            if (threadDisplayId < 1) {
                throw new HanabiraException("Wrong thread id: " + threadDisplayId);
            }
            displayId = true;
            this.threadId = threadDisplayId;
            return this;
        }

        public ThreadRequestBuilder onBoard(String boardKey) {
            if (Hanabira.getCache().findBoardByKey(boardKey) != null) {
                this.boardKey = boardKey;
            } else {
                throw new HanabiraException("Unknown board key: " + boardKey);
            }
            return this;
        }

        public ThreadRequestBuilder since(int postDisplayId) {
            if (postDisplayId < 1) {
                throw new HanabiraException("Wrong post id: " + postDisplayId);
            }
            last = postDisplayId;
            return this;
        }

        public ThreadRequestBuilder noMoreThan(int count) {
            if (count < 0) {
                throw new HanabiraException("Wrong count amount: " + count);
            }
            this.count = count;
            return this;
        }

        @Override
        public HanabiraRequest build() {
            StringBuilder params = basicParams();
            switch (type) {
                case ALL:
                    // no additional params
                    break;
                case NEW:
                    if (last != -1) {
                        tryAddAmp(params);
                        params.append("last_post=");
                        params.append(last);
                        tryAddParamsStarter(params);
                    } else {
                        throw new HanabiraException("Param \"last_post\" must be defined for request of new posts");
                    }
                    break;
                case LAST:
                    if (last != -1) {
                        tryAddAmp(params);
                        params.append("last_post=");
                        params.append(last);
                        if (count != -1) {
                            tryAddAmp(params);
                            params.append("count=");
                            params.append(last);
                        }
                        tryAddParamsStarter(params);
                    } else {
                        throw new HanabiraException("Param \"last_post\" must be defined for request of new posts");
                    }
                    break;
            }
            return new HanabiraRequest(
                    flower.append(checkAndGetThreadDefinition()).append(params.toString()).toString());
        }

        private String checkAndGetThreadDefinition() {
            if ((boardKey != null) == (displayId)) {
                return displayId ? String.format("/api/thread/%s/%d.json", boardKey, threadId) : String.format(
                        "/api/thread/%d.json", threadId);
            } else {
                throw new HanabiraException(
                        String.format("Wrong arguments: \"board\"=%s while using " + "%sdisplay id of thread", boardKey,
                                displayId ? "" : "non-"));
            }
        }
    }

    public class PostRequestBuilder extends RequestBuilder<PostRequestBuilder> {

        int     postId;
        Integer threadDisplayId;
        String  boardKey;
        boolean displayId;

        public PostRequestBuilder forId(int postId) {
            if (postId < 1) {
                throw new HanabiraException("Wrong thread id: " + postId);
            }
            displayId = false;
            this.postId = postId;
            return this;
        }

        public PostRequestBuilder forDisplayId(int postDisplayId) {
            if (postDisplayId < 1) {
                throw new HanabiraException("Wrong thread id: " + postDisplayId);
            }
            displayId = true;
            this.postId = postDisplayId;
            return this;
        }

        public PostRequestBuilder onBoard(@NonNull String boardKey) {
            if (Hanabira.getCache().findBoardByKey(boardKey) != null) {
                this.boardKey = boardKey;
            } else {
                throw new HanabiraException("Unknown board key: " + boardKey);
            }
            return this;
        }

        public PostRequestBuilder inThread(int threadDisplayId) {
            if (threadDisplayId < 1) {
                throw new HanabiraException("Wrong thread id: " + threadDisplayId);
            }
            this.threadDisplayId = threadDisplayId;
            return this;
        }


        private String checkAndGetPostDefinition() {
            if ((threadDisplayId != null) == (boardKey != null) == (displayId)) {
                return displayId ? String.format("/api/post/%s%s/%d.json", boardKey,
                        threadDisplayId == null ? "" : "/" + threadDisplayId, postId) : String.format(
                        "/api/thread/%d.json", postId);
            } else {
                throw new HanabiraException(
                        String.format("Wrong arguments: \"board\"=%s while using " + "%sdisplay id of posts", boardKey,
                                displayId ? "" : "non-"));
            }
        }

        @Override
        public HanabiraRequest build() {
            return new HanabiraRequest(
                    flower.append(checkAndGetPostDefinition()).append(super.basicParams()).toString());
        }
    }

    public class SpecialRequestBuilder extends RequestBuilder<SpecialRequestBuilder> {

        private SpecialRequestType type;
        private boolean            withUserThreads;

        public SpecialRequestBuilder user(boolean withUserThreads) {
            type = SpecialRequestType.USER;
            this.withUserThreads = withUserThreads;
            return this;
        }

        public SpecialRequestBuilder diff() {
            type = SpecialRequestType.DIFF;
            return this;
        }

        @Override
        public HanabiraRequest build() {
            String specialRequest;
            StringBuilder params = basicParams();
            switch (type) {
                case USER:
                    specialRequest = "/api/user.json";
                    if (withUserThreads) {
                        tryAddAmp(params);
                        params.append("threads");
                        tryAddParamsStarter(params);
                    }
                    break;
                case DIFF:
                    specialRequest = "/api/chan/stats/diff.json";
                    break;
                default:
                    throw new HanabiraException("Request type not selected");
            }
            return new HanabiraRequest(flower.append(specialRequest).append(params).toString());
        }
    }

    public BoardRequestBuilder board() {
        return new BoardRequestBuilder();
    }

    public ThreadRequestBuilder thread() {
        return new ThreadRequestBuilder();
    }

    public PostRequestBuilder post() {
        return new PostRequestBuilder();
    }

    public SpecialRequestBuilder specials() {
        return new SpecialRequestBuilder();
    }

    public enum ThreadRequestType {
        ALL, NEW, LAST
    }

    public enum SpecialRequestType {
        USER, DIFF
    }

}


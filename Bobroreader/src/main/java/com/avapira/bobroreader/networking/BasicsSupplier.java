/*
 * Bobroreader is open source software, created, maintained, and shared under
 * the MIT license by Avadend Piroserpen Arts. The project includes components
 * from other open source projects which remain under their existing licenses,
 * detailed in their respective source files.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015. Avadend Piroserpen Arts Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */
package com.avapira.bobroreader.networking;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.avapira.bobroreader.hanabira.entity.HanabiraUser;
import com.avapira.bobroreader.util.Consumer;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class BasicsSupplier {

    public static final String TAG = "Network";

    public static final String BASIC_DOMAIN = "http://dobrochan.ru";

    public static final String diff      = BASIC_DOMAIN + "/api/chan/stats/diff.json";
    public static final String user      = BASIC_DOMAIN + "/api/user.json";
    public static final String boardPage = BASIC_DOMAIN + "/%s/%d.json";
    public static final String threadAll = BASIC_DOMAIN + "/api/thread/%s/all.json?thread";

    public static class PostSupplier {
        private static final String DISPLAY_ID           = "/api/post/%s.json";
        private static final String POST_ID              = "/api/post/%s.json";
        private static final String THREAD_ID_DISPLAY_ID = "/api/post/%s/%s.json";

        private static final String BOARD_EXPORT = "thread";

    }

    private final RequestQueue   volleyQueue;
    private       CountDownLatch requestDiff;

    public BasicsSupplier(Context context) {
        volleyQueue = Volley.newRequestQueue(context);
    }

    public void getUser(final Consumer<HanabiraUser> consumer) {
        StringRequest reqJson = new StringRequest(user, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                HanabiraUser user = gson.fromJson(response, HanabiraUser.class);
                consumer.accept(user);
            }
        }, errorListener);
        volleyQueue.add(reqJson);
    }


    public void getBoardsIds(final Consumer<Iterator<String>> consumer) {
        Log.i(TAG, diff);
        JsonObjectRequest reqJson = new JsonObjectRequest(diff, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                consumer.accept(response.keys());
            }
        }, errorListener);
        volleyQueue.add(reqJson);
    }

    public void getDiff(boolean wait, final Consumer<Map<String, Integer>> consumer) {
        if (wait) {
            requestDiff = new CountDownLatch(1);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        requestDiff.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    getDiffInternal(consumer);
                }
            }).start();
        } else {
            getDiffInternal(consumer);
        }

    }

    private void getDiffInternal(final Consumer<Map<String, Integer>> consumer) {
        Log.i(TAG, diff);
        JsonObjectRequest reqJson = new JsonObjectRequest(diff, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Map<String, Integer> diff = collectDiff(response);
                consumer.accept(diff);
            }
        }, errorListener);
        volleyQueue.add(reqJson);
    }

    @NonNull
    public static Map<String, Integer> collectDiff(JSONObject response) {
        Map<String, Integer> diff = new HashMap<>();
        Iterator<String> keys = response.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            try {
                diff.put(k, response.getInt(k));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return diff;
    }

    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        public void onErrorResponse(VolleyError e) {
            e.printStackTrace();
        }
    };

    public void getBoardPage(String boardkey, int pageNum, final Response.Listener<String> listener) {
        String formattedUrl = String.format(boardPage, boardkey, pageNum);
        Log.i(TAG, formattedUrl);
        StringRequest boardPageRequest = new StringRequest(formattedUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if(requestDiff!=null)requestDiff.countDown();
                listener.onResponse(response);
            }
        }, errorListener);
        volleyQueue.add(boardPageRequest);

    }

    public void getThread(int threadId, final Response.Listener<String> listener) {
        String formattedUrl = String.format(threadAll, threadId);
        Log.i(TAG, formattedUrl);
        StringRequest threadRequest = new StringRequest(formattedUrl, listener, errorListener);
        volleyQueue.add(threadRequest);
    }

}

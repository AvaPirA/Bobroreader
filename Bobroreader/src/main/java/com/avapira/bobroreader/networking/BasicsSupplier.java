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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.avapira.bobroreader.util.Consumer;
import com.avapira.bobroreader.hanabira.entity.HanabiraUser;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class BasicsSupplier {

    public static final String BASIC_DOMAIN = "http://dobrochan.ru";

    public static final String diff = "/api/chan/stats/diff.json";
    public static final String user = "/api/user.json";



    public static class PostSupplier {
        private static final String DISPLAY_ID           = "/api/post/%s.json";
        private static final String POST_ID              = "/api/post/%s.json";
        private static final String THREAD_ID_DISPLAY_ID = "/api/post/%s/%s.json";

        private static final String BOARD_EXPORT = "thread";

    }

    public static void getUser(Context ctx, final Consumer<HanabiraUser> consumer) {
        RequestQueue queue = Volley.newRequestQueue(ctx);
        StringRequest reqJson = new StringRequest(BASIC_DOMAIN + user, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                HanabiraUser user = gson.fromJson(response, HanabiraUser.class);
                consumer.accept(user);
            }
        }, errList);
        queue.add(reqJson);
    }


    public static void getBoardsIds(Context ctx, final Consumer<Iterator<String>> consumer) {
        RequestQueue queue = Volley.newRequestQueue(ctx);
        JsonObjectRequest reqJson = new JsonObjectRequest(BASIC_DOMAIN + diff, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                consumer.accept(response.keys());
            }
        }, errList);
        queue.add(reqJson);
    }

    public static void getDiff(Context ctx, final Consumer<Map<String, Integer>> consumer) {
        RequestQueue queue = Volley.newRequestQueue(ctx);
        JsonObjectRequest reqJson = new JsonObjectRequest(BASIC_DOMAIN + diff, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
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
                consumer.accept(diff);
            }
        }, errList);
        queue.add(reqJson);
    }

    private static final Response.ErrorListener errList = new Response.ErrorListener() {
        public void onErrorResponse(VolleyError e) {
            e.printStackTrace();
        }
    };

}

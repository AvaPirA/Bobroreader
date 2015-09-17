package com.avapira.bobroreader.hanabira;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.android.volley.Response;
import com.avapira.bobroreader.Bober;
import com.avapira.bobroreader.R;
import com.avapira.bobroreader.hanabira.cache.ActiveCache;
import com.avapira.bobroreader.hanabira.cache.HanabiraCache;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;
import com.avapira.bobroreader.hanabira.entity.HanabiraUser;
import com.avapira.bobroreader.hanabira.networking.HanabiraRequestBuilder;
import com.avapira.bobroreader.hanabira.networking.PersistentCookieStore;
import com.avapira.bobroreader.util.Consumer;
import org.joda.time.LocalDateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class Hanabira {

    private static Hanabira flower;

    private final HanabiraCache          cacheImpl;          // cache
    private final HanabiraRequestBuilder hanabiraSupplier;   // network

    private final SharedPreferences prefs;
    private final Resources         mockerRes;

    private CountDownLatch diffRequestWaiter;

    private Hanabira(Context ctx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        mockerRes = ctx.getResources();
        cacheImpl = new ActiveCache(ctx);
        hanabiraSupplier = HanabiraRequestBuilder.init(ctx);
        CookieHandler.setDefault(
                new CookieManager(new PersistentCookieStore(ctx), CookiePolicy.ACCEPT_ORIGINAL_SERVER));
    }

    private static class ThreadCollector implements Response.Listener<String> {

        private final Consumer<HanabiraThread> consumer;

        private ThreadCollector(Consumer<HanabiraThread> consumer) {this.consumer = consumer;}

        @Override
        public void onResponse(String response) {
            consumer.accept(HanabiraThread.fromJson(response, HanabiraThread.class));
        }
    }

    public static void bind(Context ctx) {
        if (flower == null) {
            flower = new Hanabira(ctx);
            Log.d("Hanabira", "Create");
        } else {
            throw new IllegalStateException("Switching hanabira context is impossible");
        }
    }

    public static void unbind() {
        HanabiraRequestBuilder.destroy();
        flower = null;
        Log.d("Hanabira", "Destroy");
    }

    public static Hanabira getFlower() {
        return flower;
    }

    public static HanabiraCache getStem() {
        return flower.cacheImpl;
    }

    private static LocalDateTime extractLocatDateTime(@NonNull String ldt) {
        return LocalDateTime.parse(ldt.replace(' ', 'T'));
    }

    private boolean useMockedNetwork() {
        return prefs.getBoolean("pref_mocked_network", false);
    }

    public void getBoardPage(String boardKey, final int pageNum, final Consumer<List<Integer>> callback) {
        if (useMockedNetwork()) {
            callback.accept(HanabiraBoard.fromJson(Bober.rawJsonToString(mockerRes, R.raw.u_0), HanabiraBoard.class)
                                         .getPage(pageNum));
        } else {
            Response.Listener<String> boardPageCollector = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (diffRequestWaiter != null) {diffRequestWaiter.countDown();}
                    callback.accept(HanabiraBoard.fromJson(response, HanabiraBoard.class).getPage(pageNum));
                }
            };
            hanabiraSupplier.board().forKey(boardKey).atPage(pageNum).build().doRequest(boardPageCollector);
        }
    }

    public void getUser(final Consumer<HanabiraUser> consumer) {
        if (useMockedNetwork()) {
            String raw = Bober.rawJsonToString(mockerRes, R.raw.user);
            consumer.accept(HanabiraUser.fromJson(raw, HanabiraUser.class));
        } else {
            Response.Listener<String> userCollector = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    consumer.accept(HanabiraUser.fromJson(response, HanabiraUser.class));
                }
            };
            hanabiraSupplier.specials().user(false).build().doRequest(userCollector);
        }
    }

    public void getBoardIds(final Consumer<Iterator<String>> consumer) {
        if (useMockedNetwork()) {
            throw new UnsupportedOperationException();
        } else {
            Response.Listener<String> boardIdsCollector = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        consumer.accept(new JSONObject(response).keys());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            hanabiraSupplier.specials().diff().build().doRequest(boardIdsCollector);
        }
    }

    public void getDiff(boolean wait, final Consumer<Map<String, Integer>> consumer) {
        if (useMockedNetwork()) {
            throw new UnsupportedOperationException();
        } else {
            final Response.Listener<String> diffCollector = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject json = new JSONObject(response);

                        Map<String, Integer> diff = new HashMap<>();
                        Iterator<String> keys = json.keys();
                        while (keys.hasNext()) {
                            String k = keys.next();
                            try {
                                diff.put(k, json.getInt(k));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        consumer.accept(diff);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            final HanabiraRequestBuilder.HanabiraRequest request = hanabiraSupplier.specials().diff().build();

            if (wait) {
                diffRequestWaiter = new CountDownLatch(1);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            diffRequestWaiter.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        request.doRequest(diffCollector);
                    }
                }).start();
            } else {
                request.doRequest(diffCollector);
            }
        }
    }

    public void getFullThread(int threadId, final Consumer<HanabiraThread> consumer) {
        if (useMockedNetwork()) {
            consumer.accept(
                    HanabiraThread.fromJson(Bober.rawJsonToString(mockerRes, R.raw.x112992_all), HanabiraThread.class));
        } else {
            hanabiraSupplier.thread()
                            .get(HanabiraRequestBuilder.ThreadRequestType.ALL)
                            .forId(threadId)
                            .build()
                            .doRequest(new ThreadCollector(consumer));
        }
    }

    public void getThreadWithUpdate(final int threadId, final Consumer<HanabiraThread> consumer) {
        if (useMockedNetwork()) {

        } else {
            hanabiraSupplier.thread()
                            .get(HanabiraRequestBuilder.ThreadRequestType.INFO)
                            .forId(threadId)
                            .build()
                            .doRequest(new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject threadInfoJson = new JSONObject(response);
                                        HanabiraThread cachedThread = Hanabira.getStem().findThreadById(threadId);
                                        if (!cachedThread.getLastHit()
                                                         .equals(extractLocatDateTime(
                                                                 threadInfoJson.getString("last_hit")))) {
                                            // new post after last one cached exists
                                            int diff = threadInfoJson.getInt("posts_count") -
                                                    cachedThread.getPostsCount(); // how much?
                                            HanabiraRequestBuilder.HanabiraRequest request;
                                            if (diff > 0) {
                                                request = hanabiraSupplier.thread()
                                                                          .get(HanabiraRequestBuilder
                                                                                  .ThreadRequestType.LAST)
                                                                          .forId(threadId)
                                                                          .noMoreThan(diff)
                                                                          .build();
                                            } else {
                                                // last_hit later than cached, but cached have more posts?
                                                // that's likely because some posts were deleted
                                                // hence do full reload (don't cry, my GPRS princess)
                                                request = hanabiraSupplier.thread()
                                                                          .get(HanabiraRequestBuilder
                                                                                  .ThreadRequestType.ALL)
                                                                          .forId(threadId)
                                                                          .build();
                                            }
                                            request.doRequest(new ThreadCollector(consumer));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
    }

    public void checkForDeletedPosts(final int threadId, final Consumer<Boolean> consumer) {
        //just make getThreadInfo and compare with cache.thread.post_count
        if (useMockedNetwork()) {

        } else {
            hanabiraSupplier.thread()
                            .get(HanabiraRequestBuilder.ThreadRequestType.INFO)
                            .forId(threadId)
                            .build()
                            .doRequest(new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        consumer.accept(Hanabira.getStem().findThreadById(threadId).getPostsCount() !=
                                                new JSONObject(response).getInt("posts_count"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
    }
}

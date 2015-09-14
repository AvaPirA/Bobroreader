package com.avapira.bobroreader.hanabira;

import android.content.Context;
import android.preference.PreferenceManager;
import com.android.volley.Response;
import com.avapira.bobroreader.Bober;
import com.avapira.bobroreader.R;
import com.avapira.bobroreader.hanabira.cache.ActiveCache;
import com.avapira.bobroreader.hanabira.cache.HanabiraCache;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
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
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
public class Hanabira {

    private static Hanabira flower = new Hanabira();

    private Context                context;
    private HanabiraCache          cacheImpl;
    private HanabiraRequestBuilder hanabiraSupplier;

    private CountDownLatch diffRequestWaiter;

    private Hanabira() {}

    public static void bind(Context ctx) {
        flower.context = ctx;
        flower.cacheImpl = new ActiveCache(flower.context);
        flower.hanabiraSupplier = HanabiraRequestBuilder.init(flower.context);
        flower.bindCookies();
    }

    private void bindCookies() {
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(context),
                CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
    }

    public static Hanabira getFlower() {
        return flower.context == null ? null : flower;
    }

    public static HanabiraCache getCache() {
        return getFlower().cacheImpl;
    }

    private boolean useMockedNetwork() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_mocked_network", false);
    }

    public void getBoardPage(String boardKey, final int pageNum, final Consumer<List<Integer>> callback) {
        if (useMockedNetwork()) {
            callback.accept(HanabiraBoard.fromJson(Bober.rawJsonToString(context.getResources(), R.raw.u_0),
                    HanabiraBoard.class).getPage(pageNum));
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

    public void updateThread(int threadId, final Consumer<TreeMap<LocalDateTime, Integer>> callback) {
        if (useMockedNetwork()) {
            throw new UnsupportedOperationException();
        } else {
//            hanabiraSupplier.thread().forId(threadId)
        }
    }

    public void getUser(final Consumer<HanabiraUser> consumer) {
        if (useMockedNetwork()) {
            String raw = Bober.rawJsonToString(context.getResources(), R.raw.user);
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
}

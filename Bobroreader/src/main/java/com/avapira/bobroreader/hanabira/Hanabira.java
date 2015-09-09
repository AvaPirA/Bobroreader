package com.avapira.bobroreader.hanabira;

import android.content.Context;
import android.preference.PreferenceManager;
import com.android.volley.Response;
import com.avapira.bobroreader.Bober;
import com.avapira.bobroreader.R;
import com.avapira.bobroreader.hanabira.cache.ActiveCache;
import com.avapira.bobroreader.hanabira.cache.HanabiraCache;
import com.avapira.bobroreader.hanabira.entity.HanabiraUser;
import com.avapira.bobroreader.networking.BasicsSupplier;
import com.avapira.bobroreader.networking.PersistentCookieStore;
import com.avapira.bobroreader.util.Consumer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;

/**
 *
 */
public class Hanabira {

    private static Hanabira flower = new Hanabira();

    private Context        context;
    private HanabiraCache  cacheImpl;
    private BasicsSupplier network;

    private Hanabira() {}

    public static void bind(Context ctx) {
        flower.context = ctx;
        flower.cacheImpl = new ActiveCache(flower.context);
        flower.network = new BasicsSupplier(flower.context);
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

    public void updateBoardPage(String boardKey, int pageNum, Response.Listener<String> andThen) {
        if (useMockedNetwork()) {
            andThen.onResponse(Bober.rawJsonToString(context.getResources(), R.raw.u_0));
        } else {
            network.getBoardPage(boardKey, pageNum, andThen);
        }
    }

    public void updateThread(int threadId){
        //TODO
    }

    public void getUser(Consumer<HanabiraUser> consumer) {
        if (useMockedNetwork()) {
            String raw = Bober.rawJsonToString(context.getResources(), R.raw.user);
            consumer.accept(HanabiraUser.fromJson(raw, HanabiraUser.class));
        } else {
            network.getUser(consumer);
        }
    }

    public void getDiff(boolean wait, Consumer<Map<String, Integer>> consumer) {
        if (useMockedNetwork()) {
            String raw = Bober.rawJsonToString(context.getResources(), R.raw.diff);
            try {
                consumer.accept(BasicsSupplier.collectDiff(new JSONObject(raw)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            network.getDiff(wait, consumer);
        }
    }
}

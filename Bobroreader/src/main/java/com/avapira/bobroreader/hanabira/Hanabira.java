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
import com.avapira.bobroreader.util.Consumer;

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

    public static void init(Context ctx) {
        flower.context = ctx;
        flower.cacheImpl = new ActiveCache(flower.context);
        flower.network = new BasicsSupplier(flower.context);
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

    public void getUser(Consumer<HanabiraUser> consumer) {
        network.getUser(consumer);
    }

    public void getDiff(Consumer<Map<String, Integer>> consumer) {
        network.getDiff(consumer);
    }
}

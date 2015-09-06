package com.avapira.bobroreader.hanabira;

import android.content.Context;
import com.android.volley.Response;
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

    private Context       context;
    private HanabiraCache cacheImpl;
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

    public void updateBoardPage(String boardKey, int pageNum, Response.Listener<String> andThen) {
        network.getBoardPage(boardKey, pageNum, andThen);
    }

    public void getUser(Consumer<HanabiraUser> consumer) {
        network.getUser(consumer);
    }

    public void getDiff(Consumer<Map<String, Integer>> consumer) {
        network.getDiff(consumer);
    }
}

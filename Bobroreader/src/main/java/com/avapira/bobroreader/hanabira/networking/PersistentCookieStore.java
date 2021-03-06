/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lukas Zorich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.avapira.bobroreader.hanabira.networking;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;

import java.net.*;
import java.util.List;

/**
 * Repository for cookies. CookieManager will store cookies of every incoming HTTP response into
 * CookieStore, and retrieve cookies for every outgoing HTTP request.
 * <p/>
 * Cookies are stored in {@link android.content.SharedPreferences} and will persist on the
 * user's device between application session. {@link com.google.gson.Gson} is used to serialize
 * the cookies into a json string in order to be able to save the cookie to
 * {@link android.content.SharedPreferences}
 * <p/>
 * Created by lukas on 17-11-14.
 */
public class PersistentCookieStore implements CookieStore {

    /**
     * The default preferences string.
     */
    private final static String PREF_DEFAULT_STRING       =
            "{\"discard\":false,\"domain\":\"dobrochan.ru\",\"httpOnly\":false,\"maxAge\":-1,\"name\":\"hanabira\"," +
                    "\"path\":\"/\",\"secure\":false," +
                    "\"value\":\"b278577bb1befca8ea8068e59bbef27b4e28878e078cd88a419378f3e70b1234fcd3c645\"," +
                    "\"version\":0}";
    private final static String PREF_DEFAULT_EMPTY_STRING = "";

    /**
     * The preferences name.
     */
    private final static String PREFS_NAME = PersistentCookieStore.class.getName();

    /**
     * The preferences session cookie key.
     */
    private final static String PREF_SESSION_COOKIE = "session_cookie";

    private final CookieStore mStore;
    private final Context     mContext;

    /**
     * @param context The application context
     */
    public PersistentCookieStore(Context context) {
        mContext = context.getApplicationContext();
        mStore = new CookieManager().getCookieStore();

        String jsonSessionCookie = getJsonSessionCookieString();
        if (!jsonSessionCookie.equals(PREF_DEFAULT_EMPTY_STRING)) {
            Gson gson = new Gson();
            HttpCookie cookie = gson.fromJson(jsonSessionCookie, HttpCookie.class);
            URI uri = URI.create(cookie.getDomain());
            mStore.add(uri, cookie);
        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        mStore.add(URI.create(cookie.getDomain()), cookie);
        saveSessionCookie(cookie);
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        try {
            uri = new URI("http", uri.getHost(), null, null);
            return mStore.get(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<HttpCookie> getCookies() {
        return mStore.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return mStore.getURIs();
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        return mStore.remove(uri, cookie);
    }

    @Override
    public boolean removeAll() {
        return mStore.removeAll();
    }

    private String getJsonSessionCookieString() {
        return getPrefs().getString(PREF_SESSION_COOKIE, PREF_DEFAULT_EMPTY_STRING); // XXX here's my own test-cookie
    }

    /**
     * Saves the HttpCookie to SharedPreferences as a json string.
     *
     * @param cookie The cookie to save in SharedPreferences.
     */
    private void saveSessionCookie(HttpCookie cookie) {
        Gson gson = new Gson();
        String jsonSessionCookieString = gson.toJson(cookie);
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(PREF_SESSION_COOKIE, jsonSessionCookieString);
        editor.apply();
    }

    private SharedPreferences getPrefs() {
        return mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
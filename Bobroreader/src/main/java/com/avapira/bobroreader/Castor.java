package com.avapira.bobroreader;

import android.content.Context;
import android.net.Uri;

/**
 *
 */
public interface Castor {

    Context getApplicationContext();
    void onThreadSelected(String board, int threadDisplayId);
    void onThreadSelected(int threadId);
    void onFragmentInteraction(Uri uri);
    void retitleOnLoading();
    void retitleOnBoardLoad(String key, int page);
    void retitleOnThreadLoad(String key, String title);
    void onOpenPost(String board, int postDisplayId);
}

package com.avapira.bobroreader;

import android.net.Uri;

/**
 *
 */
interface Castor {

    void onThreadSelected(int threadId);
    void onFragmentInteraction(Uri uri);
    void retitleOnLoading();
    void retitleOnBoardLoad(String key, int page);
    void retitleOnThreadLoad(String key, String title);
}

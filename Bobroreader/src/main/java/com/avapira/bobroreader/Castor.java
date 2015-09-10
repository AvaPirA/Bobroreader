package com.avapira.bobroreader;

import android.net.Uri;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

/**
 *
 */
public interface Castor {

    public static enum ToolbarState {
        INITIAL, LOADING, BOARD, state, THREAD
    }

    void onThreadSelected(int threadId);
    void onFragmentInteraction(Uri uri);
    void retitleOnLoading();
    void retitleOnBoardLoad(String key, int page);
    void retitleOnThreadLoad(HanabiraThread thread);
}

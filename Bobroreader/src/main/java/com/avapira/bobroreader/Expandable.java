package com.avapira.bobroreader;

import android.view.View;

/**
 *
 */
public interface Expandable {
    View getExpandView();
    void closeHolder(boolean animate);
    void openHolder(boolean animate);
}

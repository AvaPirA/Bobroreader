package com.avapira.bobroreader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

class HidingScrollListener extends RecyclerView.OnScrollListener {

    private final FrameLayout toolbarContainer;
    private final float       TOOLBAR_ELEVATION_RISE;
    private final float       TOOLBAR_ELEVATION_DOWN;
    private int     verticalOffset;
    private boolean scrollingUp;
    private boolean expandTriggered;

    public HidingScrollListener(FrameLayout toolbarContainer, Context context) {
        this.toolbarContainer = toolbarContainer;
        TOOLBAR_ELEVATION_RISE = context.getResources().getDimension(R.dimen.tiny);
        TOOLBAR_ELEVATION_DOWN = context.getResources().getDimension(R.dimen.micro);
        reset();
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            final int height = toolbarContainer.getHeight();
            if (scrollingUp) {
                if (expandTriggered) {
                    toolbarAnimateShow();
                } else {
                    if (verticalOffset > height && toolbarContainer.getTranslationY() < height * -0.5) {
                        toolbarAnimateHide();
                    } else {
                        toolbarAnimateShow();
                    }
                }
            } else {
                if (verticalOffset > height && toolbarContainer.getTranslationY() < height * -0.5) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow();
                }
            }
        }
    }

    @Override
    public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        verticalOffset += dy;
        scrollingUp = dy < 0;
        int toolbarYOffset = (int) (dy - toolbarContainer.getTranslationY());
        toolbarYOffset = Math.max(0, toolbarYOffset);
        toolbarContainer.animate().cancel();
        final int height = toolbarContainer.getHeight();
        if (scrollingUp) {
            if (toolbarYOffset < height) {
                toolbarSetElevation(verticalOffset == 0 ? TOOLBAR_ELEVATION_DOWN : TOOLBAR_ELEVATION_RISE);
                toolbarContainer.setTranslationY(-toolbarYOffset);
            } else {
                toolbarSetElevation(0); //no shadowing from top
                toolbarContainer.setTranslationY(-height); // no rising more than own height
            }
        } else {
            toolbarContainer.setTranslationY(-toolbarYOffset);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toolbarSetElevation(float elevation) {
        toolbarContainer.setElevation(elevation);
    }

    private void toolbarAnimateShow() {
        toolbarContainer.animate()
                        .translationY(0)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                toolbarSetElevation(
                                        verticalOffset == 0 ? TOOLBAR_ELEVATION_DOWN : TOOLBAR_ELEVATION_RISE);
                            }
                        });
    }

    private void toolbarAnimateHide() {
        toolbarContainer.animate()
                        .translationY(-toolbarContainer.getHeight())
                        .setInterpolator(new FastOutSlowInInterpolator());
    }

    public void expandTriggered() {
        expandTriggered = true;
    }

    public void reset() {
        toolbarSetElevation(TOOLBAR_ELEVATION_DOWN);
        toolbarContainer.setTranslationY(0);
        verticalOffset = 0;
        scrollingUp = false;
        expandTriggered = false;
    }
}
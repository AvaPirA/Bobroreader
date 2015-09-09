package com.avapira.bobroreader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

public class HidingScrollListener extends RecyclerView.OnScrollListener {
    private       FrameLayout toolbarContainer;
    private final float       TOOLBAR_ELEVATION;
    int     verticalOffset;
    boolean scrollingUp;
    boolean expandTriggered = false;

    public HidingScrollListener(FrameLayout toolbarContainer, int elevation) {
        this.toolbarContainer = toolbarContainer;
        TOOLBAR_ELEVATION = elevation;
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
                if (verticalOffset > height) {
                    toolbarSetElevation(TOOLBAR_ELEVATION);
                }
                toolbarContainer.setTranslationY(-toolbarYOffset);
            } else {
                toolbarSetElevation(1);
                toolbarContainer.setTranslationY(-height);
            }
        } else {
            if (toolbarYOffset < 0) {
                if (verticalOffset <= 0) {
                    toolbarSetElevation(0);
                }
                toolbarContainer.setTranslationY(0);
            } else {
                if (verticalOffset > height) {
                    toolbarSetElevation(TOOLBAR_ELEVATION);
                }
                toolbarContainer.setTranslationY(-toolbarYOffset);
            }
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
                                toolbarSetElevation(verticalOffset == 0 ? 0 : TOOLBAR_ELEVATION);
                            }
                        });
    }

    private void toolbarAnimateHide() {
        toolbarContainer.animate()
                        .translationY(-toolbarContainer.getHeight())
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                toolbarSetElevation(0);
                            }
                        });
    }

    public void expandTriggered() {
        expandTriggered = true;
    }
}
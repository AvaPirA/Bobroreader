package com.avapira.bobroreader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 */
public class KeepOneH<VH extends RecyclerView.ViewHolder & Expandable> {
    private int _opened = -1;
    private BoardFragment.HidingScrollListener scrollListener;

    public static void openHolder(final RecyclerView.ViewHolder holder, final View expandView, final boolean animate) {
        if (animate) {
            expandView.setVisibility(View.VISIBLE);
            final Animator animator = animateItemViewHeight(holder);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(expandView, View.ALPHA, 1);
                    alphaAnimator.addListener(new NotRecycleAdapter(holder));
                    alphaAnimator.start();
                }
            });
            animator.start();
        }
        else {
            expandView.setVisibility(View.VISIBLE);
            expandView.setAlpha(1);
        }
    }

    public static void closeHolder(final RecyclerView.ViewHolder holder, final View expandView, final boolean animate) {
        if (animate) {
            expandView.setVisibility(View.GONE);
            final Animator animator = animateItemViewHeight(holder);
            expandView.setVisibility(View.VISIBLE);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    expandView.setVisibility(View.GONE);
                    expandView.setAlpha(0);
                }
                @Override public void onAnimationCancel(Animator animation) {
                    expandView.setVisibility(View.GONE);
                    expandView.setAlpha(0);
                }
            });
            animator.start();
        }
        else {
            expandView.setVisibility(View.GONE);
            expandView.setAlpha(0);
        }
    }

    public void bind(VH holder, int pos, BoardFragment.HidingScrollListener scrollListener) {
        this.scrollListener = scrollListener;
        if (pos == _opened)
            openHolder(holder, holder.getExpandView(), false);
        else
            closeHolder(holder, holder.getExpandView(), false);
    }

    public void toggle(VH holder) {
        scrollListener.expandTriggered();
        if (_opened == holder.getPosition()) {
            _opened = -1;
            closeHolder(holder, holder.getExpandView(), true);
        } else {
            int previous = _opened;
            _opened = holder.getPosition();
            openHolder(holder, holder.getExpandView(), true);

            final VH oldHolder = (VH) ((RecyclerView) holder.itemView.getParent()).findViewHolderForPosition(previous);
            if (oldHolder != null) { closeHolder(oldHolder, oldHolder.getExpandView(), true); }
        }
    }
    public static Animator animateItemViewHeight(final RecyclerView.ViewHolder holder) {
        View parent = (View) holder.itemView.getParent();
        if (parent == null)
            throw new IllegalStateException("Cannot animate the layout of a view that has no parent");

        int start = holder.itemView.getMeasuredHeight();
        holder.itemView.measure(View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        int end = holder.itemView.getMeasuredHeight();

        final ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                lp.height = (int) animation.getAnimatedValue();
                holder.itemView.setLayoutParams(lp);
            }

        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemView.setLayoutParams(params);
            }
        });

        animator.addListener(new NotRecycleAdapter(holder));

        return animator;
    }

    public static class NotRecycleAdapter extends AnimatorListenerAdapter {
        private final RecyclerView.ViewHolder _holder;

        public NotRecycleAdapter(RecyclerView.ViewHolder holder) {
            _holder = holder;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            _holder.setIsRecyclable(false);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            _holder.setIsRecyclable(true);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            _holder.setIsRecyclable(true);
        }
    }
}

package com.avapira.bobroreader;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ThreadWithPreviewViewHolder extends RecyclerView.ViewHolder {

    public final TextView             threadTitle;
    public final PostHolder           opPost;
    public final HorizontalScrollView filesScroller;
    public final LinearLayout         previewList;
    public final TextView             replies;
    public final TextView             recentBtn;
    public final List<PostHolder>     recents;

    public int recentsHeight;

    public ThreadWithPreviewViewHolder(View itemView, int recentPostsAmount, Context context) {
        super(itemView);
        threadTitle = (TextView) itemView.findViewById(R.id.text_thread_title);

        opPost = new PostHolder(itemView);
        replies = (TextView) itemView.findViewById(R.id.text_post_content_replies);
        filesScroller = (HorizontalScrollView) itemView.findViewById(R.id.post_files_scroller);
        previewList = (LinearLayout) itemView.findViewById(R.id.layout_thread_expandable_posts_preview);
        recentBtn = (TextView) itemView.findViewById(R.id.thread_controls_recent);

        recents = new ArrayList<>(recentPostsAmount);
        if (previewList != null) {
            previewList.removeAllViews();
            int oneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                    context.getResources().getDisplayMetrics());
            for (int i = 0; i < recentPostsAmount; i++) {
                previewList.addView(createDivider(context, oneDp));
                LayoutInflater.from(context).inflate(R.layout.layout_post, previewList);
                recents.add(new PostHolder(previewList.getChildAt(2 * i + 1)));
            }
        }
    }

    public void setStaticText(HanabiraThread thread, HanabiraPost op) {
        threadTitle.setText(thread.getTitle());
        opPost.fillWithData(op);
        List<Integer> recentsList = thread.getLastN(3);
        for (int i = 0; i < recentsList.size(); i++) {
            recents.get(i).fillWithData(recentsList.get(i));
        }
        previewList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recentsHeight = previewList.getHeight();
                previewList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                previewList.setVisibility(View.GONE);
            }
        });
    }

    private View createDivider(Context context, int oneDp) {
        View divider = new View(context);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, oneDp);
        ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(lp);
        mlp.setMargins(0, 2 * oneDp, 0, 2 * oneDp);
        divider.setLayoutParams(mlp);
        return divider;
    }
}

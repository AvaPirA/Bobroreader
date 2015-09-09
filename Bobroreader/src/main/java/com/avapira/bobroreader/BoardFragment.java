/*
 * Bobroreader is open source software, created, maintained, and shared under
 * the MIT license by Avadend Piroserpen Arts. The project includes components
 * from other open source projects which remain under their existing licenses,
 * detailed in their respective source files.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015. Avadend Piroserpen Arts Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.avapira.bobroreader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import com.android.volley.Response;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BoardFragment extends Fragment {

    private static final String TAG = BoardFragment.class.getSimpleName();

    ActionBar             toolbar;
    FrameLayout           toolbarContainer;
    ProgressBar           progressBar;
    GestureDetectorCompat detector;
    RecyclerView          recycler;
    HidingScrollListener  scrollListener;
    private String boardKey;
    private int    page;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NotificationFragment.
     */
    public static BoardFragment newInstance() {
        BoardFragment fragment = new BoardFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public BoardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            boardKey = savedInstanceState.getString("board");
            page = savedInstanceState.getInt("page");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boardKey = boardKey == null ? getArguments().getString("board") : boardKey;
    }

    private void switchPage(int newPage) {
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle("Page loading...");
        }
        progressBar.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.INVISIBLE);
        page = newPage;
        Hanabira.getFlower().updateBoardPage(boardKey, page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                final HanabiraBoard hanabiraBoard = HanabiraBoard.fromJson(response, HanabiraBoard.class);
                final BoardAdapter boardAdapter = new BoardAdapter(hanabiraBoard.getPageThreads(page));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recycler.setAdapter(boardAdapter);
                        progressBar.setVisibility(View.GONE);
                        recycler.setVisibility(View.VISIBLE);

                        String title = String.format("/%s/ - %s " + "[%s]", hanabiraBoard.getKey(),
                                                     hanabiraBoard.getInfo().title, page);

                        if (toolbar != null) {
                            toolbar.setTitle(title);
                        }
                    }
                });
            }

        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("board", boardKey);
        outState.putInt("page", page);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scroller, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        toolbarContainer = (FrameLayout) getActivity().findViewById(R.id.frame_toolbar_container);
        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        recycler = (RecyclerView) view.findViewById(R.id.thread_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
//        recycler.addOnItemTouchListener(new TouchEventInterceptor());
        scrollListener = new HidingScrollListener();
        recycler.addOnScrollListener(scrollListener);
//        detector = new GestureDetectorCompat(getActivity(), new RecyclerGestureListener());
        switchPage(page);
    }

    public class HidingScrollListener extends RecyclerView.OnScrollListener {
        private final float TOOLBAR_ELEVATION = getResources().getDimension(R.dimen.tiny);
        int     verticalOffset;
        boolean scrollingUp;
        boolean expandTriggered = false;

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

    private class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ThreadWithPreviewViewHolder> {
        public static final int VIEW_TYPE_PREV_PAGE = 1;
        public static final int VIEW_TYPE_THREAD    = 2;
        public static final int VIEW_TYPE_NEXT_PAGE = 3;
        private final       int ELLIPSIZE_MAX_LINES = 15;
        private final       int recentListSize      = 3;
        List<HanabiraThread> threads = new ArrayList<>();

        KeepOneHolderOpen keeper = new KeepOneHolderOpen();

        public BoardAdapter(List<HanabiraThread> tt) {
            threads = tt;
            Hanabira.getCache().asyncParse(threads, recentListSize);
        }

        @LayoutRes
        public int getLayoutIdForViewType(int viewType) {
            switch (viewType) {
                case VIEW_TYPE_PREV_PAGE:
                    return R.layout.board_header_view;
                case VIEW_TYPE_THREAD:
                    return R.layout.layout_thread_with_preview;
                case VIEW_TYPE_NEXT_PAGE:
                    return R.layout.board_footer_view;
                default:
                    throw new IllegalArgumentException("Wrong view type received");
            }
        }

        @Override
        public ThreadWithPreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            double start = System.nanoTime();
            View postcard = LayoutInflater.from(getContext()).inflate(getLayoutIdForViewType(viewType), parent, false);
            if (page == 0 && viewType == VIEW_TYPE_PREV_PAGE) {
                postcard.findViewById(R.id.frame_header_container).setVisibility(View.GONE);
            }
            ThreadWithPreviewViewHolder tpvh = new ThreadWithPreviewViewHolder(postcard);
            Log.i("onCreateViewHolder", "Time: " + Double.toString(((double) System.nanoTime() - start) / 10e5));
            return tpvh;
        }

        @Override
        public void onBindViewHolder(final ThreadWithPreviewViewHolder holder, int position) {
            long start = System.nanoTime();
            switch (getItemViewType(position)) {
                case VIEW_TYPE_NEXT_PAGE:
                case VIEW_TYPE_PREV_PAGE:
                    return;
            }
            int threadIndex = position - 1;

            HanabiraThread thread = threads.get(threadIndex);
            HanabiraPost op = Hanabira.getCache().findPostByDisplayId(thread.getDispayId());
//            HanabiraPost op = Hanabira.getCache().findPostByDisplayId(thread.getPosts().firstEntry().getValue());
            holder.setStaticText(thread, op);

            if (op.getFiles() == null || op.getFiles().size() == 0) {
                holder.filesScroller.setVisibility(View.GONE);
            }
            holder.expandBtn.setEnabled(true);
            holder.expandBtn.setText("Expand");
            holder.recentBtn.setEnabled(thread.getPostsCount() > 1);

            keeper.bind(holder, position);

//            filesScroller.setLayoutManager(
//                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//            filesScroller.setAdapter(new FilesAdapter(/*todo*/));
            double ms = ((double) (System.nanoTime() - start)) / 10e5;
            if (ms > 16) {
                Log.w("onBindViewHolder", "Time: " + ms);
            } else { Log.i("onBindViewHolder", "Time: " + ms); }
        }


        @Override
        public int getItemCount() {
            return threads.size() + 2; // prevPage+[threads]+nextPage
        }

        @Override
        public int getItemViewType(int position) {
            if (position > 0) {
                if (position < threads.size() + 1) {
                    return VIEW_TYPE_THREAD;
                } else {
                    return VIEW_TYPE_NEXT_PAGE;
                }
            } else {
                return VIEW_TYPE_PREV_PAGE;
            }
        }

        public class ThreadWithPreviewViewHolder extends RecyclerView.ViewHolder implements Expandable {

            public final TextView             threadTitle;
            public final PostHolder           postHolder;
            public final HorizontalScrollView filesScroller;
            public final LinearLayout         previewList;
            public final TextView             replies;
            public final Button               optionsBtn;
            public final Button               expandBtn;
            public final Button               recentBtn;
            public final Button               openBtn;


            public final List<PostHolder> recents;

            public ThreadWithPreviewViewHolder(View itemView) {
                super(itemView);
                threadTitle = (TextView) itemView.findViewById(R.id.text_thread_title);

                postHolder = new PostHolder(itemView);
                replies = (TextView) itemView.findViewById(R.id.text_post_content_replies);
                filesScroller = (HorizontalScrollView) itemView.findViewById(R.id.post_files_scroller);
                previewList = (LinearLayout) itemView.findViewById(R.id.layout_thread_expandable_posts_preview);
                optionsBtn = (Button) itemView.findViewById(R.id.thread_controls_options);
                expandBtn = (Button) itemView.findViewById(R.id.thread_controls_expand);
                recentBtn = (Button) itemView.findViewById(R.id.thread_controls_recent);
                openBtn = (Button) itemView.findViewById(R.id.thread_controls_open);

                if (optionsBtn != null) {
                    optionsBtn.setOnClickListener(null);
                    // hope either not null
                    expandBtn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            System.out.println(v);
                            expandBtn.setEnabled(postHolder.message.getLineCount() > ELLIPSIZE_MAX_LINES);
                            if (postHolder.message.getMaxLines() == Integer.MAX_VALUE) {
                                postHolder.message.setMaxLines(ELLIPSIZE_MAX_LINES);
                                expandBtn.setText("Expand");
                            } else {
                                postHolder.message.setMaxLines(Integer.MAX_VALUE);
                                expandBtn.setText("Collapse");
                            }
                            animateItemViewHeight().start();
                        }
                    });
                    recentBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            keeper.toggle(ThreadWithPreviewViewHolder.this);
                        }
                    });
                    openBtn.setOnClickListener(null);
                }

                recents = new ArrayList<>(recentListSize);
                if (previewList != null) {
                    previewList.removeAllViews();
                    int oneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                                                                getResources().getDisplayMetrics());
                    for (int i = 0; i < recentListSize; i++) {
                        previewList.addView(createDivider(getContext(), oneDp));
                        LayoutInflater.from(getContext()).inflate(R.layout.layout_post, previewList);
                        View v = previewList.getChildAt(2 * i + 1);
                        recents.add(new PostHolder(v));
                    }
                }

            }

            public void setStaticText(HanabiraThread thread, HanabiraPost op) {
                threadTitle.setText(thread.getTitle());
                postHolder.fillWithData(op);
                postHolder.message.setMaxLines(ELLIPSIZE_MAX_LINES);
                List<Integer> recentsList = thread.getLastN(3);
                int i = 0;
                for (; i < recentsList.size(); i++) {
                    recents.get(i).fillWithData(recentsList.get(i));
                }
                for (; i < recents.size(); i++) {
                    recents.get(i).hide();
                }
            }

            private View createDivider(Context context, int oneDp) {
                View divider = new View(context);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, oneDp);
                ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(lp);
                mlp.setMargins(0, 2 * oneDp, 0, 2 * oneDp);
                divider.setLayoutParams(mlp);
                return divider;
            }

            @Override
            public View getExpandView() {
                return previewList;
            }

            public void openHolder(final boolean animate) {
                final View expandView = getExpandView();
                if (animate) {
                    expandView.setVisibility(View.VISIBLE);
                    final Animator animator = animateItemViewHeight();
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(expandView, View.ALPHA, 1);
                            alphaAnimator.addListener(new NotRecycleAdapter());
                            alphaAnimator.setInterpolator(new DecelerateInterpolator());
                            alphaAnimator.start();
                        }
                    });
                    animator.start();
                } else {
                    expandView.setVisibility(View.VISIBLE);
                    expandView.setAlpha(1);
                }
            }

            public void closeHolder(final boolean animate) {
                final View expandView = getExpandView();
                if (animate) {
                    expandView.setVisibility(View.GONE);
                    final Animator animator = animateItemViewHeight();
                    expandView.setVisibility(View.VISIBLE);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(expandView, View.ALPHA, 0);
                            alphaAnimator.addListener(new NotRecycleAdapter());
                            alphaAnimator.setInterpolator(new DecelerateInterpolator());
                            alphaAnimator.start();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            expandView.setVisibility(View.GONE);
                            expandView.setAlpha(0);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            expandView.setVisibility(View.GONE);
                            expandView.setAlpha(0);
                        }
                    });
                    animator.start();
                } else {
                    expandView.setVisibility(View.GONE);
                    expandView.setAlpha(0);
                }
            }

            public Animator animateItemViewHeight() {
                View parent = (View) itemView.getParent();
                if (parent == null) {
                    throw new IllegalStateException("Cannot animate the layout of a view that has no parent");
                }

                int start = itemView.getMeasuredHeight();
                itemView.measure(View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.AT_MOST),
                                 View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int end = itemView.getMeasuredHeight();

                final ValueAnimator animator = ValueAnimator.ofInt(start, end);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final ViewGroup.LayoutParams lp = itemView.getLayoutParams();
                        lp.height = (int) animation.getAnimatedValue();
                        itemView.setLayoutParams(lp);
                    }

                });

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        final ViewGroup.LayoutParams params = itemView.getLayoutParams();
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        itemView.setLayoutParams(params);
                    }
                });

                animator.setInterpolator(new FastOutSlowInInterpolator());
                animator.addListener(new NotRecycleAdapter());

                return animator;
            }

            public class NotRecycleAdapter extends AnimatorListenerAdapter {

                @Override
                public void onAnimationStart(Animator animation) { setIsRecyclable(false); }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setIsRecyclable(true);
                }

                @Override
                public void onAnimationCancel(Animator animation) { setIsRecyclable(true); }
            }
        }

    }

    public class KeepOneHolderOpen {
        private int _opened = -1;

        public void bind(BoardAdapter.ThreadWithPreviewViewHolder holder, int pos) {
            if (pos == _opened) { holder.openHolder(false); } else {
                holder.closeHolder(false);
            }
        }

        public void toggle(BoardAdapter.ThreadWithPreviewViewHolder holder) {
            scrollListener.expandTriggered();
            if (_opened == holder.getLayoutPosition()) {
                _opened = -1;
                holder.closeHolder(true);
            } else {
                int previous = _opened;
                _opened = holder.getLayoutPosition();
                holder.openHolder(true);

                final BoardAdapter.ThreadWithPreviewViewHolder oldHolder = (BoardAdapter.ThreadWithPreviewViewHolder)
                        ((RecyclerView) holder.itemView
                        .getParent()).findViewHolderForLayoutPosition(previous);
                if (oldHolder != null) { oldHolder.closeHolder(true); }
            }
        }

    }


    private class RecyclerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
//            View view = recycler.findChildViewUnder(e.getX(), e.getY());
//            for (View v : view.getTouchables()) {
//                if (v.getId() == R.id.text_post_content_message) {
//                    v.onTouchEvent(e);
//                }
//            }
            return super.onSingleTapConfirmed(e);
        }


        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

    }


    private class FilesAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        protected class FileViewHolder extends RecyclerView.ViewHolder {

            View view;

            public FileViewHolder(View view) {
                super(view);
                this.view = view;
            }
        }
    }

    private class TouchEventInterceptor implements RecyclerView.OnItemTouchListener {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            detector.onTouchEvent(e);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}

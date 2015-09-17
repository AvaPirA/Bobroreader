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
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;
import com.avapira.bobroreader.util.Consumer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BoardFragment extends Fragment {

    private static final String TAG                 = BoardFragment.class.getSimpleName();
    private static final String ARG_KEY             = "arg_board_key";
    private static final String ARG_PAGE            = "arg_board_page";
    private static final String ARG_RECYCLER_LAYOUT = "arg_board_recycler_layout";
    private ProgressBar          progressBar;
    private RecyclerView         recycler;
    private HidingScrollListener scrollListener;
    private int recentListSize = 3;
    private String       boardKey;
    private int          page;
    private Castor       supervisor;
    private BoardAdapter boardAdapter;

    public BoardFragment() {
        // Required empty public constructor
    }

    private class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ThreadWithPreviewViewHolder> {

        public static final int VIEW_TYPE_PREV_PAGE = 1;
        public static final int VIEW_TYPE_THREAD    = 2;
        public static final int VIEW_TYPE_NEXT_PAGE = 3;
        final List<Integer> threadIds;
        private final int ELLIPSIZE_MAX_LINES = 15;
        private final List<Boolean> requestFillRecentPosts;
        private int whereIsRecentPostsShownPosition = -239;

        public BoardAdapter(List<Integer> tt) {
            threadIds = tt;
            requestFillRecentPosts = new ArrayList<>(tt.size());
            for (int i = 0; i < tt.size(); i++) {
                requestFillRecentPosts.add(true);
            }
            Hanabira.getStem().asyncParse(threadIds, recentListSize);
        }

        public class ThreadWithPreviewViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final TextView             threadTitle;
            final PostHolder           postHolder;
            final HorizontalScrollView filesScroller;
            final LinearLayout         previewList;
            final TextView             replies;
            final Button               optionsBtn;
            final ImageButton          expandBtn;
            final Button               recentBtn;
            final Button               openBtn;


            public PostHolder[] recents;

            public ThreadWithPreviewViewHolder(final View itemView) {
                super(itemView);
                threadTitle = (TextView) itemView.findViewById(R.id.text_thread_title);

                postHolder = new PostHolder(itemView);
                replies = (TextView) itemView.findViewById(R.id.text_post_content_replies);
                filesScroller = (HorizontalScrollView) itemView.findViewById(R.id.post_files_scroller);
                previewList = (LinearLayout) itemView.findViewById(R.id.layout_thread_expandable_posts_preview);
                optionsBtn = (Button) itemView.findViewById(R.id.thread_controls_options);
                expandBtn = (ImageButton) itemView.findViewById(R.id.thread_controls_expand);
                recentBtn = (Button) itemView.findViewById(R.id.thread_controls_recent);
                openBtn = (Button) itemView.findViewById(R.id.thread_controls_open);

                if (previewList != null) {
                    for (int i = 0; i < recentListSize; i++) {
                        LayoutInflater.from(getContext()).inflate(R.layout.layout_post, previewList);
                    }
                    recents = new PostHolder[previewList.getChildCount()];
                    for (int i = 0; i < recents.length; i++) {
                        recents[i] = new PostHolder(previewList.getChildAt(i));
                    }
                }

                if (optionsBtn != null) {
                    optionsBtn.setOnClickListener(this);
                    // hope either not null
                    expandBtn.setOnClickListener(this);
                    recentBtn.setOnClickListener(this);
                    openBtn.setOnClickListener(this);
                }
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

            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.thread_controls_options:
                        return;
                    case R.id.thread_controls_expand:
                        onExpandClick();
                        break;
                    case R.id.thread_controls_open:
                        onOpenClick();
                        break;
                    case R.id.thread_controls_recent:
                        onRecentClick();
                        break;
                }
            }

            private void onRecentClick() {
                toggle(ThreadWithPreviewViewHolder.this);
            }

            private void onOpenClick() {
                supervisor.onThreadSelected(
                        Hanabira.getStem().findPostByDisplayId(threadIds.get(getAdapterPosition() - 1)).getThreadId());
            }

            public void onExpandClick() {
                final int now = postHolder.message.getMaxLines();
                final int will = now == Integer.MAX_VALUE ? ELLIPSIZE_MAX_LINES : Integer.MAX_VALUE;
                final boolean growth = will > now;
                if (postHolder.message.getLineCount() <= ELLIPSIZE_MAX_LINES) {
                    expandBtn.animate()
                             .rotation(360f)
                             .setDuration(500)
                             .setInterpolator(new BounceInterpolator())
                             .withEndAction(new Runnable() {
                                 @Override
                                 public void run() {
                                     expandBtn.setRotation(0);
                                 }
                             })
                             .start();
                    return;
                }
                scrollListener.expandTriggered();
                postHolder.message.setMaxLines(will); // set to measure
                final Animator animator = animateViewHeight(postHolder.message);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (postHolder.message.getLineCount() != will) {
                            postHolder.message.setMaxLines(will);
                        }
                        postHolder.message.measure(View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.AT_MOST),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                        System.out.println(postHolder.message.getMeasuredHeight());
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        expandBtn.animate().rotation(growth ? 180f : 360f).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                expandBtn.setRotation(growth ? 180f : 0f);
                            }
                        }).setInterpolator(new FastOutSlowInInterpolator()).start();
                        postHolder.message.setMaxLines(Integer.MAX_VALUE);
                    }
                });
                animator.start();
            }

            public void openHolder(final boolean animate) {
                int threadIndex = getLayoutPosition() - 1;
                if (requestFillRecentPosts.get(threadIndex)) {
                    requestFillRecentPosts.set(threadIndex, false);
                    List<Integer> recentsList = Hanabira.getStem()
                                                        .findThreadByDisplayId(threadIds.get(getLayoutPosition() - 1))
                                                        .getLastN(recentListSize);
                    int i = 0;
                    for (; i < recentsList.size(); i++) {
                        recents[i].fillWithData(recentsList.get(i));
                    }
                }

                if (animate) {
                    previewList.setVisibility(View.VISIBLE);
                    final Animator animator = animateViewHeight(itemView);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(previewList, View.ALPHA, 1);
                            alphaAnimator.addListener(new NotRecycleAdapter());
                            alphaAnimator.setInterpolator(new DecelerateInterpolator());
                            alphaAnimator.start();
                        }
                    });
                    animator.start();
                } else {
                    previewList.setVisibility(View.VISIBLE);
                    previewList.setAlpha(1);
                }
            }

            public void closeHolder(final boolean animate) {
                if (animate) {
                    previewList.setVisibility(View.GONE);
                    final Animator animator = animateViewHeight(itemView);
                    previewList.setVisibility(View.VISIBLE);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(previewList, View.ALPHA, 0);
                            alphaAnimator.addListener(new NotRecycleAdapter());
                            alphaAnimator.setInterpolator(new DecelerateInterpolator());
                            alphaAnimator.start();
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            previewList.setVisibility(View.GONE);
                            previewList.setAlpha(0);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            previewList.setVisibility(View.GONE);
                            previewList.setAlpha(0);
                        }
                    });
                    animator.start();
                } else {
                    previewList.setVisibility(View.GONE);
                    previewList.setAlpha(0);
                }
            }

            public Animator animateViewHeight(final View v) {
                View parent = (View) v.getParent();
                if (parent == null) {
                    throw new IllegalStateException("Cannot animate the layout of a view that has no parent");
                }

                int start = v.getMeasuredHeight();
                System.out.println("Start: " + start);
                v.measure(View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                int end = v.getMeasuredHeight();
                System.out.println("End: " + end);
                final ValueAnimator animator = ValueAnimator.ofInt(start, end);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        final ViewGroup.LayoutParams lp = v.getLayoutParams();
                        lp.height = (int) animation.getAnimatedValue();
                        v.setLayoutParams(lp);
                    }

                });

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        final ViewGroup.LayoutParams params = v.getLayoutParams();
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        v.setLayoutParams(params);
                    }
                });

                animator.setInterpolator(new FastOutSlowInInterpolator());
                animator.addListener(new NotRecycleAdapter());

                return animator;
            }
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
            if (viewType == VIEW_TYPE_PREV_PAGE && page == 0) {
                postcard.findViewById(R.id.board_header_text).setVisibility(View.GONE);
            }
            if (viewType == VIEW_TYPE_NEXT_PAGE &&
                    page == Hanabira.getStem().findBoardByKey(boardKey).getPagesCount() - 1) {
                postcard.findViewById(R.id.frame_footer_container).setVisibility(View.GONE);
            }
            ThreadWithPreviewViewHolder tpvh = new ThreadWithPreviewViewHolder(postcard);
            Log.i("onCreateViewHolder", "Time: " + Double.toString(((double) System.nanoTime() - start) / 10e5));
            return tpvh;
        }

        @Override
        public void onBindViewHolder(final ThreadWithPreviewViewHolder holder, int position) {
            long start = System.nanoTime();
            boolean borderItem = true;
            switch (getItemViewType(position)) {
                case VIEW_TYPE_NEXT_PAGE:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchPage(page + 1);
                        }
                    });
                    break;
                case VIEW_TYPE_PREV_PAGE:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchPage(page - 1);
                        }
                    });
                    break;
                default:
                    borderItem = false;
            }
            if (borderItem) { return; }

            int threadIndex = position - 1;
            int threadDisplayId = threadIds.get(threadIndex);

            HanabiraThread thread = Hanabira.getStem().findThreadByDisplayId(threadDisplayId);
            HanabiraPost op = Hanabira.getStem().findPostByDisplayId(threadDisplayId);
//            HanabiraPost op = Hanabira.getStem().findPostByDisplayId(thread.getPosts().firstEntry().getValue());
            holder.threadTitle.setText(thread.getTitle());
            holder.postHolder.fillWithData(op);
            holder.postHolder.message.setMaxLines(ELLIPSIZE_MAX_LINES);// reset the
            holder.expandBtn.setRotation(0f);                          // expanding

            if (op.getFiles() == null || op.getFiles().size() == 0) {
                holder.filesScroller.setVisibility(View.GONE);
            }
            holder.recentBtn.setEnabled(thread.getPostsCount() > 1);
            requestFillRecentPosts.set(threadIndex, true);

            prepare(holder, position);

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
            return threadIds.size() + 2; // prevPage+[threadIds]+nextPage
        }

        @Override
        public int getItemViewType(int position) {
            if (position > 0) {
                if (position < threadIds.size() + 1) {
                    return VIEW_TYPE_THREAD;
                } else {
                    return VIEW_TYPE_NEXT_PAGE;
                }
            } else {
                return VIEW_TYPE_PREV_PAGE;
            }
        }

        public void prepare(BoardAdapter.ThreadWithPreviewViewHolder holder, int position) {
            if (position == whereIsRecentPostsShownPosition) {
                holder.openHolder(false);
            } else {
                holder.closeHolder(false);
            }
        }

        public boolean toggle(BoardAdapter.ThreadWithPreviewViewHolder holder) {
            scrollListener.expandTriggered();
            if (whereIsRecentPostsShownPosition == holder.getLayoutPosition()) {
                whereIsRecentPostsShownPosition = -239;
                holder.closeHolder(true);
                return false;
            } else {
                int previous = whereIsRecentPostsShownPosition;
                whereIsRecentPostsShownPosition = holder.getLayoutPosition();
                holder.openHolder(true);

                final BoardAdapter.ThreadWithPreviewViewHolder oldHolder = (BoardAdapter.ThreadWithPreviewViewHolder)
                        ((RecyclerView) holder.itemView
                        .getParent()).findViewHolderForLayoutPosition(previous);
                if (oldHolder != null) { oldHolder.closeHolder(true); }
                return true;
            }
        }

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NotificationFragment.
     */
    public static BoardFragment newInstance(String boardKey) {
        Log.d(TAG, "new instance");
        BoardFragment fragment = new BoardFragment();
        Bundle b = new Bundle();
        b.putString(ARG_KEY, boardKey);
        b.putInt(ARG_PAGE, 1);
        fragment.setArguments(b);
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d(TAG, "restore");
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            boardKey = savedInstanceState.getString("board");
            page = savedInstanceState.getInt("page");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "create");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardKey = boardKey == null ? getArguments().getString(ARG_KEY) : boardKey;
        }
    }

    private void switchPage(final int newPage) {
        Log.d(TAG, "switch page from " + page + " to " + newPage);
        supervisor.retitleOnLoading();
        progressBar.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.INVISIBLE);
        page = newPage;
        Hanabira.getFlower().getBoardPage(boardKey, page, new Consumer<List<Integer>>() {
            @Override
            public void accept(List<Integer> hanabiraThreads) {
                boardAdapter = new BoardAdapter(hanabiraThreads);
                hookUpThreads();
            }
        });
    }

    /**
     * @deprecated Use {@link #onAttach(Context)} instead.
     */
    @Deprecated
    public void onAttach(Activity activity) {
        Log.d(TAG, "attach");
        super.onAttach(activity);
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            recentListSize = Integer.parseInt(prefs.getString("pref_board_recent_list_size", "3"));
            supervisor = (Castor) activity;
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "detach");
        super.onDetach();
        supervisor = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "SIS");
        super.onSaveInstanceState(outState);
        outState.putString(ARG_KEY, boardKey);
        outState.putInt(ARG_PAGE, page);
        outState.putParcelable(ARG_RECYCLER_LAYOUT, recycler.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "activity created (open bundle)");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            boardKey = savedInstanceState.getString(ARG_KEY, null);
            page = savedInstanceState.getInt(ARG_PAGE, 0);
            recycler.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(ARG_RECYCLER_LAYOUT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scroller, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "view created (find and switch)");

        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        recycler = (RecyclerView) view.findViewById(R.id.thread_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        if (boardAdapter == null) {
            // open board page
            scrollListener = new HidingScrollListener(getContext());
            switchPage(page);
        } else {
            // popping from fragments stack
            hookUpThreads();
            // should be run immediately because it's already main thread
        }

        scrollListener.resetContainer((FrameLayout) getActivity().findViewById(R.id.frame_toolbar_container));
        recycler.addOnScrollListener(scrollListener);
    }

    private void hookUpThreads() {
        Log.d(TAG, "hook up page " + page);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recycler.setAdapter(boardAdapter);
                progressBar.setVisibility(View.GONE);
                recycler.setVisibility(View.VISIBLE);
                supervisor.retitleOnBoardLoad(boardKey, page);
            }
        });
    }

    @Override
    public void onDestroyView() {
        recycler = null;
        progressBar = null;
        Log.d(TAG, "destroy view");
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "start");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "resume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "pause");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "stop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroy");
        super.onDestroy();
    }
}

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
import android.annotation.TargetApi;
import android.app.Fragment;
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
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
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
        recycler.addOnItemTouchListener(new TouchEventInterceptor());
        scrollListener = new HidingScrollListener();
        recycler.addOnScrollListener(scrollListener);
        detector = new GestureDetectorCompat(getActivity(), new RecyclerGestureListener());
        switchPage(page);
    }

    /**
     * Thanks <a href =http://rylexr.tinbytes.com/2015/04/27/how-to-hideshow-android-toolbar-when-scrolling-google
     * -play-musics-behavior>that</a> blog for a complete solution
     * TODO apply fixes from comments
     */
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

    private class BoardAdapter extends RecyclerView.Adapter<ThreadWithPreviewViewHolder> {
        public static final int VIEW_TYPE_PREV_PAGE = 1;
        public static final int VIEW_TYPE_THREAD    = 2;
        public static final int VIEW_TYPE_NEXT_PAGE = 3;
        private final       int recentListSize      = 3;
        List<HanabiraThread> threads = new ArrayList<>();

        KeepOneH<ThreadWithPreviewViewHolder> keeper = new KeepOneH<>();

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
            ThreadWithPreviewViewHolder tpvh = new ThreadWithPreviewViewHolder(postcard, recentListSize, getContext(),
                                                                               keeper);
            Log.i("onCreateViewHolder", "Time: " + Double.toString(((double) System.nanoTime() - start) / 10e5));
            return tpvh;
        }

        @Override
        public void onBindViewHolder(final ThreadWithPreviewViewHolder holder, int position) {
            long start = System.nanoTime();
            DebugTimer.start();
            switch (getItemViewType(position)) {
                case VIEW_TYPE_NEXT_PAGE:
                case VIEW_TYPE_PREV_PAGE:
                    return;
            }
            int threadIndex = position - 1;

            HanabiraThread thread = threads.get(threadIndex);
            HanabiraPost op = Hanabira.getCache().findPostByDisplayId(thread.getDispayId());
//            HanabiraPost op = Hanabira.getCache().findPostByDisplayId(thread.getPosts().firstEntry().getValue());
            DebugTimer.lap("cache");
            holder.setStaticText(thread, op);

            if (op.getFiles() == null || op.getFiles().size() == 0) {
                holder.filesScroller.setVisibility(View.GONE);
            }
            holder.recentBtn.setEnabled(thread.getPostsCount() > 1);
            DebugTimer.lap("flags");

            keeper.bind(holder, position, scrollListener);
            DebugTimer.lap("kpbind");

//            filesScroller.setLayoutManager(
//                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//            filesScroller.setAdapter(new FilesAdapter(/*todo*/));
            double ms = ((double) (System.nanoTime() - start)) / 10e5;
            if (ms > 16) {
                DebugTimer.logLaps();
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

    }


    private class RecyclerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recycler.findChildViewUnder(e.getX(), e.getY());
            Toast.makeText(getContext(), Integer.toString(recycler.getChildViewHolder(view).getLayoutPosition()),
                           Toast.LENGTH_SHORT).show();
            for (View v : view.getTouchables()) {
                if (v.getId() == R.id.text_post_content_message) {
                    v.onTouchEvent(e);
                }
            }
            View view2 = view.findViewById(R.id.layout_thread_expandable_posts_preview);
            for (View v : view2.getTouchables()) {
                if (v.getId() == R.id.text_post_content_message) {
                    v.onTouchEvent(e);
                }
            }
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

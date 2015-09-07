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
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import com.android.volley.Response;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.HanabiraParser;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;
import org.joda.time.format.DateTimeFormat;

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
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recycler.addOnItemTouchListener(new TouchEventInterceptor());
        recycler.addOnScrollListener(new HidingScrollListener());
        detector = new GestureDetectorCompat(getActivity(), new RecyclerGestureListener());
        switchPage(page);
    }

    /**
     * Thanks <a href =http://rylexr.tinbytes.com/2015/04/27/how-to-hideshow-android-toolbar-when-scrolling-google
     * -play-musics-behavior>that</a> blog for a complete solution
     * TODO apply fixes from comments
     */
    public class HidingScrollListener extends RecyclerView.OnScrollListener {
        private final float TOOLBAR_ELEVATION = getResources().getDimension(R.dimen.small);
        // Keeps track of the overall vertical offset in the list
        int verticalOffset;

        // Determines the scroll UP/DOWN direction
        boolean scrollingUp;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                final int height = toolbarContainer.getHeight();
                if (scrollingUp) {
                    if (verticalOffset > height) {
                        toolbarAnimateHide();
                    } else {
                        toolbarAnimateShow(verticalOffset);
                    }
                } else {
                    if (toolbarContainer.getTranslationY() < height * -0.6 && verticalOffset > height) {
                        toolbarAnimateHide();
                    } else {
                        toolbarAnimateShow(verticalOffset);
                    }
                }
            }
        }

        @Override
        public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            verticalOffset += recyclerView.computeVerticalScrollOffset();
            scrollingUp = dy > 0;
            int toolbarYOffset = (int) (dy - toolbarContainer.getTranslationY());
            toolbarContainer.animate().cancel();
            final int height = toolbarContainer.getHeight();
            if (scrollingUp) {
                if (toolbarYOffset < height) {
                    if (verticalOffset > height) {
                        toolbarSetElevation(TOOLBAR_ELEVATION);
                    }
                    toolbarContainer.setTranslationY(-toolbarYOffset);
                } else {
                    toolbarSetElevation(0);
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

        private void toolbarAnimateShow(final int verticalOffset) {
            toolbarContainer.animate()
                            .translationY(0)
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180)
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
                            .setInterpolator(new LinearInterpolator())
                            .setDuration(180)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    toolbarSetElevation(0);
                                }
                            });
        }
    }

    private class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ThreadPreviewViewHolder> {
        private class ViewType {
            public static final int PREV_PAGE = 1;
            public static final int THREAD    = 2;
            public static final int NEXT_PAGE = 3;
        }

        List<HanabiraThread> threads = new ArrayList<>();
        List<CharSequence> cachedParsedPosts;

        public BoardAdapter(List<HanabiraThread> tt) {
            threads = tt;
            cachedParsedPosts = new ArrayList<>(threads.size());
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < threads.size(); i++) {
                        HanabiraPost post = Hanabira.getCache().findPostByDisplayId(threads.get(i).getDispayId());
                        cachedParsedPosts.add(i, new HanabiraParser(post, getContext()).getFormatted());
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "parsed", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        }

        private final float opPostElevation = 5 * BoardFragment.this.getResources().getDimension(R.dimen.micro) / 2;

        @Override
        public ThreadPreviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            long start = System.nanoTime();
            View postcard;
            TextView threadTitle = null, displayId = null, authorName = null, rightHeader = null, text = null,
                    replies = null, recentBtn = null;
            CardView card = null;
            HorizontalScrollView filesScroller = null;
            LinearLayout expandablePreviews = null;
            switch (viewType) {
                case ViewType.PREV_PAGE:
                    postcard = LayoutInflater.from(getContext()).inflate(R.layout.board_header_view, parent, false);
                    if (page == 0) {
                        postcard.findViewById(R.id.frame_header_container).setVisibility(View.GONE);
                    }
                    break;
                case ViewType.THREAD:
                    postcard = LayoutInflater.from(getContext())
                                             .inflate(R.layout.layout_thread_with_preview, parent, false);
                    threadTitle = (TextView) postcard.findViewById(R.id.text_thread_title);
                    displayId = (TextView) postcard.findViewById(R.id.text_post_header_display_id);
                    authorName = (TextView) postcard.findViewById(R.id.text_post_header_author_name);
                    rightHeader = (TextView) postcard.findViewById(R.id.text_post_header_datetime);
                    text = (TextView) postcard.findViewById(R.id.text_post_content_message);
                    replies = (TextView) postcard.findViewById(R.id.text_post_content_replies);
                    card = (CardView) postcard.findViewById(R.id.card_post);
                    filesScroller = (HorizontalScrollView) postcard.findViewById(R.id.post_files_scroller);
                    expandablePreviews = (LinearLayout) postcard.findViewById(
                            R.id.layout_thread_expandable_posts_preview);
                    recentBtn = (TextView) postcard.findViewById(R.id.thread_controls_recent);
                    break;
                case ViewType.NEXT_PAGE:
                    postcard = LayoutInflater.from(getContext()).inflate(R.layout.board_footer_view, parent, false);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong view type received");
            }
            ThreadPreviewViewHolder tpvh = new ThreadPreviewViewHolder(postcard, threadTitle, card, displayId,
                    authorName, rightHeader, text, filesScroller, expandablePreviews, replies, recentBtn);
            long time = System.nanoTime() - start;
            Log.i("onCreateViewHolder", "Time: " + Double.toString(((double) time) / 10e6));
            return tpvh;
        }

        @Override
        public void onBindViewHolder(final ThreadPreviewViewHolder holder, int position) {
            long start = System.nanoTime();
            switch (getItemViewType(position)) {
                case ViewType.NEXT_PAGE:
                case ViewType.PREV_PAGE:
                    return;
            }
            int threadNumOnPage = position - 1;

            HanabiraThread thread = threads.get(threadNumOnPage);
            HanabiraPost op = Hanabira.getCache().findPostByDisplayId(thread.getPosts().firstEntry().getValue());

            holder.threadTitle.setText(thread.getTitle());
            holder.displayId.setText("№".concat(Integer.toString(op.getDisplayId())));
            holder.authorName.setText(op.getName());
            holder.rightHeader.setText(DateTimeFormat.forPattern("dd MMMM yyyy (EEE)\nHH:mm:ss").print(op.getDate()));
            if (cachedParsedPosts.size() > threadNumOnPage) {
                holder.text.setText(cachedParsedPosts.get(threadNumOnPage));
            } else {
                holder.text.setText(new HanabiraParser(op, getContext()).getFormatted());
                Log.w("onBindViewHolder", "Parser cache miss at " + threadNumOnPage);
            }
            holder.text.setMovementMethod(LinkMovementMethod.getInstance());
            holder.card.setCardElevation(opPostElevation);
            if (op.getFiles() == null || op.getFiles().size() == 0) {
                holder.filesRecycler.setVisibility(View.GONE);
            }
            final int[] PREVIEW_HEIGHT = new int[1];
            holder.itemView.findViewById(R.id.layout_thread_expandable_posts_preview).getViewTreeObserver()
                       .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                           @Override
                           public void onGlobalLayout() {
                               PREVIEW_HEIGHT[0] = holder.previewList.getHeight();
                               holder.previewList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                               holder.previewList.setVisibility(View.GONE);
                           }

                       });
            holder.recentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int visibility = holder.previewList.getVisibility();
                    if (visibility == View.GONE) {
                        expand(holder.previewList);
                    } else {
                        collapse(holder.previewList);
                    }
                }

                private void expand(final View v) {
                    v.setVisibility(View.VISIBLE);
                    ValueAnimator mAnimator = slideAnimator(0, PREVIEW_HEIGHT[0]);
                    mAnimator.start();
                }

                private void collapse(final View v) {
                    ValueAnimator mAnimator = slideAnimator(PREVIEW_HEIGHT[0], 0);
                    mAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {}

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            //Height=0, but it set visibility to GONE
                            v.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {}

                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    });
                    mAnimator.start();
                }


                private ValueAnimator slideAnimator(int start, int end) {

                    ValueAnimator animator = ValueAnimator.ofInt(start, end);

                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            //Update Height
                            int value = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = holder.previewList.getLayoutParams();
                            layoutParams.height = value;
                            holder.previewList.setLayoutParams(layoutParams);
                        }
                    });
                    return animator;
                }
            });
//            filesRecycler.setLayoutManager(
//                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//            filesRecycler.setAdapter(new FilesAdapter(/*todo*/));
            long time = System.nanoTime() - start;
            Log.i("onBindViewHolder", "Time: " + Double.toString(((double) time) / 10e6));
        }


        @Override
        public int getItemCount() {
            return threads.size() + 2; // prevPage+[threads]+nextPage
        }

        @Override
        public int getItemViewType(int position) {
            if (position > 0) {
                if (position < threads.size() + 1) {
                    return ViewType.THREAD;
                } else {
                    return ViewType.NEXT_PAGE;
                }
            } else {
                return ViewType.PREV_PAGE;
            }
        }


        protected class ThreadPreviewViewHolder extends RecyclerView.ViewHolder {

            public final TextView             threadTitle;
            public final CardView             card;
            public final TextView             displayId;
            public final TextView             authorName;
            public final TextView             rightHeader;
            public final TextView             text;
            public final HorizontalScrollView filesRecycler;
            public final LinearLayout         previewList;
            public final TextView             replies;
            public final TextView             recentBtn;

            public ThreadPreviewViewHolder(View itemView,
                                           TextView threadTitle,
                                           CardView card,
                                           TextView displayId,
                                           TextView authorName,
                                           TextView rightHeader,
                                           TextView text,
                                           HorizontalScrollView filesRecycler,
                                           final LinearLayout previewList,
                                           TextView replies,
                                           TextView recentBtn) {
                super(itemView);
                this.threadTitle = threadTitle;
                this.card = card;
                this.displayId = displayId;
                this.authorName = authorName;
                this.rightHeader = rightHeader;
                this.text = text;
                this.filesRecycler = filesRecycler;
                this.previewList = previewList;
                this.replies = replies;
                this.recentBtn = recentBtn;
            }
        }
    }

    private class RecyclerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recycler.findChildViewUnder(e.getX(), e.getY());
            CardView cv = (CardView) view.findViewById(R.id.card_post);
            TextView tv = ((TextView) view.findViewById(R.id.text_post_content_message));

            Toast.makeText(getContext(), tv.getHeight() + ":" + cv.getHeight(), Toast.LENGTH_SHORT).show();

            tv.onTouchEvent(e);
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

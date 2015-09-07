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

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.volley.Response;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.HanabiraParser;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;
import com.avapira.bobroreader.util.TestCardViewFragment;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BoardFragment extends Fragment {

    private static final String TAG = ThreadFragment.class.getSimpleName();


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
    public static TestCardViewFragment newInstance() {
        TestCardViewFragment fragment = new TestCardViewFragment();
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
        progressBar.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.INVISIBLE);
        page = newPage;
        Hanabira.getFlower().updateBoardPage(boardKey, page, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                HanabiraBoard hanabiraBoard = HanabiraBoard.fromJson(response, HanabiraBoard.class);
                final BoardAdapter boardAdapter = new BoardAdapter(hanabiraBoard.getPageThreads(page));
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recycler.setAdapter(boardAdapter);
                        progressBar.setVisibility(View.GONE);
                        recycler.setVisibility(View.VISIBLE);
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
        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        recycler = (RecyclerView) view.findViewById(R.id.thread_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recycler.addOnItemTouchListener(new TouchEventInterceptor());
        detector = new GestureDetectorCompat(getActivity(), new RecyclerGestureListener());
        switchPage(page);
    }

    private class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ThreadPreviewViewHolder> {
        private class ViewTypes {
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
            switch (viewType) {
                case ViewTypes.PREV_PAGE:
                    postcard = LayoutInflater.from(getContext()).inflate(R.layout.board_header_view, parent, false);
                    break;
                case ViewTypes.THREAD:
                    postcard = LayoutInflater.from(getContext()).inflate(R.layout.thread_with_preview, parent, false);
                    break;
                case ViewTypes.NEXT_PAGE:
                    postcard = LayoutInflater.from(getContext()).inflate(R.layout.thread_footer, parent, false);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong view type received");
            }
            ThreadPreviewViewHolder tpvh = new ThreadPreviewViewHolder(postcard);
            long time = System.nanoTime() - start;
            Log.i("onCreateViewHolder", "Time: "+Double.toString(((double) time) / 10e6));
            return tpvh;
        }

        @Override
        public void onBindViewHolder(ThreadPreviewViewHolder holder, int position) {
            long start = System.nanoTime();
            if (position == 0 || position == threads.size() + 1) {
                return;
            }
            final View threadPreview = holder.itemView;
            TextView threadTitle = (TextView) threadPreview.findViewById(R.id.text_thread_title);
            CardView card = (CardView) threadPreview.findViewById(R.id.post_card);
            TextView displayId = (TextView) threadPreview.findViewById(R.id.post_display_id);
            TextView authorName = (TextView) threadPreview.findViewById(R.id.post_author_name);
            TextView rightHeader = (TextView) threadPreview.findViewById(R.id.post_right_header_text);
            TextView text = (TextView) threadPreview.findViewById(R.id.post_text);
            RecyclerView filesRecycler = (RecyclerView) threadPreview.findViewById(R.id.post_files_recycler);
            ListView previewList = (ListView) threadPreview.findViewById(R.id.list_preview_posts);
//            TextView replies = (TextView) threadPreview.findViewById(R.id.post_replies);

            int threadNumOnPage = position - 1;

            HanabiraThread thread = threads.get(threadNumOnPage);
            HanabiraPost op = Hanabira.getCache().findPostByDisplayId(thread.getPosts().firstEntry().getValue());

            threadTitle.setText(thread.getTitle());
            displayId.setText("№".concat(Integer.toString(op.getDisplayId())));
            authorName.setText(op.getName());
            rightHeader.setText(DateTimeFormat.forPattern("dd MMMM yyyy (EEE)\nHH:mm:ss").print(op.getDate()));
            if (cachedParsedPosts.size() > threadNumOnPage) {
                text.setText(cachedParsedPosts.get(threadNumOnPage));
            } else {
                text.setText(new HanabiraParser(op, getContext()).getFormatted());
                Log.w("onBindViewHolder", "Parser cache miss at " + threadNumOnPage);
            }
            text.setMovementMethod(LinkMovementMethod.getInstance());
            card.setCardElevation(opPostElevation);
            if (op.getFiles() == null || op.getFiles().size() == 0) {
                filesRecycler.setVisibility(View.GONE);
            }

            final String[] mapKeys = {"subject", "text"};
            int[] viewIds = {R.id.text_thread_preview_list_item_subject, R.id.text_thread_preview_list_item_text};
            List<Map<String, CharSequence>> previewData = new ArrayList<>();
            List<Integer> postsIds = thread.getLastN(3);
            List<HanabiraPost> posts = new ArrayList<>();
            for (Integer i : postsIds) {
                posts.add(Hanabira.getCache().findPostByDisplayId(i));
            }
            for (final HanabiraPost p : posts) {
                previewData.add(new HashMap<String, CharSequence>() {
                    {
                        put(mapKeys[0], p.getSubject());
                        put(mapKeys[1], p.getMessage());
                    }
                });
            }
            SimpleAdapter previewAdapter = new SimpleAdapter(threadPreview.getContext(), previewData,
                                                             R.layout.thread_preview_list_item, mapKeys, viewIds);
            previewList.setAdapter(previewAdapter);

//            filesRecycler.setLayoutManager(
//                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//            filesRecycler.setAdapter(new FilesAdapter(/*todo*/));
            long time = System.nanoTime() - start;
            Log.i("onBindViewHolder", "Time: "+Double.toString(((double) time) / 10e6));
        }

        @Override
        public int getItemCount() {
            return threads.size() + 2; // prevPage+[threads]+nextPage
        }

        @Override
        public int getItemViewType(int position) {
            if (position > 0) {
                if (position < threads.size() + 1) {
                    return ViewTypes.THREAD;
                } else {
                    return ViewTypes.NEXT_PAGE;
                }
            } else {
                return ViewTypes.PREV_PAGE;
            }
        }

        protected class ThreadPreviewViewHolder extends RecyclerView.ViewHolder {

            public ThreadPreviewViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    private class RecyclerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recycler.findChildViewUnder(e.getX(), e.getY());
            TextView tv = ((TextView) view.findViewById(R.id.post_text));

            CharSequence displId = ((TextView) view.findViewById(R.id.post_display_id)).getText();
            float elevation = view.findViewById(R.id.post_card).getElevation();
            Toast.makeText(getContext(),
                           displId +" "+ Float.toString(elevation) + "|" + Float.toString(view.getElevation()),
                           Toast.LENGTH_SHORT).show();

            tv.onTouchEvent(e);
            return super.onSingleTapConfirmed(e);
        }


        public void onLongPress(MotionEvent e) {
            View view = recycler.findChildViewUnder(e.getX(), e.getY());
            View list = view.findViewById(R.id.list_preview_posts);
            // switch preview list visibility on long press
            list.setVisibility(list.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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

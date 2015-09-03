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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.*;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.HanabiraParser;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.avapira.bobroreader.util.TestCardViewFragment;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ThreadFragment extends Fragment {

    private static final String TAG = ThreadFragment.class.getSimpleName();

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

    public ThreadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scroller, container, false);
    }

    GestureDetectorCompat detector;
    RecyclerView          recycler;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recycler = (RecyclerView) view.findViewById(R.id.thread_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(new ThreadAdapter(getPosts()));
        recycler.addOnItemTouchListener(new TouchEventInterceptor());
        detector = new GestureDetectorCompat(getActivity(), new RecyclerGestureListener());
    }

    public List<HanabiraPost> getPosts() {
        List<HanabiraPost> list = new ArrayList<>();
        Resources res = getResources();
        list.add(HanabiraPost.fromJson(Bober.rawJsonToString(res, R.raw.d_55048_57432), HanabiraPost.class));
        list.add(HanabiraPost.fromJson(Bober.rawJsonToString(res, R.raw.d_55048_57442), HanabiraPost.class));
        list.add(HanabiraPost.fromJson(Bober.rawJsonToString(res, R.raw.d_55048_57479), HanabiraPost.class));
        return list;
    }


    private class ThreadAdapter extends RecyclerView.Adapter<ThreadAdapter.PostCardViewHolder> {
        private class VIEW_TYPES {
            public static final int Header = 1;
            public static final int Normal = 2;
            public static final int Footer = 3;
        }

        List<HanabiraPost> posts = new ArrayList<>();

        public ThreadAdapter(List<HanabiraPost> pp) {
            posts = pp; // FIXME Aware of non-consistent pp changes
        }

        @Override
        public PostCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View postcard;
            switch (viewType) {
                case VIEW_TYPES.Footer:
                    postcard = LayoutInflater.from(getContext()).inflate(R.layout.footer_view, parent, false);
                    break;
                case VIEW_TYPES.Normal:
                    postcard = LayoutInflater.from(getContext()).inflate(R.layout.card_post, parent, false);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong view type for this Recycler");
            }
            return new PostCardViewHolder(postcard);
        }

        @Override
        public void onBindViewHolder(PostCardViewHolder holder, int position) {
            if (position == posts.size()) {
                return;
            }
            View postcard = holder.view;
            CardView card = (CardView) postcard.findViewById(R.id.post_card);
            TextView displayId = (TextView) postcard.findViewById(R.id.post_display_id);
            TextView authorName = (TextView) postcard.findViewById(R.id.post_author_name);
            TextView rightHeader = (TextView) postcard.findViewById(R.id.post_right_header_text);
            TextView text = (TextView) postcard.findViewById(R.id.post_text);
            RecyclerView filesRecycler = (RecyclerView) postcard.findViewById(R.id.post_files_recycler);
//            TextView replies = (TextView) postcard.findViewById(R.id.post_replies);

            HanabiraPost cursor = posts.get(position);
            displayId.setText("№".concat(Integer.toString(cursor.getDisplayId())));
            authorName.setText(cursor.getName());
            rightHeader.setText(DateTimeFormat.forPattern("dd MMMM yyyy (EEE)\nHH:mm:ss").print(cursor.getDate()));
            text.setText(new HanabiraParser(cursor, getContext()).getFormatted());
            text.setMovementMethod(LinkMovementMethod.getInstance());
            if (cursor.isOp()) {
                card.setCardElevation(5 * card.getCardElevation() / 2);
            }

            if (cursor.getFiles() == null || cursor.getFiles().size() == 0) {
                filesRecycler.setVisibility(View.GONE);
            }

//            filesRecycler.setLayoutManager(
//                    new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
//            filesRecycler.setAdapter(new FilesAdapter(/*todo*/));
        }

        @Override
        public int getItemCount() {
            return posts.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == posts.size()) {
                return VIEW_TYPES.Footer;
            } else { return VIEW_TYPES.Normal; }
        }

        protected class PostCardViewHolder extends RecyclerView.ViewHolder {
            private View view;

            public PostCardViewHolder(View view) {
                super(view);
                this.view = view;
            }
        }
    }

    private class RecyclerGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recycler.findChildViewUnder(e.getX(), e.getY());
            TextView tv = ((TextView) view.findViewById(R.id.post_text));
            tv.onTouchEvent(e);
            return super.onSingleTapConfirmed(e);
        }


        public void onLongPress(MotionEvent e) {
            View view = recycler.findChildViewUnder(e.getX(), e.getY());
            int position = recycler.getChildAdapterPosition(view);

            // handle long press

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

package com.avapira.bobroreader;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.HanabiraParser;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BoardFragment extends Fragment {

    private static final String TAG = BoardFragment.class.getSimpleName();

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scroller, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        RecyclerView recycler = (RecyclerView) view.findViewById(R.id.thread_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(new BoardAdapter(getOpPosts()));
    }

    public List<HanabiraPost> getOpPosts() {
        List<HanabiraPost> list = new ArrayList<>();
        Resources res = getResources();
        list.add(HanabiraPost.fromJson(Bober.rawJsonToString(res, R.raw.d_55048_57432), HanabiraPost.class));
        list.add(HanabiraPost.fromJson(Bober.rawJsonToString(res, R.raw.d_55048_57442), HanabiraPost.class));
        list.add(HanabiraPost.fromJson(Bober.rawJsonToString(res, R.raw.d_55048_57479), HanabiraPost.class));
        return list;
    }


    private class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.OpPostCardViewHolder> {
        private class VIEW_TYPES {
            public static final int Header = 1;
            public static final int Normal = 2;
            public static final int Footer = 3;
        }

        List<HanabiraPost> posts = new ArrayList<>();

        public BoardAdapter(List<HanabiraPost> pp) {
            posts = pp; // FIXME Aware of non-consistent pp changes
        }

        @Override
        public OpPostCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
            return new OpPostCardViewHolder(postcard);
        }

        @Override
        public void onBindViewHolder(OpPostCardViewHolder holder, int position) {
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
            displayId.setText("№".concat(cursor.getDisplayId()));
            authorName.setText(cursor.getName());
            rightHeader.setText(DateTimeFormat.forPattern("dd MMMM yyyy (EEE)\nHH:mm:ss").print(cursor.getDate()));
            text.setText(new HanabiraParser(cursor, getContext()).getFormatted());
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

        protected class OpPostCardViewHolder extends RecyclerView.ViewHolder {
            private View view;

            public OpPostCardViewHolder(View view) {
                super(view);
                this.view = view;
            }
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
}

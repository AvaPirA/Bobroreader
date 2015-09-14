package com.avapira.bobroreader;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;
import com.avapira.bobroreader.util.Consumer;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class ThreadFragment extends Fragment {
    private static final String ARG_THREAD_ID       = "arg_thread_id";
    private static final String ARG_RECYCLER_LAYOUT = "arg_thread_recycler_layout";

    private int threadId;

    private Castor               supervisor;
    private HidingScrollListener scrollListener;
    private ProgressBar          progressBar;

    public static ThreadFragment newInstance(int threadId) {
        ThreadFragment fragment = new ThreadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_THREAD_ID, threadId);
        fragment.setArguments(args);
        return fragment;
    }

    public ThreadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            threadId = getArguments().getInt(ARG_THREAD_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scroller, container, false);
    }

    RecyclerView recycler;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        recycler = (RecyclerView) view.findViewById(R.id.thread_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        scrollListener = new HidingScrollListener(
                (FrameLayout) getActivity().findViewById(R.id.frame_toolbar_container), getContext());
        recycler.addOnScrollListener(scrollListener);
        loadThread();
    }

    private void loadThread() {
        supervisor.retitleOnLoading();
        recycler.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        final HanabiraThread thread = Hanabira.getCache().findThreadById(threadId);
        Hanabira.getFlower().updateThread(threadId, new Consumer<TreeMap<LocalDateTime, Integer>>() {
            @Override
            public void accept(TreeMap<LocalDateTime, Integer> posts) {
                if (posts == null) {
                    posts = Hanabira.getCache().findThreadById(threadId).getPosts();
                }
                final ThreadAdapter adapter = new ThreadAdapter(posts);
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        recycler.setAdapter(adapter);
                        progressBar.setVisibility(View.GONE);
                        recycler.setVisibility(View.VISIBLE);
                        supervisor.retitleOnThreadLoad(thread);
                    }
                });
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_THREAD_ID, threadId);
        outState.putParcelable(ARG_RECYCLER_LAYOUT, recycler.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            threadId = savedInstanceState.getInt(ARG_THREAD_ID, 0);
            recycler.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(ARG_RECYCLER_LAYOUT));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            supervisor = (Castor) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        supervisor = null;
    }

    private class ThreadAdapter extends RecyclerView.Adapter<PostViewHolder> {

        TreeMap<LocalDateTime, Integer> posts;
        private List<Integer> listedPosts;

        public ThreadAdapter(TreeMap<LocalDateTime, Integer> pp) {
            this.posts = pp;
            listedPosts = new ArrayList<>(pp.size());
            for (Integer i : pp.values()) {
                listedPosts.add(i);
            }
            Hanabira.getCache().asyncParse(pp.values());
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext())
                                      .inflate(viewType == 0 ? R.layout.layout_post : R.layout.layout_post_divider,
                                              parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
        }

        @Override
        public void onBindViewHolder(PostViewHolder holder, int position) {
            if (getItemViewType(position) == 0) {
                holder.postHolder.fillWithData(listedPosts.get(position/2));
            }
        }

        @Override
        public int getItemCount() {
            return 2 * listedPosts.size() - 1;
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        PostHolder postHolder;

        public PostViewHolder(View itemView) {
            super(itemView);
            postHolder = new PostHolder(itemView);
        }
    }

}

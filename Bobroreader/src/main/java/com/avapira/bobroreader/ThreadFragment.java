package com.avapira.bobroreader;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;
import com.avapira.bobroreader.util.Consumer;

import java.util.ArrayList;
import java.util.List;


public class ThreadFragment extends Fragment {

    private static final String ARG_THREAD_ID         = "arg_thread_id";
    private static final String ARG_THREAD_DISPLAY_ID = "arg_thread_display_id";
    private static final String ARG_THREAD_BOARD      = "arg_thread_board";
    private static final String ARG_RECYCLER_LAYOUT   = "arg_thread_recycler_layout";
    private RecyclerView         recycler;
    private AmbiguousId          id;
    private Castor               supervisor;
    private HidingScrollListener scrollListener;
    private ProgressBar          progressBar;
    private ThreadAdapter        threadAdapter;

    public ThreadFragment() {
        // Required empty public constructor
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        final PostHolder postHolder;

        public PostViewHolder(View itemView) {
            super(itemView);
            postHolder = new PostHolder(itemView);
        }
    }

    private final class SuccessLoadingRunnable implements Runnable {

        final HanabiraThread thread;

        public SuccessLoadingRunnable(HanabiraThread thread) {
            this.thread = thread;
        }

        public void run() {
            threadAdapter = new ThreadAdapter(thread);
            hookUpData();
        }
    }

    private class ThreadAdapter extends RecyclerView.Adapter<PostViewHolder> {

        public static final int VT_FOOTER  = -2;
        public static final int VT_HEADER  = -1;
        public static final int VT_DIVIDER = 0;
        public static final int VT_POST    = 1;
        private List<Integer> reflectedPosts;

        public ThreadAdapter(HanabiraThread thread) {
            updateDataSet(thread);
        }

        public List<Integer> getReflectedPosts() {
            return reflectedPosts;
        }

        private void updateDataSet(HanabiraThread thread) {
            reflectedPosts = new ArrayList<>(thread.getPosts().size());
            for (Integer i : thread.getPosts().values()) {
                reflectedPosts.add(i);
            }
            Hanabira.getStem().asyncParse(thread.getPosts().values());
        }

        @Override
        public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutId;
            switch (viewType) {
                case VT_FOOTER:
                    layoutId = R.layout.thread_footer_view;
                    break;
                case VT_HEADER:
                    layoutId = R.layout.thread_header_view;
                    break;
                case VT_DIVIDER:
                    layoutId = R.layout.layout_post_divider;
                    break;
                case VT_POST:
                    layoutId = R.layout.layout_post;
                    break;
                default:
                    throw new InternalError();
            }
            View view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            return new PostViewHolder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VT_HEADER; //header
            } else if (position == getItemCount() - 1) {
                return VT_FOOTER; //footer
            } else {
                return position % 2;
            }
        }

        @Override
        public void onBindViewHolder(PostViewHolder holder, int position) {
            if (getItemViewType(position) == VT_POST) {
                holder.postHolder.fillWithData(reflectedPosts.get(position / 2));
            }
        }

        @Override
        public int getItemCount() {
            return 2 * reflectedPosts.size() + 1;
        }
    }

    public static Fragment newInstance(int threadId) {
        Fragment fragment = new ThreadFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_THREAD_ID, threadId);
        fragment.setArguments(args);
        return fragment;
    }


    public static Fragment newInstance(String board, int threadDisplayId) {
        Fragment fragment = new ThreadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_THREAD_BOARD, board);
        args.putInt(ARG_THREAD_DISPLAY_ID, threadDisplayId);
        fragment.setArguments(args);
        return fragment;
    }

    private void hookUpData() {
        recycler.setAdapter(threadAdapter);
        progressBar.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);
        HanabiraThread t = Hanabira.getStem().findThread(id);
        supervisor.retitleOnThreadLoad(HanabiraBoard.Info.getForId(t.getBoardId()).boardKey, t.getTitle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int threadId = getArguments().getInt(ARG_THREAD_ID);
            if (threadId == 0) {
                id = new AmbiguousId(getArguments().getString(ARG_THREAD_BOARD),
                        getArguments().getInt(ARG_THREAD_DISPLAY_ID));
            } else {
                id = new AmbiguousId(threadId);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scroller, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        recycler = (RecyclerView) view.findViewById(R.id.thread_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        if (threadAdapter == null) {
            // open board page
            scrollListener = new HidingScrollListener(getContext());
            loadThread();
        } else {
            // popping from fragments stack
            hookUpData();
        }
        scrollListener.resetContainer((FrameLayout) getActivity().findViewById(R.id.frame_toolbar_container));
        recycler.addOnScrollListener(scrollListener);
    }

    private void loadThread() {
        supervisor.retitleOnLoading();
        recycler.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        final HanabiraThread thread = Hanabira.getStem().findThread(id);

        if (thread == null || thread.getPostsCount() != thread.getPosts().size()) {
            // full load required
            Hanabira.getFlower().getFullThread(id, new Consumer<HanabiraThread>() {
                @Override
                public void accept(HanabiraThread thread) {
                    if (thread != null) {
                        Activity activity = getActivity();
                        if(activity != null) {
                            activity.runOnUiThread(new SuccessLoadingRunnable(thread));
                        } else {
                            Log.e("ThreadFragment#load", "NPE on runOnUi");
                        }
                    } else {
                        throw new RuntimeException("No such thread");
                    }

                }
            });
        } else {
            // only check for new posts is required
            // 1)run network (check+load if needed)
            // 2)swap adapter if
            // 3)check for deleted
            Hanabira.getFlower().getThreadWithUpdate(id, new Consumer<HanabiraThread>() {
                @Override
                public void accept(HanabiraThread thread) {
                    getActivity().runOnUiThread(new SuccessLoadingRunnable(thread));
                    Hanabira.getFlower().checkForDeletedPosts(id, new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean deleted) {
                            if (deleted) {
                                // show snackbar with reload action
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(getActivity().findViewById(R.id.snackbarPosition),
                                                "Some posts deleted", Snackbar.LENGTH_LONG)
                                                .setAction("Reload", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        loadThread();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (id.isDisplay()) {
            outState.putString(ARG_THREAD_BOARD, id.getBoard());
            outState.putInt(ARG_THREAD_DISPLAY_ID, id.getDisplayId());
        } else {
            outState.putInt(ARG_THREAD_ID, id.getId());
        }
        outState.putParcelable(ARG_RECYCLER_LAYOUT, recycler.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            int threadId = savedInstanceState.getInt(ARG_THREAD_ID);
            if (threadId == 0) {
                id = new AmbiguousId(savedInstanceState.getString(ARG_THREAD_BOARD),
                        savedInstanceState.getInt(ARG_THREAD_DISPLAY_ID));
            } else {
                id = new AmbiguousId(threadId);
            }
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
}

package com.avapira.bobroreader;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraThread;


public class ThreadFragment extends Fragment {
    private static final String ARG_THREAD_ID       = "arg_thread_id";
    private static final String ARG_RECYCLER_LAYOUT = "arg_thread_recycler_layout";

    private int threadId;

    private Castor               supervisor;
    private HidingScrollListener scrollListener;
    private ProgressBar          progressBar;

    private class ThreadAdapter extends RecyclerView.Adapter {
        //TODO
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //TODO
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            //TODO
        }

        @Override
        public int getItemCount() {
            //TODO
            return 0;
        }
    }

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
                (FrameLayout) getActivity().findViewById(R.id.frame_toolbar_container),
                (int) getResources().getDimension(R.dimen.tiny));
        recycler.addOnScrollListener(scrollListener);
        loadThread();
    }

    private void loadThread() {
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        supervisor.retitleOnLoading();
        progressBar.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.INVISIBLE);

        Hanabira.getFlower().updateThread(threadId);
        HanabiraThread thread = null;
        //TODO LOAD

        recycler.setAdapter(new ThreadAdapter());
        progressBar.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);
        //TODO MOVE ALL TOOLBAR INTERACTIONS TO ACTIVITY (FROM ALL FRAGMENTS)
        supervisor.retitleOnThreadLoad(thread);
    }

    private static void updateToolbarTitle(ActionBar toolbar, String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    private static void updateToolbarWithThreadTitle(ActionBar toolbar, HanabiraThread thread) {
        String threadTitle = thread.getTitle();
        if (threadTitle == null || "".equals(threadTitle)) {
            threadTitle = "Unnamed";
        }
        String title = String.format("%s/ %s " + "[%s]", HanabiraBoard.Info.getKeyForId(thread.getBoardId()),
                threadTitle, thread.getPostsCount());

        updateToolbarTitle(toolbar, title);
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

}

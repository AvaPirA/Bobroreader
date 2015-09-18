package com.avapira.bobroreader;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;

/**
 *
 */
public class PostDialogFragment extends DialogFragment {

    public static final  String ARG_TITLE = "title";
    private static final String ARG_ID    = "id";
    final                String LOG_TAG   = "myLogs";

    public static DialogFragment newInstance(String board, int id) {
        DialogFragment fragment = new PostDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TITLE, String.format(">>%s/%d", board, id));
        bundle.putInt(ARG_ID, id);
        fragment.setArguments(bundle);
        return fragment;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_post, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (getArguments() == null) {
            throw new RuntimeException("Not properly initialized post dialog");
        }
        PostHolder ph = new PostHolder(view);
        getDialog().setTitle(getArguments().getString(ARG_TITLE));
        int id = getArguments().getInt(ARG_ID);
        HanabiraPost postData = Hanabira.getStem().findPostById(id);
        if (postData == null) {
            ph.displayId.setText("№"+id);
            ph.message.setText("LALKA NOT IMPLEMENTED POST DOWNLOADING");
        } else {
            ph.fillWithData(postData);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(LOG_TAG, "Dialog 1: onDismiss");
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.d(LOG_TAG, "Dialog 1: onCancel");
    }
}

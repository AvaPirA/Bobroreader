/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.avapira.bobroreader;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.HanabiraParser;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Fragment that demonstrates how to use CardView.
 */
public class CardViewFragment extends Fragment {

    private static final String TAG = CardViewFragment.class.getSimpleName();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NotificationFragment.
     */
    public static CardViewFragment newInstance() {
        CardViewFragment fragment = new CardViewFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public CardViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_card_view, container, false);
    }


    static Iterator<Integer> rawJsons = Arrays.asList(
            new Integer[]{ R.raw.lorem_ipsum, R.raw.d_55048_57432, R.raw.d_55048_57442, R.raw.d_55048_57479})
                                              .iterator();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView nameView = (TextView) view.findViewById(R.id.anon_name);
        CardView cardView = (CardView) view.findViewById(R.id.cardview);
        TextView textView = (TextView) view.findViewById(R.id.post_text);

        final HanabiraPost post = getPost();
        final CharSequence text = new HanabiraParser(post, getContext()).getFormatted();
        nameView.setText(post.getName());
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(text);
        if (post.isOp()) {
            cardView.setCardElevation(2 * cardView.getCardElevation());
        }
    }

    private HanabiraPost getPost() {
        String jsonString = Bober.rawJsonToString(getResources(), rawJsons.next());
        return HanabiraPost.fromJson(jsonString, HanabiraPost.class);
    }

}
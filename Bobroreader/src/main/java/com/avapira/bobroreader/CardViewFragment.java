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
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.google.gson.*;
import org.joda.time.LocalDateTime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Fragment that demonstrates how to use CardView.
 */
public class CardViewFragment extends Fragment {

    private static final String TAG = CardViewFragment.class.getSimpleName();

    CardView mCardView;
    TextView textView;

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


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCardView = (CardView) view.findViewById(R.id.cardview);
        textView = (TextView) view.findViewById(R.id.post_text);
        String jsonString = rawJsonToString();
        System.out.println(jsonString);
        HanabiraPost post = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
                                                                  new JsonDeserializer<LocalDateTime>() {
                                                                      @Override
                                                                      public LocalDateTime deserialize(JsonElement json,
                                                                                                       Type typeOfT,
                                                                                                       JsonDeserializationContext context)
                                                                      throws JsonParseException {
                                                                          return LocalDateTime.parse(
                                                                                  json.getAsString().replace(' ', 'T'));
                                                                      }
                                                                  }).create().fromJson(jsonString, HanabiraPost.class);
        SpannableString str = formatHanabiraPost(post.getMessage());
//        SpannableStringBuilder sb = new SpannableStringBuilder(getResources().getString(R.string.));
//        textView.setText();
//        textView.setMovementMethod(LinkTouchMovementMethod.getInstance());
    }

    private SpannableString formatHanabiraPost(String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        return null;
    }

    private String rawJsonToString() {
        BufferedInputStream bis = new BufferedInputStream(getResources().openRawResource(R.raw.d_55048_57479));
        try {
            byte[] bytes = new byte[bis.available()];
            System.out.format("%s bytes read", bis.read(bytes));
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    private static class LinkTouchMovementMethod extends LinkMovementMethod {
        private        TouchableSpan           mPressedSpan;
        private static LinkTouchMovementMethod instance;

        public static LinkTouchMovementMethod getInstance() {
            if (instance == null) {
                instance = new LinkTouchMovementMethod();
            }
            return instance;
        }


        @Override
        public boolean onTouchEvent(@NonNull TextView textView,
                                    @NonNull Spannable spannable,
                                    @NonNull MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mPressedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(true);
                    Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                                           spannable.getSpanEnd(mPressedSpan));
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                TouchableSpan touchedSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                    mPressedSpan.setPressed(false);
                    mPressedSpan = null;
                    Selection.removeSelection(spannable);
                }
            } else {
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(false);
                    super.onTouchEvent(textView, spannable, event);
                }
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
            return true;
        }

        private TouchableSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            TouchableSpan[] link = spannable.getSpans(off, off, TouchableSpan.class);
            TouchableSpan touchedSpan = null;
            if (link.length > 0) {
                touchedSpan = link[0];
            }
            return touchedSpan;
        }

    }

    public static abstract class TouchableSpan extends ClickableSpan {
        private boolean mIsPressed;
        private int     mPressedBackgroundColor;
        private int     mNormalTextColor;
        private int     mPressedTextColor;

        public TouchableSpan(int normalTextColor, int pressedTextColor, int pressedBackgroundColor) {
            mNormalTextColor = normalTextColor;
            mPressedTextColor = pressedTextColor;
            mPressedBackgroundColor = pressedBackgroundColor;
        }

        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(mIsPressed ? mPressedTextColor : mNormalTextColor);
            ds.bgColor = mIsPressed ? mPressedBackgroundColor : 0xffeeeeee;
            ds.setUnderlineText(false);
        }
    }
}
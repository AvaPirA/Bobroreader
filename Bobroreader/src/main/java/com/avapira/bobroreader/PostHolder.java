package com.avapira.bobroreader;

import android.text.Layout;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.cache.ActiveCache;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 */
public class PostHolder {

    final TextView displayId;
    final TextView date;
    final TextView name;
    final TextView message;

    public PostHolder(TextView displayId, TextView date, TextView name, TextView message) {
        this.displayId = displayId;
        this.date = date;
        this.name = name;
        this.message = message;
    }

    public PostHolder(View parent) {
        displayId = (TextView) parent.findViewById(R.id.text_post_header_display_id);
        date = (TextView) parent.findViewById(R.id.text_post_header_datetime);
        name = (TextView) parent.findViewById(R.id.text_post_header_author_name);
        message = (TextView) parent.findViewById(R.id.text_post_content_message);
    }

    private class LinkMovementMethodOverride implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            TextView widget = (TextView) v;
            Object text = widget.getText();
            if (text instanceof Spanned) {
                Spanned buffer = (Spanned) text;

                int action = event.getAction();

                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    x -= widget.getTotalPaddingLeft();
                    y -= widget.getTotalPaddingTop();

                    x += widget.getScrollX();
                    y += widget.getScrollY();

                    Layout layout = widget.getLayout();
                    int line = layout.getLineForVertical(y);
                    int off = layout.getOffsetForHorizontal(line, x);
                    ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                    if (link.length != 0) {
                        if (action == MotionEvent.ACTION_UP) {
                            for (ClickableSpan l : link) {
                                l.onClick(widget);
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static CharSequence formatDisplayId(int displayId) {
        return String.format("№%d", displayId);
    }

    private static CharSequence formatDate(LocalDateTime localDateTime) {
        return DateTimeFormat.forPattern("dd MMMM yyyy (EEE)\nHH:mm:ss").print(localDateTime);
    }

    public void fillWithData(HanabiraPost post) {
        displayId.setText(formatDisplayId(post.getDisplayId()));
        date.setText(formatDate(post.getCreatedDate()));
        name.setText(post.getName());
        message.setText(((ActiveCache) Hanabira.getStem()).getParsedPost(post.getPostId()));
        message.setOnTouchListener(new LinkMovementMethodOverride());
    }

    public void fillWithData(int postId) {
        fillWithData(Hanabira.getStem().findPostById(postId));
        showContainingList();
    }

    private void showContainingList() {
        ((View) message.getParent().getParent()).setVisibility(View.VISIBLE);
    }

    public void hideContainingList() {
        ((View) message.getParent().getParent()).setVisibility(View.GONE);
    }
}

package com.avapira.bobroreader;

import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

/**
 *
 */
public class PostHolder {

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

    TextView displayId;
    TextView date;
    TextView name;
    TextView message;

    public void fillWithData(HanabiraPost post) {
        displayId.setText(formatDisplayId(post.getDisplayId()));
        DebugTimer.lap("  disp");
        date.setText(formatDate(post.getCreatedDate()));
        DebugTimer.lap("  date");
        name.setText(post.getName());
        DebugTimer.lap("  name");
        final CharSequence parsedPost = Hanabira.getCache().getParsedPost(post.getDisplayId());
        DebugTimer.lap("  cache msg");
        message.setText(parsedPost);
        DebugTimer.lap("  msg");
        message.setMovementMethod(LinkMovementMethod.getInstance());
        DebugTimer.lap("  movement");
    }

    public void fillWithData(int postDisplayId) {
        fillWithData(Hanabira.getCache().findPostByDisplayId(postDisplayId));
        show();
        DebugTimer.lap("  cache post");
    }

    public void show() {
        ((View) message.getParent().getParent()).setVisibility(View.VISIBLE);
    }

    public void hide() {
        ((View) message.getParent().getParent()).setVisibility(View.GONE);
    }

    private static CharSequence formatDisplayId(int displayId) {
        return String.format("№%d", displayId);
    }

    private static CharSequence formatDate(LocalDateTime localDateTime) {
        return DateTimeFormat.forPattern("dd MMMM yyyy (EEE)\nHH:mm:ss").print(localDateTime);
    }
}

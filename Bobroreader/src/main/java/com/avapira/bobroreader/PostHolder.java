package com.avapira.bobroreader;

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
        date.setText(formatDate(post.getCreatedDate()));
        name.setText(post.getName());
        message.setText(Hanabira.getCache().getParsedPost(post.getDisplayId()));
    }

    public void fillWithData(int postDisplayId) {
        fillWithData(Hanabira.getCache().findPostByDisplayId(postDisplayId));
    }


    private CharSequence formatDisplayId(int displayId) {
        return String.format("№%d", displayId);
    }

    private CharSequence formatDate(LocalDateTime localDateTime) {
        return DateTimeFormat.forPattern("dd MMMM yyyy (EEE)\nHH:mm:ss").print(localDateTime);
    }

}

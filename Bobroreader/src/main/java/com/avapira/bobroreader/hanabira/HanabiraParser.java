/*
 * Bobroreader is open source software, created, maintained, and shared under
 * the MIT license by Avadend Piroserpen Arts. The project includes components
 * from other open source projects which remain under their existing licenses,
 * detailed in their respective source files.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015. Avadend Piroserpen Arts Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.avapira.bobroreader.hanabira;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.*;
import android.text.util.Linkify;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.avapira.bobroreader.R;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.mikepenz.iconics.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class HanabiraParser {

    private static abstract class SpanObjectFactory {
        abstract Object getSpan();

        public static final SpanObjectFactory ITALIC      = new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new StyleSpan(Typeface.ITALIC);
            }
        };
        public static final SpanObjectFactory BOLD        = new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new StyleSpan(Typeface.BOLD);
            }
        };
        public static final SpanObjectFactory BOLD_ITALIC = new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new StyleSpan(Typeface.BOLD_ITALIC);
            }
        };
        public static final SpanObjectFactory SPOILER     = new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new SpoilerSpan();
            }
        };
        public static final SpanObjectFactory CODE_BLOCK  = new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new CodeBlockSpan();
            }
        };
    }

    private static class QuoteSpan extends CharacterStyle implements UpdateAppearance {
        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setColor(boxedQuoteColor);
        }

    }

    private static class SpoilerSpan extends ClickableSpan implements UpdateAppearance {

        Boolean wasShown;

        public SpoilerSpan() {
            super();
            wasShown = !HanabiraParser.showSpoilers;
            // todo maybe make it as "always show"/"show on start(hideable)"/"hidden"?
        }

        @Override
        public void onClick(View widget) {
            TextView tv = (TextView) widget;
            Spanned spTxt = (Spanned) tv.getText();
            tv.setTextColor(wasShown ? boxedSpoilerHiddenColor : boxedSpoilerShownColor);
            wasShown = !wasShown;
        }

        @Override
        public void updateDrawState(@NonNull TextPaint tp) {
            tp.bgColor = boxedSpoilerHiddenColor;
            if(wasShown) {
                tp.setColor(boxedSpoilerHiddenColor);
            } else {
                tp.setColor(boxedSpoilerShownColor);
            }
        }
    }

    private static class CodeBlockSpan extends CharacterStyle implements UpdateAppearance {

        @Override
        public void updateDrawState(TextPaint tp) {
            tp.setColor(boxedSpoilerHiddenColor);
            tp.setTextSize(boxedCodeFontSize);
            tp.setTypeface(Typeface.MONOSPACE);
        }
    }

    private class HanabiraLinkSpan extends ClickableSpan implements UpdateAppearance {
        private final String board;
        private final String post;

        public HanabiraLinkSpan(@Nullable String board, @NonNull String post) {
            this.board =
                    board == null ? HanabiraBoard.getForId(HanabiraParser.this.post.getBoardId()).getBoard() : board;
            this.post = post;
        }

        @Override
        public void onClick(View widget) {
            Toast.makeText(HanabiraParser.this.context, String.format("Open >>%s/%s", board, post), Toast.LENGTH_SHORT)
                 .show();
            //open >>/board/post
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(boxedRefLinkColor);
            ds.setUnderlineText(true);
        }
    }

    private static float   boxedCodeFontSize;
    private static Integer boxedQuoteColor;
    private static Integer boxedSpoilerHiddenColor;
    private static Integer boxedSpoilerShownColor;
    private static Integer boxedRefLinkColor;
    private static boolean showSpoilers;

    Pattern refPost = Pattern.compile(">>(/?[a-z]{1,4}/)?([0-9]+)");
    Pattern bold    = Pattern.compile("(\\*\\*|__)(.*?)\\1");
    Pattern italic  = Pattern.compile("(\\*|_)(.*?)\\1");
    Pattern ulList  = Pattern.compile("\\n\\*(.*)");
    Pattern olList  = Pattern.compile("\\n[0-9]+\\.(.*)");
    Pattern code    = Pattern.compile("`(.*?)`");

    private final Context context;

    private final SpannableStringBuilder builder;
    private final List<Integer>          skips;
    private final HanabiraPost           post;

    public HanabiraParser(HanabiraPost post, Context context) {
        if (boxedSpoilerHiddenColor == null) {
            boxedCodeFontSize = Utils.convertDpToPx(context, 12);
            boxedSpoilerHiddenColor = context.getColor(R.color.dobro_dark);
            boxedSpoilerShownColor = context.getColor(R.color.dobro_primary_text);
            boxedRefLinkColor = context.getColor(R.color.dobro_ref_text);
            boxedQuoteColor = context.getColor(R.color.dobro_quote);
            showSpoilers = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_spoilers", false);
        }
        this.context = context;
        this.post = post;
        builder = new SpannableStringBuilder(replaceInternalLinkWithReference(post.getMessage()));
        Linkify.addLinks(builder, Linkify.WEB_URLS);
        skips = new ArrayList<>();

        // all the 4+ spaces in row in the start of string is a non-parsed code
        // actually not
        // original parser also require that code-block alse to be separated by empty lines
        Pattern codeBlockPattern = Pattern.compile("^\\s{4,}.*$", Pattern.MULTILINE);
        Matcher codeBlockMatcher = codeBlockPattern.matcher(builder);
        while (codeBlockMatcher.find()) {
            skips.add(codeBlockMatcher.start());
            skips.add(codeBlockMatcher.end());
        }

        //simplify blocks
//        for (int i = 0; i < tmpSkips.size() - 1; i++) {
//            while (tmpSkips.get(i).) //todo continue here
//        }
    }


    public CharSequence getFormatted() {
        formatTwoLevelBulletList();
        paintQuotes();

        //replaceAll
        formatCode();
        formatSpoiler();
        paintBoldItalic();
        paintBold();
        paintItalic();

        embedRefs();
        formatStrikethrough();
        formatWordsReversion();
        return builder;

    }

    private void formatWordsReversion() {
        ///xxx powel etot imbicil prosto na 4ai
    }

    private void formatStrikethrough() {
        int removedFormatCharsDelta = 0;
        Pattern pattern = Pattern.compile("(\\^H)+");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            boolean cont = false;
            for (int i = 0; i < skips.size(); i += 2) {
                if (matcher.start() > skips.get(i) && matcher.start() < skips.get(i + 1)) {
                    cont = true;
                }
            }
            if (cont) { continue; }

            int pos_start = matcher.start() - removedFormatCharsDelta;
            int pos_end = matcher.end() - removedFormatCharsDelta;
            builder.setSpan(new StrikethroughSpan(), pos_start - (pos_end - pos_start) / 2, pos_start, 0);
            builder.delete(pos_start, pos_end);
            removedFormatCharsDelta += pos_end - pos_start;
        }
    }

    private void embedRefs() {
        Pattern referencePattern = Pattern.compile(">>(/?([a-z]{1,4})/)?(\\d+)");
        Matcher refMatcher = referencePattern.matcher(builder);
        while (refMatcher.find()) {
            boolean skip = false;
            for (int i = 0; i < skips.size(); i += 2) {
                if (refMatcher.start() > skips.get(i) && refMatcher.start() < skips.get(i + 1)) {
                    skip = true;
                    break;
                }
            }
            if (skip) { continue; }

            int start = refMatcher.start();
            int end = refMatcher.end();
            builder.setSpan(new HanabiraLinkSpan(refMatcher.group(2), refMatcher.group(3)), start, end, 0);
        }
    }

    private void formatCode() {
        replaceAll("``", "``", SpanObjectFactory.CODE_BLOCK);
        replaceAll("`", "`", SpanObjectFactory.CODE_BLOCK);
        replaceAll("^``\r\n", "\r\n``$", SpanObjectFactory.CODE_BLOCK, Pattern.DOTALL);
    }

    private void paintItalic() {
        replaceAll("\\*", "\\*", SpanObjectFactory.ITALIC);
        replaceAll("_", "_", SpanObjectFactory.ITALIC);
    }

    private void paintBold() {
        replaceAll("\\*\\*", "\\*\\*", SpanObjectFactory.BOLD);
        replaceAll("__", "__", SpanObjectFactory.BOLD);
    }

    private void paintBoldItalic() {
        replaceAll("_\\*\\*", "\\*\\*_", SpanObjectFactory.BOLD_ITALIC);
        replaceAll("__\\*", "\\*__", SpanObjectFactory.BOLD_ITALIC);
    }

    private void formatSpoiler() {
        replaceAll("%%", "%%", SpanObjectFactory.SPOILER);
        replaceAll("%%", "%%", SpanObjectFactory.SPOILER);
        replaceAll("^%%\r\n", "\r\n%%$", SpanObjectFactory.SPOILER, Pattern.DOTALL);
    }

    private void leftSideSpan(String regex, Object span) {

    }

    private void paintQuotes() {
        Pattern pattern = Pattern.compile("^>[^>].*?$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            builder.setSpan(new QuoteSpan(), start, end, 0);
        }
    }

    private void formatTwoLevelBulletList() {
        Pattern p = Pattern.compile("^(\\*\\s){1,2}", Pattern.MULTILINE);
        Matcher m = p.matcher(builder);
        while (m.find()) {
            int st = m.start();
            if (m.group().matches("(\\*\\s){2}")) {
                builder.replace(st, st + 3, "  ○ ");
            } else {
                builder.replace(st, st + 2, "● ");
            }
        }
    }

    private static String replaceInternalLinkWithReference(String message) {
        Pattern p = Pattern.compile("https?://dobrochan\\.(?:ru|com|org)/([a-z]{1,4})/res/(\\d+)\\.xhtml(?:#i(\\d+))?");
        Matcher matcher = p.matcher(message);
        if (matcher.groupCount() == 4) { // whole, board, thread, (opt) post
            return matcher.replaceAll(">>$1/$3"); //>>/board/display_id_post
        } else {
            return matcher.replaceAll(">>$1/$2"); //>>/board/thread_display_id
        }
    }

    private void replaceAll(String begin, String end, SpanObjectFactory factory) {
        replaceAll(begin, end, factory, 0);
    }

    private void replaceAll(String begin, String end, SpanObjectFactory factory, int flag) {
        int removedFormatCharsDelta = 0;
        Pattern pattern = Pattern.compile(String.format("(%s)(.*?)(%s)", begin, end), Pattern.MULTILINE | flag);
        String beginRestore = restoreBreaks(begin);
        String endRestore = restoreBreaks(end);
        Matcher matcher = pattern.matcher(builder);
        String doubleBeginCheck = begin.replace("\\", ""); //remove escapers to use just as plain string
        String inlinedString;

        boolean code = doubleBeginCheck.contains("`");
        while (matcher.find()) {
            int start = matcher.start(2);
            int finish = matcher.end(2);
            if (code) {
                skips.add(start);
                skips.add(finish);
            } else {
                CodeBlockSpan[] spans = builder.getSpans(start, finish, CodeBlockSpan.class);
                if (spans != null && spans.length != 0) { continue; }
            }

            inlinedString = matcher.group(2);
            if (inlinedString == null || "".equals(inlinedString)) { continue; }

            int lengthPrefix = matcher.group(1).length();
            builder.replace(matcher.start(1) - removedFormatCharsDelta, matcher.end(1) - removedFormatCharsDelta,
                    beginRestore);

            builder.replace(matcher.start(3) - lengthPrefix - removedFormatCharsDelta + beginRestore.length(),
                    matcher.end(3) - lengthPrefix - removedFormatCharsDelta + beginRestore.length(), endRestore);

            SpannableString rep = new SpannableString(matcher.group(2));
            rep.setSpan(factory.getSpan(), 0, rep.length(), 0);
            if (!code) {
                Linkify.addLinks(rep, Linkify.WEB_URLS);
                // fixme twice used Linkify? try remove and just setSpan to builder
            }
            builder.replace(matcher.start() - removedFormatCharsDelta+beginRestore.length(),
                    matcher.start() + rep.length() - removedFormatCharsDelta+endRestore.length(), rep);

            // store deletions
            removedFormatCharsDelta += matcher.group(1).length() - beginRestore.length();
            removedFormatCharsDelta += matcher.group(3).length() - endRestore.length();
        }
    }

    private String restoreBreaks(String border) {
        Pattern newline = Pattern.compile("\\r*\\n*");
        Matcher nlMatchBegin = newline.matcher(border);
        StringBuilder sb = new StringBuilder();
        while (nlMatchBegin.find()) {
            sb.append(nlMatchBegin.group());
        }
        return sb.toString();
    }
}
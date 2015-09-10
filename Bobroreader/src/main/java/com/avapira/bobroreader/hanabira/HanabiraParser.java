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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.*;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.avapira.bobroreader.R;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraPost;
import com.mikepenz.iconics.utils.Utils;

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
            tp.setColor(quoteColor);
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
            Log.d("Spoiler#onClick", "clickclick; wasShown = " + wasShown);
            wasShown = !wasShown;
            widget.invalidate();
        }

        @Override
        public void updateDrawState(@NonNull TextPaint tp) {
            tp.bgColor = spoilerHiddenColor;
            if (wasShown) {
                tp.setColor(spoilerHiddenColor);
            } else {
                tp.setColor(spoilerShownColor);
            }
        }
    }

    private static class CodeBlockSpan extends TypefaceSpan implements UpdateAppearance {

        public CodeBlockSpan() {
            super("monospaced");
        }

        @Override
        public void updateDrawState(@NonNull TextPaint tp) {
            tp.setColor(spoilerHiddenColor);
            applyCustomTypeFace(tp);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint paint) {
            applyCustomTypeFace(paint);
        }

        private static void applyCustomTypeFace(Paint paint) {
            int oldStyle;
            Typeface old = paint.getTypeface();
            if (old == null) {
                oldStyle = 0;
            } else {
                oldStyle = old.getStyle();
            }

            int fake = oldStyle & ~Typeface.MONOSPACE.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                paint.setFakeBoldText(true);
            }

            if ((fake & Typeface.ITALIC) != 0) {
                paint.setTextSkewX(-0.25f);
            }

            paint.setTypeface(Typeface.MONOSPACE);
        }
    }

    private static class BulletListSpan implements LeadingMarginSpan {

        private int level;

        public BulletListSpan(int level) {
            if (level < 1 || level > 2) {
                throw new IllegalArgumentException("Only 1- and 2-level bullet lists available");
            }
            this.level = level;
        }

        @Override
        public int getLeadingMargin(boolean first) {
            switch (level) {
                case 1:
                    return first ? 0 : bulletMarginPerLevel;
                case 2:
                    return first ? bulletMarginPerLevel : 2 * bulletMarginPerLevel;
                default:
                    throw new InternalError(
                            String.format("Found level value equals %s instead \'1\' or \'2\' after constructor check",
                                    level));

            }
        }

        @Override
        public void drawLeadingMargin(Canvas c,
                                      Paint p,
                                      int x,
                                      int dir,
                                      int top,
                                      int baseline,
                                      int bottom,
                                      CharSequence text,
                                      int start,
                                      int end,
                                      boolean first,
                                      Layout layout) {

        }
    }

    private class HanabiraLinkSpan extends ClickableSpan implements UpdateAppearance {
        private final String board;
        private final String post;

        public HanabiraLinkSpan(@Nullable String board, @NonNull String post) {
            this.board = board == null ? HanabiraBoard.Info.getKeyForId(HanabiraParser.this.post.getBoardId()) : board;
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
            ds.setColor(refLinkColor);
            ds.setUnderlineText(true);
        }
    }

    public static boolean contextInitCompleted = false;
    private static int     bulletMarginPerLevel;
    private static int     quoteColor;
    private static int     spoilerHiddenColor;
    private static int     spoilerShownColor;
    private static int     refLinkColor;
    private static boolean showSpoilers;

    Pattern refPost = Pattern.compile(">>(/?[a-z]{1,4}/)?([0-9]+)");
    Pattern bold    = Pattern.compile("(\\*\\*|__)(.*?)\\1");
    Pattern italic  = Pattern.compile("(\\*|_)(.*?)\\1");
    Pattern ulList  = Pattern.compile("\\n\\*(.*)");
    Pattern olList  = Pattern.compile("\\n[0-9]+\\.(.*)");
    Pattern code    = Pattern.compile("`(.*?)`");

    private final Context context;

    private final SpannableStringBuilder builder;
    private final HanabiraPost           post;

    public HanabiraParser(HanabiraPost post, Context context) {
        if (!contextInitCompleted) {
            bulletMarginPerLevel = Utils.convertDpToPx(context, 12);
            spoilerHiddenColor = context.getColor(R.color.dobro_dark);
            spoilerShownColor = context.getColor(R.color.dobro_primary_text);
            refLinkColor = context.getColor(R.color.dobro_ref_text);
            quoteColor = context.getColor(R.color.dobro_quote);
            showSpoilers = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("show_spoilers", false);
            contextInitCompleted = true;
        }
        this.context = context;
        this.post = post;
        builder = new SpannableStringBuilder(replaceInternalLinkWithReference(post.getMessage()));
        Linkify.addLinks(builder, Linkify.WEB_URLS);
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
        formatWordsHiding();
        return builder;

    }

    private void formatWordsHiding() {
        int removedCharactersDelta = 0;
        Pattern pattern = Pattern.compile("(\\^W)+");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            final int start = matcher.start() - removedCharactersDelta;
            final int end = matcher.end() - removedCharactersDelta;
            CodeBlockSpan[] spans = builder.getSpans(start, start, CodeBlockSpan.class);
            if (spans != null && spans.length != 0) { continue; }

            int wordsAmount = matcher.group().length() / 2;
            char[] chars = new char[builder.length()];
            builder.getChars(0, builder.length(), chars, 0);
            int runner = start;
            try {
                while (wordsAmount > 0) {
                    if (Character.isSpaceChar(chars[--runner])) {
                        wordsAmount--;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                runner = 0;
            }
            builder.setSpan(new StrikethroughSpan(), runner, start, 0);
            builder.delete(start, end);
            removedCharactersDelta = end - start;
        }
    }

    private void formatStrikethrough() {
        int removedFormatCharsDelta = 0;
        Pattern pattern = Pattern.compile("(\\^H)+");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            int pos_start = matcher.start() - removedFormatCharsDelta;
            int pos_end = matcher.end() - removedFormatCharsDelta;
            // don't reformat in code blocks
            CodeBlockSpan[] spans = builder.getSpans(pos_start, pos_end, CodeBlockSpan.class);
            if (spans != null && spans.length != 0) { continue; }

            builder.setSpan(new StrikethroughSpan(), pos_start - (pos_end - pos_start) / 2, pos_start, 0);
            builder.delete(pos_start, pos_end);
            removedFormatCharsDelta += pos_end - pos_start;
        }
    }

    private void embedRefs() {
        Pattern referencePattern = Pattern.compile(">>(/?([a-z]{1,4})/)?(\\d+)");
        Matcher refMatcher = referencePattern.matcher(builder);

        while (refMatcher.find()) {
            int start = refMatcher.start();
            int end = refMatcher.end();

            // don't reformat in code blocks
            CodeBlockSpan[] spans = builder.getSpans(start, end, CodeBlockSpan.class);
            if (spans != null && spans.length != 0) { continue; }

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
        Pattern p = Pattern.compile("^(\\*\\s){1,2}([^\\*\\s].*?)$", Pattern.MULTILINE);
        Matcher m = p.matcher(builder);

        // when happens replacement with such strings as old.length!=new.length, actual size of builder changing and
        // indexing broken for following matching. Since that we need to track that discrepancy
        int delta = 0;
        final int replacementLength = 2; // == "○ ".length() == "● ".length()

        while (m.find()) {
            int start = m.start() - delta;
            int paraLength = m.group().length();
            boolean single = !m.group().substring(0, m.start(2) - m.start()).matches("\\*\\s\\*\\s");
            String replacement = single ? "● " : "○ ";
            int level = single ? 1 : 2;
            int oldStringLength = 2 * level; // "* " or "* * "
            int diff = oldStringLength - replacementLength;

            builder.replace(start, start + oldStringLength, replacement);
            builder.setSpan(new BulletListSpan(level), start - delta, start + paraLength - delta - diff, 0);
            delta += diff;
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

    @SuppressWarnings("ConstantConditions")
    private void replaceAll(String begin, String end, SpanObjectFactory factory, int flag) {
        int removedFormatCharsDelta = 0;
        Pattern pattern = Pattern.compile(String.format("(%s)(.+?)(%s)", begin, end), Pattern.MULTILINE | flag);
        String beginRestore = restoreBreaks(begin);
        String endRestore = restoreBreaks(end);
        Matcher matcher = pattern.matcher(builder);
        String inlinedString;

        boolean code = begin.contains("`");
        while (matcher.find()) {
            int start = matcher.start(2);
            int finish = matcher.end(2);

            // don't reformat in code blocks
            if (!code) {
                CodeBlockSpan[] spans = builder.getSpans(start, finish, CodeBlockSpan.class);
                if (spans != null && spans.length != 0) { continue; }
            }

            // don't reformat double borders while searchin for sinlges
            // e.g.: searching for "*abc*", found "**"
            inlinedString = matcher.group(2);
            if (inlinedString == null || "".equals(inlinedString)) {
                System.out.println(matcher.group());
                continue;
            }

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
            builder.replace(matcher.start() - removedFormatCharsDelta + beginRestore.length(),
                    matcher.start() + rep.length() - removedFormatCharsDelta + endRestore.length(), rep);

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
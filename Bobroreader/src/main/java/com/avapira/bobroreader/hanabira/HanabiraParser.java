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
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class HanabiraParser {
    private static class SpanItalic extends SpanObjectFactory {
        @Override
        Object getSpan() {
            return new StyleSpan(Typeface.ITALIC);
        }
    }

    private static class SpanBold extends SpanObjectFactory {
        @Override
        Object getSpan() {
            return new StyleSpan(Typeface.BOLD);
        }
    }

    private static class SpanBoldItalic extends SpanObjectFactory {
        @Override
        Object getSpan() {
            return new StyleSpan(Typeface.BOLD_ITALIC);
        }
    }

    public static void main(String[] args) {
        Pattern refPost = Pattern.compile(">>(/[a-z]{1,4}/)?([0-9]+)");
        Pattern bold1 = Pattern.compile("(\\*{2}(.*?)\\*{2})");
        Pattern bold2 = Pattern.compile("(_{2}.*?_{2})");
        Pattern italic1 = Pattern.compile("(\\*\\\\*(.+)\\*)");

        Pattern bold = Pattern.compile("(\\*\\*|__)(.*?)\\1");
        Pattern italic = Pattern.compile("(\\*|_)(.*?)\\1");
        Pattern ulList = Pattern.compile("\\n\\*(.*)");
        Pattern olList = Pattern.compile("\\n[0-9]+\\.(.*)");
        Pattern code = Pattern.compile("`(.*?)`");
    }

    static abstract class SpanObjectFactory {
        abstract Object getSpan();
    }

    final SpannableStringBuilder builder;
    final List<Integer>          skips;

    public HanabiraParser(String message) {
        message = replaceInternalLinkWithReference(message);
        Linkify.addLinks(builder, Linkify.WEB_URLS);
        builder = new SpannableStringBuilder(message);
        skips = new ArrayList<>();

        // all the 4+ spacen in row in the start of string is a non-parsed code
        // actually not
        // original parser also require that code-block alse to be separated by empty lines
        List<Integer> tmpSkips = new ArrayList<>();
        Pattern codeBlockPattern = Pattern.compile("^\\s{4,}.*$", Pattern.MULTILINE);
        Matcher codeBlockMatcher = codeBlockPattern.matcher(builder);
        while (codeBlockMatcher.find()) {
            skips.add(codeBlockMatcher.start());
            skips.add(codeBlockMatcher.end());
        }

        //simplify blocks
        for (int i = 0; i < tmpSkips.size() - 1; i++) {
            while (tmpSkips.get(i).) //todo continue here
        }
    }

    public CharSequence getFormatted(String message, String board, Context context, DobroPost dobropost) {
        if (message == null || message.length() == 0) { return ""; }
        formatTwoLevelBulletList();
        formatQuotes();
        formatReferences();
        formatSpoiler();

        // bold+italic
        formatBoldItalic();
        // bold
        formatBold();
        formatItalic();
        formatCode();

        someMethod(board, context, dobropost);
        someMethod2();
        return builder.toString();

    }

    private void someMethod2() {
        int delta = 0;
        String reverce = new StringBuffer(builder.toString()).reverse().toString();
        Pattern wordBounds = Pattern.compile("[^ ]+");
        Matcher wordMatcher = wordBounds.matcher(reverce);
        Pattern pattern = Pattern.compile("(\\^W)+");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            boolean cont = false;
            for (int i = 0; i < skips.size(); i += 2) {
                if (matcher.start() > skips.get(i) && matcher.start() < skips.get(i + 1)) {
                    cont = true;
                }
            }
            if (cont) { continue; }
            int pos_start = matcher.start() - delta;
            int pos_end = matcher.end() - delta;
            int wordsCount = (pos_end - pos_start) / 2;
            int word_start = reverce.length() - matcher.start();
            wordMatcher.reset();
            for (int i = 0; i < wordsCount; i++) {
                if (wordMatcher.find(word_start)) {
                    builder.setSpan(new StrikethroughSpan(), reverce.length() - wordMatcher.end() - delta,
                                    reverce.length() - wordMatcher.start() - delta, 0);
                    word_start = wordMatcher.end();
                }
            }
            delta += pos_end - pos_start;
            builder.delete(pos_start, pos_end);
        }
    }

    private void someMethod(String board, Context context, DobroPost dobropost) {

        formatReferences(board, context, dobropost);
        formatStrikethrough();
    }

    private void formatStrikethrough() {
        int delta = 0;
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

            int pos_start = matcher.start() - delta;
            int pos_end = matcher.end() - delta;
            delta += pos_end - pos_start;
            builder.setSpan(new StrikethroughSpan(), pos_start - (pos_end - pos_start) / 2, pos_start, 0);
            builder.delete(pos_start, pos_end);
        }
    }

    private void formatReferences(String board, Context context, DobroPost dobropost, Vector<Integer> skip_vector) {
        Pattern referencePattern = Pattern.compile(">>(?:/)?([a-z]{1,4}/)?(\\d+)");
        Matcher referenceMatcher = referencePattern.matcher(builder);
        while (referenceMatcher.find()) {
            boolean skip = false;
            for (int i = 0; i < skip_vector.size(); i += 2) {
                if (referenceMatcher.start() > skip_vector.get(i) &&
                        referenceMatcher.start() < skip_vector.get(i + 1)) {
                    skip = true;
                    break;
                }
            }
            if (skip) { continue; }

            int start = referenceMatcher.start();
            int end = referenceMatcher.end();
            builder.setSpan(new DobroLinkSpan(
                    referenceMatcher.group(1) == null ? board : referenceMatcher.group(1).replace("/", ""),
                    referenceMatcher.group(2), context, dobropost), start, end, 0);
        }
    }

    private void formatCode() {// mono
        replaceAll("``", "``", new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new ForegroundColorSpan(Color.parseColor("#2FA1E7"));
            }
        });
        replaceAll("`", "`", new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new ForegroundColorSpan(Color.parseColor("#2FA1E7"));
            }
        });
        // mono
        replaceAll("^``\r\n", "\r\n``$", new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new ForegroundColorSpan(Color.parseColor("#2FA1E7"));
            }
        }, Pattern.DOTALL);
    }

    private void formatItalic() {// italic
        replaceAll("\\*", "\\*", new SpanItalic());
        replaceAll("_", "_", new SpanItalic());
    }

    private void formatBold() {
        replaceAll("\\*\\*", "\\*\\*", new SpanBold());
        replaceAll("__", "__", new SpanBold());
    }

    private void formatBoldItalic() {
        replaceAll("_\\*\\*", "\\*\\*_", new SpanBoldItalic());
        replaceAll("__\\*", "\\*__", new SpanBoldItalic());
    }

    private void formatSpoiler() {// spoiler
        replaceAll("%%", "%%", new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new SpoilerSpan();
            }
        });
        replaceAll("%%", " %%", new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new SpoilerSpan();
            }
        });
        replaceAll("^%%\r\n", "\r\n%%$", new SpanObjectFactory() {
            @Override
            Object getSpan() {
                return new SpoilerSpan();
            }
        }, Pattern.DOTALL);
    }

    private void formatReferences() {// dbl quote
        Pattern pattern = Pattern.compile("^>(\\s*>)+[^(\\d+|[a-z]{1,4}/\\d+)].*?$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            int pos_start = matcher.start();
            int pos_end = matcher.end();
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#406010")), pos_start, pos_end, 0);
        }
    }

    private void formatQuotes() {// quote
        Pattern pattern = Pattern.compile("^>[^>].*?$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            int pos_start = matcher.start();
            int pos_end = matcher.end();
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#789922")), pos_start, pos_end, 0);
        }
    }

    private void formatTwoLevelBulletList() {
        Pattern p = Pattern.compile("^(\\*){1,2} ", Pattern.MULTILINE);
        Matcher m = p.matcher(builder);
        while (m.find()) {
            int st = m.start();
            if (m.group().length() == 2) {
                builder.replace(st, st + 2, "● ");
            } else {
                builder.replace(st, st + 3, " ○ ");
            }
        }
    }

    private static String replaceInternalLinkWithReference(String message) {
        Pattern p = Pattern.compile(
                "http(?:s)?://(?:suigintou.net|dobrochan.(?:ru|com|org))/([a-z]{1,4})/res/(\\d+).xhtml(?:#i" +
                        "(\\d+))?");
        Matcher matcher = p.matcher(message);
        while (matcher.find()) {
            message = message.replaceFirst(matcher.group(), String.format(">>%s/%s", matcher.group(1),
                                                                          (matcher.group(3) == null) ? matcher.group(
                                                                                  2) : matcher.group(3)));
        }
        return message;
    }

    private void replaceAll(String begin, String end, SpanObjectFactory factory) {
        replaceAll(begin, end, factory, 0);
    }

    private void replaceAll(String begin, String end, SpanObjectFactory factory, int flag) {
        int delta = 0;
        Pattern pattern = Pattern.compile(String.format("(%s(\\S.*?)%s)", begin, end), Pattern.MULTILINE | flag);
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            if (flag == 0) {
                boolean cont = false;
                for (int i = 0; i < skips.size(); i += 2) {
                    if (matcher.start(1) > skips.get(i) && matcher.start(1) < skips.get(i + 1)) {
                        cont = true;
                    }
                }

                URLSpan[] spans = builder.getSpans(matcher.start(1), matcher.end(1), URLSpan.class);
                if (spans != null && spans.length > 0 && begin.contains("_")) { cont = true; }
                if (cont) { continue; }
            }
            if (matcher.group(2).startsWith(begin.replace("\\", "")) &&
                    matcher.group(2).length() <= begin.replace("\\", "").length()) { continue; }
            int pos_start = matcher.start(1) - delta;
            int pos_end = matcher.end(1) - delta;
            SpannableString rep = new SpannableString(matcher.group(2));
            rep.setSpan(factory.getSpan(), 0, rep.length(), 0);
            Linkify.addLinks(rep, Linkify.WEB_URLS);
            builder.replace(pos_start, pos_end, rep);
            delta += matcher.group(1).length() - matcher.group(2).length();
        }
    }
}
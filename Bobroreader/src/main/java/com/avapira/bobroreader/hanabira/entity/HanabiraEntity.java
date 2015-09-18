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

package com.avapira.bobroreader.hanabira.entity;

import android.support.annotation.Nullable;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.google.gson.*;
import org.joda.time.LocalDateTime;

import java.lang.reflect.Type;
import java.util.*;

/**
 *
 */
abstract class HanabiraEntity {

    private static final Gson gson;
    private static final Gson prettyGson;

    private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
            return extractLocatDateTime(json);
        }
    }

    private static class HanabiraBoardDeserializer implements JsonDeserializer<HanabiraBoard> {

        String boardKey;

        @Override
        public HanabiraBoard deserialize(JsonElement json, Type type, JsonDeserializationContext context)
        throws JsonParseException {
            JsonObject boardsJsonObject = json.getAsJsonObject().get("boards").getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> boardsSet = boardsJsonObject.entrySet();
            if (boardsSet.size() > 1) {
                throw new IllegalArgumentException("Few board entries received for a request");
            }
            Map.Entry<String, JsonElement> boardEntry = boardsSet.iterator().next();
            boardKey = boardEntry.getKey();
            JsonObject boardObject = boardEntry.getValue().getAsJsonObject();
            int pages = boardObject.get("pages").getAsInt();

            HanabiraBoard cachedBoard = Hanabira.getStem().findBoardByKey(boardKey);
            if (cachedBoard == null) {
                cachedBoard = new HanabiraBoard(boardKey, pages, null);
                Hanabira.getStem().saveBoard(cachedBoard);
            } else {
                cachedBoard.update(pages, null);
            }

            JsonElement pageElement = boardObject.get("page");
            int page = (pageElement == null) ? 0 : pageElement.getAsInt();

            List<Integer> threadsOnPageIds = new ArrayList<>();
            for (JsonElement threadElement : boardObject.getAsJsonArray("threads")) {
                threadsOnPageIds.add(createThreadWithPosts(threadElement.getAsJsonObject(), boardKey).getThreadId());
            }
            cachedBoard.updatePage(page, threadsOnPageIds);

            return cachedBoard;
        }

    }

    private static class HanabiraThreadDeserializer implements JsonDeserializer<HanabiraThread> {

        @Override
        public HanabiraThread deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
            if (json == null) {
                throw new IllegalArgumentException();
            }
            JsonObject jsonObject = json.getAsJsonObject();
            return createThreadWithPosts(jsonObject, null);
        }
    }

    private static LocalDateTime extractLocatDateTime(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        } else {
            return LocalDateTime.parse(jsonElement.getAsString().replace(' ', 'T'));
        }
    }

    private static HanabiraThread createThreadWithPosts(JsonObject threadObject, @Nullable String boardKey) {
        int threadId = threadObject.get("thread_id").getAsInt();
        LocalDateTime modifiedDate = extractLocatDateTime(threadObject.get("last_modified"));

        HanabiraThread thread = Hanabira.getStem().findThreadById(threadId);
        boolean oldThread = thread != null;
        if (oldThread) {
            if (modifiedDate == null || !thread.isUpToDate(modifiedDate)) {
                // update thread meta
                thread.setPostsCount(threadObject.get("posts_count").getAsInt());
                thread.setArchived(threadObject.get("archived").getAsBoolean());
                thread.setFilesCount(threadObject.get("files_count").getAsInt());
                thread.setTitle(threadObject.get("title").getAsString());
                thread.setLastHit(extractLocatDateTime(threadObject.get("last_hit")));
                thread.setModifiedDate(modifiedDate);
            }
        } else {
            // brand new thread
            int displayId = threadObject.get("display_id").getAsInt();
            boolean archived = threadObject.get("archived").getAsBoolean();
            int postsCount = threadObject.get("posts_count").getAsInt();
            int filesCount = threadObject.get("files_count").getAsInt();
            String title = threadObject.get("title").getAsString();
            int boardId = boardKey != null ? HanabiraBoard.Info.getIdForKey(boardKey) : threadObject.get("board_id")
                                                                                                    .getAsInt();
            boolean autosage = threadObject.get("autosage").getAsBoolean();
            LocalDateTime lastHit = extractLocatDateTime(threadObject.get("last_hit"));

            thread = new HanabiraThread(displayId, threadId, modifiedDate, postsCount, filesCount, boardId, archived,
                    title, autosage, lastHit);
            Hanabira.getStem().saveThread(thread, boardKey);
        }

        // cache posts
        for (JsonElement postElement : threadObject.getAsJsonArray("posts")) {
            createAndSavePost(postElement.getAsJsonObject(), threadId, boardKey);
        }

        if (!oldThread) {
            // set thread creation date
            HanabiraPost opPost = Hanabira.getStem().findPostByDisplayId(boardKey, thread.getDisplayId());
            if (opPost == null || !opPost.isOp()) {
                throw new InputMismatchException("Op post not received");
            }
            thread.setCreatedDate(opPost.getCreatedDate());
        }
        return thread;
    }

    private static HanabiraPost createAndSavePost(JsonObject postObject, int threadId, String boardKey) {
        // todo files
        int postId = postObject.get("post_id").getAsInt();
        LocalDateTime modifiedDate = extractLocatDateTime(postObject.get("last_modified"));

        HanabiraPost cachedPost = Hanabira.getStem().findPostById(postId);
        if (cachedPost != null) {
            if (modifiedDate == null || !cachedPost.isUpToDate(modifiedDate)) {
                // update
                cachedPost.setModifiedDate(modifiedDate);
                cachedPost.setMessage(postObject.get("message").getAsString());
                cachedPost.setName(postObject.get("name").getAsString());
                cachedPost.setSubject(postObject.get("subject").getAsString());
            }
        } else {
            // brand new post
            int displayId = postObject.get("display_id").getAsInt();
            LocalDateTime createdDate = extractLocatDateTime(postObject.get("date"));
            String message = postObject.get("message").getAsString();
            String subject = postObject.get("subject").getAsString();
            String name = postObject.get("name").getAsString();
            boolean op = postObject.get("op").getAsBoolean();
            int boardId =
                    boardKey != null ? HanabiraBoard.Info.getIdForKey(boardKey) : postObject.get("board_id").getAsInt();
            cachedPost = new HanabiraPost(displayId, modifiedDate, createdDate, postId, message, subject, boardId, name,
                    threadId, op);
            Hanabira.getStem().savePost(cachedPost, boardKey);
        }
        return cachedPost;
    }

    public static <T extends HanabiraEntity> T fromJson(String jsonString, Class<T> clazz) {
        return gson.fromJson(jsonString, clazz);
    }

    public String toJson() {
        return gson.toJson(this);
    }

    String toPrettyJson() {
        return prettyGson.toJson(this);
    }

    static {
        gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                                .registerTypeAdapter(HanabiraBoard.class, new HanabiraBoardDeserializer())
                                .registerTypeAdapter(HanabiraThread.class, new HanabiraThreadDeserializer())
                                .create();
        prettyGson = new GsonBuilder().setPrettyPrinting().create();
    }

}

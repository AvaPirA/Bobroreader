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

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.joda.time.LocalDateTime;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
abstract class HanabiraEntity {

    private static LocalDateTime extractLocatDateTime(JsonElement jsonElement) {
         return LocalDateTime.parse(jsonElement.getAsString().replace(' ', 'T'));
    }

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
            JsonObject boardData = boardEntry.getValue().getAsJsonObject();
            final int pages = boardData.get("pages").getAsInt();
            final Object capabilities = null;

            JsonArray threads = boardData.getAsJsonArray("threads");
            final TreeSet<HanabiraThread> threadsSet = new TreeSet<>(new HanabiraThread.ModificationDateComparator());
            for (JsonElement threadElement : threads) {
                threadsSet.add(createThread(threadElement.getAsJsonObject()));
            }
        }

        private HanabiraThread createThread(JsonObject threadObject) {

            int dispayId = threadObject.get("display_id").getAsInt();
            int threadId = threadObject.get("thread_id").getAsInt();
            boolean archived = threadObject.get("archived").getAsBoolean();
            LocalDateTime modifiedDate = extractLocatDateTime(threadObject.get("last_modified"));
            int filesCount = threadObject.get("files_count").getAsInt();
            String title = threadObject.get("title").getAsString();
            int postsCount = threadObject.get("posts_count").getAsInt();
            int boardId = HanabiraBoardInfo.getForBoard(boardKey).getId();
            boolean autosage = threadObject.get("autosage").getAsBoolean();
            LocalDateTime lastHit = extractLocatDateTime(threadObject.get("last_hit"));

            JsonArray opAndEndOfThread = threadObject.getAsJsonArray("posts");
            TreeSet<HanabiraPost> postsSet = new TreeSet<>(new HanabiraPost.ModificationDateComparator());
            for(JsonElement postElement) {
            }
            LocalDateTime createdDate = postsSet.first().getDate(); // OP post and thread creation date is the same
        }
    }


    static {
        gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer()).create();
        prettyGson = new GsonBuilder().setPrettyPrinting().create();
    }

    private static final Gson gson;
    private static final Gson prettyGson;

    public static <T extends HanabiraEntity> T fromJson(String jsonString, Class<T> clazz) {
        return gson.fromJson(jsonString, clazz);
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public String toPrettyJson() {
        return prettyGson.toJson(this);
    }

}

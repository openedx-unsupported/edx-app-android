package org.edx.mobile.model.course;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * it is served as a base class for data{} block. as
 * the detail/schema of the block depends on the block type
 */
public class BlockData implements Serializable{
    public static class Deserializer implements JsonDeserializer<BlockData> {
        @Override
        public BlockData deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            //TODO - can not figure out a way to pass parent properties, for example, "type" field
            //so have to check the existence of certain fields
            if (jsonObject.has("encoded_videos") || jsonObject.has("transcripts")) {
                return context.deserialize(jsonObject, VideoData.class);
            } else if (jsonObject.has("topic_id")) {
                return context.deserialize(jsonObject, DiscussionData.class);
            }
            return new BlockData();
        }
    }
}

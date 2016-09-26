package org.edx.mobile.model.course;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.logger.Logger;

import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;

/**
 * Created by hanning on 5/19/15.
 */
public class CourseStructureJsonHandler {
    protected static final Logger logger = new Logger(CourseStructureJsonHandler.class.getName());

    public CourseStructureV1Model processInput(String jsonInput) {
        BlockDataDeserializer dataDeserializer = new BlockDataDeserializer();
        BlockListDeserializer blockListDeserializer = new BlockListDeserializer();
        BlockTypeDeserializer blockTypeListDeserializer = new BlockTypeDeserializer();

        //ideally we should use this approach. but it requires base class to
        //define some properties.
//        RuntimeTypeAdapterFactory<BlockData> rta = RuntimeTypeAdapterFactory.of(
//            BlockData.class)
//            .registerSubtype(VideoData.class);

        Gson gson = new GsonBuilder()  //.registerTypeAdapterFactory(rta)
                .registerTypeAdapter(BlockData.class, dataDeserializer)
                .registerTypeAdapter(BlockType.class, blockTypeListDeserializer)
                .registerTypeAdapter(BlockList.class, blockListDeserializer).create();

        return gson.fromJson(jsonInput, CourseStructureV1Model.class);
    }


    private static class BlockDataDeserializer implements JsonDeserializer<BlockData> {
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

    private static class BlockListDeserializer implements JsonDeserializer<BlockList> {
        @Override
        public BlockList deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            Type mapType = new TypeToken<Map<String, BlockModel>>() {}.getType();
            JsonObject jsonObject = json.getAsJsonObject();
            Map<String, BlockModel> map = (Map<String, BlockModel>) context.deserialize(jsonObject, mapType);
            return new BlockList(map);
        }
    }

    private static class BlockTypeDeserializer implements JsonDeserializer<BlockType> {
        @Override
        public BlockType deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            String value = json.getAsString();
            try {
                //we force the String.toUpperCase to use English local.
                //as it is just a mapping from english string to constants.
                return BlockType.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception ex) {
                logger.debug(ex.getMessage());
                return BlockType.OTHERS;
            }
        }
    }
}

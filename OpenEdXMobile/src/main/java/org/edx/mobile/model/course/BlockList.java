package org.edx.mobile.model.course;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 *  It is not great.. but we have to match the data structure
 *  returned from server
 */
public class BlockList extends HashMap<String, BlockModel> {
    public BlockList(Map<String,BlockModel> map){
        super(map);
    }

    public static class Deserializer implements JsonDeserializer<BlockList> {
        @Override
        public BlockList deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
            Type mapType = new TypeToken<Map<String, BlockModel>>() {}.getType();
            JsonObject jsonObject = json.getAsJsonObject();
            Map<String, BlockModel> map = context.deserialize(jsonObject, mapType);
            return new BlockList(map);
        }
    }
}

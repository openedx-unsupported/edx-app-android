package org.edx.mobile.model.course;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.edx.mobile.logger.Logger;

import java.lang.reflect.Type;
import java.util.Locale;

/**
 * Created by hanning on 5/21/15.
 */
public enum  BlockType {

    COURSE{ @Override public boolean isContainer() {return true;} },
    CHAPTER{ @Override public boolean isContainer() {return true;} },
    SECTION{ @Override public boolean isContainer() {return true;} },
    SEQUENTIAL{ @Override public boolean isContainer() {return true;} },
    VERTICAL{ @Override public boolean isContainer() {return true;} },
    VIDEO{ @Override public boolean isContainer() {return false;} },
    HTML{ @Override public boolean isContainer() {return false;} },
    PROBLEM{ @Override public boolean isContainer() {return false;} },
    DISCUSSION{ @Override public boolean isContainer() {return false;} },
    OTHERS{ @Override public boolean isContainer() {return false;} };

    abstract boolean isContainer();

    public static class Deserializer implements JsonDeserializer<BlockType> {
        private final Logger logger = new Logger(Deserializer.class.getName());

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

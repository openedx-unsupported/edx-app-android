package org.edx.mobile.tta.wordpress_client.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.JsonAdapter;

import java.lang.reflect.Type;
/**
 * Created by JARVICE on 27-03-2018.
 */
public class CustomFilter
{
    @JsonAdapter(ChoicesDeserializer.class)
    private String[] choices;

    private String name;

    public String[] getChoices ()
    {
        return choices;
    }

    public void setChoices (String[] choices)
    {
        this.choices = choices;
    }

    public String getName ()
    {
        if(name==null)
            name="";
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }



    @Override
    public String toString()
    {
        return "CustomFilter [choices = "+choices+", name = "+name+"]";
    }


    //for dynamic mapping

    public static class ChoicesDeserializer implements JsonDeserializer<String[]> {

        @Override
        public String[] deserialize(JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {

            if(json==null)
                return new String[]{};


            if (json instanceof JsonArray) {

                return new Gson().fromJson(json, String[].class);
            }

            String child = context.deserialize(json, String.class);

            return new String[] { child };
        }
    }
}
package org.edx.mobile.model.api;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.R;

import java.io.Serializable;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class TranscriptModel implements Serializable{

    @SerializedName("en")
    public String englishUrl;

    @SerializedName("es")
    public String spanishUrl;

    @SerializedName("de")
    public String germanUrl;

    @SerializedName("pt")
    public String portugueseUrl;

    @SerializedName("zh")
    public String chineseUrl;
    
    @SerializedName("fr")
    public String frenchUrl;

    public LinkedHashMap<String, String> getLanguageList(Context context){
        LinkedHashMap<String, String> languageArray = new LinkedHashMap<String, String>();
        if(chineseUrl!=null){
            languageArray.put(context.getString(R.string.cc_chinese_code), 
                    context.getString(R.string.lbl_cc_chinese));
        }
        if(englishUrl!=null){
            languageArray.put(context.getString(R.string.cc_english_code), 
                    context.getString(R.string.lbl_cc_english));
        }
        if(frenchUrl!=null){
            languageArray.put(context.getString(R.string.cc_french_code), 
                    context.getString(R.string.lbl_cc_french));
        }
        if(germanUrl!=null){
            languageArray.put(context.getString(R.string.cc_german_code), 
                    context.getString(R.string.lbl_cc_german));
        }
        if(portugueseUrl!=null){
            languageArray.put(context.getString(R.string.cc_portugal_code), 
                    context.getString(R.string.lbl_cc_portugal));
        }
        if(spanishUrl!=null){
            languageArray.put(context.getString(R.string.cc_spanish_code), 
                    context.getString(R.string.lbl_cc_spanish));
        }
        
        if(languageArray.size()>0){
            return languageArray;
        }else{
            return null;
        }
    }

    /*public String labelFor(Context context, String lang) {
        int resId = context.getResources().getIdentifier(lang, "string", context.getPackageName());
        String label = context.getResources().getString(resId);
        return label;
    }*/
}

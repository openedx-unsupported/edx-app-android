package org.edx.mobile.tta.utils;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.enums.SourceType;

public class SourceUtil {

    public static int getSourceColor(String sourceName){
        if (sourceName.equalsIgnoreCase(SourceType.course.toString())){
            return R.color.secondary_blue;
        } else if (sourceName.equalsIgnoreCase(SourceType.chatshala.toString())){
            return R.color.secondary_blue_light;
        } else if (sourceName.equalsIgnoreCase(SourceType.hois.toString())){
            return R.color.secondary_red;
        } else {
            return R.color.secondary_green;
        }
    }

}

package org.edx.mobile.tta.utils;

import org.edx.mobile.R;
import org.edx.mobile.tta.data.enums.SourceName;

public class ContentSourceUtil {

    public static int getSourceColor(String sourceName){
        if (sourceName.equalsIgnoreCase(SourceName.course.toString())){
            return R.color.secondary_blue;
        } else if (sourceName.equalsIgnoreCase(SourceName.chatshala.toString())){
            return R.color.secondary_blue_light;
        } else if (sourceName.equalsIgnoreCase(SourceName.hois.toString())){
            return R.color.secondary_red;
        } else if (sourceName.equalsIgnoreCase(SourceName.toolkit.toString())){
            return R.color.secondary_green;
        } else {
            return R.color.secondary_blue_light;
        }
    }

    public static int getSourceDrawable_10x10(String sourceName){
        if (sourceName.equalsIgnoreCase(SourceName.course.toString())){
            return R.drawable.t_icon_course_10;
        } else if (sourceName.equalsIgnoreCase(SourceName.chatshala.toString())){
            return R.drawable.t_icon_chatshala_10;
        } else if (sourceName.equalsIgnoreCase(SourceName.hois.toString())){
            return R.drawable.t_icon_hois_10;
        } else if (sourceName.equalsIgnoreCase(SourceName.toolkit.toString())){
            return R.drawable.t_icon_toolkit_10;
        } else {
            return R.drawable.t_icon_course_10;
        }
    }

    public static int getSourceDrawable_15x15(String sourceName){
        if (sourceName.equalsIgnoreCase(SourceName.course.toString())){
            return R.drawable.t_icon_course_15;
        } else if (sourceName.equalsIgnoreCase(SourceName.chatshala.toString())){
            return R.drawable.t_icon_chatshala_15;
        } else if (sourceName.equalsIgnoreCase(SourceName.hois.toString())){
            return R.drawable.t_icon_hois_10;
        } else if (sourceName.equalsIgnoreCase(SourceName.toolkit.toString())){
            return R.drawable.t_icon_toolkit_15;
        } else {
            return R.drawable.t_icon_course_15;
        }
    }

}

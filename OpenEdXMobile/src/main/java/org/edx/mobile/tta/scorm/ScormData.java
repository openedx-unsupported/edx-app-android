package org.edx.mobile.tta.scorm;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.model.course.BlockData;

public class ScormData extends BlockData {

    @SerializedName("last_modified")
    public String lastModified;

    @SerializedName("scorm_data")
    public String scormData;

    @SerializedName("scorm_image_url")
    public String scormImageUrl;

    @SerializedName("scorm_duration")
    public String scormDuration;

}

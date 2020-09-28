package org.humana.mobile.tta.scorm;

import com.google.gson.annotations.SerializedName;

import org.humana.mobile.model.course.BlockData;

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
/*public class ScormData extends BlockData {
    @SerializedName("last_modified")
    public String lastModified;
    @SerializedName("scorm_data")
    public String scormData="https://tfi-scorm.s3.amazonaws.com/TeachForIndia/BR403/scorm/881ffd5291e24dad9ca73874109a7c2e/76a219a07bf0dcf9b49afc7f59380f0e9028da11.zip";
    @SerializedName("scorm_image_url")
    public String scormImageUrl;
    @SerializedName("scorm_duration")
    public String scormDuration;
}*/


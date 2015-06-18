package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by hanning on 5/19/15.
 */
public class BlockCount implements Serializable{
    @SerializedName("video")
    public int videoCount;
}

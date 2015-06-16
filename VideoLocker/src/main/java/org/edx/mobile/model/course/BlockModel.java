package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hanning on 5/19/15.
 */
public class BlockModel implements Serializable{
    //TODO - we can not use enum type here as server side
    //may return different type not included here....


    @SerializedName("id")
    public String id;

    @SerializedName("type")
    public BlockType type;


    @SerializedName("display_name")
    public String displayName;

    @SerializedName("graded")
    public boolean graded;

    @SerializedName("responsive_ui")
    public boolean responsiveUI;

    @SerializedName("block_url")
    public String blockUrl;

    @SerializedName("block_count")
    public BlockCount blockCount;

    @SerializedName("web_url")
    public String webUrl;

    @SerializedName("format")
    public String format;

    // descendants: (list) A list of IDs of the children of the block if the block's depth in the
   // course hierarchy is less than the navigation_depth.  Otherwise, a list of IDs of the aggregate descendants
   // of the block.
    @SerializedName("descendants")
    public List<String> descendants;

    @SerializedName("block_json")
    public BlockData data;

    public boolean isContainer(){
        return descendants != null && descendants.size() > 0;
    }
}

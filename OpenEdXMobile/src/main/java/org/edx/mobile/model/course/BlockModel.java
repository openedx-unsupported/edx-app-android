package org.edx.mobile.model.course;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BlockModel implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("block_id")
    public String blockId;

    @SerializedName("type")
    public BlockType type;

    @SerializedName("display_name")
    public String displayName;

    @SerializedName("graded")
    public boolean graded;

    @SerializedName("student_view_multi_device")
    public boolean studentViewMultiDevice;

    @SerializedName("student_view_url")
    public String studentViewUrl;

    @SerializedName("block_counts")
    public BlockCount blockCounts;

    @SerializedName("lms_web_url")
    public String lmsWebUrl;

    @SerializedName("format")
    public String format;

    @SerializedName("due")
    public String dueDate;

    // descendants: (list) A list of IDs of the children of the block if the block's depth in the
    // course hierarchy is less than the navigation_depth.  Otherwise, a list of IDs of the aggregate descendants
    // of the block.
    @SerializedName("descendants")
    @Nullable
    public List<String> descendants;

    @SerializedName("student_view_data")
    public BlockData data;

    public boolean isContainer() {
        return type != null ? type.isContainer() : (descendants != null && descendants.size() > 0);
    }
}

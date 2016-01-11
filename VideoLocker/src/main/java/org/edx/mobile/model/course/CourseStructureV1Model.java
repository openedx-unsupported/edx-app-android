package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * directly map raw json data from server.
 */
public class CourseStructureV1Model implements Serializable {

    @SerializedName("blocks")
    public BlockList blockData;

    @SerializedName("root")
    public String root;

    public BlockModel getBlockById(String id) {
        return blockData.get(id);
    }

    /**
     * if parent is null, returns empty list.
     */
    public List<BlockModel> getDescendants(BlockModel parent) {
        List<BlockModel> descendants = new ArrayList<>();
        if (parent == null || !parent.isContainer() || null == parent.descendants)
            return descendants;
        for (String id : parent.descendants) {
            BlockModel model = getBlockById(id);
            if (model != null) {
                descendants.add(model);
            }
        }
        return descendants;
    }
}

package org.edx.mobile.model.course;

import java.util.HashMap;
import java.util.Map;

/**
 *  It is not great.. but we have to match the data structure
 *  returned from server
 */
public class BlockList extends HashMap<String, BlockModel> {
    public BlockList(Map<String,BlockModel> map){
        super(map);
    }
}

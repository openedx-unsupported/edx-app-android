package org.edx.mobile.model.api;

import org.edx.mobile.util.LogUtil;

public class SyncLastAccessedSubsectionResponse {

    public String last_visited_module_id;
    public String[] last_visited_module_path;
    
    public String getLastVisitedModuleId() {
        if(last_visited_module_path != null && last_visited_module_path.length > 2) {
            if (last_visited_module_path[2].contains("sequential")) {
                return last_visited_module_path[2];
            } else {
                for(String path : last_visited_module_path) {
                    if(path.contains("sequential")) {
                        return path;
                    }
                }
            }
        } else {
            LogUtil.error(getClass().getName(), "last visited module path is NULL");
        }
        return null;
    }
}

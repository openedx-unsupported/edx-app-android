package org.edx.mobile.model.api;

import org.edx.mobile.logger.Logger;

public class SyncLastAccessedSubsectionResponse {

    public String last_visited_module_id;
    public String[] last_visited_module_path;
    private final Logger logger = new Logger(getClass().getName());

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
        }
        logger.warn("Last visited module path is NULL");
        return null;
    }
}

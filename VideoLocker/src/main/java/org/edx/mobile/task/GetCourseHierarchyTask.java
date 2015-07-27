package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.services.ServiceManager;

import java.util.Map;

@Deprecated
public abstract class GetCourseHierarchyTask extends
Task<Map<String, SectionEntry>> {
    String courseId;
    public GetCourseHierarchyTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    public Map<String, SectionEntry> call( ) throws Exception{
        try {
            //String url = (String) (params[1]);
            if(courseId!=null){
                ServiceManager api = environment.getServiceManager();
                try {
                    // return instant data from cache
                    final Map<String, SectionEntry> map = api.getCourseHierarchy(courseId, true);
                    if (map != null) {
                        handler.post(new Runnable() {
                            public void run() {
                                try {
                                    onSuccess(map);
                                }catch (Exception ex){
                                    handle(ex);
                                    logger.error(ex);
                                }
                                stopProgress();
                            }
                        });
                    }
                } catch(Exception ex) {
                    logger.error(ex);
                }

                // return live data
                return api.getCourseHierarchy(courseId, false);
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}

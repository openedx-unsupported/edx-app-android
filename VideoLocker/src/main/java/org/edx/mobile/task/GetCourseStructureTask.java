package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.http.OkHttpUtil;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.module.prefs.PrefManager;

import java.util.Date;

public abstract class GetCourseStructureTask extends
Task<CourseComponent> {

    String courseId;

    public GetCourseStructureTask(Context context, String courseId) {
        super(context);
        this.courseId = courseId;
    }

    public CourseComponent call( ) throws Exception{
        try {

            if(courseId!=null){
                PrefManager.UserPrefManager prefManager = new PrefManager.UserPrefManager(MainApplication.instance());
                long lastFetchTime = prefManager.getLastCourseStructureFetch(courseId);
                long curTime = new Date().getTime();
                OkHttpUtil.REQUEST_CACHE_TYPE useCacheType = OkHttpUtil.REQUEST_CACHE_TYPE.PREFER_CACHE;
                //if last fetch happened over one hour ago, re-fetch data
                if ( lastFetchTime + 3600 * 1000 < curTime ){
                    useCacheType =  OkHttpUtil.REQUEST_CACHE_TYPE.IGNORE_CACHE;;
                    prefManager.setLastCourseStructureFetch(courseId, curTime);
                }
                final CourseComponent model = environment.getServiceManager().getCourseStructure(courseId, useCacheType);
                if (model != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            try {
                                onSuccess(model);
                            } catch (Exception e) {
                                handle(e);
                                logger.error(e);
                            }
                            stopProgress();
                        }
                    });
                }
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}

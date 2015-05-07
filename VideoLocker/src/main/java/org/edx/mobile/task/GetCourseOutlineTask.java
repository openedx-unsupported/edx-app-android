package org.edx.mobile.task;

import android.content.Context;

import org.edx.mobile.http.Api;
import org.edx.mobile.model.IChapter;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.mocked.MockedCourseOutlineProvider;
import org.edx.mobile.services.CourseManager;
import org.edx.mobile.view.CourseBaseActivity;

import java.util.ArrayList;
import java.util.Map;

public abstract class GetCourseOutlineTask extends
Task<ICourse> {

    public GetCourseOutlineTask(Context context) {
        super(context);
    }

    protected ICourse doInBackground(Object... params) {
        try {
            String courseId = (String) (params[0]);
            //String url = (String) (params[1]);
            if(courseId!=null){
                Api api = new Api(context);
                //TODO - the api  api.getVideosByCourseId(courseId, true)
                // should check local cache, and if local cache is null, it should
                //return the live data. but current implementation does not do that!
                try {
                    // return instant data from cache
                    final ArrayList<VideoResponseModel> response = api.getVideosByCourseId(courseId, true);
                    if (response != null) {
//                        String result = new MockedCourseOutlineProvider().getCourseOutline();
//                        return CourseManager.fromEnrollment(result, courseId);
                          return CourseManager.fromEnrollment(response, courseId);
                    }
                } catch(Exception ex) {
                    logger.error(ex);
                }

                try {
                    // return instant data from live.
                    final ArrayList<VideoResponseModel> response = api.getVideosByCourseId(courseId, false);
                    if (response != null) {
                        return CourseManager.fromEnrollment(response, courseId);
                    }
                } catch(Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }
}

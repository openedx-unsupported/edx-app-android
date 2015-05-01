package org.edx.mobile.services;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.model.ChapterModel;
import org.edx.mobile.model.CourseModel;
import org.edx.mobile.model.IChapter;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IVertical;
import org.edx.mobile.model.SequentialModel;
import org.edx.mobile.model.VerticalModel;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.PathModel;
import org.edx.mobile.model.api.SummaryModel;
import org.edx.mobile.model.api.VideoResponseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * no matter it is a smart phone, phablet, or tablet, app should show
 * the detail of only one course detail. so keep the logic at service
 * level is better than passing around between activities and fragments.
 *
 * TODO - the data will be persistent.
 *
 */
public class CourseManager {

    private static CourseManager instance;

    public static synchronized CourseManager getSharedInstance(){
        if ( instance == null )
            instance = new CourseManager();
        return instance;
    }

    private ICourse courseInView;
    private EnrolledCoursesResponse courseData;
    private IChapter chapterInView;
    private ISequential sequentialInView;

    private CourseManager(){

    }

    public void clear(){
        courseInView = null;
        courseData = null;
        chapterInView = null;
        sequentialInView = null;
    }

    public ICourse getCourseInView() {
        return courseInView;
    }

    public void setCourseInView(ICourse courseInView) {
        this.courseInView = courseInView;
        if (this.courseInView != null && courseData != null &&
            ! this.courseData.getCourse().getId().equalsIgnoreCase(this.courseInView.getId())){
            this.courseData = null;
        }
        chapterInView = null;
        sequentialInView = null;
    }

    public IChapter getChapterInView() {
        return chapterInView;
    }

    public void setChapterInView(IChapter chapterInView) {
        this.chapterInView = chapterInView;
        this.sequentialInView = null;
    }

    public EnrolledCoursesResponse getCourseData() {
        return courseData;
    }

    public void setCourseData(EnrolledCoursesResponse courseData) {
        this.courseData = courseData;
        if (courseInView != null && this.courseData != null
            && ! this.courseData.getCourse().getId().equalsIgnoreCase(courseInView.getId())){
            this.courseInView = null;
            chapterInView = null;
            sequentialInView = null;
        }
    }

    public ISequential getSequentialInView() {
        return sequentialInView;
    }

    public void setSequentialInView(ISequential sequentialInView) {
        this.sequentialInView = sequentialInView;
    }

    /**
     * this method exists
     *
     * @param serverResponse data from
     *    getBaseUrl() + "/api/mobile/v0.5/video_outlines/courses/" + course_id
     * @return course with hierarchy
     */
    public static ICourse fromEnrollment(String serverResponse, String courseId){
        Gson gson = new GsonBuilder().create();
        TypeToken<ArrayList<VideoResponseModel>> t = new TypeToken<ArrayList<VideoResponseModel>>() {
        };

        ArrayList<VideoResponseModel> list = gson.fromJson(serverResponse, t.getType());
        return fromEnrollment(list, courseId);
    }

    public static ICourse fromEnrollment(ArrayList<VideoResponseModel> videoResponseModel, String courseId){
        ICourse course = new CourseModel();
        course.setId(courseId);

        for (VideoResponseModel m : videoResponseModel) {
            // add each video to its corresponding chapter and section

            // add this as a chapter
            PathModel ochapter = m.getChapter();
            if (ochapter == null)
                continue;
            String chapterId = ochapter.id;
            String chapterName = ochapter.name;
            IChapter chapter = course.getChapterById(chapterId);
            if (chapter == null) {
                chapter = new ChapterModel(course, chapterId, chapterName);
                course.getChapters().add(chapter);
                chapter.setCategory(ochapter.category);
            }
            // carry this courseId with video model
            m.setCourseId(courseId);

            PathModel osequential = m.getSection();
            if (osequential == null)
                continue;

            ISequential sequential = chapter.getSequentialById(osequential.id);
            if (sequential == null) {
                sequential = new SequentialModel(chapter, osequential.id, osequential.name);
                chapter.getSequential().add(sequential);
                sequential.setCategory(osequential.category);

            }
            sequential.setSectionUrl(m.section_url);

            PathModel overtical = m.getVertical();
            if (overtical == null)
                continue;

            IVertical vertical = sequential.getVerticalById(overtical.id);
            if (vertical == null) {
                vertical = new VerticalModel(sequential, overtical.id, overtical.name);
                sequential.getVerticals().add(vertical);
                vertical.setId(overtical.id);
                vertical.setCategory(overtical.category);
            }
            vertical.setUnitUrl(m.unit_url);

            SummaryModel unit = m.getSummary();
            if (unit == null)
                continue;
            unit.setVertical(vertical);
            //FIXME - create random unit category for testing purpose.
            unit.setCategory( unit.getId().hashCode() %2 == 0 ? "video" : "htmlm");
            List<String> namedPath = m.getNamed_path();
            if (namedPath != null && !namedPath.isEmpty() && TextUtils.isEmpty(unit.getName())) {
                unit.setName(namedPath.get(namedPath.size() - 1));
            }

            vertical.getUnits().add(unit);
        }
        return course;
    }
}

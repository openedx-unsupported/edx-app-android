package org.humana.mobile.tta.ui.course.view_model;

import android.content.Context;

import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;

public class CourseAboutViewModel extends BaseViewModel {

    private Content content;
    public EnrolledCoursesResponse course;

    public CourseAboutViewModel(Context context, TaBaseFragment fragment, Content content, EnrolledCoursesResponse course) {
        super(context, fragment);
        this.content = content;
        this.course = course;
    }
}

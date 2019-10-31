package org.humana.mobile.tta.ui.course.view_model;

import android.content.Context;

import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;

public class CourseHandoutsViewModel extends BaseViewModel {

    private Content course;

    public CourseHandoutsViewModel(Context context, TaBaseFragment fragment, Content course, EnrolledCoursesResponse enrolledCoursesResponse) {
        super(context, fragment);
        this.course = course;
    }
}

package org.edx.mobile.tta.ui.course;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.exception.CourseContentNotValidException;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CourseOutlineAsyncLoader;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.services.LastAccessManager;
import org.edx.mobile.services.VideoDownloadHelper;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.course.view_model.CourseMaterialViewModel;
import org.edx.mobile.view.common.TaskProgressCallback;

import de.greenrobot.event.EventBus;

public class CourseMaterialTab extends TaBaseFragment {

    private CourseMaterialViewModel viewModel;

    private Content content;
    private EnrolledCoursesResponse courseData;
    private CourseComponent rootComponent;

    public static CourseMaterialTab newInstance(Content content, EnrolledCoursesResponse course, CourseComponent rootComponent){
        CourseMaterialTab courseMaterialTab = new CourseMaterialTab();
        courseMaterialTab.content = content;
        courseMaterialTab.courseData = course;
        courseMaterialTab.rootComponent = rootComponent;
        return courseMaterialTab;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new CourseMaterialViewModel(getActivity(), this, content, courseData, rootComponent);
        viewModel.registerEventBus();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_course_material, viewModel).getRoot();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.unregisterEvnetBus();
    }
}

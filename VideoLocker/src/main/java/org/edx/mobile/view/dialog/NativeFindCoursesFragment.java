package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.Space;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.course.CourseList;
import org.edx.mobile.course.GetCourseListTask;
import org.edx.mobile.view.adapters.FindCoursesListAdapter;
import org.edx.mobile.view.common.TaskProgressCallback;

import roboguice.fragment.RoboFragment;

public class NativeFindCoursesFragment extends RoboFragment {

    @Inject
    IEdxEnvironment environment;

    @Nullable
    private GetCourseListTask task;

    @Nullable
    private ViewHolder viewHolder;

    private FindCoursesListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FindCoursesListAdapter(getActivity(), environment) {
            @Override
            public void onItemClicked(CourseDetail model) {
                // TODO: Load course detail
                Toast.makeText(getActivity(), "Clicked " + model.name, Toast.LENGTH_SHORT).show();
            }
        };
        task = new GetCourseListTask(getActivity()) {
            @Override
            protected void onSuccess(CourseList courseList) throws Exception {
                super.onSuccess(courseList);
                adapter.setItems(courseList.results);
                if (null != viewHolder) viewHolder.loadingIndicator.setVisibility(View.GONE);
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                super.onException(e);
                showErrorMessage(e);
                if (null != viewHolder) viewHolder.loadingIndicator.setVisibility(View.GONE);
            }
        };
        task.setProgressCallback(null);
        task.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_courses,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.viewHolder = new ViewHolder(view);
        viewHolder.loadingIndicator.setVisibility(View.VISIBLE);
        viewHolder.listView.setAdapter(adapter);
        viewHolder.listView.setOnItemClickListener(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.viewHolder = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != task) {
            task.cancel(true);
        }
    }

    public static class ViewHolder {
        public final ListView listView;
        public final View loadingIndicator;

        public ViewHolder(View view) {
            this.listView = (ListView) view.findViewById(R.id.course_list);
            this.loadingIndicator = view.findViewById(R.id.api_spinner);
        }
    }
}

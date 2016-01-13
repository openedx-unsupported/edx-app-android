package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.course.CourseList;
import org.edx.mobile.course.GetCourseListTask;
import org.edx.mobile.view.adapters.FindCoursesListAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;

import roboguice.fragment.RoboFragment;

public class NativeFindCoursesFragment extends RoboFragment {

    @Inject
    IEdxEnvironment environment;

    @Nullable
    private GetCourseListTask task;

    @Nullable
    private ViewHolder viewHolder;

    private int nextPage = 1;

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
        viewHolder.listView.setVisibility(View.GONE);
        viewHolder.loadingIndicator.setVisibility(View.VISIBLE);
        final FindCoursesListAdapter adapter = new FindCoursesListAdapter(getActivity(), environment) {
            @Override
            public void onItemClicked(CourseDetail model) {
                environment.getRouter().showCourseDetail(getActivity(), model);
            }
        };
        InfiniteScrollUtils.configureListViewWithInfiniteList(viewHolder.listView, adapter, new InfiniteScrollUtils.PageLoader<CourseDetail>() {
            @Override
            public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<CourseDetail> callback) {
                if (null != task) {
                    task.cancel(true);
                }
                task = new GetCourseListTask(getActivity(), nextPage) {
                    @Override
                    protected void onSuccess(CourseList courseList) throws Exception {
                        super.onSuccess(courseList);
                        callback.onPageLoaded(courseList.results, courseList.pagination.next != null);
                        ++nextPage;
                        if (null != viewHolder) {
                            viewHolder.listView.setVisibility(View.VISIBLE);
                            viewHolder.loadingIndicator.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    protected void onException(Exception e) throws RuntimeException {
                        super.onException(e);
                        showErrorMessage(e);
                        if (null != viewHolder) {
                            viewHolder.loadingIndicator.setVisibility(View.GONE);
                        }
                    }
                };
                task.setProgressCallback(null);
                task.execute();
            }
        });
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
            this.loadingIndicator = view.findViewById(R.id.loading_indicator);
        }
    }
}

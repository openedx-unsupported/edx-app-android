package org.edx.mobile.view.dialog;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.model.Page;
import org.edx.mobile.view.adapters.FindCoursesListAdapter;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.TaskMessageCallback;

import retrofit2.Call;

public class NativeFindCoursesFragment extends BaseFragment {

    @Inject
    private CourseAPI courseAPI;

    @Inject
    IEdxEnvironment environment;

    @Nullable
    private Call<Page<CourseDetail>> call;

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
        final Activity activity = getActivity();
        this.viewHolder = new ViewHolder(view);
        viewHolder.listView.setVisibility(View.GONE);
        viewHolder.loadingIndicator.setVisibility(View.VISIBLE);
        final FindCoursesListAdapter adapter = new FindCoursesListAdapter(activity, environment) {
            @Override
            public void onItemClicked(CourseDetail model) {
                environment.getRouter().showCourseDetail(activity, model);
            }
        };
        // Add empty views to cause a dividers to render at the top and bottom of the list
        viewHolder.listView.addHeaderView(new View(getContext()), null, false);
        viewHolder.listView.addFooterView(new View(getContext()), null, false);
        InfiniteScrollUtils.configureListViewWithInfiniteList(viewHolder.listView, adapter, new InfiniteScrollUtils.PageLoader<CourseDetail>() {
            @Override
            public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<CourseDetail> callback) {
                if (null != call) {
                    call.cancel();
                }
                call = courseAPI.getCourseList(nextPage);

                final TaskMessageCallback mCallback = activity instanceof TaskMessageCallback ? (TaskMessageCallback) activity : null;
                call.enqueue(new ErrorHandlingCallback<Page<CourseDetail>>(activity, null,
                        mCallback, CallTrigger.LOADING_UNCACHED) {
                    @Override
                    protected void onResponse(@NonNull final Page<CourseDetail> coursesPage) {
                        callback.onPageLoaded(coursesPage);
                        ++nextPage;
                        if (null != viewHolder) {
                            viewHolder.listView.setVisibility(View.VISIBLE);
                            viewHolder.loadingIndicator.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    protected void onFailure(@NonNull final Throwable error) {
                        callback.onError();
                        nextPage = 1;
                        if (null != viewHolder) {
                            viewHolder.loadingIndicator.setVisibility(View.GONE);
                        }
                    }
                });
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
        if (null != call) {
            call.cancel();
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

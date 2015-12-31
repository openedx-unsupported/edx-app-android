package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CoursesAsyncLoader;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.facebook.FacebookSessionUtil;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.FetchCourseFriendsService;
import org.edx.mobile.services.ServiceManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class MyCourseListTabFragment extends CourseListTabFragment {

    private final int MY_COURSE_LOADER_ID = 0x905000;

    protected TextView noCourseText;
    private boolean refreshOnResume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        environment.getSegment().trackScreenView(ISegment.Screens.MY_COURSES);
        EventBus.getDefault().register(this);
    }

    @Override
    public void handleCourseClick(EnrolledCoursesResponse model) {
        environment.getRouter().showCourseDashboardTabs(getActivity(), environment.getConfig(), model, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        noCourseText = (TextView) view.findViewById(R.id.no_course_tv);
        return view;
    }

    protected void loadData(boolean forceRefresh, boolean showProgress) {
        if (forceRefresh) {
            Intent clearFriends = new Intent(getActivity(), FetchCourseFriendsService.class);

            clearFriends.putExtra(FetchCourseFriendsService.TAG_FORCE_REFRESH, true);

            getActivity().startService(clearFriends);
        }

        //This Show progress is used to display the progress when a user enrolls in a Course
        if (showProgress && progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        Bundle args = new Bundle();
        args.putString(CoursesAsyncLoader.TAG_COURSE_OAUTH, FacebookSessionUtil.getAccessToken());
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, args, this);
    }

    @Override
    protected int getViewResourceID() {
        return R.layout.fragment_my_course_list_tab;
    }

    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {
        return new CoursesAsyncLoader(getActivity(), bundle, environment, environment.getServiceManager()) {
            @Override
            protected List<EnrolledCoursesResponse> getCourses(ServiceManager api) throws Exception {
                List<EnrolledCoursesResponse> response = api.getEnrolledCourses();
                environment.getNotificationDelegate().syncWithServerForFailure();
                environment.getNotificationDelegate().checkCourseEnrollment(response);
                return response;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader, AsyncTaskResult<List<EnrolledCoursesResponse>> result) {

        progressBar.setVisibility(View.GONE);

        if (result == null) {
            logger.warn("result is found null, was expecting non-null");
            return;
        }

        myCourseList.setVisibility(View.VISIBLE);

        if (result.getEx() != null) {
            if (result.getEx() instanceof AuthException) {
                PrefManager prefs = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                prefs.clearAuth();

                logger.error(result.getEx());
                getActivity().finish();
            }
        } else if (result.getResult() != null) {
            invalidateSwipeFunctionality();

            ArrayList<EnrolledCoursesResponse> newItems = new ArrayList<EnrolledCoursesResponse>(result.getResult());

            ((MyCoursesListActivity) getActivity()).updateDatabaseAfterDownload(newItems);

            if (result.getResult().size() == 0) {
                adapter.clear();
            } else {
                adapter.setItems(newItems);
                adapter.notifyDataSetChanged();
            }

        } else {
            adapter.clear();
        }
    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader) {
        adapter.clear();
        adapter.notifyDataSetChanged();
        myCourseList.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(EnrolledInCourseEvent event) {
        refreshOnResume = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshOnResume) {
            loadData(false, true);
            refreshOnResume = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

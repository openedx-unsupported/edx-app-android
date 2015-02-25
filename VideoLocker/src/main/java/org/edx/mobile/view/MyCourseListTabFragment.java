package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.Api;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CoursesAsyncLoader;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.FetchCourseFriendsService;
import org.edx.mobile.module.facebook.FacebookSessionUtil;

import java.util.ArrayList;
import java.util.List;

public class MyCourseListTabFragment extends CourseListTabFragment {

    private static final String TAG = MyCourseListTabFragment.class.getSimpleName();

    private final int MY_COURSE_LOADER_ID = 0x905000;
    protected TextView noCourseText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            segIO.screenViewsTracking(getString(R.string.label_my_courses));
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void handleCourseClick(EnrolledCoursesResponse model) {
        Router.getInstance().showCourseDetailTabs(getActivity(), model, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        noCourseText = (TextView) view.findViewById(R.id.no_course_tv);
        return view;
    }

    protected void loadData(boolean forceRefresh) {
        if(forceRefresh){
            Intent clearFriends = new Intent(getActivity(), FetchCourseFriendsService.class);

            clearFriends.putExtra(FetchCourseFriendsService.TAG_FORCE_REFRESH, true);

            getActivity().startService(clearFriends);
        }

        Bundle args = new Bundle();
        args.putString(CoursesAsyncLoader.TAG_COURSE_OAUTH, FacebookSessionUtil.getAccessToken());
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, args, this);
    }

    @Override
    protected int getViewResourceID() {
        return R.layout.fragment_my_course_list_tab;
    }

    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {

        return new CoursesAsyncLoader(getActivity(), bundle){
            @Override
            protected List<EnrolledCoursesResponse> getCourses(Api api) throws Exception {
                return api.getEnrolledCourses();
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader, AsyncTaskResult<List<EnrolledCoursesResponse>> result) {

        if (progressBar != null) progressBar.setVisibility(View.GONE);

        if (result == null) {
            logger.warn("result is found null, was expecting non-null");
            return;
        }

        if(result.getEx() != null)
        {
            if(result.getEx() instanceof AuthException){
                PrefManager prefs = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
                prefs.clearAuth();

                logger.error(result.getEx());
                getActivity().finish();
            }
        } else if (result.getResult() != null) {
            invalidateSwipeFunctionality();

            ArrayList<EnrolledCoursesResponse> newItems = new ArrayList<EnrolledCoursesResponse>(result.getResult());

            ((MyCoursesListActivity)getActivity()).updateDatabaseAfterDownload(newItems);

            if(result.getResult().size() == 0){
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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    }
}

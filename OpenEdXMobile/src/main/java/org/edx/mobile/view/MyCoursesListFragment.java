package org.edx.mobile.view;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentMyCoursesListBinding;
import org.edx.mobile.databinding.PanelFindCourseBinding;
import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.notifications.SnackbarErrorNotification;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CoursesAsyncLoader;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.task.RestoreVideosCacheDataTask;
import org.edx.mobile.util.KonnekteerUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.MyCoursesAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class MyCoursesListFragment extends BaseFragment
        implements NetworkObserver, RefreshListener,
        LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    private static final int MY_COURSE_LOADER_ID = 0x905000;

    private MyCoursesAdapter adapter;
    private FragmentMyCoursesListBinding binding;
    private final Logger logger = new Logger(getClass().getSimpleName());
    private boolean refreshOnResume = false;

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private LoginPrefs loginPrefs;

    private FullScreenErrorNotification errorNotification;

    private SnackbarErrorNotification snackbarErrorNotification;

    // Reason of usage: Helps in deciding if we want to show a full screen error or a SnackBar.
    private boolean isInitialServerCallDone = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new MyCoursesAdapter(getActivity(), environment) {
            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                environment.getRouter().showCourseDashboardTabs(getActivity(), environment.getConfig(), model, false);
            }

            @Override
            public void onAnnouncementClicked(EnrolledCoursesResponse model) {
                environment.getRouter().showCourseDashboardTabs(getActivity(), environment.getConfig(), model, true);
            }
        };
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.MY_COURSES);
        EventBus.getDefault().register(this);

        // Restore cache of the courses for which the user has downloaded any videos
        RestoreVideosCacheDataTask.executeInstanceIfNeeded(MainApplication.application);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_courses_list, container, false);
        errorNotification = new FullScreenErrorNotification(binding.myCourseList);
        snackbarErrorNotification = new SnackbarErrorNotification(binding.getRoot());
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide the progress bar as swipe layout has its own progress indicator
                binding.loadingIndicator.getRoot().setVisibility(View.GONE);
                errorNotification.hideError();
                loadData(false);
            }
        });
        binding.swipeContainer.setColorSchemeResources(R.color.edx_brand_primary_accent,
                R.color.edx_brand_gray_x_back, R.color.edx_brand_gray_x_back,
                R.color.edx_brand_gray_x_back);
        if (environment.getConfig().getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
            // As per docs, the footer needs to be added before adapter is set to the ListView
            addFindCoursesFooter();
        }
        // Add empty views to cause dividers to render at the top and bottom of the list
        binding.myCourseList.addHeaderView(new View(getContext()), null, false);
        binding.myCourseList.addFooterView(new View(getContext()), null, false);
        binding.myCourseList.setAdapter(adapter);
        binding.myCourseList.setOnItemClickListener(adapter);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData(true);
    }

    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {
        return new CoursesAsyncLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader, AsyncTaskResult<List<EnrolledCoursesResponse>> result) {
        adapter.clear();
        final Exception exception = result.getEx();
        if (exception != null) {
            if (exception instanceof AuthException) {
                loginPrefs.clear();
                getActivity().finish();
            } else if (exception instanceof HttpStatusException) {
                final HttpStatusException httpStatusException = (HttpStatusException) exception;
                switch (httpStatusException.getStatusCode()) {
                    case HttpStatus.UNAUTHORIZED:{
                        environment.getRouter().forceLogout(getContext(),
                                environment.getAnalyticsRegistry(),
                                environment.getNotificationDelegate());
                        break;
                    }
                }
            } else {
                logger.error(exception);
            }

            errorNotification.showError(getActivity(), exception, R.string.lbl_reload,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (NetworkUtil.isConnected(getContext())) {
                                onRefresh();
                            }
                        }
                    });
        } else if (result.getResult() != null) {
            ArrayList<EnrolledCoursesResponse> newItems = new ArrayList<EnrolledCoursesResponse>(result.getResult());
            ((MyCoursesListActivity) getActivity()).updateDatabaseAfterDownload(newItems);

            // Subscribe to organization (app) level push notifications
            String orgCode = environment.getConfig().getPushNotificationsConfig().getmKonnekteerOrgCode();
            KonnekteerUtil.subscribe(orgCode, null, this.getContext());

            // Subscribe to course level push notifications
            for (EnrolledCoursesResponse enrolledCoursesResponse : newItems) {
                CourseEntry course = enrolledCoursesResponse.getCourse();
                KonnekteerUtil.subscribe(null, course.getId(), this.getContext());
            }

            if (result.getResult().size() > 0) {
                adapter.setItems(newItems);
                adapter.notifyDataSetChanged();
            }else if(environment.getConfig().isJumpToFindCoursesEnabled()){
                environment.getRouter().showFindCourses(getActivity());
            }

            if (adapter.isEmpty() && !environment.getConfig().getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
                errorNotification.showError(R.string.no_courses_to_display,
                        FontAwesomeIcons.fa_exclamation_circle, 0, null);
                binding.myCourseList.setVisibility(View.GONE);
            } else {
                binding.myCourseList.setVisibility(View.VISIBLE);
                errorNotification.hideError();
            }
        }
        binding.swipeContainer.setRefreshing(false);
        binding.loadingIndicator.getRoot().setVisibility(View.GONE);

        isInitialServerCallDone = true;
        if (!(NetworkUtil.isConnected(getActivity()))) {
            onOffline();
        } else {
            onOnline();
        }
    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader) {
        adapter.clear();
        adapter.notifyDataSetChanged();
        binding.myCourseList.setVisibility(View.GONE);
        binding.loadingIndicator.getRoot().setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (refreshOnResume) {
            loadData(true);
            refreshOnResume = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof NetworkSubject) {
            ((NetworkSubject) activity).registerNetworkObserver(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() instanceof NetworkSubject) {
            ((NetworkSubject) getActivity()).unregisterNetworkObserver(this);
        }
    }

    @Override
    public void onOnline() {
        if (binding.swipeContainer != null) {
            binding.swipeContainer.setEnabled(true);
        }
    }

    @Override
    public void onOffline() {
        //Disable swipe functionality and hide the loading view
        binding.swipeContainer.setEnabled(false);
        binding.swipeContainer.setRefreshing(false);
        if (isInitialServerCallDone && !errorNotification.isShowing()) {
            snackbarErrorNotification.showOfflineError(this);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(EnrolledInCourseEvent event) {
        refreshOnResume = true;
    }

    protected void loadData(boolean showProgress) {
        if (showProgress) {
            binding.loadingIndicator.getRoot().setVisibility(View.VISIBLE);
            errorNotification.hideError();
        }
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, null, this);
    }

    private void addFindCoursesFooter() {
        final PanelFindCourseBinding footer = DataBindingUtil.inflate(LayoutInflater.from(getActivity()),
                R.layout.panel_find_course, binding.myCourseList, false);
        binding.myCourseList.addFooterView(footer.getRoot(), null, false);
        footer.courseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getAnalyticsRegistry().trackUserFindsCourses();
                environment.getRouter().showFindCourses(getActivity());
            }
        });
    }

    @Override
    public void onRefresh() {
        loadData(true);
    }

    @Override
    protected void onRevisit() {
        if (NetworkUtil.isConnected(getActivity())) {
            onOnline();
            snackbarErrorNotification.hideError();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.my_courses, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search: {
                environment.getAnalyticsRegistry().trackUserFindsCourses();
                environment.getRouter().showFindCourses(getContext());
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}

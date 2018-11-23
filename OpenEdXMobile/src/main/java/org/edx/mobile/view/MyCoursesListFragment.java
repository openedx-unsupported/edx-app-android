package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentMyCoursesListBinding;
import org.edx.mobile.databinding.PanelFindCourseBinding;
import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.event.MainDashboardRefreshEvent;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CoursesAsyncLoader;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.MyCoursesAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class MyCoursesListFragment extends OfflineSupportBaseFragment
        implements RefreshListener,
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

    //TODO: All these callbacks aren't essentially part of MyCoursesListFragment and should move in
    // the Tabs container fragment that's going to be implemented in LEARNER-3251

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new MyCoursesAdapter(getActivity(), environment) {
            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                environment.getRouter().showCourseDashboardTabs(getActivity(), model, false);
            }

            @Override
            public void onAnnouncementClicked(EnrolledCoursesResponse model) {
                environment.getRouter().showCourseDashboardTabs(getActivity(), model, true);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_courses_list, container, false);
        errorNotification = new FullScreenErrorNotification(binding.myCourseList);
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide the progress bar as swipe layout has its own progress indicator
                binding.loadingIndicator.getRoot().setVisibility(View.GONE);
                errorNotification.hideError();
                loadData(false);
            }
        });
        UiUtil.setSwipeRefreshLayoutColors(binding.swipeContainer);
        // Add empty view to cause divider to render at the top of the list.
        binding.myCourseList.addHeaderView(new View(getContext()), null, false);
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
                    case HttpStatus.UNAUTHORIZED: {
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

            updateDatabaseAfterDownload(newItems);

            if (result.getResult().size() > 0) {
                adapter.setItems(newItems);
            }
            addFindCoursesFooter();
            adapter.notifyDataSetChanged();

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

        if (!EventBus.getDefault().isRegistered(MyCoursesListFragment.this)) {
            EventBus.getDefault().registerSticky(MyCoursesListFragment.this);
        }
    }

    public void updateDatabaseAfterDownload(ArrayList<EnrolledCoursesResponse> list) {
        if (list != null && list.size() > 0) {
            //update all videos in the DB as Deactivated
            environment.getDatabase().updateAllVideosAsDeactivated(dataCallback);

            for (int i = 0; i < list.size(); i++) {
                //Check if the flag of isIs_active is marked to true,
                //then activate all videos
                if (list.get(i).isIs_active()) {
                    //update all videos for a course fetched in the API as Activated
                    environment.getDatabase().updateVideosActivatedForCourse(list.get(i).getCourse().getId(),
                            dataCallback);
                } else {
                    list.remove(i);
                }
            }

            //Delete all videos which are marked as Deactivated in the database
            environment.getStorage().deleteAllUnenrolledVideos();
        }
    }

    private DataCallback<Integer> dataCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
        }

        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

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
        // Validate footer is not already added.
        if (binding.myCourseList.getFooterViewsCount() > 0) {
            return;
        }
        if (environment.getConfig().getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
            // Add 'Find a Course' list item as a footer.
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
        // Add empty view to cause divider to render at the bottom of the list.
        binding.myCourseList.addFooterView(new View(getContext()), null, false);
    }

    @Override
    public void onRefresh() {
        EventBus.getDefault().post(new MainDashboardRefreshEvent());
    }

    @SuppressWarnings("unused")
    public void onEvent(MainDashboardRefreshEvent event) {
        loadData(true);
    }

    @Override
    protected void onRevisit() {
        super.onRevisit();
        if (NetworkUtil.isConnected(getActivity())) {
            binding.swipeContainer.setEnabled(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (getActivity() != null) {
            if (NetworkUtil.isConnected(getContext())) {
                binding.swipeContainer.setEnabled(true);
            } else {
                //Disable swipe functionality and hide the loading view
                binding.swipeContainer.setEnabled(false);
                binding.swipeContainer.setRefreshing(false);
            }
            onNetworkConnectivityChangeEvent(event);
        }
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return errorNotification != null && errorNotification.isShowing();
    }
}

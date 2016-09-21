package org.edx.mobile.view;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentMyCoursesListBinding;
import org.edx.mobile.databinding.PanelFindCourseBinding;
import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.HttpResponseStatusException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CoursesAsyncLoader;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.adapters.MyCoursesAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class MyCoursesListFragment extends BaseFragment implements NetworkObserver, LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    private static final int MY_COURSE_LOADER_ID = 0x905000;

    private MyCoursesAdapter adapter;
    private FragmentMyCoursesListBinding binding;
    private final Logger logger = new Logger(getClass().getSimpleName());
    private boolean refreshOnResume = false;

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private LoginPrefs loginPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        environment.getSegment().trackScreenView(ISegment.Screens.MY_COURSES);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_courses_list, container, false);
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Hide the progress bar as swipe layout has its own progress indicator
                binding.loadingIndicator.getRoot().setVisibility(View.GONE);
                binding.noCourseTv.setVisibility(View.GONE);
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
        if (!(NetworkUtil.isConnected(getActivity()))) {
            onOffline();
        } else {
            onOnline();
        }
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
        if (result.getEx() != null) {
            if (result.getEx() instanceof AuthException) {
                loginPrefs.clear();
                getActivity().finish();
            } else if (result.getEx() instanceof HttpResponseStatusException &&
                    ((HttpResponseStatusException) result.getEx()).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                environment.getRouter().forceLogout(
                        getContext(),
                        environment.getSegment(),
                        environment.getNotificationDelegate());
            } else {
                logger.error(result.getEx());
            }
        } else if (result.getResult() != null) {
            ArrayList<EnrolledCoursesResponse> newItems = new ArrayList<EnrolledCoursesResponse>(result.getResult());

            ((MyCoursesListActivity) getActivity()).updateDatabaseAfterDownload(newItems);

            if (result.getResult().size() > 0) {
                adapter.setItems(newItems);
                adapter.notifyDataSetChanged();
            }
        }
        binding.swipeContainer.setRefreshing(false);
        binding.loadingIndicator.getRoot().setVisibility(View.GONE);
        if (adapter.isEmpty() && !environment.getConfig().getCourseDiscoveryConfig().isCourseDiscoveryEnabled()) {
            binding.myCourseList.setVisibility(View.GONE);
            binding.noCourseTv.setVisibility(View.VISIBLE);
        } else {
            binding.myCourseList.setVisibility(View.VISIBLE);
            binding.noCourseTv.setVisibility(View.GONE);
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
    public void onStop() {
        super.onStop();
        hideOfflinePanel();
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
        if (binding.offlineBar != null && binding.swipeContainer != null) {
            binding.offlineBar.setVisibility(View.GONE);
            hideOfflinePanel();
            binding.swipeContainer.setEnabled(true);
        }
    }

    @Override
    public void onOffline() {
        binding.offlineBar.setVisibility(View.VISIBLE);
        showOfflinePanel();
        //Disable swipe functionality and hide the loading view
        binding.swipeContainer.setEnabled(false);
        binding.swipeContainer.setRefreshing(false);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(EnrolledInCourseEvent event) {
        refreshOnResume = true;
    }

    protected void loadData(boolean showProgress) {
        if (showProgress) {
            binding.loadingIndicator.getRoot().setVisibility(View.VISIBLE);
            binding.noCourseTv.setVisibility(View.GONE);
        }
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, null, this);
    }

    private void showOfflinePanel() {
        ViewAnimationUtil.showMessageBar(binding.offlinePanel);
    }

    private void hideOfflinePanel() {
        ViewAnimationUtil.stopAnimation(binding.offlinePanel);
        if (binding.offlinePanel.getVisibility() == View.VISIBLE) {
            binding.offlinePanel.setVisibility(View.GONE);
        }
    }

    private void addFindCoursesFooter() {
        final PanelFindCourseBinding footer = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.panel_find_course, binding.myCourseList, false);
        binding.myCourseList.addFooterView(footer.getRoot(), null, false);
        footer.courseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getSegment().trackUserFindsCourses();
                environment.getRouter().showFindCourses(getActivity());
            }
        });
        footer.courseNotListedTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showWebViewDialog((getActivity()), getString(R.string.course_not_listed_file_name), null);
            }
        });
    }

}

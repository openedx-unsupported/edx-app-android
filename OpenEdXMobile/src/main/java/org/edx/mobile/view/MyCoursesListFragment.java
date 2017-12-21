package org.edx.mobile.view;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentMyCoursesListBinding;
import org.edx.mobile.databinding.PanelFindCourseBinding;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.EnrolledInCourseEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
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
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.user.UserAPI;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UserProfileUtils;
import org.edx.mobile.view.adapters.MyCoursesAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import retrofit2.Call;

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

    @Inject
    private UserService userService;

    private FullScreenErrorNotification errorNotification;

    private SnackbarErrorNotification snackbarErrorNotification;

    // Reason of usage: Helps in deciding if we want to show a full screen error or a SnackBar.
    private boolean isInitialServerCallDone = false;

    private ProfileModel profile;

    private ToolbarCallbacks toolbarCallbacks;

    @Nullable
    private Call<Account> getAccountCall;

    //TODO: All these callbacks aren't essentially part of MyCoursesListFragment and should move in
    // the Tabs container fragment that's going to be implemented in LEARNER-3251
    /**
     * The container Activity must implement this interface so the frag can communicate
     */
    public interface ToolbarCallbacks {
        @Nullable
        SearchView getSearchView();
        @Nullable
        TextView getTitleView();
        @Nullable
        ImageView getProfileView();
    }

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
        final boolean isUserProfileEnabled = environment.getConfig().isUserProfilesEnabled();
        if (environment.getConfig().isTabsLayoutEnabled() && isUserProfileEnabled) {
            profile = loginPrefs.getCurrentUserProfile();
            sendGetUpdatedAccountCall();
        }
        if (!isUserProfileEnabled) {
            toolbarCallbacks.getProfileView().setVisibility(View.GONE);
        }
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.MY_COURSES);
        EventBus.getDefault().register(this);
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

    public void sendGetUpdatedAccountCall() {
        getAccountCall = userService.getAccount(profile.username);
        getAccountCall.enqueue(new UserAPI.AccountDataUpdatedCallback(
                getActivity(),
                profile.username,
                null, // Disable global loading indicator
                null)); // No place to show an error notification
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

            if (result.getResult().size() > 0) {
                adapter.setItems(newItems);
                adapter.notifyDataSetChanged();
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
        if (null != getAccountCall) {
            getAccountCall.cancel();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        toolbarCallbacks = (ToolbarCallbacks) activity;
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
        final Config config = environment.getConfig();
        if (config.isTabsLayoutEnabled()) {
            menu.findItem(R.id.menu_item_search).setVisible(false);
            menu.findItem(R.id.menu_item_account).setVisible(true);
            menu.findItem(R.id.menu_item_account).setIcon(
                    new IconDrawable(getContext(), FontAwesomeIcons.fa_gear)
                            .colorRes(getContext(), R.color.white)
                            .actionBarSize(getContext()));
        } else {
            menu.findItem(R.id.menu_item_account).setVisible(false);
            menu.findItem(R.id.menu_item_search).setVisible(config.getCourseDiscoveryConfig().isCourseDiscoveryEnabled());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_search: {
                environment.getAnalyticsRegistry().trackUserFindsCourses();
                environment.getRouter().showFindCourses(getContext());
                return true;
            }
            case R.id.menu_item_account: {
                environment.getRouter().showAccountActivity(getActivity());
                //TODO: remove following code block after testing, (once LEARNER-3251 is done)
//                if (toolbarCallbacks.getSearchView().getVisibility() == View.VISIBLE) {
//                    hideSearchBar();
//                } else {
//                    showSearchBar();
//                }
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void hideSearchBar() {
        toolbarCallbacks.getSearchView().setVisibility(View.GONE);
        toolbarCallbacks.getTitleView().setVisibility(View.VISIBLE);
    }

    //TODO: This function has to update once LEARNER-3251 is done
    private void showSearchBar() {
        final SearchView searchView = toolbarCallbacks.getSearchView();
        searchView.setVisibility(View.VISIBLE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.onActionViewCollapsed();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean queryTextFocused) {
                final TextView titleView = toolbarCallbacks.getTitleView();
                if (queryTextFocused) {
                    titleView.setVisibility(View.GONE);
                } else {
                    titleView.setVisibility(View.VISIBLE);
                    searchView.onActionViewCollapsed();
                }
            }
        });
    }

    private void loadProfileImage(@NonNull ProfileImage profileImage, @NonNull ImageView imageView) {
        if (profileImage.hasImage()) {
            Glide.with(this)
                    .load(profileImage.getImageUrlMedium())
                    .into(imageView);
        } else {
            Glide.with(this)
                    .load(R.drawable.profile_photo_placeholder)
                    .into(imageView);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        if (!environment.getConfig().isTabsLayoutEnabled() || !environment.getConfig().isUserProfilesEnabled()) {
            return;
        }
        final ImageView profileImage = toolbarCallbacks.getProfileView();
        if (event.getUsername().equalsIgnoreCase(profile.username)) {
            UserProfileUtils.loadProfileImage(getContext(), event, profileImage);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull AccountDataLoadedEvent event) {
        if (!environment.getConfig().isTabsLayoutEnabled() || !environment.getConfig().isUserProfilesEnabled()) {
            return;
        }
        final Account account = event.getAccount();
        if (account.getUsername().equalsIgnoreCase(profile.username)) {
            final ImageView profileImage = toolbarCallbacks.getProfileView();
            if (profileImage != null) {
                loadProfileImage(account.getProfileImage(), profileImage);
            }
        }
    }
}

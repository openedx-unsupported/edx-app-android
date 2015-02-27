package org.edx.mobile.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.facebook.Session;
import com.facebook.SessionState;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.services.FetchCourseFriendsService;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.SocialProvider;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;
import org.edx.mobile.module.facebook.FacebookSessionUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.MyCourseAdapter;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.dialog.FindCoursesDialogFragment;

import java.util.List;

public abstract class CourseListTabFragment extends Fragment implements NetworkObserver, MyCourseAdapter.CourseFriendsListener, LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    protected MyCourseAdapter adapter;

    protected SwipeRefreshLayout swipeLayout;
    protected LinearLayout offlinePanel;
    protected View offlineBar;
    protected ProgressBar progressBar;

    protected PrefManager pmFeatures;

    protected ISegment segIO;

    protected IUiLifecycleHelper uiHelper;
    protected ListView myCourseList;

    FetchFriendsReceiver fetchFriendsObserver;

    protected Logger logger = new Logger(getClass().getSimpleName());

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof NetworkSubject) {
            ((NetworkSubject)activity).registerNetworkObserver(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getActivity() instanceof NetworkSubject) {
            ((NetworkSubject)getActivity()).unregisterNetworkObserver(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
            segIO = SegmentFactory.getInstance();
            segIO.screenViewsTracking(getString(R.string.label_my_courses));
        }catch(Exception e){
            logger.error(e);
        }

        fetchFriendsObserver = new FetchFriendsReceiver();

        SocialProvider fbProvider = new FacebookProvider();
        pmFeatures = new PrefManager(getActivity(), PrefManager.Pref.FEATURES);
        boolean showSocialFeatures = fbProvider.isLoggedIn() && pmFeatures.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
        adapter = new MyCourseAdapter(getActivity(), showSocialFeatures, this) {

            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                handleCourseClick(model);
            }

            @Override
            public void onAnnouncementClicked(EnrolledCoursesResponse model) {
                Router.getInstance().showCourseDetailTabs(getActivity(), model, true);
            }
        };

        uiHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {

                if (state.isOpened()) {
                    adapter.notifyDataSetChanged();
                }


            }
        });
        uiHelper.onCreate(savedInstanceState);
        loadData(false,false);

    }

    public abstract void handleCourseClick( EnrolledCoursesResponse model);

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uiHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        uiHelper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onPause() {
        uiHelper.onPause();
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getViewResourceID(), container, false);

        offlineBar = view.findViewById(R.id.offline_bar);
        offlinePanel = (LinearLayout) view.findViewById(R.id.offline_panel);
        progressBar = (ProgressBar) view.findViewById(R.id.api_spinner);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Hide the progress bar as swipe functionality has its own Progress indicator
                if(progressBar!=null){
                    progressBar.setVisibility(View.GONE);
                }
                loadData(true,false);
            }
        });

        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                R.color.grey_act_background , R.color.grey_act_background ,
                R.color.grey_act_background);

        myCourseList = (ListView) view.findViewById(R.id.my_course_list);
        myCourseList.setAdapter(adapter);
        myCourseList.setOnItemClickListener(adapter);

        setupFooter(myCourseList);

        return view;
    }

    protected abstract int getViewResourceID();

    protected abstract void loadData(boolean forceRefresh, boolean showProgress);


    protected void invalidateSwipeFunctionality(){
        swipeLayout.setRefreshing(false);
    }

    protected void validateSwipeFunctionality(){
        swipeLayout.setRefreshing(true);
    }

    @Override
    public void onOnline() {
        if (offlineBar != null && swipeLayout != null) {
            offlineBar.setVisibility(View.GONE);
            hideOfflinePanel();
            swipeLayout.setEnabled(true);
        }
    }

    public void hideOfflinePanel() {
        UiUtil.stopAnimation(offlinePanel);
        if(offlinePanel.getVisibility()==View.VISIBLE){
            offlinePanel.setVisibility(View.GONE);
        }
    }

    public void showOfflinePanel() {
        UiUtil.animateLayouts(offlinePanel);
    }

    @Override
    public void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);
        showOfflinePanel();
        swipeLayout.setEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideOfflinePanel();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(fetchFriendsObserver);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter fetchFriendsFilter = new IntentFilter(FetchCourseFriendsService.NOTIFY_FILTER);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(fetchFriendsObserver, fetchFriendsFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AppConstants.offline_flag) {
            onOffline();
        } else {
            onOnline();
        }

        uiHelper.onResume();

        //Let the adapter know if it's connection status to facebook has changed.
        boolean socialConnected = new FacebookProvider().isLoggedIn() && pmFeatures.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
        adapter.setShowSocial(socialConnected);

    }

    /**
     * Adds a footer view to the list, which has "FIND A COURSE" button.
     * @param myCourseList - ListView
     */
    private void setupFooter(ListView myCourseList) {
        try {
            View footer = LayoutInflater.from(getActivity()).inflate(R.layout.panel_find_course, null);
            myCourseList.addFooterView(footer, null, false);
            Button course_btn = (Button) footer.findViewById(R.id.course_btn);
            course_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        segIO.trackUserFindsCourses();
                    } catch (Exception e) {
                        logger.error(e);
                    }

                    try {
                        if (Config.getInstance().getEnrollmentConfig().isEnabled()) {
                            //Call the Find courses activity
                            Router.getInstance().showFindCourses(getActivity());
                        } else {
                            //Show the dialog only if the activity is started. This is to avoid Illegal state
                            //exceptions if the dialog fragment tries to show even if the application is not in foreground
                            if (isAdded() && isVisible()) {
                                FindCoursesDialogFragment findCoursesFragment = new FindCoursesDialogFragment();
                                findCoursesFragment.setStyle(DialogFragment.STYLE_NORMAL,
                                        android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                                findCoursesFragment.setCancelable(false);
                                findCoursesFragment.show(getFragmentManager(), "dialog-find-courses");
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });

            ETextView courseNotListedTv = (ETextView) footer.findViewById(R.id.course_not_listed_tv);
            courseNotListedTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCourseNotListedDialog();
                }
            });
        }catch(Exception e){
            logger.error(e);
        }
    }

    public void showCourseNotListedDialog() {
        ((BaseFragmentActivity)getActivity()).showWebDialog(getString(R.string.course_not_listed_file_name), false,
                null);
    }

    private class FetchFriendsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String courseId = intent.getStringExtra(FetchCourseFriendsService.EXTRA_BROADCAST_COURSE_ID);

            AsyncTaskResult<List<SocialMember>> result = FetchCourseFriendsService.fetchResult(courseId);

            if (result !=null && result.getResult() != null) {
                int listPos = adapter.getPositionForCourseId(courseId);
                if(listPos < 0)
                    return;
                adapter.getItem(listPos).getCourse().setMembers_list(result.getResult());
                adapter.notifyDataSetChanged();

            }

        }
    }

    @Override
    public void fetchCourseFriends(EnrolledCoursesResponse course) {

        boolean loggedInSocial = new FacebookProvider().isLoggedIn();

        if (!loggedInSocial){
            return;
        }

        Intent fetchFriends = new Intent(getActivity(), FetchCourseFriendsService.class);

        fetchFriends.putExtra(FetchCourseFriendsService.TAG_COURSE_ID, course.getCourse().getId());
        fetchFriends.putExtra(FetchCourseFriendsService.TAG_COURSE_OAUTH, FacebookSessionUtil.getAccessToken());

        getActivity().startService(fetchFriends);
    }

}

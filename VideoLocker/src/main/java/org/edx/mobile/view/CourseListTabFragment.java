package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.interfaces.NetworkSubject;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.adapters.MyCourseAdapter;

import java.util.List;

import roboguice.fragment.RoboFragment;

public abstract class CourseListTabFragment extends RoboFragment implements NetworkObserver, LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>> {

    protected MyCourseAdapter adapter;

    protected SwipeRefreshLayout swipeLayout;
    protected LinearLayout offlinePanel;
    protected View offlineBar;
    protected ProgressBar progressBar;

    protected PrefManager pmFeatures;

    @Inject
    protected IEdxEnvironment environment;

    protected ListView myCourseList;

    protected Logger logger = new Logger(getClass().getSimpleName());

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pmFeatures = new PrefManager(getActivity(), PrefManager.Pref.FEATURES);
        adapter = new MyCourseAdapter(getActivity(), environment) {

            @Override
            public void onItemClicked(EnrolledCoursesResponse model) {
                handleCourseClick(model);
            }

            @Override
            public void onAnnouncementClicked(EnrolledCoursesResponse model) {
                environment.getRouter().showCourseDashboardTabs(getActivity(), environment.getConfig(), model, true);
            }
        };

        loadData(false, false);
    }

    public abstract void handleCourseClick(EnrolledCoursesResponse model);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getViewResourceID(), container, false);

        offlineBar = view.findViewById(R.id.offline_bar);
        offlinePanel = (LinearLayout) view.findViewById(R.id.offline_panel);
        progressBar = (ProgressBar) view.findViewById(R.id.loading_indicator);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Hide the progress bar as swipe functionality has its own Progress indicator
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                loadData(true, false);
            }
        });

        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                R.color.grey_act_background, R.color.grey_act_background,
                R.color.grey_act_background);

        myCourseList = (ListView) view.findViewById(R.id.my_course_list);
        //As per docs, the footer needs to be added before adapter is set to the ListView
        setupFooter(myCourseList);

        myCourseList.setAdapter(adapter);
        myCourseList.setOnItemClickListener(adapter);

        if (!(NetworkUtil.isConnected(getActivity()))) {
            onOffline();
        } else {
            onOnline();
        }

        return view;
    }

    protected abstract int getViewResourceID();

    protected abstract void loadData(boolean forceRefresh, boolean showProgress);

    protected void invalidateSwipeFunctionality() {
        swipeLayout.setRefreshing(false);
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
        ViewAnimationUtil.stopAnimation(offlinePanel);
        if (offlinePanel.getVisibility() == View.VISIBLE) {
            offlinePanel.setVisibility(View.GONE);
        }
    }

    public void showOfflinePanel() {
        ViewAnimationUtil.showMessageBar(offlinePanel);
    }

    @Override
    public void onOffline() {
        offlineBar.setVisibility(View.VISIBLE);
        showOfflinePanel();
        //Disable swipe functionality and hide the loading view
        swipeLayout.setEnabled(false);
        invalidateSwipeFunctionality();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideOfflinePanel();
    }

    /**
     * Adds a footer view to the list, which has "FIND A COURSE" button.
     *
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
                        environment.getSegment().trackUserFindsCourses();
                    } catch (Exception e) {
                        logger.error(e);
                    }

                    environment.getRouter().showFindCourses(getActivity());
                }
            });

            TextView courseNotListedTv = (TextView) footer.findViewById(R.id.course_not_listed_tv);
            courseNotListedTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showCourseNotListedDialog();
                }
            });
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void showCourseNotListedDialog() {
        ((BaseFragmentActivity) getActivity()).showWebDialog(getString(R.string.course_not_listed_file_name),
                null);
    }
}

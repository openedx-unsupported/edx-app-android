package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;

import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.event.NewVersionAvailableEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.AppStoreUtils;
import org.edx.mobile.util.IntentFactory;
import org.edx.mobile.util.Version;

import java.text.ParseException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectView;

public class MyCoursesListActivity extends BaseSingleFragmentActivity {

    @NonNull
    @InjectView(R.id.coordinator_layout)
    private CoordinatorLayout coordinatorLayout;

    @Inject
    NotificationDelegate notificationDelegate;

    public static Intent newIntent() {
        // These flags will make it so we only have a single instance of this activity,
        // but that instance will not be restarted if it is already running
        return IntentFactory.newIntentForComponent(MyCoursesListActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWhatsNew();
        configureDrawer();
        setTitle(getString(R.string.label_my_courses));
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.MY_COURSES);
    }

    private void initWhatsNew() {
        if (environment.getConfig().isWhatsNewEnabled()) {
            boolean shouldShowWhatsNew = false;
            final PrefManager.AppInfoPrefManager appPrefs = new PrefManager.AppInfoPrefManager(this);
            final String lastWhatsNewShownVersion = appPrefs.getWhatsNewShownVersion();
            if (lastWhatsNewShownVersion == null) {
                shouldShowWhatsNew = true;
            } else {
                try {
                    final Version oldVersion = new Version(lastWhatsNewShownVersion);
                    final Version newVersion = new Version(BuildConfig.VERSION_NAME);
                    if (oldVersion.isNMinorVersionsDiff(newVersion,
                            AppConstants.MINOR_VERSIONS_DIFF_REQUIRED_FOR_WHATS_NEW)) {
                        shouldShowWhatsNew = true;
                    }
                } catch (ParseException e) {
                    shouldShowWhatsNew = false;
                    logger.error(e);
                }
            }
            if (shouldShowWhatsNew) {
                environment.getRouter().showWhatsNewActivity(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            /* This is the main Activity, and is where the new version availability
             * notifications are being posted. These events are posted as sticky so
             * that they can be compared against new instances of them to be posted
             * in order to determine whether it has new information content. The
             * events have an intrinsic property to mark them as consumed, in order
             * to not have to remove the sticky events (and thus lose the last
             * posted event information). Finishing this Activity should be
             * considered as closing the current session, and the notifications
             * should be reposted on a new session. Therefore, we clear the session
             * information by removing the sticky new version availability events
             * from the event bus.
             */
            EventBus.getDefault().removeStickyEvent(NewVersionAvailableEvent.class);
        }
    }

    @Override
    public Fragment getFirstFragment() {
        return new MyCoursesListFragment();
    }


    @Override
    protected void onResume() {
        super.onResume();
        notificationDelegate.checkAppUpgrade();
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

    /**
     * Event bus callback for new app version availability event.
     *
     * @param newVersionAvailableEvent The new app version availability event.
     */
    public void onEvent(@NonNull final NewVersionAvailableEvent newVersionAvailableEvent) {
        if (!newVersionAvailableEvent.isConsumed()) {
            final Snackbar snackbar = Snackbar.make(coordinatorLayout,
                    newVersionAvailableEvent.getNotificationString(this),
                    Snackbar.LENGTH_INDEFINITE);
            if (AppStoreUtils.canUpdate(this)) {
                snackbar.setAction(R.string.label_update,
                        AppStoreUtils.OPEN_APP_IN_APP_STORE_CLICK_LISTENER);
            }
            snackbar.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    newVersionAvailableEvent.markAsConsumed();
                }
            });
            snackbar.show();
        }
    }
}

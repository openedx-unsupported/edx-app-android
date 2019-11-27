package org.edx.mobile.services;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.view.View;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.Sha1Util;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LastAccessManager {

    public interface LastAccessManagerCallback {
        boolean isFetchingLastAccessed();

        void setFetchingLastAccessed(boolean accessed);

        void showLastAccessedView(String lastAccessedSubSectionId, String courseId, View view);
    }

    protected final Logger logger = new Logger(getClass().getName());

    @NonNull
    private final LoginPrefs loginPrefs;

    @Inject
    private CourseAPI courseApi;

    @Inject
    public LastAccessManager(@NonNull LoginPrefs loginPrefs) {
        this.loginPrefs = loginPrefs;
    }

    public void fetchLastAccessed(final LastAccessManagerCallback callback, final String courseId) {
        fetchLastAccessed(callback, null, courseId);
    }

    public void fetchLastAccessed(final LastAccessManagerCallback callback, final View view, final String courseId) {
        if (!callback.isFetchingLastAccessed()) {
            final String username = loginPrefs.getUsername();
            if (courseId != null && username != null) {
                final LastAccessedPrefManager prefManager = new LastAccessedPrefManager(MainApplication.instance(), username, courseId);
                final String prefModuleId = prefManager.getLastAccessedSubsectionId();

                logger.debug("Last Accessed Module ID from Preferences "
                        + prefModuleId);

                callback.showLastAccessedView(prefModuleId, courseId, view);
                courseApi.getLastAccessedSubsection(courseId).enqueue(
                        new ErrorHandlingCallback<SyncLastAccessedSubsectionResponse>(
                                MainApplication.instance()) {
                            @Override
                            protected void onResponse(@NonNull final SyncLastAccessedSubsectionResponse result) {
                                syncWithServerOnSuccess(result, prefModuleId, prefManager, courseId, callback, view);
                            }

                            @Override
                            protected void onFailure(@NonNull Throwable error) {
                                callback.setFetchingLastAccessed(false);
                            }
                        });

                callback.setFetchingLastAccessed(true);
            }
        }
    }

    private void syncWithServerOnSuccess(SyncLastAccessedSubsectionResponse result,
                                         String prefModuleId,
                                         LastAccessedPrefManager prefManager,
                                         String courseId,
                                         LastAccessManagerCallback callback,
                                         View view
    ) {
        String server_moduleId;
        if (result != null && result.getLastVisitedModuleId() != null) {
            //Handle the last Visited Module received from Sever
            server_moduleId = result.getLastVisitedModuleId();
            logger.debug("Last Accessed Module ID from Server Get "
                    + server_moduleId);
            if (prefManager.isSyncedLastAccessedSubsection()) {
                //If preference last accessed flag is true, put the last access fetched
                //from server in Preferences and display it on Last Accessed.
                prefManager.putLastAccessedSubsection(server_moduleId, true);
                callback.showLastAccessedView(server_moduleId, courseId, view);
            } else {
                //Preference's last accessed is not synced with server,
                //Sync with server and display the result from server on UI.
                if (prefModuleId != null && prefModuleId.length() > 0) {
                    syncLastAccessedWithServer(prefManager, view, prefModuleId, courseId, callback);
                }
            }
        } else {
            //There is no Last Accessed module on the server
            if (prefModuleId != null && prefModuleId.length() > 0) {
                syncLastAccessedWithServer(prefManager, view, prefModuleId, courseId, callback);
            }
        }
        callback.setFetchingLastAccessed(false);
    }


    private void syncLastAccessedWithServer(final LastAccessedPrefManager prefManager,
                                            final View view,
                                            String prefModuleId,
                                            final String courseId,
                                            final LastAccessManagerCallback callback) {
        courseApi.syncLastAccessedSubsection(courseId, prefModuleId).enqueue(
                new ErrorHandlingCallback<SyncLastAccessedSubsectionResponse>(
                        MainApplication.instance()) {
                    @Override
                    protected void onResponse(@NonNull final SyncLastAccessedSubsectionResponse result) {
                        if (result.getLastVisitedModuleId() != null) {
                            prefManager.putLastAccessedSubsection(result.getLastVisitedModuleId(), true);
                            logger.debug("Last Accessed Module ID from Server Sync "
                                    + result.getLastVisitedModuleId());
                            callback.showLastAccessedView(result.getLastVisitedModuleId(), courseId, view);
                        }
                    }
                });
    }

    public void setLastAccessed(@NonNull String courseId, @NonNull String subsectionId) {
        new LastAccessedPrefManager(MainApplication.instance(), loginPrefs.getUsername(), courseId)
                .putLastAccessedSubsection(subsectionId, false);
    }

    private static class LastAccessedPrefManager {

        // Preference keys
        private static final String LAST_ACCESS_MODIFICATION_TIME = "last_access_modification_time";
        private static final String LAST_ACCESSED_MODULE_ID = "last_access_module_id";
        private static final String LAST_ACCESSED_SYNCED_FLAG = "lastaccess_synced_flag";

        @NonNull
        private final Context context;

        @NonNull
        private final String prefName;

        public LastAccessedPrefManager(@NonNull Context context, @NonNull String username, @NonNull String courseId) {
            this.context = context;
            this.prefName = getPrefNameForLastAccessedBy(username, courseId);
        }

        /**
         * Stores information of last accesses subsection for given id.
         * Modification date is also stored for current time.
         * Synced is marked as FALSE.
         */
        public void putLastAccessedSubsection(String subsectionId, boolean lastAccessedFlag) {
            SharedPreferences.Editor edit = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit();
            edit.putString(LAST_ACCESSED_MODULE_ID, subsectionId);
            edit.putString(LAST_ACCESS_MODIFICATION_TIME, DateUtil.getCurrentTimeStamp());
            edit.putBoolean(LAST_ACCESSED_SYNCED_FLAG, lastAccessedFlag);
            edit.commit();
        }

        /**
         * @return true if given courseId's last access is synced with server, false otherwise.
         */
        public boolean isSyncedLastAccessedSubsection() {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getBoolean(LAST_ACCESSED_SYNCED_FLAG, true);
        }


        /**
         * @return last accessed subsection id for the given course.
         */
        public String getLastAccessedSubsectionId() {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
                    .getString(LAST_ACCESSED_MODULE_ID, null);
        }
    }


    /**
     * @return preference file name that can be used to store information about last accessed subsection.
     * This preference file name is SHA1 hash of a combination of username, courseId and a constant suffix.
     */
    private static String getPrefNameForLastAccessedBy(@NonNull String username, @NonNull String courseId) {
        try {
            return Sha1Util.SHA1(username + "-" + courseId + "-last-accessed-subsection_info");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

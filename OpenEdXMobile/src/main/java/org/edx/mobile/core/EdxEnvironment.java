package org.edx.mobile.core;

import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.CourseCalendarPrefs;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.RemoteFeaturePrefs;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.Router;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.greenrobot.event.EventBus;

@Singleton
public class EdxEnvironment implements IEdxEnvironment {

    @Inject
    IDatabase database;

    @Inject
    IStorage storage;

    @Inject
    IDownloadManager downloadManager;

    @Inject
    UserPrefs userPrefs;

    @Inject
    LoginPrefs loginPrefs;

    @Inject
    CourseCalendarPrefs courseCalendarPrefs;

    @Inject
    RemoteFeaturePrefs remoteFeaturePrefs;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Inject
    NotificationDelegate notificationDelegate;

    @Inject
    Router router;

    @Inject
    Config config;

    @Inject
    EventBus eventBus;

    @Inject
    public EdxEnvironment() {
    }

    @Override
    public IDatabase getDatabase() {
        return database;
    }

    @Override
    public IDownloadManager getDownloadManager() {
        return downloadManager;
    }

    @Override
    public UserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public LoginPrefs getLoginPrefs() {
        return loginPrefs;
    }

    public CourseCalendarPrefs getCourseCalendarPrefs() {
        return courseCalendarPrefs;
    }

    @Override
    public RemoteFeaturePrefs getRemoteFeaturePrefs() {
        return remoteFeaturePrefs;
    }

    public AnalyticsRegistry getAnalyticsRegistry() {
        return analyticsRegistry;
    }

    @Override
    public NotificationDelegate getNotificationDelegate() {
        return notificationDelegate;
    }

    @Override
    public Router getRouter() {
        return router;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public IStorage getStorage() {
        return storage;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}

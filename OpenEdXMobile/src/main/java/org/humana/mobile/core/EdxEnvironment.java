package org.humana.mobile.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.humana.mobile.module.analytics.AnalyticsRegistry;
import org.humana.mobile.module.analytics.ISegment;
import org.humana.mobile.module.db.IDatabase;
import org.humana.mobile.module.download.IDownloadManager;
import org.humana.mobile.module.notification.NotificationDelegate;
import org.humana.mobile.module.prefs.LoginPrefs;
import org.humana.mobile.module.prefs.UserPrefs;
import org.humana.mobile.module.storage.IStorage;
import org.humana.mobile.services.ServiceManager;
import org.humana.mobile.util.Config;
import org.humana.mobile.view.Router;

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
    ISegment segment;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    @Inject
    NotificationDelegate notificationDelegate;

    @Inject
    Router router;

    @Inject
    Config config;

    @Inject
    ServiceManager serviceManager;

    @Inject
    EventBus eventBus;

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

    @Override
    public ISegment getSegment() {
        return segment;
    }

    @Override
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

    @Override
    public ServiceManager getServiceManager() {
        return serviceManager;
    }
}

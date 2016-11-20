package org.edx.mobile.core;


import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.edx.mobile.module.analytics.AnalyticsProvider;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.Router;

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
    AnalyticsProvider analyticsProvider;

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

    public AnalyticsProvider getAnalyticsProvider() {
        return analyticsProvider;
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

    @Override
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}

package org.edx.mobile.core;


import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.AppFeaturesPrefs;
import org.edx.mobile.module.prefs.AppInfoPrefs;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.util.Config;
import org.edx.mobile.view.Router;

/**
 * TODO - we should decompose this class into environment setting and service provider settings.
 */
public interface IEdxEnvironment {

    IDatabase getDatabase();

    IStorage getStorage();

    IDownloadManager getDownloadManager();

    UserPrefs getUserPrefs();

    LoginPrefs getLoginPrefs();

    AppFeaturesPrefs getAppFeaturesPrefs();

    AppInfoPrefs getAppInfoPrefs();

    AnalyticsRegistry getAnalyticsRegistry();

    NotificationDelegate getNotificationDelegate();

    Router getRouter();

    Config getConfig();
}

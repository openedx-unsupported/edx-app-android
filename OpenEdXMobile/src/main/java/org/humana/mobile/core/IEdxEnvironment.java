package org.humana.mobile.core;


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

/**
 * TODO - we should decompose this class into environment setting and service provider settings.
 */
public interface IEdxEnvironment {

    IDatabase getDatabase();

    IStorage getStorage();

    IDownloadManager getDownloadManager();

    UserPrefs getUserPrefs();

    LoginPrefs getLoginPrefs();

    ISegment getSegment();

    AnalyticsRegistry getAnalyticsRegistry();

    NotificationDelegate getNotificationDelegate();

    Router getRouter();

    Config getConfig();

    ServiceManager getServiceManager();
}

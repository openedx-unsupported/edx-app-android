package org.edx.mobile.core;


import com.google.inject.Inject;
import com.qualcomm.qlearn.sdk.discussion.DiscussionAPI;

import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.module.notification.NotificationDelegate;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.services.ServiceManager;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.images.ImageCacheManager;
import org.edx.mobile.view.Router;

/**
 * TODO - we should decompose this class into environment setting and service provider settings.
 */
public interface IEdxEnvironment {

    IDatabase getDatabase();

    IStorage getStorage();

    IDownloadManager getDownloadManager();

    ImageCacheManager getImageCacheManager();

    UserPrefs getUserPrefs();

    ISegment getSegment();

    NotificationDelegate getNotificationDelegate();

    Router getRouter();

    Config getConfig();

    ServiceManager getServiceManager();

    //TODO - it should be part of ServiceManager
    DiscussionAPI getDiscussionAPI();
}

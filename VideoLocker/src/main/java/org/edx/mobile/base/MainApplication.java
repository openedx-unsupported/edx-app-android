package org.edx.mobile.base;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;
import com.parse.Parse;
import com.parse.ParseInstallation;

import org.edx.mobile.event.CourseAnnouncementEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.Environment;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.PropertyUtil;
import org.edx.mobile.util.images.ImageCacheManager;
import org.edx.mobile.util.images.RequestManager;
import org.edx.mobile.view.Router;

import java.util.Locale;

import de.greenrobot.event.EventBus;
import io.fabric.sdk.android.Fabric;

/**
 * This class initializes the modules of the app based on the configuration.
 */
public class MainApplication extends Application{
    //FIXME - temporary solution
    public static final boolean Q4_ASSESSMENT_FLAG = false;
    //FIXME - temporary solution
    public static final boolean ForumEnabled = false;

    protected final Logger logger = new Logger(getClass().getName());

    private static MainApplication application;

    public static final MainApplication instance(){
        return application;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * Initializes the request manager, image cache,
     * all third party integrations and shared components.
     */
    private void init() {
        // initialize logger
        Logger.init(this.getApplicationContext());

        application = this;
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());

        // setup environment
        Environment env = new Environment();
        env.setupEnvironment(this.getApplicationContext());

        // setup image cache
        createImageCache();

        // initialize SegmentIO
        if (Config.getInstance().getSegmentConfig().isEnabled()) {
            SegmentFactory.makeInstance(this);
        }

        // initialize Fabric
        if (Config.getInstance().getFabricConfig().isEnabled()) {
            Fabric.with(this, new Crashlytics());
        }

        // initialize NewRelic with crash reporting disabled
        if (Config.getInstance().getNewRelicConfig().isEnabled()) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(Config.getInstance().getNewRelicConfig().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        // initialize Facebook SDK
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(getApplicationContext());
        if ( !isOnZeroRatedNetwork
                && Config.getInstance().getFacebookConfig().isEnabled()) {
            com.facebook.Settings.setApplicationId(Config.getInstance().getFacebookConfig().getFacebookAppId());
        }

        // initialize Parse notification
        // it maybe good to support multiple notification providers running
        // at the same time, as it is less like to be the case in the future,
        // we at two level of controls just for easy change of different providers.
        if ( Config.getInstance().isNotificationEnabled() ){
            Config.ParseNotificationConfig parseNotificationConfig =
                    Config.getInstance().getParseNotificationConfig();
            if ( parseNotificationConfig.isEnabled() ) { 
                Parse.enableLocalDatastore(this);
                Parse.initialize(this, parseNotificationConfig.getParseApplicationId(), parseNotificationConfig.getParseClientKey());
                tryToUpdateParseForAppUpgrade(this);
            }
        }


        // try repair of download data if app version is updated
        new Storage(this).repairDownloadCompletionData();

        //TODO - ideally this should belong to SegmentFactory, but code refactoring is need because of the way it constructs new instances
        EventBus.getDefault().registerSticky(this);
    }

    public void onEvent(CourseAnnouncementEvent event) {
        if ( event.type == CourseAnnouncementEvent.EventType.MESSAGE_RECEIVED ) {
            SegmentFactory.getInstance().trackNotificationReceived(event.courseId);
            EventBus.getDefault().removeStickyEvent(event);
        }
        if ( event.type == CourseAnnouncementEvent.EventType.MESSAGE_TAPPED ) {
            SegmentFactory.getInstance().trackNotificationTapped(event.courseId);
            EventBus.getDefault().removeStickyEvent(event);
        }
    }
    
    /**
     * Create the image cache. Uses Memory Cache by default. 
     * Change to Disk for a Disk based LRU implementation.
     */
    private void createImageCache(){
        int DISK_IMAGECACHE_SIZE = 1024*1024*10;
        CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
        //PNG is lossless so quality is ignored but must be provided
        int DISK_IMAGECACHE_QUALITY = 100;

        RequestManager.init(this);
        ImageCacheManager.getInstance().init(this,
                this.getPackageCodePath()
                , DISK_IMAGECACHE_SIZE
                , DISK_IMAGECACHE_COMPRESS_FORMAT
                , DISK_IMAGECACHE_QUALITY
                , ImageCacheManager.CacheType.MEMORY);
    }


    /**
     * callback when application is launched from background or from a cold launch,
     */
    public void onApplicationLaunchedFromBackground(){
        logger.debug("onApplicationLaunchedFromBackground");
        PrefManager pref = new PrefManager(this, PrefManager.Pref.LOGIN);
        if ( pref.hasAuthTokenSocialCookie() ){
             Router.getInstance().forceLogout(this);
        }
    }


    /**
     * if app is launched from upgrading, we need to resync with parse server.
     * @param context
     */
    private void tryToUpdateParseForAppUpgrade(Context context){

        PrefManager.AppInfoPrefManager pmanager = new PrefManager.AppInfoPrefManager(context);
        Long previousVersion = pmanager.getAppVersionCode();
        boolean hadNotification = pmanager.isNotificationEnabled();
        int  curVersion = PropertyUtil.getManifestVersionCode(context);
        if (  previousVersion < curVersion ){
            if ( hadNotification ) {
                pmanager.setAppUpgradeNeedSyncWithParse(true);
            }
        }
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        final String languageKey = "preferredLanguage";
        final String countryKey =  "preferredCountry";
        String savedPreferredLanguage = installation.getString(languageKey);
        String savedPreferredCountry = installation.getString(countryKey);
        Locale locale = Locale.getDefault();
        String currentPreferredLanguage = locale.getLanguage();
        String currentPreferredCountry = locale.getCountry();
        boolean dirty = false;
        if (!currentPreferredLanguage.equals(savedPreferredLanguage) ) {
            installation.put(languageKey, currentPreferredLanguage);
            dirty = true;
        }
        if (!currentPreferredCountry.equals(savedPreferredCountry) ) {
            installation.put(countryKey, currentPreferredCountry);
            dirty = true;
        }
        if ( dirty ) {
            try {
                installation.saveInBackground();
            }catch (Exception ex){
                logger.error(ex);
            }
        }

        pmanager.setAppVersionCode(curVersion);
        pmanager.setNotificationEnabled(true);
    }

    private final class MyActivityLifecycleCallbacks
            implements Application.ActivityLifecycleCallbacks{

        Activity  prevPausedOne;

        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        public void onActivityDestroyed(Activity activity) {

        }

        public void onActivityPaused(Activity activity) {
            prevPausedOne = activity;
        }

        public void onActivityResumed(Activity activity) {
             if( null ==  prevPausedOne || prevPausedOne == activity ){
                 //application launched from background,
                 onApplicationLaunchedFromBackground();
             }
        }

        public void onActivitySaveInstanceState(Activity activity,
                                                Bundle outState) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityStopped(Activity activity) {
        }
    }
}
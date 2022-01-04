package org.edx.mobile.base;


import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.appboy.Appboy;
import com.appboy.AppboyLifecycleCallbackListener;
import com.appboy.configuration.AppboyConfig;
import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.newrelic.agent.android.NewRelic;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.core.EdxDefaultModule;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.AppUpdatedEvent;
import org.edx.mobile.event.NewRelicEvent;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.UnacknowledgedNoticeResponse;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.analytics.FirebaseAnalytics;
import org.edx.mobile.module.analytics.SegmentAnalytics;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.receivers.NetworkConnectivityReceiver;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.NotificationUtil;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.branch.referral.Branch;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;

/**
 * This class initializes the modules of the app based on the configuration.
 */
public abstract class MainApplication extends MultiDexApplication {

    protected final Logger logger = new Logger(getClass().getName());

    public static MainApplication application;

    public static final MainApplication instance() {
        return application;
    }

    private Injector injector;

    @Inject
    protected Config config;

    @Inject
    protected AnalyticsRegistry analyticsRegistry;

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
        application = this;
        // FIXME: Disable RoboBlender to avoid annotation processor issues for now, as we already have plans to move to some other DI framework. See LEARNER-1687.
        // ref: https://github.com/roboguice/roboguice/wiki/RoboBlender-wiki#disabling-roboblender
        // ref: https://developer.android.com/studio/build/gradle-plugin-3-0-0-migration
        RoboGuice.setUseAnnotationDatabases(false);
        injector = RoboGuice.getOrCreateBaseApplicationInjector((Application) this, RoboGuice.DEFAULT_STAGE,
                (Module) RoboGuice.newDefaultRoboModule(this), (Module) new EdxDefaultModule(this));

        injector.injectMembers(this);

        EventBus.getDefault().register(new CrashlyticsCrashReportObserver());

        if (config.getNewRelicConfig().isEnabled()) {
            EventBus.getDefault().register(new NewRelicObserver());
        }

        // initialize NewRelic with crash reporting disabled
        if (config.getNewRelicConfig().isEnabled()) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(config.getNewRelicConfig().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        // Add Segment as an analytics provider if enabled in the config
        if (config.getSegmentConfig().isEnabled()) {
            analyticsRegistry.addAnalyticsProvider(injector.getInstance(SegmentAnalytics.class));
        }
        if (config.getFirebaseConfig().isAnalyticsSourceFirebase()) {
            // Only add Firebase as an analytics provider if enabled in the config and Segment is disabled
            // because if Segment is enabled, we'll be using Segment's implementation for Firebase
            analyticsRegistry.addAnalyticsProvider(injector.getInstance(FirebaseAnalytics.class));
        }

        if (config.getFirebaseConfig().isEnabled()) {
            // Firebase notification needs to initialize the FirebaseApp before
            // subscribe/unsubscribe to/from the topics
            FirebaseApp.initializeApp(this);
            if (config.areFirebasePushNotificationsEnabled()) {
                NotificationUtil.subscribeToTopics(config);
            } else if (!config.areFirebasePushNotificationsEnabled()) {
                NotificationUtil.unsubscribeFromTopics(config);
            }
        }

        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        checkIfAppVersionUpgraded(this);

        // Init Branch
        if (config.getBranchConfig().isEnabled()) {
            Branch.getAutoInstance(this);
        }

        // Initialize Facebook SDK
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(getApplicationContext(), config);
        if (!isOnZeroRatedNetwork && config.getFacebookConfig().isEnabled()) {
            // Facebook sdk should be initialized through AndroidManifest meta data declaration but
            // we are generating the meta data through gradle script due to which it is necessary
            // to manually initialize the sdk here.
            FacebookSdk.setApplicationId(config.getFacebookConfig().getFacebookAppId());
            FacebookSdk.sdkInitialize(getApplicationContext());
        }

        // Braze SDK Initialization
        if (config.getBrazeConfig().isEnabled() && config.getFirebaseConfig().isEnabled()) {
            AppboyConfig appboyConfig = new AppboyConfig.Builder()
                    .setIsFirebaseCloudMessagingRegistrationEnabled(config.areFirebasePushNotificationsEnabled()
                            && config.getBrazeConfig().isPushNotificationsEnabled())
                    .setFirebaseCloudMessagingSenderIdKey(config.getFirebaseConfig().getProjectNumber())
                    .setHandlePushDeepLinksAutomatically(true)
                    .build();
            Appboy.configure(this, appboyConfig);
            registerActivityLifecycleCallbacks(new AppboyLifecycleCallbackListener(true, true));
        }
    }

    public void showBanner(LoginAPI loginAPI, boolean delayCall) {
        if (delayCall) {
            new Handler().postDelayed(() -> callBannerAPI(loginAPI), 10000);
        } else {
            callBannerAPI(loginAPI);
        }
    }

    private void callBannerAPI(LoginAPI loginAPI) {
        loginAPI.getUnacknowledgedNotice().enqueue(new Callback<UnacknowledgedNoticeResponse>() {
            @Override
            public void onResponse(@NonNull Call<UnacknowledgedNoticeResponse> call,
                                   @NonNull Response<UnacknowledgedNoticeResponse> response) {
                if (getEnvironment(getApplicationContext()).getLoginPrefs().getUsername() != null
                        && response.isSuccessful() && response.code() == HttpStatus.OK) {
                    if (response.body() != null && !response.body().getResults().isEmpty()) {
                        getEnvironment(getApplicationContext()).getRouter().showAuthenticatedWebViewActivity(
                                getApplicationContext(), response.body().getResults().get(0), "", true
                        );
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnacknowledgedNoticeResponse> call, @NonNull Throwable throwable) {

            }
        });
    }

    private void checkIfAppVersionUpgraded(Context context) {
        PrefManager.AppInfoPrefManager prefManager = new PrefManager.AppInfoPrefManager(context);
        long previousVersionCode = prefManager.getAppVersionCode();
        final long curVersionCode = BuildConfig.VERSION_CODE;
        if (previousVersionCode < 0) {
            // App opened first time after installation
            // Save version code and name in preferences
            prefManager.setAppVersionCode(curVersionCode);
            prefManager.setAppVersionName(BuildConfig.VERSION_NAME);
            logger.debug("App opened first time, VersionCode:" + curVersionCode);
        } else if (previousVersionCode < curVersionCode) {
            final String previousVersionName = prefManager.getAppVersionName();
            // Update version code and name in preferences
            prefManager.setAppVersionCode(curVersionCode);
            prefManager.setAppVersionName(BuildConfig.VERSION_NAME);
            logger.debug("App updated, VersionCode:" + previousVersionCode + "->" + curVersionCode);
            // App updated
            onAppUpdated(previousVersionCode, curVersionCode, previousVersionName, BuildConfig.VERSION_NAME);
        }
    }

    private void onAppUpdated(final long previousVersionCode, final long curVersionCode,
                              final String previousVersionName, final String curVersionName) {
        // Try repair of download data on updating of app version
        injector.getInstance(IStorage.class).repairDownloadCompletionData();
        // Fire app updated event
        EventBus.getDefault().postSticky(new AppUpdatedEvent(previousVersionCode, curVersionCode,
                previousVersionName, curVersionName));
    }

    public static class CrashlyticsCrashReportObserver {
        @SuppressWarnings("unused")
        public void onEventMainThread(Logger.CrashReportEvent e) {
            FirebaseCrashlytics.getInstance().recordException(e.getError());
        }
    }

    public static class NewRelicObserver {
        @SuppressWarnings("unused")
        public void onEventMainThread(NewRelicEvent e) {
            NewRelic.setInteractionName("Display " + e.getScreenName());
        }
    }

    public Injector getInjector() {
        return injector;
    }

    @NonNull
    public static IEdxEnvironment getEnvironment(@NonNull Context context) {
        return RoboGuice.getInjector(context.getApplicationContext()).getInstance(IEdxEnvironment.class);
    }
}

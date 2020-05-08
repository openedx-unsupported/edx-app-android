package org.humana.mobile.base;


import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.crashlytics.android.core.CrashlyticsCore;
import com.evernote.android.state.StateSaver;
import com.facebook.FacebookSdk;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;
import com.newrelic.agent.android.NewRelic;

import org.humana.mobile.BuildConfig;
import org.humana.mobile.R;
import org.humana.mobile.core.EdxDefaultModule;
import org.humana.mobile.core.IEdxEnvironment;
import org.humana.mobile.event.AppUpdatedEvent;
import org.humana.mobile.event.NewRelicEvent;
import org.humana.mobile.http.provider.OkHttpClientProvider;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.module.analytics.AnalyticsRegistry;
import org.humana.mobile.module.analytics.AnswersAnalytics;
import org.humana.mobile.module.analytics.FirebaseAnalytics;
import org.humana.mobile.module.analytics.SegmentAnalytics;
import org.humana.mobile.module.prefs.PrefManager;
import org.humana.mobile.module.storage.IStorage;
import org.humana.mobile.receivers.NetworkConnectivityReceiver;
import org.humana.mobile.tta.utils.LocaleHelper;
import org.humana.mobile.util.Config;
import org.humana.mobile.util.NetworkUtil;
import org.humana.mobile.util.NotificationUtil;

import java.io.InputStream;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import roboguice.RoboGuice;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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

    public static GoogleAnalytics sAnalytics;
    public static Tracker sTracker;

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

        LocaleHelper.setLocale(getApplicationContext(), "en");

        // initialize Fabric
        if (config.getFabricConfig().isEnabled() && !BuildConfig.DEBUG) {
            Fabric.with(this, config.getFabricConfig().getKitsConfig().getEnabledKits());

            if (config.getFabricConfig().getKitsConfig().isCrashlyticsEnabled()) {
                EventBus.getDefault().register(new CrashlyticsCrashReportObserver());
            }

            if (config.getFabricConfig().getKitsConfig().isAnswersEnabled()) {
                analyticsRegistry.addAnalyticsProvider(injector.getInstance(AnswersAnalytics.class));
            }
        }

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

        // Add Firebase as an analytics provider if enabled in the config
        if (config.getFirebaseConfig().isAnalyticsEnabled()) {
            analyticsRegistry.addAnalyticsProvider(injector.getInstance(FirebaseAnalytics.class));
        }

        if (config.getFirebaseConfig().areNotificationsEnabled()) {
            NotificationUtil.subscribeToTopics(config);
        } else if (!config.getFirebaseConfig().areNotificationsEnabled() &&
                config.getFirebaseConfig().isEnabled()) {
            NotificationUtil.unsubscribeFromTopics(config);
        }

        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(new NetworkConnectivityReceiver(), new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

        checkIfAppVersionUpgraded(this);

        // Register Font Awesome module in android-iconify library
        Iconify.with(new FontAwesomeModule());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/OpenSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Init Branch
        if (Config.FabricBranchConfig.isBranchEnabled(config.getFabricConfig())) {
            Branch.getAutoInstance(this);
        }

        // Force Glide to use our version of OkHttp which now supports TLS 1.2 out-of-the-box for
        // Pre-Lollipop devices
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Glide.get(this).register(GlideUrl.class, InputStream.class,
                    new OkHttpUrlLoader.Factory(injector.getInstance(OkHttpClientProvider.class).get()));
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

        Bridge.initialize(this, new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                StateSaver.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                StateSaver.restoreInstanceState(target, state);
            }
        });

        sAnalytics = GoogleAnalytics.getInstance(this);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
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
            CrashlyticsCore.getInstance().logException(e.getError());
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

    /**
     * Gets the default {@link Tracker} for this {@link MainApplication}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }

}
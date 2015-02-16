package org.edx.mobile.base;


import android.app.Application;
import android.content.IntentFilter;
import android.graphics.Bitmap.CompressFormat;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.receivers.NetworkConnectivityReceiver;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.Environment;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.images.ImageCacheManager;
import org.edx.mobile.util.images.RequestManager;

import io.fabric.sdk.android.Fabric;

/**
 * This class initializes the modules of the app based on the configuration.
 */
public class MainApplication extends Application {

    NetworkConnectivityReceiver connectivityReceiver;

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

        // setup environment
        Environment env = new Environment();
        env.setupEnvironment(this.getApplicationContext());

        // setup image cache
        createImageCache();

        // initialize SegmentIO
        if (Config.getInstance().getThirdPartyTraffic().isSegmentEnabled()
                && Config.getInstance().getSegmentIOWriteKey() != null) {
            SegmentFactory.makeInstance(this);
        }

        // initialize Fabric
        if (Config.getInstance().getThirdPartyTraffic().isFabricEnabled()
                && Config.getInstance().getFabricKey() != null) {
            Fabric.with(this, new Crashlytics());
        }

        // initialize NewRelic with crash reporting disabled
        if (Config.getInstance().getThirdPartyTraffic().isNewRelicEnabled()
                && Config.getInstance().getNewRelicKey() != null) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(Config.getInstance().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        // initialize Facebook SDK
        boolean isOnZeroRatedNetwork = NetworkUtil.isOnZeroRatedNetwork(getApplicationContext());
        if ( !isOnZeroRatedNetwork
                && Config.getInstance().getThirdPartyTraffic().isFacebookEnabled()
                && Config.getInstance().getFacebookAppId() != null) {
            com.facebook.Settings.setApplicationId(Config.getInstance().getFacebookAppId());
        }

        // try repair of download data if app version is updated
        new Storage(this).repairDownloadCompletionData();

        // register connectivity receiver
        connectivityReceiver = new NetworkConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
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
}
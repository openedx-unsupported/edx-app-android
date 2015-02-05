package org.edx.mobile.base;


import android.app.Application;
import android.content.IntentFilter;
import android.graphics.Bitmap.CompressFormat;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.receivers.NetworkConnectivityReceiver;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.Environment;
import org.edx.mobile.util.images.ImageCacheManager;
import org.edx.mobile.util.images.RequestManager;

import io.fabric.sdk.android.Fabric;

/**
 * Code for adding an L1 image cache to Volley. 
 * 
 */
public class MainApplication extends Application {
    
    private static int DISK_IMAGECACHE_SIZE = 1024*1024*10;
    private static CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = CompressFormat.PNG;
    private static int DISK_IMAGECACHE_QUALITY = 100;  //PNG is lossless so quality is ignored but must be provided

    NetworkConnectivityReceiver connectivityReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        init();

        connectivityReceiver = new NetworkConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    /**
     * Initialize the request manager and the image cache
     * Initialize shared components
     */
    private void init() {
        // initialize logger
        Logger.init(this.getApplicationContext());

        Environment env = new Environment();
        env.setupEnvironment(this.getApplicationContext());

        RequestManager.init(this);
        createImageCache();

        // initialize SegmentIO, empty writeKey is handled in SegmentTracker
        SegmentFactory.makeInstance(this);

        if(Config.getInstance().getFabricKey() != null) {
            Fabric.with(this, new Crashlytics());
        }
        if(Config.getInstance().getNewRelicKey() != null) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(Config.getInstance().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        // initialize Facebook SDK
        if (Config.getInstance().getFacebookAppId() != null) {
            com.facebook.Settings.setApplicationId(Config.getInstance().getFacebookAppId());
        }
    }
    
    /**
     * Create the image cache. Uses Memory Cache by default. 
     * Change to Disk for a Disk based LRU implementation.
     */
    private void createImageCache(){
        ImageCacheManager.getInstance().init(this,
                this.getPackageCodePath()
                , DISK_IMAGECACHE_SIZE
                , DISK_IMAGECACHE_COMPRESS_FORMAT
                , DISK_IMAGECACHE_QUALITY
                , ImageCacheManager.CacheType.MEMORY);
    }
    
}
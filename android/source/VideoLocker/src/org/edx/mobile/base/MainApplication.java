package org.edx.mobile.base;


import android.app.Application;
import android.graphics.Bitmap.CompressFormat;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.SegmentFactory;
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
    
    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    /**
     * Intialize the request manager and the image cache
     * Initialize shared components
     */
    private void init() {
        // initialize logger
        Logger.init(this.getApplicationContext());

        // setup environment
        Environment env = new Environment();
        env.setupEnvironment(this.getApplicationContext());

        // setup image cache
        createImageCache();

        // initialize SegmentIO, empty writeKey is handled in SegmentTracker
        SegmentFactory.makeInstance(this);

        // initialize Fabric
        if(Config.getInstance().getFabricKey() != null) {
            Fabric.with(this, new Crashlytics());
        }

        // initialize NewRelic with crashlytics disabled
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
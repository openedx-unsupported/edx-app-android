package org.edx.mobile.base;


import android.app.Application;
import android.graphics.Bitmap.CompressFormat;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.validate.ValidationUtil;
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

        // setup configuration in the Environment
        Environment.makeInstance(this.getApplicationContext());

        // setup image cache
        createImageCache();

        // initialize SegmentIO, empty writeKey is handled in SegmentTracker
        SegmentFactory.makeInstance(this);

        // initialize Fabric
        if(ValidationUtil.isNotNull(Environment.getInstance().getConfig().getFabricKey())) {
            Fabric.with(this, new Crashlytics());
        }

        // initialize NewRelic
        if(ValidationUtil.isNotNull(Environment.getInstance().getConfig().getNewRelicKey())) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(Environment.getInstance().getConfig().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        // initialize Facebook SDK
        if (ValidationUtil.isNotNull(Environment.getInstance().getConfig().getFacebookAppId())) {
            com.facebook.Settings.setApplicationId(Environment.getInstance().getConfig().getFacebookAppId());
        }
    }
    
    /**
     * Create the image cache. Uses Memory Cache by default. 
     * Change to Disk for a Disk based LRU implementation.
     */
    private void createImageCache(){
        RequestManager.init(this);
        ImageCacheManager.getInstance().init(this,
                this.getPackageCodePath()
                , DISK_IMAGECACHE_SIZE
                , DISK_IMAGECACHE_COMPRESS_FORMAT
                , DISK_IMAGECACHE_QUALITY
                , ImageCacheManager.CacheType.MEMORY);
    }
    
}
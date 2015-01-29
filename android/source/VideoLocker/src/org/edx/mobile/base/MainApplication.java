package org.edx.mobile.base;


import android.app.Application;
import android.graphics.Bitmap.CompressFormat;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import io.fabric.sdk.android.Fabric;
import org.edx.mobile.logger.Logger;
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

        Environment.makeInstance(this.getApplicationContext());
        RequestManager.init(this);
        createImageCache();
        if(Environment.getInstance().getConfig().getFabricKey() != null) {
            Fabric.with(this, new Crashlytics());
        }
        if(Environment.getInstance().getConfig().getNewRelicKey() != null) {
            //Crash reporting for new relic has been disabled
            NewRelic.withApplicationToken(Environment.getInstance().getConfig().getNewRelicKey())
                    .withCrashReportingEnabled(false)
                    .start(this);
        }

        // initialize Facebook SDK
        if (Environment.getInstance().getConfig().getFacebookAppId() != null) {
            com.facebook.Settings.setApplicationId(Environment.getInstance().getConfig().getFacebookAppId());
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
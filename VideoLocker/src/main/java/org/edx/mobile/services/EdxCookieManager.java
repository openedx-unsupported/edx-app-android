package org.edx.mobile.services;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.event.SessionIdRefreshEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.task.GetSessesionExchangeCookieTask;

import java.io.File;
import java.net.HttpCookie;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 *  A central place for course data model transformation
 */
public class EdxCookieManager {

    // We'll assume that cookies are valid for at least one hour; after that
    // they'll be requeried on API levels lesser than Marshmallow (which
    // provides an error callback with the HTTP error code) prior to usage.
    private static final long FRESHNESS_INTERVAL = TimeUnit.HOURS.toMillis(1);

    private long authSessionCookieExpiration = -1;

    protected final Logger logger = new Logger(getClass().getName());

    private static EdxCookieManager instance;

    private GetSessesionExchangeCookieTask task;

    public static synchronized EdxCookieManager getSharedInstance(){
        if ( instance == null )
            instance = new EdxCookieManager();
        return instance;
    }

    public void clearWebWiewCookie(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager.getInstance().removeAllCookie();
        } else {
            try {
                CookieSyncManager.createInstance(context);
                android.webkit.CookieManager.getInstance().removeAllCookie();
            }catch (Exception ex){
                logger.debug(ex.getMessage());
            }
        }
        authSessionCookieExpiration = -1;

    }

    public void clearWebViewCache(Context context){
        try {
            clearWebWiewCookie(context);

            boolean success = context.deleteDatabase("webview.db");
            logger.debug("delete webview.db result = " + success);
            success =  context.deleteDatabase("webviewCache.db");
            logger.debug("delete webviewCache.db result = " + success);
        } catch (Exception e) {
            logger.error(e);
        }


        File webviewCacheDir = new File(context.getCacheDir().getAbsolutePath()+"/webviewCache");
        if(webviewCacheDir.exists()){
            context.deleteFile(webviewCacheDir.getAbsolutePath());
        }

        File appCacheDir = new File(context.getFilesDir().getAbsolutePath()+ "/webcache");
        if(appCacheDir.exists()){
            context.deleteFile(appCacheDir.getAbsolutePath());
        }
    }

    public synchronized  void tryToRefreshSessionCookie( ){
        if ( task == null || task.isCancelled()  ) {
            task =new GetSessesionExchangeCookieTask(MainApplication.instance()) {
                @Override
                public void onSuccess(List<HttpCookie> result) {
                    if (result == null || result.isEmpty()) {
                        logger.debug("result is empty");
                        EventBus.getDefault().post(new SessionIdRefreshEvent(false));
                        return;
                    }

                    final CookieManager cookieManager = CookieManager.getInstance();
                    clearWebWiewCookie(context);
                    for (HttpCookie cookie : result) {
                        cookieManager.setCookie(environment.getConfig().getApiHostURL(), cookie.toString());
                    }
                    authSessionCookieExpiration = System.currentTimeMillis() + FRESHNESS_INTERVAL;
                    EventBus.getDefault().post(new SessionIdRefreshEvent(true));
                    task = null;
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                    EventBus.getDefault().post(new SessionIdRefreshEvent(false));
                    task = null;
                }
            };
            task.execute();
        }
    }

    public boolean isSessionCookieMissingOrExpired() {
        return authSessionCookieExpiration < System.currentTimeMillis();
    }
}

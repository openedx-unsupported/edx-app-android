package org.edx.mobile.services;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.util.WeakList;

/**
 *  by default, ViewPager will populate UI for both current one-in-view and cached one-in-the-future.
 *  in order to improve performance, we wont download it in parallel.
 */
public class ViewPagerWebViewDownloadManager{

    public static interface HtmlTaskCallback{
         void startLoadingPage();

    }

    protected final Logger logger = new Logger(getClass().getName());


    private WeakList<HtmlTaskCallback> weakList;

    public ViewPagerWebViewDownloadManager(){
        weakList = new WeakList<>();
    }

    public synchronized void removeTask(HtmlTaskCallback callback){
        weakList.remove(callback);
    }

    public synchronized  void addTask(HtmlTaskCallback callback){
        weakList.add(callback);
        tryToRunTask();
    }

    public synchronized void finishProcess(HtmlTaskCallback callback) {
         weakList.remove(callback);
         tryToRunTask();
    }

    private void tryToRunTask(){
        HtmlTaskCallback callback = weakList.getFirstValid();
        if ( callback == null )
            return;
        callback.startLoadingPage();
    }


}

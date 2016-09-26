package org.edx.mobile.services;

import android.os.Handler;
import android.os.Looper;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.util.WeakList;
import org.edx.mobile.view.common.RunnableCourseComponent;
import org.edx.mobile.view.common.TaskCallback;

import java.util.List;

/**
 *  by default, ViewPager will populate UI for both current one-in-view and cached one-in-the-future.
 *  in order to improve performance, we wont download it in parallel.
 *  but it matters only for the initial load when user navigate from course outline page to detail page.
 *
 */
public class ViewPagerDownloadManager implements TaskCallback {

    //TODO - current we just disable the pre-loading behavior
    //for potential memory usage issue for certain type devices
    //we will enable it when we can reduce javascript file size
    //in assesssment webview
    public static boolean USING_UI_PRELOADING = false;

    protected final Logger logger = new Logger(getClass().getName());

    public static ViewPagerDownloadManager instance = new ViewPagerDownloadManager();

    private WeakList<RunnableCourseComponent> runnableCourseComponentWeakList;

    //it is the first component to show
    private CourseComponent mainComponent;
    private CourseComponent nextComponent;
    private CourseComponent prevComponent;

    private boolean taskIsRunning;

    private ViewPagerDownloadManager(){
        runnableCourseComponentWeakList = new WeakList<>();
    }

    public void clear(){
        runnableCourseComponentWeakList.clear();
        mainComponent = null;
        nextComponent = null;
        prevComponent = null;
        taskIsRunning = false;
    }

    /**
     *  specify the cache range for viewpager. we only care about the initial visualization of the viewpagers.
     *  for the [prev, main, next] fragment UI/pages.
     *
     */
    public void setMainComponent(CourseComponent component,List<CourseComponent> unitList){
        if ( !USING_UI_PRELOADING )
            return;
        clear();
        this.mainComponent = component;
        int index = unitList.indexOf(component);
        if (index > 0 ){
            prevComponent = unitList.get(index -1);
        }
        if ( index < unitList.size() - 1 ){
            nextComponent = unitList.get(index +1);
        }
    }

    /**
     *  specify the cache range for viewpager. we only care about the initial visualization of the viewpagers.
     *  for the [prev, main, next] fragment UI/pages.
     *  for the viewpagers in this range, also main page is not finish downloading, it will be initial phase.
     *  we will apply our loading policy only in initial phase
     */
    public synchronized boolean inInitialPhase(CourseComponent component){
         if ( component != mainComponent && component != nextComponent && component != prevComponent )
             return false;

         return runnableCourseComponentWeakList.size() > 0;
    }

    public synchronized void removeTask(RunnableCourseComponent callback){
        runnableCourseComponentWeakList.remove(callback);
    }

    /**
     * if the UI is mainUI, we will kick off the downloading process for this mainUI.
     * if mainUI is downloaded, but no task is running, we kick off the downloading process for this UI
     */
    public synchronized  void addTask(RunnableCourseComponent callback){
        runnableCourseComponentWeakList.add(callback);
        if ( mainComponent != null && mainComponent.equals(callback.getCourseComponent())) {
             taskIsRunning = true;
            callback.run();
            return;
        }
        if ( !taskIsRunning  ){
               taskIsRunning = true;
               callback.run();
        }
    }

    private void tryToRunTask(){

        if ( runnableCourseComponentWeakList.size() == 0 )
            return;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                //we use only default caching policy here for ViewPager, which is caching size = 1
                Runnable callback =  UiUtil.isLeftToRightOrientation() ? runnableCourseComponentWeakList.getLastValid() : runnableCourseComponentWeakList.getFirstValid();
                if ( callback == null ) {
                    tryToRunTask(); //if we are in some kind of race condition for GC, let's retry.
                    //it wont run into infinite loop as we check the list size at the very beginning
                } else {
                    taskIsRunning = true;
                    callback.run();
                }
            }
        });

    }


    @Override
    public synchronized  void done(Runnable task, boolean success) {
        RunnableCourseComponent runnableCourseComponent
             = (RunnableCourseComponent)task;
        if( runnableCourseComponentWeakList.remove(runnableCourseComponent) ){
            taskIsRunning = false;
            tryToRunTask();
        }
    }

    public boolean isTaskIsRunning(){
        return taskIsRunning;
    }

    public int numTaskInStack(){
        return runnableCourseComponentWeakList.size();
    }


}

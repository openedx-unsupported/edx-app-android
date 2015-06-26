package org.edx.mobile.test;

import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.services.ViewPagerDownloadManager;
import org.edx.mobile.view.common.RunnableCourseComponent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class ViewPagerDownloadManagerTest {
    final CourseComponent mainComponent = Mockito.mock(CourseComponent.class);
    final CourseComponent prevComponent = Mockito.mock(CourseComponent.class);
    final CourseComponent nextComponent = Mockito.mock(CourseComponent.class);
    final CourseComponent otherComponent = Mockito.mock(CourseComponent.class);
    RunnableCourseComponent mainComponentUI;
    RunnableCourseComponent prevComponentUI;
    RunnableCourseComponent nextComponentUI;
    RunnableCourseComponent otherComponentUI;

    @Before
    public  void setUp() throws Exception {
        //use mockito.answer is a little inconvenience, let's use fake approach
        mainComponentUI = new RunnableCourseComponent(){
            @Override
            public CourseComponent getCourseComponent() {
                return mainComponent;
            }
            @Override
            public void run() {
                ViewPagerDownloadManager.instance.done(this, true);
            }
        };
        prevComponentUI = new RunnableCourseComponent(){
            @Override
            public CourseComponent getCourseComponent() {
                return prevComponent;
            }
            @Override
            public void run() {
                ViewPagerDownloadManager.instance.done(this, true);
            }
        };
        nextComponentUI = new RunnableCourseComponent(){
            @Override
            public CourseComponent getCourseComponent() {
                return nextComponent;
            }
            @Override
            public void run() {
                ViewPagerDownloadManager.instance.done(this, true);
            }
        };

        otherComponentUI = new RunnableCourseComponent(){
            @Override
            public CourseComponent getCourseComponent() {
                return otherComponent;
            }
            @Override
            public void run() {
                ViewPagerDownloadManager.instance.done(this, true);
            }
        };
        ViewPagerDownloadManager.USING_UI_PRELOADING = true;
    }

    @Test
    public void testInInitialPhase() throws Exception {

        List<CourseComponent> unitlist = new ArrayList<>();
        unitlist.add(prevComponent);
        unitlist.add(mainComponent);
        unitlist.add(nextComponent);
        ViewPagerDownloadManager.instance.setMainComponent(mainComponent, unitlist);

        assertTrue("InInitialPhase() op failed",  !ViewPagerDownloadManager.instance.inInitialPhase(prevComponent) );
        assertTrue("InInitialPhase() op failed",  !ViewPagerDownloadManager.instance.inInitialPhase(mainComponent) );
        assertTrue("InInitialPhase() op failed",  !ViewPagerDownloadManager.instance.inInitialPhase(nextComponent) );
        assertTrue("InInitialPhase() op failed",  !ViewPagerDownloadManager.instance.inInitialPhase(otherComponent) );

        ViewPagerDownloadManager.instance.addTask(prevComponentUI);
        assertTrue("InInitialPhase() op failed",  !ViewPagerDownloadManager.instance.inInitialPhase(otherComponent) );

        ViewPagerDownloadManager.instance.addTask(mainComponentUI);
        assertTrue("InInitialPhase() op failed",  !ViewPagerDownloadManager.instance.inInitialPhase(otherComponent) );

    }


    @Test
    public void testAddTask() throws Exception {

        List<CourseComponent> unitlist = new ArrayList<>();
        unitlist.add(prevComponent);
        unitlist.add(mainComponent);
        unitlist.add(nextComponent);
        ViewPagerDownloadManager.instance.setMainComponent(mainComponent, unitlist);

        ViewPagerDownloadManager.instance.addTask(prevComponentUI);
        assertTrue("addTask() op failed",  !ViewPagerDownloadManager.instance.isTaskIsRunning() );
        assertTrue("addTask() op failed",  ViewPagerDownloadManager.instance.numTaskInStack() == 0 );

        ViewPagerDownloadManager.instance.addTask(mainComponentUI);
        assertTrue("addTask() op failed for add main component",   ViewPagerDownloadManager.instance.numTaskInStack() == 0 );

        assertTrue("addTask() op failed for add main component",   !ViewPagerDownloadManager.instance.isTaskIsRunning() );

        ViewPagerDownloadManager.instance.addTask(nextComponentUI);
        assertTrue("addTask() op failed for add main component",   ViewPagerDownloadManager.instance.numTaskInStack() == 0 );


    }
}

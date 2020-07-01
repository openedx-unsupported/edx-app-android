package org.edx.mobile.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.JsonObject;

import org.edx.mobile.R;
import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.model.Filter;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.DiscussionBlockModel;
import org.edx.mobile.model.course.EncodedVideos;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.course.VideoData;
import org.edx.mobile.model.course.VideoInfo;
import org.edx.mobile.view.adapters.CourseUnitPagerAdapter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.android.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.edx.mobile.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@Ignore("Include this test once we have migrated to the androidX")
// TODO: To be fixed in LEARNER-7466
public class CourseUnitNavigationActivityTest extends CourseBaseActivityTest {
    /**
     * Method for defining the subclass of {@link CourseUnitNavigationActivity}
     * that is being tested. Should be overridden by subclasses.
     *
     * @return The {@link CourseUnitNavigationActivity} subclass that is being
     * tested
     */
    @Override
    protected Class<? extends CourseUnitNavigationActivity> getActivityClass() {
        return CourseUnitNavigationActivity.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Intent getIntent() {
        EnrolledCoursesResponse courseData;
        try {
            courseData = executeStrict(courseAPI.getEnrolledCourses()).get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String courseId = courseData.getCourse().getId();
        CourseStructureV1Model model;
        CourseComponent courseComponent;
        try {
            model = executeStrict(courseAPI.getCourseStructure(config.getApiUrlVersionConfig().getBlocksApiVersion(), courseId));
            courseComponent = (CourseComponent) CourseAPI.normalizeCourseStructure(model, courseId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<CourseComponent> leafComponents = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(leafComponents,
                EnumSet.allOf(BlockType.class));
        CourseComponent courseUnit = leafComponents.get(0);
        Intent intent = super.getIntent();
        Bundle extras = new Bundle();
        extras.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        extras.putString(Router.EXTRA_COURSE_COMPONENT_ID, courseUnit.getId());
        intent.putExtra(Router.EXTRA_BUNDLE, extras);
        return intent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean appliesPrevTransitionOnRestart() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void initializeTest() {
        super.initializeTest();

        Intent intent = getIntent();
        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass(), intent);
        CourseUnitNavigationActivity activity = controller.get();

        controller.create();
        assertNotNull(activity.findViewById(R.id.course_unit_nav_bar));
        View prev = activity.findViewById(R.id.goto_prev);
        assertNotNull(prev);
        assertThat(prev).isInstanceOf(TextView.class);
        TextView prevButton = (TextView) prev;
        View next = activity.findViewById(R.id.goto_next);
        assertNotNull(next);
        assertThat(next).isInstanceOf(TextView.class);
        TextView nextButton = (TextView) next;
        View prevUnitTitle = activity.findViewById(R.id.prev_unit_title);
        assertNotNull(prevUnitTitle);
        assertThat(prevUnitTitle).isInstanceOf(TextView.class);
        TextView prevUnitLabel = (TextView) prevUnitTitle;
        View nextUnitTitle = activity.findViewById(R.id.next_unit_title);
        assertNotNull(nextUnitTitle);
        assertThat(nextUnitTitle).isInstanceOf(TextView.class);
        TextView nextUnitLabel = (TextView) nextUnitTitle;
        View pager = activity.findViewById(R.id.pager2);
        assertNotNull(pager);
        assertThat(pager).isInstanceOf(ViewPager2.class);
        ViewPager2 viewPager = (ViewPager2) pager;
        RecyclerView.Adapter pagerAdapter = viewPager.getAdapter();
        assertNotNull(pagerAdapter);

        // Text navigation through units
        Bundle extras = intent.getBundleExtra(Router.EXTRA_BUNDLE);
        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse)
                extras.getSerializable(Router.EXTRA_COURSE_DATA);
        assertNotNull(courseData);
        String courseId = courseData.getCourse().getId();
        CourseStructureV1Model model;
        CourseComponent courseComponent;
        try {
            model = executeStrict(courseAPI.getCourseStructure(config.getApiUrlVersionConfig().getBlocksApiVersion(), courseId));
            courseComponent = (CourseComponent) CourseAPI.normalizeCourseStructure(model, courseId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertNotNull(courseComponent);
        final String unitId = extras.getString(Router.EXTRA_COURSE_COMPONENT_ID);
        assertNotNull(unitId);
        CourseComponent currentUnit = courseComponent.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent courseComponent) {
                return unitId.equals(courseComponent.getId());
            }
        });
        assertNotNull(currentUnit);
        // Since Robolectric current does not call the scroll callbacks due
        // to not supporting view drawing (see
        // https://github.com/robolectric/robolectric/issues/2007), we can't
        // test the ViewPager navigation at the moment.
        // TODO: Uncomment the following code when this issue is fixed
        /*List<CourseComponent> units = new ArrayList<>();
        courseComponent.fetchAllLeafComponents(units,
                EnumSet.allOf(BlockType.class));
        assertThat(units).isNotEmpty();
        controller.start().postCreate((Bundle)null).resume().visible();
        ListIterator<CourseComponent> unitIterator = units.listIterator(1);
        for (CourseComponent prevUnit = null;;) {
            int unitIndex = unitIterator.previousIndex();
            CourseComponent nextUnit = unitIterator.hasNext() ?
                    unitIterator.next() : null;
            verifyState(activity, unitIndex, currentUnit, prevUnit, nextUnit,
                    viewPager, pagerAdapter, prevButton, nextButton,
                    prevUnitLabel, nextUnitLabel);
            if (nextUnit == null) break;
            // The Scheduler needs to be paused while clicking the next button
            // to enable the FragmentStatePagerAdapter to clear it's transaction
            // state, and thus avoid the commit being called recursively
            Scheduler foregroundScheduler = ShadowApplication.getInstance()
                    .getForegroundThreadScheduler();
            foregroundScheduler.pause();
            assertTrue(nextButton.performClick());
            foregroundScheduler.unPause();
            prevUnit = currentUnit;
            currentUnit = nextUnit;
        }
        // Now iterate back in reverse order to test the previous button
        unitIterator = units.listIterator(units.size() - 1);
        for (CourseComponent nextUnit = null;;) {
            int unitIndex = unitIterator.nextIndex();
            CourseComponent prevUnit = unitIterator.hasPrevious() ?
                    unitIterator.previous() : null;
            verifyState(activity, unitIndex, currentUnit, prevUnit, nextUnit,
                    viewPager, pagerAdapter, prevButton, nextButton,
                    prevUnitLabel, nextUnitLabel);
            if (prevUnit == null) break;
            Scheduler foregroundScheduler = ShadowApplication.getInstance()
                    .getForegroundThreadScheduler();
            foregroundScheduler.pause();
            assertTrue(prevButton.performClick());
            foregroundScheduler.unPause();
            nextUnit = currentUnit;
            currentUnit = prevUnit;
        }*/
    }

    /**
     * Testing download progress menu visibility states and click behaviour
     * (starting DownloadActivity). Only when both AppConstants.offline_flag
     * is true and there is a downloading entry in the database, should the
     * progress bar be visible.
     */
    /**
     * Generic method for verifying the state of the CourseUnitNavigationActivity
     * at a specific unit.
     *
     * @param activity An instance of CourseUnitNavigationActivity that has been
     *                 initialized
     * @param unitIndex The index of the unit among all the leaves for the course
     * @param currentUnit The selected course unit
     * @param prevUnit The unit previous to the current selection
     * @param nextUnit The unit next to the current selection
     * @param viewPager The ViewPager instance containing the CourseUnitFragment
     *                  instances
     * @param pagerAdapter The PagerAdapter associated with the ViewPager
     * @param prevButton The button for going to the previous unit
     * @param nextButton The button for going to the next unit
     * @param prevUnitLabel The label for the previous unit
     * @param nextUnitLabel The label for the next unit
     */
    private void verifyState(CourseUnitNavigationActivity activity,
            int unitIndex, CourseComponent currentUnit,
            CourseComponent prevUnit, CourseComponent nextUnit,
            ViewPager2 viewPager, RecyclerView.Adapter pagerAdapter,
            TextView prevButton, TextView nextButton,
            TextView prevUnitLabel, TextView nextUnitLabel) {
        assertTitle(activity, currentUnit.getDisplayName());

        Class<? extends CourseUnitFragment> fragmentClass;
        if (currentUnit instanceof VideoBlockModel) {
            fragmentClass = BaseCourseUnitVideoFragment.class;
        } else if (!currentUnit.isMultiDevice() ){
            fragmentClass = CourseUnitMobileNotSupportedFragment.class;
        } else if (currentUnit.getType() != BlockType.VIDEO &&
                currentUnit.getType() != BlockType.HTML &&
                currentUnit.getType() != BlockType.OTHERS &&
                currentUnit.getType() != BlockType.DISCUSSION &&
                currentUnit.getType() != BlockType.PROBLEM ) {
            fragmentClass = CourseUnitEmptyFragment.class;
        } else if (currentUnit instanceof HtmlBlockModel) {
            fragmentClass = CourseUnitWebViewFragment.class;
        } else {
            fragmentClass = CourseUnitMobileNotSupportedFragment.class;
        }
        Object item = pagerAdapter.getItemId(unitIndex);
        assertNotNull(item);
        assertThat(item).isInstanceOf(fragmentClass);
        Bundle args = ((Fragment) item).getArguments();
        assertNotNull(args);
        assertEquals(currentUnit, args.getSerializable(
                Router.EXTRA_COURSE_UNIT));

        assertEquals(prevUnit != null, prevButton.isEnabled());
        assertEquals(nextUnit != null, nextButton.isEnabled());
        CourseComponent prevSection = prevUnit == null ?
                null : prevUnit.getParent();
        CourseComponent nextSection = nextUnit == null ?
                null : nextUnit.getParent();
        if (prevSection == null ||
                currentUnit.getParent().equals(prevSection)) {
            assertThat(prevUnitLabel).isNotVisible();
            assertThat(prevButton).hasText(R.string.assessment_previous);
        } else {
            assertThat(prevUnitLabel).isVisible();
            assertThat(prevUnitLabel).hasText(prevSection.getDisplayName());
        }
        if (nextSection == null ||
                currentUnit.getParent().equals(nextSection)) {
            assertThat(nextUnitLabel).isNotVisible();
            assertThat(nextButton).hasText(R.string.assessment_next);
        } else {
            assertThat(nextUnitLabel).isVisible();
            assertThat(nextUnitLabel).hasText(nextSection.getDisplayName());
        }
    }
    /**
     * Generic method for asserting proper setup for the current orientation
     *
     * @param activity The current activity
     */
    private void assertOrientationSetup(CourseUnitNavigationActivity activity) {
        boolean isLandscape = activity.getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_LANDSCAPE;
        ActionBar bar = activity.getSupportActionBar();
        View courseUnitNavBar = activity.findViewById(R.id.course_unit_nav_bar);
        assertNotNull(courseUnitNavBar);
        View pagerView = activity.findViewById(R.id.pager2);
        assertNotNull(pagerView);
        assertThat(pagerView).isInstanceOf(ViewPager2.class);
        assertEquals(true, (pagerView).isEnabled());
    }

    /**
     * Generic method for testing the setup for the specified orientation
     *
     * @param orientation The orientation to be tested
     */
    public void testOrientationSetup(int orientation) {
        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass(), getIntent());
        CourseUnitNavigationActivity activity = controller.get();
        activity.getResources().getConfiguration().orientation = orientation;
        controller.create();
        assertOrientationSetup(activity);
    }

    /**
     * Testing setup for different orientations
     */
    @Test
    public void orientationsSetupTest() {
        testOrientationSetup(Configuration.ORIENTATION_PORTRAIT);
        testOrientationSetup(Configuration.ORIENTATION_LANDSCAPE);
    }

    /**
     * Generic method for testing setup on orientation changes
     *
     * @param activity The current activity
     * @param orientation The orientation change to test
     */
    private void testOrientationChange(
            CourseUnitNavigationActivity activity, int orientation) {
        Configuration config = activity.getResources().getConfiguration();
        assertNotEquals(orientation, config.orientation);
        config.orientation = orientation;
        activity.onConfigurationChanged(config);
        assertOrientationSetup(activity);
    }

    /**
     * Testing orientation changes
     */
    @Test
    public void orientationChangeTest() {
        CourseUnitNavigationActivity activity =
                Robolectric.buildActivity(getActivityClass(), getIntent()).create().get();
        Configuration config = activity.getResources().getConfiguration();
        config.orientation = Configuration.ORIENTATION_LANDSCAPE;

        testOrientationChange(activity, Configuration.ORIENTATION_PORTRAIT);
        assertEquals(Configuration.ORIENTATION_PORTRAIT,
                activity.getResources().getConfiguration().orientation);
        assertOrientationSetup(activity);

        testOrientationChange(activity, Configuration.ORIENTATION_LANDSCAPE);
        assertEquals(Configuration.ORIENTATION_LANDSCAPE,
                activity.getResources().getConfiguration().orientation);
        assertOrientationSetup(activity);
    }

    /**
     * Testing creation of various fragments in the {@link CourseUnitNavigationActivity}'s
     * ViewPager, by supplying its {@link CourseUnitPagerAdapter} with all possible
     * {@link CourseComponent} models.
     */
    // FIXME: Enable this test once LEARNER-6713 is merged
    @Ignore
    @Test
    public void testUnitFragmentCreation() {
        EnrolledCoursesResponse courseData = Mockito.mock(EnrolledCoursesResponse.class);
        CourseUnitFragment.HasComponent hasComponent = Mockito.mock(CourseUnitFragment.HasComponent.class);

        List<CourseComponent> unitList = new ArrayList<>();
        List<Class<? extends CourseUnitFragment>> classesList = new ArrayList<>();

        VideoBlockModel encodeVideosModel = Mockito.mock(VideoBlockModel.class);
        VideoData videoData = Mockito.mock(VideoData.class);
        videoData.encodedVideos = Mockito.mock(EncodedVideos.class);
        when(videoData.encodedVideos.getPreferredVideoInfo())
                .thenReturn(Mockito.mock(VideoInfo.class));
        when(encodeVideosModel.getData()).thenReturn(videoData);
        unitList.add(encodeVideosModel);
        classesList.add(BaseCourseUnitVideoFragment.class);

        VideoBlockModel youtubeVideosModel = Mockito.mock(VideoBlockModel.class);
        VideoData videoData2 = Mockito.mock(VideoData.class);
        videoData2.encodedVideos = Mockito.mock(EncodedVideos.class);
        when(videoData2.encodedVideos.getYoutubeVideoInfo())
                .thenReturn(Mockito.mock(VideoInfo.class));
        when(youtubeVideosModel.getData()).thenReturn(videoData2);
        unitList.add(youtubeVideosModel);
        classesList.add(CourseUnitYoutubePlayerFragment.class);
        classesList.add(CourseUnitOnlyOnYoutubeFragment.class);
        DiscussionBlockModel discussionModel = Mockito.mock(DiscussionBlockModel.class);
        unitList.add(discussionModel);
        if (config.isDiscussionsEnabled()) {
            classesList.add(CourseUnitDiscussionFragment.class);
        } else {
            classesList.add(CourseUnitMobileNotSupportedFragment.class);
        }

        CourseComponent nonMultiDeviceModel = Mockito.mock(CourseComponent.class);
        when(nonMultiDeviceModel.isMultiDevice()).thenReturn(false);
        unitList.add(nonMultiDeviceModel);
        classesList.add(CourseUnitMobileNotSupportedFragment.class);

        HtmlBlockModel htmlModel = Mockito.mock(HtmlBlockModel.class);
        when(htmlModel.isMultiDevice()).thenReturn(true);
        when(htmlModel.getType()).thenReturn(BlockType.HTML);
        unitList.add(htmlModel);
        classesList.add(CourseUnitWebViewFragment.class);

        CourseComponent unknownModel = Mockito.mock(CourseComponent.class);
        when(unknownModel.isMultiDevice()).thenReturn(true);
        when(unknownModel.getType()).thenReturn(BlockType.COURSE);
        unitList.add(unknownModel);
        classesList.add(CourseUnitEmptyFragment.class);

        CourseComponent problemModel = Mockito.mock(CourseComponent.class);
        when(problemModel.isMultiDevice()).thenReturn(true);
        when(problemModel.getType()).thenReturn(BlockType.PROBLEM);
        unitList.add(problemModel);
        classesList.add(CourseUnitMobileNotSupportedFragment.class);

        CourseComponent othersModel = Mockito.mock(CourseComponent.class);
        when(othersModel.isMultiDevice()).thenReturn(true);
        when(othersModel.getType()).thenReturn(BlockType.OTHERS);
        unitList.add(othersModel);
        classesList.add(CourseUnitMobileNotSupportedFragment.class);

        ActivityController<? extends CourseUnitNavigationActivity> controller =
                Robolectric.buildActivity(getActivityClass(), getIntent());

        CourseUnitPagerAdapter adapter = new CourseUnitPagerAdapter(controller.get(), config,
                unitList, courseData, null, hasComponent);

        for (int size = unitList.size(), i = 0; i < size; i++) {
            assertThat(adapter.getItem(i)).isInstanceOf(classesList.get(i));
        }
    }

    /**
     * Tests creation of various fragments in the {@link CourseUnitNavigationActivity}'s
     * ViewPager, by supplying its {@link CourseUnitPagerAdapter} with a single
     * {@link CourseComponent} model at a time via parameterization.
     */
    // TODO: Robolectric doesn't have a Gradle-based parameterized test suite, need to implement it.
    @Ignore
    @RunWith(Parameterized.class)
    public static class ParameterizedFragmentCreation extends UiTest {
        private static final String DISCUSSIONS_ENABLED = "DISCUSSIONS_ENABLED";

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            List<Object[]> argsList = new ArrayList<>();
            {
                VideoBlockModel encodedVideosModel = Mockito.mock(VideoBlockModel.class);
                VideoData videoData = Mockito.mock(VideoData.class);
                videoData.encodedVideos = Mockito.mock(EncodedVideos.class);
                when(videoData.encodedVideos.getPreferredVideoInfo())
                        .thenReturn(Mockito.mock(VideoInfo.class));
                when(encodedVideosModel.getData()).thenReturn(videoData);
                argsList.add(new Object[] {encodedVideosModel, BaseCourseUnitVideoFragment.class, true});

                VideoBlockModel youtubeVideosModel = Mockito.mock(VideoBlockModel.class);
                VideoData videoData2 = Mockito.mock(VideoData.class);
                videoData2.encodedVideos = Mockito.mock(EncodedVideos.class);
                when(videoData2.encodedVideos.getYoutubeVideoInfo())
                        .thenReturn(Mockito.mock(VideoInfo.class));
                when(youtubeVideosModel.getData()).thenReturn(videoData2);
                argsList.add(new Object[] {youtubeVideosModel, CourseUnitOnlyOnYoutubeFragment.class, true});

                DiscussionBlockModel discussionModel = Mockito.mock(DiscussionBlockModel.class);
                argsList.add(new Object[] {discussionModel, CourseUnitDiscussionFragment.class, true});
                argsList.add(new Object[] {discussionModel, CourseUnitMobileNotSupportedFragment.class, false});

                CourseComponent nonMultiDeviceModel = Mockito.mock(CourseComponent.class);
                when(nonMultiDeviceModel.isMultiDevice()).thenReturn(false);
                argsList.add(new Object[] {nonMultiDeviceModel, CourseUnitMobileNotSupportedFragment.class, true});

                HtmlBlockModel htmlModel = Mockito.mock(HtmlBlockModel.class);
                when(htmlModel.isMultiDevice()).thenReturn(true);
                when(htmlModel.getType()).thenReturn(BlockType.HTML);
                argsList.add(new Object[] {htmlModel, CourseUnitWebViewFragment.class, true});

                CourseComponent unknownModel = Mockito.mock(CourseComponent.class);
                when(unknownModel.isMultiDevice()).thenReturn(true);
                when(unknownModel.getType()).thenReturn(BlockType.COURSE);
                argsList.add(new Object[] {unknownModel, CourseUnitEmptyFragment.class, true});

                CourseComponent problemModel = Mockito.mock(CourseComponent.class);
                when(problemModel.isMultiDevice()).thenReturn(true);
                when(problemModel.getType()).thenReturn(BlockType.PROBLEM);
                argsList.add(new Object[] {problemModel, CourseUnitMobileNotSupportedFragment.class, true});

                CourseComponent othersModel = Mockito.mock(CourseComponent.class);
                when(othersModel.isMultiDevice()).thenReturn(true);
                when(othersModel.getType()).thenReturn(BlockType.OTHERS);
                argsList.add(new Object[] {othersModel, CourseUnitMobileNotSupportedFragment.class, true});
            }

            return argsList;
        }

        /**
         * The {@link CourseComponent} that we provide to the {@link CourseUnitPagerAdapter} as input.
         */
        @Parameterized.Parameter
        public CourseComponent paramCourseComponent;

        /**
         * The {@link Fragment} that we expect the {@link CourseUnitPagerAdapter} will create an
         * instance of.
         */
        @Parameterized.Parameter(value = 1)
        public Class expectedFragmentClass;

        /**
         * A flag denoting whether inline discussions should be enabled in the config, that is
         * provided as a parameter.
         */
        @Parameterized.Parameter(value = 2)
        public boolean isDiscussionsEnabled;

        @Test
        public void test() throws IOException {
            config = new org.edx.mobile.util.Config(generateConfigProperties());
            EnrolledCoursesResponse courseData = Mockito.mock(EnrolledCoursesResponse.class);
            CourseUnitFragment.HasComponent hasComponent = Mockito.mock(CourseUnitFragment.HasComponent.class);

            CourseUnitPagerAdapter adapter = new CourseUnitPagerAdapter(
                    Robolectric.buildActivity(CourseUnitNavigationActivity.class).get(), config,
                    Collections.singletonList(paramCourseComponent), courseData, null, hasComponent);

            assertThat(adapter.getItem(0)).isInstanceOf(expectedFragmentClass);
        }

        @Override
        protected JsonObject generateConfigProperties() throws IOException {
            JsonObject properties = super.generateConfigProperties();
            properties.addProperty(DISCUSSIONS_ENABLED, isDiscussionsEnabled);
            return properties;
        }
    }
}

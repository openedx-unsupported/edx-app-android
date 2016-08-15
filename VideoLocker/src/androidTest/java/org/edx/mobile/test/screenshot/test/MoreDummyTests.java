package org.edx.mobile.test.screenshot.test;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.facebook.testing.screenshot.Screenshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentCourseDashboardBinding;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.view.CourseDashboardActivity;
import org.edx.mobile.view.LaunchActivity;
import org.edx.mobile.view.LoginActivity;
import org.edx.mobile.view.Router;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class MoreDummyTests {

    @Rule
    public ActivityTestRule<CourseDashboardActivity> mActivityRule =
            new ActivityTestRule<CourseDashboardActivity>(CourseDashboardActivity.class) {
                @Override
                protected Intent getActivityIntent() {
                    Context targetContext = InstrumentationRegistry.getInstrumentation()
                            .getTargetContext();


                    try {
                        TypeToken<ArrayList<EnrolledCoursesResponse>> t = new TypeToken<ArrayList<EnrolledCoursesResponse>>() {
                        };

                        Gson gson = new GsonBuilder().create();

                        ArrayList<EnrolledCoursesResponse> list = gson.fromJson("[{\"created\":\"2016-04-01T20:28:39.551479Z\",\"mode\":\"audit\",\"is_active\":true,\"course\":{\"courseware_access\":{\"has_access\":true,\"error_code\":null,\"developer_message\":null,\"user_message\":null},\"start_type\":\"timestamp\",\"end\":\"2030-12-31T23:30:00Z\",\"name\":\"Testing Course\",\"course_about\":\"https://mobile-devi.sandbox.edx.org/courses/course-v1:edX+Test101+course/about\",\"media\":{\"course_image\":{\"uri\":\"/asset-v1:edX+Test101+course+type@asset+block@demo_course_image.jpg\",\"name\":\"Course Image\"}},\"course_updates\":\"https://mobile-devi.sandbox.edx.org/api/mobile/v0.5/course_info/course-v1:edX+Test101+course/updates\",\"number\":\"Test101\",\"course_image\":\"/asset-v1:edX+Test101+course+type@asset+block@demo_course_image.jpg\",\"start\":\"2015-10-01T00:30:00Z\",\"start_display\":\"Oct. 1, 2015\",\"course_handouts\":\"https://mobile-devi.sandbox.edx.org/api/mobile/v0.5/course_info/course-v1:edX+Test101+course/handouts\",\"org\":\"edX\",\"subscription_id\":\"course_MNXXK4TTMUWXMMJ2MVSFQK2UMVZXIMJQGEVWG33VOJZWK___\",\"video_outline\":\"https://mobile-devi.sandbox.edx.org/api/mobile/v0.5/video_outlines/courses/course-v1:edX+Test101+course\",\"discussion_url\":\"https://mobile-devi.sandbox.edx.org/api/discussion/v1/courses/course-v1:edX+Test101+course\",\"id\":\"course-v1:edX+Test101+course\"},\"certificate\":{}}]"
                                ,
                                t.getType());
                        EnrolledCoursesResponse model = list.get(0);

                        Bundle courseBundle = new Bundle();
                        courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, model);
                        courseBundle.putBoolean(Router.EXTRA_ANNOUNCEMENTS, false);
                        Intent courseDashboard = new Intent(targetContext, CourseDashboardActivity.class);
                        courseDashboard.putExtra(Router.EXTRA_BUNDLE, courseBundle);
                        courseDashboard.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                        return courseDashboard;
                    } catch (Exception e) {
                        System.out.println(e);

                    }
                    return new Intent(targetContext, CourseDashboardActivity.class);
                }
            };

    @Test
    public void testScreenshot_recordLaunchActivity() throws Throwable {
        View view = mActivityRule.getActivity().findViewById(R.id.ughhhh);
        Screenshot.snap(view).record();
    }
}

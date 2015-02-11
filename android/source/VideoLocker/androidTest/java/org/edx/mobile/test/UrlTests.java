package org.edx.mobile.test;

import android.net.Uri;

import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shahid on 10/2/15.
 */
public class UrlTests extends BaseTestCase {

    public void testReadUrlPaths() throws Exception {
        String url = "edxapp://enroll/course_id=HarveyMuddX/CS002x/1T2015&email_opt_in=true";

        String[] params = url.replace(URLInterceptorWebViewClient.URL_TYPE_ENROLL, "").split("&");

        Map<String, String> queryParams = new HashMap<>();
        for (String q : params) {
            String[] parts = q.split("=");
            queryParams.put(parts[0], parts[1]);
        }

        String courseId = queryParams.get("course_id");
        assertEquals("HarveyMuddX/CS002x/1T2015", courseId);
        boolean emailOptIn = Boolean.parseBoolean(queryParams.get("email_opt_in"));
        assertTrue(emailOptIn);
    }

    public void testCourseInfoPaths() throws Exception {
        String url = "edxapp://view_course/course_path=course/introduction-environmental-science-dartmouthx-dart-envs-01-x";

        String pathId = url.replace(URLInterceptorWebViewClient.URL_TYPE_COURSE_INFO, "").trim();

        assertNotNull(pathId);
        assertEquals("introduction-environmental-science-dartmouthx-dart-envs-01-x", pathId);
        print("Path Id - "+pathId);
    }
}

package org.edx.mobile.test;

import android.net.Uri;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by shahid on 10/2/15.
 */
public class UrlTests extends BaseTestCase {

    @Test
    public void testReadUrlPaths() throws Exception {
        String url = "edxapp://enroll?course_id=course-v1:BerkeleyX+GG101x-2+1T2015&email_opt_in=true";

        Uri uri = Uri.parse(url);
        String query = uri.getQuery().trim();
        String[] params = query.split("&");
        Map<String, String> queryParams = new HashMap<>();
        for (String q : params) {
            String[] parts = q.split("=");
            queryParams.put(parts[0], parts[1]);
        }

        String courseId = queryParams.get("course_id");
        assertEquals("course-v1:BerkeleyX+GG101x-2+1T2015", courseId);
        boolean emailOptIn = Boolean.parseBoolean(queryParams.get("email_opt_in"));
        assertTrue(emailOptIn);
    }

    @Test
    public void testCourseInfoPaths() throws Exception {
        String url = "edxapp://course_info?path_id=course/cosmology-anux-anu-astro4x";

        String pathId = Uri.parse(url).getQueryParameter(URLInterceptorWebViewClient.PARAM_PATH_ID);
        if(pathId.startsWith(URLInterceptorWebViewClient.COURSE)){
            pathId = pathId.replaceFirst(URLInterceptorWebViewClient.COURSE,"").trim();
        }

        assertNotNull(pathId);
        assertEquals("cosmology-anux-anu-astro4x", pathId);
    }
}

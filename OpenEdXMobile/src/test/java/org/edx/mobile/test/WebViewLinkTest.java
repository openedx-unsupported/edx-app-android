package org.edx.mobile.test;

import android.net.Uri;

import org.edx.mobile.util.links.WebViewLink;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WebViewLinkTest extends BaseTestCase {
    @Test
    public void testWebViewLinkCorrectlyParsesCourseIdWithEmailOptIn() {
        final String courseId = "course-v1:BerkeleyX+GG101x-2+1T2015";
        final boolean emailOptIn = true;
        final Uri uri = new Uri.Builder()
                .scheme(WebViewLink.SCHEME)
                .authority(WebViewLink.Authority.ENROLL.getKey())
                .appendQueryParameter(WebViewLink.Param.COURSE_ID, courseId)
                .appendQueryParameter(WebViewLink.Param.EMAIL_OPT, String.valueOf(emailOptIn))
                .build();
        final WebViewLink link = WebViewLink.parse(uri.toString());
        assertNotNull(link);
        assertNotNull(link.params);
        assertEquals(courseId, link.params.get(WebViewLink.Param.COURSE_ID));
        assertEquals(emailOptIn, Boolean.valueOf(link.params.get(WebViewLink.Param.EMAIL_OPT)));
    }

    @Test
    public void testWebViewLinkParsesCourseIdAndRemovesCoursePrefix() {
        final String courseId = "cosmology-anux-anu-astro4x";
        final Uri uri = new Uri.Builder()
                .scheme(WebViewLink.SCHEME)
                .authority(WebViewLink.Authority.COURSE_INFO.getKey())
                .appendQueryParameter(WebViewLink.Param.PATH_ID,
                        WebViewLink.PATH_ID_COURSE_PREFIX + courseId)
                .build();
        final WebViewLink link = WebViewLink.parse(uri.toString());
        assertNotNull(link);
        assertNotNull(link.params);
        assertEquals(courseId, link.params.get(WebViewLink.Param.PATH_ID));
    }

    /**
     * Tests our workaround for edx.org failing to encode plus signs in the course_id parameter
     * See https://openedx.atlassian.net/browse/MA-1901
     */
    @Test
    public void testPlusSignsPreservedInEnrollLinks() {
        final String courseIdWithPlusSign = "course+id";

        // Not using Uri.Builder because we don't want the plus signs in the course_id parameter encoded
        final String uri = WebViewLink.SCHEME
                + "://" + WebViewLink.Authority.ENROLL.getKey()
                + "?" + WebViewLink.Param.COURSE_ID + "=" + courseIdWithPlusSign;

        final WebViewLink link = WebViewLink.parse(uri);
        assertNotNull(link);
        assertNotNull(link.params);
        assertEquals(courseIdWithPlusSign, link.params.get(WebViewLink.Param.COURSE_ID));
    }

    @Test
    public void testWebViewLinkParsesEnrollCourseInfo() {
        final String courseId = "course-v1:HKUSTx+COMP102.1x+1T2018";
        final Uri uri = new Uri.Builder()
                .scheme(WebViewLink.SCHEME)
                .authority(WebViewLink.Authority.ENROLLED_COURSE_INFO.getKey())
                .appendQueryParameter(WebViewLink.Param.COURSE_ID, courseId)
                .build();
        final WebViewLink link = WebViewLink.parse(uri.toString());
        assertNotNull(link);
        assertNotNull(link.params);
        assertEquals(courseId, link.params.get(WebViewLink.Param.COURSE_ID));
    }

    @Test
    public void testWebViewLinkParsesProgramInfo() {
        final String pathId = "programs/fa4e4674-7d35-41fe-ad10-9ddefefa5bc5/details_fragment/";
        final Uri uri = new Uri.Builder()
                .scheme(WebViewLink.SCHEME)
                .authority(WebViewLink.Authority.ENROLLED_PROGRAM_INFO.getKey())
                .appendQueryParameter(WebViewLink.Param.PATH_ID, pathId)
                .build();
        final WebViewLink link = WebViewLink.parse(uri.toString());
        assertNotNull(link);
        assertNotNull(link.params);
        assertEquals(pathId, link.params.get(WebViewLink.Param.PATH_ID));
    }

    @Test
    public void testWebViewLinkParsesProgramDiscovery() {
        final Uri uri = new Uri.Builder()
                .scheme(WebViewLink.SCHEME)
                .authority(WebViewLink.Authority.COURSE.getKey())
                .appendQueryParameter(WebViewLink.Param.PROGRAMS, "")
                .build();
        final WebViewLink link = WebViewLink.parse(uri.toString());
        assertNotNull(link);
        assertNotNull(link.params);
        assertTrue(link.params.containsKey(WebViewLink.Param.PROGRAMS));
    }
}

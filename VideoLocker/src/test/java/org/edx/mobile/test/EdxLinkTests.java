package org.edx.mobile.test;

import android.net.Uri;

import org.edx.mobile.util.links.EdxCourseInfoLink;
import org.edx.mobile.util.links.EdxEnrollLink;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EdxLinkTests extends BaseTestCase {
    @Test
    public void testEdxEnrollLinkCorrectlyParsesCourseIdWithEmailOptIn() {
        final String courseId = "course-v1:BerkeleyX+GG101x-2+1T2015";
        final boolean emailOptIn = true;
        final Uri uri = new Uri.Builder()
                .scheme(EdxEnrollLink.SCHEME)
                .authority(EdxEnrollLink.AUTHORITY)
                .appendQueryParameter(EdxEnrollLink.COURSE_ID_PARAMETER_NAME, courseId)
                .appendQueryParameter(EdxEnrollLink.EMAIL_OPT_IN_PARAMETER_NAME, String.valueOf(emailOptIn))
                .build();
        final EdxEnrollLink link = EdxEnrollLink.parse(uri.toString());
        assertNotNull(link);
        assertEquals(courseId, link.courseId);
        assertEquals(emailOptIn, link.emailOptIn);
    }

    @Test
    public void testEdxCourseInfoLinkParsesCourseIdAndRemovesCoursePrefix() {
        final String courseId = "cosmology-anux-anu-astro4x";
        final Uri uri = new Uri.Builder()
                .scheme(EdxCourseInfoLink.SCHEME)
                .authority(EdxCourseInfoLink.AUTHORITY)
                .appendQueryParameter(EdxCourseInfoLink.PATH_ID_PARAMETER_NAME,
                        EdxCourseInfoLink.PATH_ID_COURSE_PREFIX + courseId)
                .build();
        final EdxCourseInfoLink link = EdxCourseInfoLink.parse(uri.toString());
        assertNotNull(link);
        assertEquals(courseId, link.pathId);
    }

    /**
     * Tests our workaround for edx.org failing to encode plus signs in the course_id parameter
     * See https://openedx.atlassian.net/browse/MA-1901
     */
    @Test
    public void testPlusSignsPreservedInEnrollLinks() {
        final String courseIdWithPlusSign = "course+id";

        // Not using Uri.Builder because we don't want the plus signs in the course_id parameter encoded
        final String uri = EdxEnrollLink.SCHEME
                + "://" + EdxEnrollLink.AUTHORITY
                + "?" + EdxEnrollLink.COURSE_ID_PARAMETER_NAME + "=" + courseIdWithPlusSign;

        final EdxEnrollLink link = EdxEnrollLink.parse(uri);
        assertNotNull(link);
        assertEquals(courseIdWithPlusSign, link.courseId);
    }
}

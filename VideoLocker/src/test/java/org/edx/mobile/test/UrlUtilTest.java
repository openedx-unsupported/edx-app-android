package org.edx.mobile.test;

import org.junit.Test;
import static org.junit.Assert.*;

import org.edx.mobile.util.UrlUtil;

public class UrlUtilTest extends BaseTestCase {

    @Test
    public void testRelativeUrlResolves() {
        String result = UrlUtil.makeAbsolute("/foo/bar", "http://example.com");
        assertEquals(result, "http://example.com/foo/bar");
    }

    @Test
    public void testAbsoluteURLNotChanged() {
        String result = UrlUtil.makeAbsolute("http://somedomain.com/foo/bar", "http://otherdomain.com");
        assertEquals(result, "http://somedomain.com/foo/bar");
    }

    @Test
    public void testMalformedURLReturnsNull() {
        String result = UrlUtil.makeAbsolute("/somepath", "@:");
        assertNull(result);
    }

    @Test
    public void testNullInputGivesNullOutput() {
        assertNull(UrlUtil.makeAbsolute(null, "http://otherdomain.com"));
        assertNull(UrlUtil.makeAbsolute("http://otherdomain.com", null));
    }
}

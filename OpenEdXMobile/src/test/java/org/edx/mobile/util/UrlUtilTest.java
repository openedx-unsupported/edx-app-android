package org.edx.mobile.util;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.test.BaseTestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UrlUtilTest extends BaseTestCase {
    private final Logger logger = new org.edx.mobile.logger.Logger(getClass().getName());

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

    @Test
    public void test_UrlBuilder() {
        String baseURL = "http://www.fakex.com/course";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search_query", "mobile linux");

        String expected = "http://www.fakex.com/course?search_query=mobile%20linux";
        String output = UrlUtil.buildUrlWithQueryParams(logger, baseURL, queryParams);
        assertEquals(expected, output);
    }

    @Test
    public void test_UrlBuilder_WithMulipleQueries() {
        String baseURL = "http://www.fakex.com/course";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search_query", "mobile linux");
        queryParams.put("subject", "Architecture");

        String expected = "http://www.fakex.com/course?subject=Architecture&search_query=mobile%20linux";
        String output = UrlUtil.buildUrlWithQueryParams(logger, baseURL, queryParams);
        assertEquals(expected, output);
    }

    @Test
    public void test_UrlBuilder_AlreadyHasQuery() {
        String baseURL = "http://www.fakex.com/course?type=mobile";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search_query", "mobile linux");

        String expected = "http://www.fakex.com/course?type=mobile&search_query=mobile%20linux";
        String output = UrlUtil.buildUrlWithQueryParams(logger, baseURL, queryParams);
        assertEquals(expected, output);
    }

    @Test
    public void test_UrlBuilder_AlreadyHasQuery_WithMultipleQueries() {
        String baseURL = "http://www.fakex.com/course?type=mobile";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("search_query", "mobile linux");
        queryParams.put("subject", "Architecture");

        String expected = "http://www.fakex.com/course?type=mobile&subject=Architecture&search_query=mobile%20linux";
        String output = UrlUtil.buildUrlWithQueryParams(logger, baseURL, queryParams);
        assertEquals(expected, output);
    }
}

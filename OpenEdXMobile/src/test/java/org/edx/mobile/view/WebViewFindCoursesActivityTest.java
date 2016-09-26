package org.edx.mobile.view;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WebViewFindCoursesActivityTest extends UiTest {

    @Test
    public void test_SearchQueryBuilder() {
        String baseURL = "http://www.fakex.com/course";
        String queryString = "mobile linux";

        String expected = "http://www.fakex.com/course?search_query=mobile+linux";
        String output = WebViewFindCoursesActivity.buildQuery(baseURL, queryString, null);
        assertEquals(expected, output);
    }


    @Test
    public void test_SearchQueryBuilder_AlreadyHasQuery() {
        String baseURL = "http://www.fakex.com/course?type=mobile";
        String queryString = "mobile linux";

        String expected = "http://www.fakex.com/course?type=mobile&search_query=mobile+linux";
        String output = WebViewFindCoursesActivity.buildQuery(baseURL, queryString, null);
        assertEquals(expected, output);
    }

}

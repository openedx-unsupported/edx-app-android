package org.edx.mobile.test.feature.data;

import com.google.gson.Gson;

import org.edx.mobile.authentication.AuthResponse;
import org.edx.mobile.course.CourseDetail;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.PaginationData;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.ProfileModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static java.util.Arrays.asList;

public enum TestValues {
    ;

    public static final Credentials ACTIVE_USER_CREDENTIALS = new Credentials("honor@example.com", "edx");
    public static final ProfileModel DUMMY_PROFILE = new Gson().fromJson("{\"id\": 1, \"username\": \"user\", \"email\": \"email@example.com\", \"name\": \"name\", \"course_enrollments\": \"https://mobile-devi.sandbox.edx.org/api/mobile/v0.5/users/staff/course_enrollments/\"}", ProfileModel.class);
    public static final AuthResponse INVALID_AUTH_TOKEN_RESPONSE = new Gson().fromJson("{\"access_token\": \"I am an invalid token\", \"token_type\": \"Bearer\", \"expires_in\": 2591999, \"scope\": \"\",\"error\":\"true\"}", AuthResponse.class);
    public static final AuthResponse VALID_AUTH_TOKEN_RESPONSE = new Gson().fromJson("{\"access_token\": \"validtoken\", \"token_type\": \"Bearer\", \"expires_in\": 2591999, \"scope\": \"\"}", AuthResponse.class);

    private static final List<CourseDetail> clist =
            new ArrayList<CourseDetail>(asList (
                new Gson().fromJson(
                        "{ \"course_id\": \"EDX/EDX101/NOW\"," +
                                "\"name\": \"EdX Test Course 1\"," +
                                "\"number\": \"EDX101\"," +
                                "\"org\": \"EDX\"," +
                                "\"short_description\": \"\"," +
                                "\"effort\": null," +
                                "\"media\": {" +
                                "\"course_image\": {" +
                                "\"uri\": \"/c4x/EDX/EDX101/asset/images_course_image.jpg\"" +
                                "}," +
                                "\"course_video\": {" +
                                "\"uri\": null" +
                                "}" +
                                "}," +
                                "\"start\": \"2015-01-01T00:00:00Z\"," +
                                "\"start_type\": \"timestamp\"," +
                                "\"start_display\": \"1 janvier 2015\"," +
                                "\"end\": null," +
                                "\"enrollment_start\": null," +
                                "\"enrollment_end\": null," +
                                "\"blocks_url\": \"https://edx.org/api/courses/v1/blocks/?course_id=EDX%2FEDX101%2FNOW\"" +
                                "}",
                        CourseDetail.class),
                new Gson().fromJson(
                        "{ \"course_id\": \"EDX/EDX102/NOW\"," +
                                "\"name\": \"EdX Test Course 2\"," +
                                "\"number\": \"EDX102\"," +
                                "\"org\": \"EDX\"," +
                                "\"short_description\": \"\"," +
                                "\"effort\": null," +
                                "\"media\": {" +
                                "\"course_image\": {" +
                                "\"uri\": \"/c4x/EDX/EDX102/asset/images_course_image.jpg\"" +
                                "}," +
                                "\"course_video\": {" +
                                "\"uri\": null" +
                                "}" +
                                "}," +
                                "\"start\": \"2016-01-01T00:00:00Z\"," +
                                "\"start_type\": \"timestamp\"," +
                                "\"start_display\": \"1 janvier 2016\"," +
                                "\"end\": null," +
                                "\"enrollment_start\": null," +
                                "\"enrollment_end\": null," +
                                "\"blocks_url\": \"https://edx.org/api/courses/v1/blocks/?course_id=EDX%2FEDX102%2FNOW\"" +
                                "}",
                        CourseDetail.class)
            ));

    public static final Page<CourseDetail> BASIC_COURSE_LIST = new Page<CourseDetail>(
            new PaginationData(1,1,  null,null),
            clist);

    public static final List<EnrolledCoursesResponse> BASIC_USER_ENROLLED_COURSES =
            new ArrayList<EnrolledCoursesResponse>(
                    asList (new Gson().fromJson(" {\n" +
                                    "        \"created\": \"2016-01-01T00:00:00Z\",\n" +
                                    "        \"mode\": \"honor\",\n" +
                                    "        \"is_active\": true,\n" +
                                    "        \"course\": {\n" +
                                    "            \"id\": \"EDX/EDX102/NOW\",\n" +
                                    "            \"name\": \"EdX Test Course 2\"\n" +
                                    "        },\n" +
                                    "        \"user\": \"honor\"\n" +
                                    "    }",
                            EnrolledCoursesResponse.class))
            );
}

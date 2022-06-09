package org.edx.mobile.feature.mock

import com.google.gson.Gson
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.edx.mobile.feature.data.TestValues
import org.edx.mobile.util.MockDataUtil

class MockServerDispatcher {

    /**
     * Return error response from mock server
     */
    internal inner class ResponseDispatcher : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val inValidToken = String.format(
                "%s %s",
                TestValues.INVALID_AUTH_TOKEN_RESPONSE.token_type,
                TestValues.INVALID_AUTH_TOKEN_RESPONSE.access_token
            )
            return if (request.requestUrl.toString().contains("/course_enrollments") &&
                request.headers["Authorization"] == inValidToken
            ) {
                MockResponse().setResponseCode(401)
            } else if (request.requestUrl.toString().contains("/oauth2/access_token/")) {
                MockResponse().setBody(MockDataUtil.getMockResponse("valid_profile"))
                    .setResponseCode(200)
            } else if (request.requestUrl.toString().contains("/my_user_info")) {
                MockResponse().setBody(Gson().toJson(TestValues.DUMMY_PROFILE))
                    .setResponseCode(200)
            } else if (request.requestUrl.toString().contains("/account/registration/")
            ) {
                if (request.method.equals("GET", true)) {
                    MockResponse().setBody(MockDataUtil.getMockResponse("registration_form"))
                        .setResponseCode(200)
                } else {
                    MockResponse().setBody(MockDataUtil.getMockResponse("valid_profile"))
                        .setResponseCode(200)
                }
            } else if (request.method.equals("POST", true) && request.requestUrl.toString()
                    .contains("validation/registration")
            ) {
                MockResponse().setBody(MockDataUtil.getMockResponse("validate_registration_form"))
                    .setResponseCode(200)
            } else if (request.method.equals("GET", true) && request.requestUrl.toString()
                    .contains("/api/mobile/v0.5/my_user_info")
            ) {
                MockResponse().setBody(MockDataUtil.getMockResponse("dummy_profile"))
                    .setResponseCode(200)
            } else {
                MockResponse().setResponseCode(200)
            }
        }
    }
}

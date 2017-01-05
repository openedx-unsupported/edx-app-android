package org.edx.mobile.test.http;

import com.google.gson.Gson;

import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.PaginationData;
import org.edx.mobile.profiles.BadgeAssertion;
import org.edx.mobile.profiles.BadgeClass;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.user.UserService;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.edx.mobile.http.util.CallUtil.executeStrict;
import static org.junit.Assert.assertEquals;

public class UserApiTest extends BaseTestCase {

    private BadgeAssertion getTestBadge() {
        return new BadgeAssertion("some user", "http://example.com/evidence", "http://example.com/image.jpg", new Date(),
                new BadgeClass(
                        "someslug", "some component", "A badge!", "A badge you get for stuff", "http://example.com/image.jpg", "somecourse"
                )
        );
    }

    private String getTestBadgeString() {
        Page<BadgeAssertion> response = new Page<>(new PaginationData(1, 1, null, null), Collections.singletonList(getTestBadge()));
        return new Gson().toJson(response);
    }

    @Test
    public void testApiReturnsResult() throws IOException, HttpStatusException {
        MockWebServer server = new MockWebServer();

        Retrofit retrofit = new Retrofit.Builder()
                .client(new OkHttpClient())
                .baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        server.enqueue(new MockResponse().setBody(this.getTestBadgeString()));

        UserService service = retrofit.create(UserService.class);
        Page<BadgeAssertion> badges = executeStrict(service.getBadges("user", 1));

        assertEquals(badges.getResults().size(), 1);
    }
}

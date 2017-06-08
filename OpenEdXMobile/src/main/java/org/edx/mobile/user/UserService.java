package org.edx.mobile.user;

import com.google.inject.Inject;

import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.model.Page;
import org.edx.mobile.profiles.BadgeAssertion;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static org.edx.mobile.http.constants.ApiConstants.PARAM_PAGE_SIZE;

public interface UserService {
    /**
     * A RoboGuice Provider implementation for UserService.
     */
    class Provider implements com.google.inject.Provider<UserService> {
        @Inject
        private RetrofitProvider retrofitProvider;

        @Override
        public UserService get() {
            return retrofitProvider.getWithOfflineCache().create(UserService.class);
        }
    }

    @GET("/api/user/v1/accounts/{username}")
    Call<Account> getAccount(@Path("username") String username);

    @Headers("Cache-Control: no-cache")
    @PATCH("/api/user/v1/accounts/{username}")
    Call<Account> updateAccount(@Path("username") String username, @Body Map<String, Object> fields);

    @Headers("Cache-Control: no-cache")
    @POST("/api/user/v1/accounts/{username}/image")
    Call<ResponseBody> setProfileImage(@Path("username") String username, @Header("Content-Disposition") String contentDisposition, @Body RequestBody file);

    @Headers("Cache-Control: no-cache")
    @DELETE("/api/user/v1/accounts/{username}/image")
    Call<ResponseBody> deleteProfileImage(@Path("username") String username);

    @GET("/api/badges/v1/assertions/user/{username}?" + PARAM_PAGE_SIZE)
    Call<Page<BadgeAssertion>> getBadges(@Path("username") String username,
                                         @Query("page") int page);
}

package org.edx.mobile.user;

import static org.edx.mobile.http.constants.ApiConstants.PARAM_PAGE_SIZE;

import androidx.annotation.NonNull;

import org.edx.mobile.http.provider.RetrofitProvider;
import org.edx.mobile.model.Page;
import org.edx.mobile.model.profile.BadgeAssertion;
import org.edx.mobile.model.user.Account;

import java.util.Map;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
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

public interface UserService {
    /**
     * A Provider implementation for UserService.
     */
    @Module
    @InstallIn(SingletonComponent.class)
    class Provider {

        @Singleton
        @Provides
        public UserService get(@NonNull RetrofitProvider retrofitProvider) {
            return retrofitProvider.getWithOfflineCache().create(UserService.class);
        }
    }

    @GET("/api/user/v1/accounts/{username}")
    Call<Account> getAccount(@Path("username") String username);

    @Headers({"Cache-Control: no-cache", "Content-type: application/merge-patch+json"})
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

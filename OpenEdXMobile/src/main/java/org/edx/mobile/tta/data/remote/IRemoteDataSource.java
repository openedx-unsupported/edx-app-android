package org.edx.mobile.tta.data.remote;

import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.data.model.HtmlResponse;
import org.edx.mobile.tta.data.model.authentication.LoginRequest;
import org.edx.mobile.tta.data.model.authentication.LoginResponse;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;
import org.edx.mobile.tta.data.model.library.ConfigModifiedDateResponse;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * Created by Arjun on 2018/9/18.
 */

public interface IRemoteDataSource {
    String BASE_URL = true ? "http://rap2api.taobao.org/app/mock/22156/" : "";
    int TIMEOUT = 10;
    int READ_TIMEOUT = 10;

    @POST("user/login/")
    Observable<BaseResponse<LoginResponse>> login(@Body LoginRequest loginRequest);

    @GET("empty/")
    Observable<BaseResponse<EmptyResponse>> getEmpty();

    @GET(ApiConstants.URL_MX_GET_COLLECTION_CONFIG)
    Observable<CollectionConfigResponse> getConfiguration();

    @GET(ApiConstants.URL_MX_GET_CONFIG_MODIFIED_DATE)
    Observable<ConfigModifiedDateResponse> getModification();

    @GET(ApiConstants.URL_MX_GET_COLLECTION_ITEMS)
    Observable<List<Content>> getContents();

    @GET
    Call<HtmlResponse> getHtmlFromUrl(@Url HttpUrl absoluteUrl);

}

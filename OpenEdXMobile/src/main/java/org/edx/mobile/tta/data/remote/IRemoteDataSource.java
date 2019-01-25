package org.edx.mobile.tta.data.remote;

import org.edx.mobile.http.constants.ApiConstants;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.CollectionConfigResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.ConfigModifiedDateResponse;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.ui.logistration.model.LoginRequest;
import org.edx.mobile.tta.ui.logistration.model.LoginResponse;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

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

}

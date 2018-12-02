package org.edx.mobile.tta.data.remote;

import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.ui.login.model.LoginRequest;
import org.edx.mobile.tta.ui.login.model.LoginResponse;

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
}

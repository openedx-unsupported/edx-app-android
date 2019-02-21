package org.edx.mobile.tta.wordpress_client.rest.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static org.edx.mobile.util.BrowserUtil.loginAPI;
import static org.edx.mobile.util.BrowserUtil.loginPrefs;

/**
 * Created by JARVICE on 22-12-2017.
 */

public class OkHttpBearerTokenAuthInterceptor implements Interceptor {

    private String token="";
    public OkHttpBearerTokenAuthInterceptor(String access_token)
    {
        token=access_token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

       /* Request request = chain.request();
        if (token!=null&& !token.isEmpty()) {
            request = request.newBuilder()
                    .addHeader("authorization", token)
                    .build();
        }

        Response response = chain.proceed(request);
        boolean unauthorized = false;
        if(response.code()==403 ||response.code()==401)
            unauthorized=true;

        if (unauthorized) {
            AuthResponse currentAuth = loginPrefs.getWPCurrentAuth();

            try {
                refreshAccessToken(currentAuth);
                token=loginPrefs.getWPAuthorizationHeader();
                request= request.newBuilder()
                        .addHeader("authorization", token)
                        .build();
                return chain.proceed(request);

            } catch (HttpResponseStatusException e) {
                e.printStackTrace();
            }
        }

        request = chain.request().newBuilder()
                    .addHeader("authorization", token)
                    .build();
            return chain.proceed(request);*/

       ////////////////
        Request request = chain.request().newBuilder()
                .addHeader("authorization", token)
                .build();
        return chain.proceed(request);
    }

    /*private WpAuthResponse refreshAccessToken(AuthResponse currentAuth)
            throws IOException, HttpResponseStatusException {

        WpClientRetrofit clientRetrofit=new WpClientRetrofit(false);
        retrofit2.Response<WpAuthResponse> refreshTokenResponse=  clientRetrofit.refreshAccessToken(currentAuth.refresh_token).execute();

        if (!refreshTokenResponse.isSuccessful()) {
            throw new HttpResponseStatusException(refreshTokenResponse.code());
        }

        WpAuthResponse refreshTokenData = refreshTokenResponse.body();
        loginPrefs.storeWPRefreshTokenResponse(refreshTokenData);
        return refreshTokenData;
    }*/
}
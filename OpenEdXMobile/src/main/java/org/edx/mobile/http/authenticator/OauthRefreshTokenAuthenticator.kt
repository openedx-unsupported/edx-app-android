package org.edx.mobile.http.authenticator

import dagger.Lazy
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Route
import org.edx.mobile.authentication.LoginService
import org.edx.mobile.event.LogoutEvent
import org.edx.mobile.http.HttpStatus.UNAUTHORIZED
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.constants.ApiConstants.TOKEN_TYPE_JWT
import org.edx.mobile.http.constants.ApiConstants.TOKEN_TYPE_REFRESH
import org.edx.mobile.http.provider.RetrofitProvider
import org.edx.mobile.http.util.CallUtil
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.util.AppConstants.HEADER_KEY_AUTHORIZATION
import org.edx.mobile.util.Config
import org.edx.mobile.util.DateUtil
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authenticator for 401 responses for refreshing oauth tokens. Checks for the expired oauth token
 * case and then uses the refresh token to retrieve a new access token. Using the new access token,
 * the original http request that received the 401 will be attempted again. If no refresh_token is
 * present, no authentication attempt is made.
 */
@Singleton
class OauthRefreshTokenAuthenticator @Inject constructor(
    var config: Lazy<Config>,
    var retrofitProvider: Lazy<RetrofitProvider>,
    var loginPrefs: Lazy<LoginPrefs>
) : Authenticator, Interceptor {

    private val logger = Logger(javaClass.name)
    private var lastTokenRefreshRequestTime = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        if (isTokenExpired(loginPrefs.get().currentAuth)) {
            val response = createUnauthorizedResponse(chain)
            val request = authenticate(chain.connection()?.route(), response)

            return request?.let { chain.proceed(it) } ?: chain.proceed(chain.request())
        }
        return chain.proceed(chain.request())
    }

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        logger.warn(response.toString())

        val currentAuth = loginPrefs.get().currentAuth
        if (currentAuth?.refresh_token == null) {
            return null
        }

        val errorCode = response.body?.let {
            getErrorCode(it.string(), currentAuth.token_type)
        } ?: return null

        when (errorCode) {
            TOKEN_EXPIRED_ERROR_MESSAGE,
            JWT_TOKEN_EXPIRED -> {
                val authResponse = refreshAndGetAccessToken(currentAuth.refresh_token)
                return authResponse?.let {
                    val requestBuilder = response.request.newBuilder()
                    addAuthorization(requestBuilder, authResponse)
                    requestBuilder.build()
                }
            }
            TOKEN_NONEXISTENT_ERROR_MESSAGE,
            TOKEN_INVALID_GRANT_ERROR_MESSAGE,
            JWT_INVALID_TOKEN -> {
                /**
                 * Retry request with the current access_token if the original access_token used in
                 * request does not match the current access_token. This case can occur when async
                 * calls are made and are attempting to refresh the access_token where one call
                 * succeeds but the other fails. https://github.com/edx/edx-app-android/pull/834
                 */
                response.request.headers[HEADER_KEY_AUTHORIZATION]?.split(" ")?.let {
                    if (it[1] != currentAuth.access_token) {
                        val requestBuilder = response.request.newBuilder()
                        addAuthorization(requestBuilder, currentAuth)
                        return requestBuilder.build()
                    }
                }

                /**
                 * If the user is logged-in & marked as disabled then on the next server response
                 * app will force the user to logout and navigate it to the launcher screen.
                 */
                EventBus.getDefault().post(LogoutEvent())
            }
            DISABLED_USER_ERROR_MESSAGE,
            JWT_DISABLED_USER_ERROR_MESSAGE -> {
                EventBus.getDefault().post(LogoutEvent())
            }
        }

        /**
         * Return null for unprecedented or unhandled Bearer or JWT errors cases i.e
         * - Error decoding token.
         * - Token is blacklisted.
         * - User retrieval failed.
         * - JWT must include a preferred_username or username claim!
         */
        return null
    }

    private fun addAuthorization(requestBuilder: Request.Builder, authResponse: AuthResponse?) {
        authResponse?.let {
            requestBuilder.removeHeader(HEADER_KEY_AUTHORIZATION)
            requestBuilder.addHeader(
                HEADER_KEY_AUTHORIZATION,
                "${it.token_type} ${it.access_token}"
            )
        }
    }

    private fun isTokenExpired(authResponse: AuthResponse?): Boolean {
        val timeInSeconds = DateUtil.getCurrentTimeInSeconds() + REFRESH_TOKEN_EXPIRY_THRESHOLD
        return authResponse != null && timeInSeconds >= authResponse.accessTokenExpiresAt
    }

    private fun canRequestTokenRefresh(): Boolean {
        return DateUtil.getCurrentTimeInSeconds() - lastTokenRefreshRequestTime >
                REFRESH_TOKEN_INTERVAL_MINIMUM
    }

    /**
     * [getErrorCode] returns error code from error response body based on the type of access token
     * i.e Bearer or JWT
     *
     * @param responseBody Error response body
     * @param tokenType Access token type i.e Bearer or JWT
     * @return Respective error code
     */
    private fun getErrorCode(responseBody: String, tokenType: String): String? {
        return try {
            val jsonObj = JSONObject(responseBody)
            val errorType = if (TOKEN_TYPE_JWT.equals(tokenType, ignoreCase = true))
                if (jsonObj.isNull("detail")) "developer_message" else "detail"
            else "error_code"
            jsonObj.getString(errorType)
        } catch (ex: JSONException) {
            logger.warn("Unable to get error message from 401 response")
            null
        }
    }

    /**
     * [refreshAndGetAccessToken] refreshes the access token in a controlled manner. It prevents
     * multiple async requests to refresh requests with the help of the [canRequestTokenRefresh]
     * method. It will return the stored [AuthResponse] from [LoginPrefs] if the refresh request
     * is already executed successfully in a controlled time, else the new [AuthResponse] will
     * be returned.
     *
     * @param refreshToken The refresh token from [AuthResponse]
     * @return [AuthResponse] of active session
     */
    private fun refreshAndGetAccessToken(refreshToken: String): AuthResponse? {
        val loginService = retrofitProvider.get().nonOAuthBased.create(LoginService::class.java)
        var authResponse: AuthResponse? = null
        try {
            if (canRequestTokenRefresh()) {
                authResponse = CallUtil.executeStrict(
                    loginService.refreshAccessToken(
                        TOKEN_TYPE_REFRESH,
                        config.get().oAuthClientId,
                        refreshToken,
                        TOKEN_TYPE_JWT,
                        true
                    )
                )
                loginPrefs.get().storeRefreshTokenResponse(authResponse)
                lastTokenRefreshRequestTime = DateUtil.getCurrentTimeInSeconds()
            } else {
                authResponse = loginPrefs.get().currentAuth
            }
        } catch (ex: HttpStatusException) {
            logger.warn(ex.message)
        } catch (ex: IOException) {
            logger.warn(ex.message)
        }
        return authResponse
    }

    /**
     * [createUnauthorizedResponse] creates an unauthorized okhttp response with the initial chain
     * request for [authenticate] method of [OauthRefreshTokenAuthenticator]. The response is
     * specially designed to trigger the 'Token Expired' case of the [authenticate] method so that
     * it can handle the refresh logic of the access token accordingly.
     *
     * @param chain Chain request for authentication
     * @return Custom unauthorized response builder with initial request
     */
    private fun createUnauthorizedResponse(chain: Interceptor.Chain) = Response.Builder()
        .code(UNAUTHORIZED)
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .message("Unauthorized")
        .headers(chain.request().headers)
        .body(getResponseBody())
        .build()

    /**
     * [getResponseBody] generates an error response body based on access token type because both
     * Bearer and JWT have their own sets of errors.
     *
     * @return ResponseBody based on access token type
     */
    private fun getResponseBody(): ResponseBody {
        val tokenType = loginPrefs.get().currentAuth?.token_type
        val jsonObject = if (TOKEN_TYPE_JWT.equals(tokenType, ignoreCase = true)) {
            JSONObject().put("detail", JWT_TOKEN_EXPIRED)
        } else {
            JSONObject().put("error_code", TOKEN_EXPIRED_ERROR_MESSAGE)
        }

        return jsonObject.toString().toResponseBody("application/json".toMediaType())
    }

    companion object {
        private const val TOKEN_EXPIRED_ERROR_MESSAGE = "token_expired"
        private const val TOKEN_NONEXISTENT_ERROR_MESSAGE = "token_nonexistent"
        private const val TOKEN_INVALID_GRANT_ERROR_MESSAGE = "invalid_grant"
        private const val DISABLED_USER_ERROR_MESSAGE = "user_is_disabled"
        private const val JWT_DISABLED_USER_ERROR_MESSAGE = "User account is disabled."
        private const val JWT_TOKEN_EXPIRED = "Token has expired."
        private const val JWT_INVALID_TOKEN = "Invalid token."

        /**
         * [REFRESH_TOKEN_EXPIRY_THRESHOLD] behave as a buffer time to be used in the expiry
         * verification method of the access token to ensure that the token doesn't expire during
         * an active session.
         */
        private const val REFRESH_TOKEN_EXPIRY_THRESHOLD = 60

        /**
         * [REFRESH_TOKEN_INTERVAL_MINIMUM] behave as a buffer time for refresh token network
         * requests. It prevents multiple calls to refresh network requests in case of an
         * unauthorized access token during async requests.
         */
        private const val REFRESH_TOKEN_INTERVAL_MINIMUM = 60
    }
}

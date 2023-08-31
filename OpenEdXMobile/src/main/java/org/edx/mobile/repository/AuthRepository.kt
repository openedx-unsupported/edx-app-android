package org.edx.mobile.repository

import android.os.Bundle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.edx.mobile.authentication.LoginAPI
import org.edx.mobile.core.EdxEnvironment
import org.edx.mobile.extenstion.isNotNullOrEmpty
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.social.SocialAuthSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val environment: EdxEnvironment,
    private val loginAPI: LoginAPI,
) {
    suspend fun loginUsingEmail(
        email: String,
        password: String,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): AuthResponse = withContext(dispatcher) {
        try {
            loginAPI.logInUsingEmail(email, password)
        } catch (exception: Exception) {
            throw exception
        }
    }

    suspend fun registerAccount(
        formFields: Bundle,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): AuthResponse? {
        val accessToken = environment.loginPrefs.socialLoginAccessToken
        val provider = environment.loginPrefs.socialLoginProvider
        val backendSourceType = SocialAuthSource.fromString(provider)

        // Set honor_code and terms_of_service to true
        formFields.putString("honor_code", "true")
        formFields.putString("terms_of_service", "true")

        // Set parameter required by social registration
        if (accessToken.isNotNullOrEmpty()) {
            formFields.putString("access_token", accessToken)
            formFields.putString("provider", provider)
            formFields.putString("client_id", environment.config.oAuthClientId)
        }

        return withContext(dispatcher) {
            try {
                val response = when (backendSourceType) {
                    SocialAuthSource.GOOGLE -> accessToken?.let {
                        loginAPI.registerUsingGoogle(formFields, it)
                    }

                    SocialAuthSource.FACEBOOK -> accessToken?.let {
                        loginAPI.registerUsingFacebook(formFields, it)
                    }

                    SocialAuthSource.MICROSOFT -> accessToken?.let {
                        loginAPI.registerUsingMicrosoft(formFields, it)
                    }

                    else -> loginAPI.registerUsingEmail(formFields)
                }
                return@withContext response
            } catch (exception: Exception) {
                throw exception
            }
        }
    }
}

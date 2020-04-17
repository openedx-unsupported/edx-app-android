package org.edx.mobile.social.microsoft

import android.content.Context
import android.text.TextUtils
import okhttp3.Request
import org.edx.mobile.http.callback.ErrorHandlingOkCallback
import org.edx.mobile.http.provider.OkHttpClientProvider
import org.edx.mobile.social.SocialFactory
import org.edx.mobile.social.SocialLoginDelegate
import org.edx.mobile.social.SocialMember
import org.edx.mobile.social.SocialProvider
import roboguice.RoboGuice

class MicrosoftProvide : SocialProvider {

    override fun login(context: Context?, callback: SocialProvider.Callback<Void>?) {
        throw UnsupportedOperationException("Not implemented / Not supported")
    }

    override fun getUserInfo(context: Context?, socialType: SocialFactory.SOCIAL_SOURCE_TYPE?,
                             accessToken: String?,
                             userInfoCallback: SocialLoginDelegate.SocialUserInfoCallback?) {
        context?.run {
            val okHttpClientProvider = RoboGuice.getInjector(context).getInstance(OkHttpClientProvider::class.java)
            okHttpClientProvider.get().newCall(Request.Builder()
                    .url(MS_GRAPH_URL)
                    .get()
                    .build())
                    .enqueue(object : ErrorHandlingOkCallback<MicrosoftUserProfile>(
                            this, MicrosoftUserProfile::class.java, null) {
                        override fun onResponse(userProfile: MicrosoftUserProfile) {
                            var name = userProfile.fullName
                            if (TextUtils.isEmpty(name)) {
                                if (!TextUtils.isEmpty(userProfile.firstName)) {
                                    name = userProfile.firstName + " "
                                }
                                if (!TextUtils.isEmpty(userProfile.surName)) {
                                    if (TextUtils.isEmpty(name)) {
                                        name = userProfile.surName
                                    } else {
                                        name += userProfile.surName
                                    }
                                }
                            }
                            userInfoCallback?.setSocialUserInfo(userProfile.email, name)
                        }
                    })
        }
    }

    override fun getUser(callback: SocialProvider.Callback<SocialMember>?) {
        throw UnsupportedOperationException("Not implemented / Not supported")
    }

    override fun isLoggedIn(): Boolean {
        throw UnsupportedOperationException("Not implemented / Not supported")
    }

    companion object {
        private const val MS_GRAPH_URL = "https://graph.microsoft.com/v1.0/me"
    }
}

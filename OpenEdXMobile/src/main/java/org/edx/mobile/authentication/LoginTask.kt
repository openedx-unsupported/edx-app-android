package org.edx.mobile.authentication

import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import org.edx.mobile.core.EdxDefaultModule.ProviderEntryPoint
import org.edx.mobile.model.authentication.AuthResponse
import org.edx.mobile.task.Task

abstract class LoginTask(
    context: Context,
    private val username: String,
    private val password: String
) : Task<AuthResponse?>(context) {

    var loginAPI = EntryPointAccessors
        .fromApplication(context, ProviderEntryPoint::class.java).getLoginAPI()

    override fun doInBackground(vararg voids: Void): AuthResponse? {
        try {
            return loginAPI.logInUsingEmail(username, password)
        } catch (e: Exception) {
            e.printStackTrace()
            handleException(e)
        }
        return null
    }
}

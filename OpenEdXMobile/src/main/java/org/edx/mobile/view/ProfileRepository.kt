package org.edx.mobile.view

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.user.UserAPI
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val userAPI: UserAPI,
    private val loginPrefs: LoginPrefs,
) {
    suspend fun removeProfileImage(): Boolean = withContext(Dispatchers.IO) {
        val response = try {
            userAPI.removeProfileImage(loginPrefs.username).execute()
        } catch (e: Exception) {
            return@withContext false
        }
        return@withContext response.isSuccessful
    }

    suspend fun uploadProfileImage(imageFile: File): Boolean = withContext(Dispatchers.IO) {
        val response = userAPI.setProfileImage(loginPrefs.username, imageFile).execute()
        return@withContext response.isSuccessful
    }
}

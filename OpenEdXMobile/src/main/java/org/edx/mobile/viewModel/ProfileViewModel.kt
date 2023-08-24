package org.edx.mobile.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.edx.mobile.event.ProfilePhotoUpdatedEvent
import org.edx.mobile.model.user.FormDescription
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import org.edx.mobile.view.ProfileRepository
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val loginPrefs: LoginPrefs,
) : ViewModel() {

    private val _showProgress = MutableLiveData(Event(false))
    val showProgress: LiveData<Event<Boolean>> = _showProgress

    private var _formDescription: FormDescription? = null
    val formDescription: FormDescription?
        get() = _formDescription

    fun uploadProfileImage(imageFile: File) {
        _showProgress.postEvent(true)
        viewModelScope.launch {
            val isSuccessful = profileRepository.uploadProfileImage(imageFile)

            if (isSuccessful) {
                EventBus.getDefault().post(
                    ProfilePhotoUpdatedEvent(loginPrefs.username, Uri.fromFile(imageFile))
                )
            }
            _showProgress.postEvent(false)
        }
    }

    fun removeProfileImage() {
        _showProgress.postEvent(true)
        viewModelScope.launch {
            val isSuccessful = profileRepository.removeProfileImage()

            if (isSuccessful) {
                EventBus.getDefault().post(ProfilePhotoUpdatedEvent(loginPrefs.username, null))
                loginPrefs.setProfileImage(loginPrefs.username, null)
            }
            _showProgress.postEvent(false)
        }
    }

    fun setProfileFormDescription(inputStream: InputStream) {
        viewModelScope.launch {
            val value: FormDescription? = try {
                Gson().fromJson(InputStreamReader(inputStream), FormDescription::class.java)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            _formDescription = value
        }
    }
}

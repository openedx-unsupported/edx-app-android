package org.edx.mobile.viewModel

import android.app.Activity
import android.graphics.Rect
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
import org.edx.mobile.third_party.crop.CropUtil
import org.edx.mobile.util.FileUtil
import org.edx.mobile.util.IOUtils
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import org.edx.mobile.view.ProfileRepository
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
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

    private val _croppedImageUri = MutableLiveData<Uri>()
    val croppedImageUri: LiveData<Uri> = _croppedImageUri

    private var _formDescription: FormDescription? = null
    val formDescription: FormDescription?
        get() = _formDescription

    fun uploadProfileImage(activity: Activity, imageUri: Uri, cropRect: Rect) {
        _showProgress.postEvent(true)
        viewModelScope.launch {
            val imageFile: File = try {
                val croppedFileName = "cropped-image" + System.currentTimeMillis() + ".jpg"
                val imageFile = File(activity.externalCacheDir, croppedFileName)
                CropUtil.crop(activity, imageUri, cropRect, 500, 500, imageFile)
                imageFile
            } catch (exception: IOException) {
                exception.printStackTrace()
                _showProgress.postEvent(false)
                return@launch
            }

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

    fun copyUriContentToFile(
        activity: Activity,
        uri: Uri,
    ) {
        viewModelScope.launch {
            if ("file" != uri.scheme) {
                try {
                    val fileUri = FileUtil.getFileUriFromMediaStoreUri(activity, uri) ?: uri
                    val filename = "cropped-image" + System.currentTimeMillis() + ".jpg"
                    val outputFile = File(activity.externalCacheDir, filename)

                    activity.contentResolver.openInputStream(fileUri)?.use { inputStream ->
                        FileOutputStream(outputFile).use { outputStream ->
                            IOUtils.copy(inputStream, outputStream)
                        }
                    }
                    _croppedImageUri.postValue(Uri.fromFile(outputFile))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
}

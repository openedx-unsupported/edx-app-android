package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.edx.mobile.event.ProfilePhotoUpdatedEvent
import org.edx.mobile.module.prefs.LoginPrefs
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import org.edx.mobile.view.ProfileRepository
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val loginPrefs: LoginPrefs,
) : ViewModel() {

    private val _showProgress = MutableLiveData(Event(false))
    val showProgress: LiveData<Event<Boolean>> = _showProgress

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
}

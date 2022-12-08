package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * The [VideoViewModel] is shared between Parent & Child Fragment so they can only communicate with
 * each other. In this way, the fragments do not need to know about each other, and the activity
 * does not need to do anything to facilitate the communication.
 */
@HiltViewModel
class VideoViewModel @Inject constructor() : ViewModel() {

    private val _clearChoices = MutableLiveData<Boolean>()
    val clearChoices: LiveData<Boolean> = _clearChoices

    private val _selectedVideosPosition = MutableLiveData<Int>()
    val selectedVideosPosition: LiveData<Int> = _selectedVideosPosition

    fun deleteVideosAtPosition(position: Int) {
        _selectedVideosPosition.value = position
    }

    fun clearChoices(shouldClear: Boolean) {
        _clearChoices.value = shouldClear
    }
}

package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import javax.inject.Inject

/**
 * The [VideoViewModel] is shared between Parent & Child Fragment so they can only communicate with
 * each other. In this way, the fragments do not need to know about each other, and the activity
 * does not need to do anything to facilitate the communication.
 */
@HiltViewModel
class VideoViewModel @Inject constructor() : ViewModel() {

    private val _clearChoices = MutableLiveData<Event<Boolean>>()
    val clearChoices: LiveData<Event<Boolean>> = _clearChoices

    private val _selectedVideosPosition = MutableLiveData<Event<Pair<Int, Int>>>()
    val selectedVideosPosition: LiveData<Event<Pair<Int, Int>>> = _selectedVideosPosition

    fun deleteVideosAtPosition(position: Pair<Int, Int>) {
        _selectedVideosPosition.postEvent(position)
    }

    fun clearChoices() {
        _clearChoices.postEvent(true)
    }
}

package org.edx.mobile.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor() : ViewModel() {

    private val _clearChoices = MutableLiveData(false)
    val clearChoices: LiveData<Boolean> = _clearChoices

    private val _selectedVideosPosition = MutableLiveData(-1)
    val selectedVideosPosition: LiveData<Int> = _selectedVideosPosition

    fun deleteVideosAtPosition(position: Int) {
        _selectedVideosPosition.value = position
    }

    fun clearChoices(shouldClear: Boolean) {
        _clearChoices.value = shouldClear
    }
}

package org.edx.mobile.viewModel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.repository.AuthRepository
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _onRegister = MutableLiveData<Event<Boolean>>()
    val onRegister: LiveData<Event<Boolean>> = _onRegister

    private val _errorMessage = MutableLiveData<Event<ErrorMessage>>()
    val errorMessage: LiveData<Event<ErrorMessage>> = _errorMessage

    fun registerAccount(parameters: Bundle) {
        viewModelScope.launch {
            val response = kotlin.runCatching { authRepository.registerAccount(parameters) }
            response.onSuccess {
                _onRegister.postEvent(it?.isSuccess == true)
            }.onFailure {
                _errorMessage.postEvent(ErrorMessage(0, it))
            }
        }
    }
}

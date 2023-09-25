package org.edx.mobile.viewModel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.edx.mobile.authentication.LoginAPI
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.exception.ErrorMessage
import org.edx.mobile.repository.AuthRepository
import org.edx.mobile.social.SocialLoginDelegate.Feature
import org.edx.mobile.util.observer.Event
import org.edx.mobile.util.observer.postEvent
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val environment: IEdxEnvironment,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _onLogin = MutableLiveData<Event<Boolean>>()
    val onLogin: LiveData<Event<Boolean>> = _onLogin

    private val _onRegister = MutableLiveData<Event<Boolean>>()
    val onRegister: LiveData<Event<Boolean>> = _onRegister

    private val _errorMessage = MutableLiveData<Event<ErrorMessage>>()
    val errorMessage: LiveData<Event<ErrorMessage>> = _errorMessage

    private val _socialLoginErrorMessage = MutableLiveData<Event<ErrorMessage>>()
    val socialLoginErrorMessage: LiveData<Event<ErrorMessage>> = _socialLoginErrorMessage

    private val _showProgress = MutableLiveData<Event<Boolean>>()
    val showProgress: LiveData<Event<Boolean>> = _showProgress

    fun loginUsingEmail(email: String, password: String) {
        _showProgress.postEvent(true)
        viewModelScope.launch {
            val response = kotlin.runCatching { authRepository.loginUsingEmail(email, password) }
            response.onSuccess {
                _onLogin.postEvent(it.isSuccess)
            }.onFailure {
                _errorMessage.postEvent(ErrorMessage(0, it))
            }
            _showProgress.postEvent(false)
        }
    }

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

    fun loginUsingSocialAccount(
        accessToken: String,
        backend: String,
        feature: Feature
    ) {
        if (feature == Feature.SIGN_IN)
            _showProgress.postEvent(true)

        viewModelScope.launch {
            val response = kotlin.runCatching {
                authRepository.loginUsingSocialAccount(
                    accessToken,
                    backend
                )
            }

            response.onSuccess {
                if (feature == Feature.REGISTRATION) {
                    environment.loginPrefs.alreadyRegisteredLoggedIn = true
                    _onRegister.postEvent(true)
                } else {
                    _showProgress.postEvent(false)
                    _onLogin.postEvent(true)
                }
            }.onFailure {
                if (it is LoginAPI.AccountNotLinkedException) {
                    _socialLoginErrorMessage.postEvent(ErrorMessage(0, it))
                } else {
                    _errorMessage.postEvent(ErrorMessage(0, it))
                }
            }
            _showProgress.postEvent(false)
        }
    }
}

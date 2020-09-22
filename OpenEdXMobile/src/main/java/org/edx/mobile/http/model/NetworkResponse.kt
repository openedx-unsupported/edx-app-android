package org.edx.mobile.http.model

/**
 * A generic class that contains data and status about loading this data.
 * Ref: https://developer.android.com/jetpack/guide#recommended-app-arch
 */
sealed class Result<out R> {
    data class Success<out T>(val isSuccessful: Boolean, val data: T?, val code: Int, val message: String) : Result<T>()
    data class Error(val throwable: Throwable) : Result<Nothing>()
}

/**
 * Custom Callback to transfer API response data from repository to model view
 * Ref: https://stackoverflow.com/questions/60004591/return-data-from-retrofit-onresponse
 */
interface NetworkResponseCallback<T>{
    fun onSuccess(result: Result.Success<T>)
    fun onError(error: Result.Error)
}

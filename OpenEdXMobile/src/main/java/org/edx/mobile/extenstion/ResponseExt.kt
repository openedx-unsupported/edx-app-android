package org.edx.mobile.extenstion

import org.edx.mobile.http.model.NetworkResponseCallback
import org.edx.mobile.http.model.Result
import org.edx.mobile.util.InAppPurchasesException
import org.json.JSONObject
import retrofit2.Response

/**
 * Transforms [Response] to [Result] object for In-App Purchases success or error scenarios
 */
fun <T> Response<T>.toInAppPurchasesResult(callback: NetworkResponseCallback<T>) {
    when (isSuccessful && body() != null) {
        true -> callback.onSuccess(
            Result.Success(
                isSuccessful = isSuccessful,
                data = body(),
                code = code(),
                message = message()
            )
        )
        false -> callback.onError(
            Result.Error(
                InAppPurchasesException(
                    httpErrorCode = code(),
                    errorMessage = getMessage(),
                )
            )
        )
    }
}

/**
 * Attempts to extract error message from api responses and fails gracefully if unable to do so.
 *
 * @return extracted text message; null if no message was received or was unable to parse it.
 */
fun <T> Response<T>.getMessage(): String? {
    if (isSuccessful) return message()
    return try {
        val errors = JSONObject(errorBody()?.string() ?: "{}")
        errors.optString("error")
    } catch (ex: Exception) {
        null
    }
}

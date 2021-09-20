package org.edx.mobile.view.custom

import android.text.TextUtils
import android.webkit.JavascriptInterface
import com.google.gson.Gson
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.model.AjaxCallData

class AjaxNativeCallback(private val completionCallback: URLInterceptorWebViewClient.CompletionCallback?) {

    @JavascriptInterface
    fun ajaxDone(json: String?) {
        if (!TextUtils.isEmpty(json)) {
            val data = Gson().fromJson<AjaxCallData>(json, AjaxCallData::class.java)
            completionCallback?.blockCompletionHandler(
                data.status == HttpStatus.OK && AjaxCallData.isCompletionRequest(
                    data
                )
            )
        }
    }
}

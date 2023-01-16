package org.edx.mobile.view.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import org.edx.mobile.extenstion.renderHtml
import java.util.regex.Pattern

class EdxDiscussionBody @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    /**
     * Method to render the [body] based on HTML paragraph tag
     * @param body  rendered body of the discussion
     */
    fun setBody(body: String?) {
        body?.let {
            if (isPlainHtml(body)) {
                this.addView(TextView(context, attrs, defStyleAttr).also {
                    it.renderHtml(body)
                })
            } else {
                this.addView(EdxWebView(context, attrs).also {
                    it.loadDataWithBaseURL(null, body, "text/html", "utf-8", null)
                })
            }
        }
    }

    /**
     * Method to check if the [body] only contains the HTML paragraph tag.
     * @param body  rendered body of discussions
     * @return True if the body only contains paragraph tags otherwise false
     */
    private fun isPlainHtml(body: String): Boolean {
        val htmlTags = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>"
        val pattern = Pattern.compile(htmlTags)
        val plainText = body.replace("<p>", "").replace("</p>", "")
        return !pattern.matcher(plainText).find()
    }
}

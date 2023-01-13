package org.edx.mobile.view.custom

import android.content.Context
import android.widget.FrameLayout
import android.widget.TextView
import org.edx.mobile.extenstion.renderHtml
import java.util.regex.Pattern

class EdxDiscussionBody constructor(context: Context) :
    FrameLayout(context) {

    /**
     * Method to render the body based on HTML paragraph tags
     */
    fun setBody(body: String?) {
        body?.let { body ->
            if (isPlainHtml(body)) {
                this.addView(TextView(context).also {
                    it.renderHtml(body)
                })
            } else {
                this.addView(EdxWebView(context, null).also {
                    it.loadDataWithBaseURL(null, body, "text/html", "utf-8", null)
                })
            }
        }
    }

    /**
     * Method to check if the text only contains the HTML paragraph tag.
     *
     * @param text  render body of discussions
     */
    private fun isPlainHtml(text: String): Boolean {
        val htmlTags = "<(\"[^\"]*\"|'[^']*'|[^'\">])*>"
        val pattern = Pattern.compile(htmlTags)
        val plainText = text.replace("<p>", "").replace("</p>", "")
        return !pattern.matcher(plainText).find()
    }
}
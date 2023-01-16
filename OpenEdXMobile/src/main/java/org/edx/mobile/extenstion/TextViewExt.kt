package org.edx.mobile.extenstion

import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView

fun TextView.renderHtml(body: String) {
    parseHtml(body)?.let { spannedHtml ->
        val urlSpans = spannedHtml.getSpans(
            0, spannedHtml.length,
            URLSpan::class.java
        )
        this.autoLinkMask = Linkify.ALL
        this.movementMethod = LinkMovementMethod.getInstance()
        this.text = spannedHtml
        val viewText = this.text as SpannableString
        for (spanObj in urlSpans) {
            val start = spannedHtml.getSpanStart(spanObj)
            val end = spannedHtml.getSpanEnd(spanObj)
            val flags = spannedHtml.getSpanFlags(spanObj)
            viewText.setSpan(spanObj, start, end, flags)
        }
    }
}

private fun parseHtml(html: String): Spanned? {
    // If the HTML contains a paragraph at the end, there will be blank lines following the text
    // Therefore, we need to trim the resulting CharSequence to remove those extra lines
    return trim(Html.fromHtml(html)) as Spanned?
}

private fun trim(s: CharSequence): CharSequence? {
    var start = 0
    var end = s.length
    while (start < end && Character.isWhitespace(s[start])) {
        start++
    }
    while (end > start && Character.isWhitespace(s[end - 1])) {
        end--
    }
    return s.subSequence(start, end)
}

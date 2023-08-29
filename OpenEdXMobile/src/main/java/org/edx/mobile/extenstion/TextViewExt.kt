package org.edx.mobile.extenstion

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.text.style.URLSpan
import android.text.util.Linkify
import android.widget.TextView
import androidx.core.text.HtmlCompat

fun TextView.renderHtml(body: String) {
    parseHtml(body)?.let { spannedHtml ->
        val urlSpans = spannedHtml.getSpans(
            0, spannedHtml.length,
            URLSpan::class.java
        )
        this.autoLinkMask = Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS
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
    return trim(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)) as Spanned?
}

private fun trim(s: CharSequence): CharSequence {
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

fun TextView.setTextWithIcon(fullText: String, drawable: Drawable, targetText: String) {
    val spannableString = SpannableString(fullText)

    // Get the drawable resource for the icon
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

    // Search for the targetText in the fullText
    val startIndex = fullText.indexOf(targetText)

    // If the targetText is found, create and set the ImageSpan
    if (startIndex != -1) {
        val endIndex = startIndex + targetText.length
        val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
        spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    this.text = spannableString
}

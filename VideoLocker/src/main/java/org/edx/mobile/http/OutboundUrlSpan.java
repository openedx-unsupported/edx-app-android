package org.edx.mobile.http;

import android.content.Context;
import android.os.Parcel;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Patterns;
import android.view.View;

import org.edx.mobile.util.BrowserUtil;

import java.util.regex.Pattern;

/**
 * Created by yervant on 1/28/15.
 */
public class OutboundUrlSpan extends URLSpan {
    public OutboundUrlSpan(String url) {
        super(url);
    }

    public OutboundUrlSpan(Parcel src) {
        super(src);
    }

    @Override
    public void onClick(View widget) {

        Context context = widget.getContext();

        if(context instanceof FragmentActivity)
            BrowserUtil.open((FragmentActivity)context, getURL());
    }

    public static Spanned interceptAllLinks(Spanned content){
        SpannableStringBuilder sb = new SpannableStringBuilder(content.toString());

        Object[] spans = content.getSpans(0, content.length(), Object.class);
        for(Object span : spans){
            int start = content.getSpanStart(span);
            int end = content.getSpanEnd(span);
            int flags = content.getSpanFlags(span);

            if(span instanceof URLSpan){
                URLSpan urlSpan = (URLSpan)span;
                String url = urlSpan.getURL();

                sb.setSpan(new OutboundUrlSpan(url), start, end, flags);
            }
            else {
                sb.setSpan(span, start, end, flags);
            }

        }

        return sb;
    }

}

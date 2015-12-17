package org.edx.mobile.view.custom;

import android.content.Context;
import android.util.AttributeSet;

public class EdxAssessmentWebView extends EdxWebView {
    public EdxAssessmentWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getSettings().setSupportZoom(false);
    }
}

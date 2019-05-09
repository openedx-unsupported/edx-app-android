package org.edx.mobile.tta.scorm;

import android.webkit.JavascriptInterface;

import org.edx.mobile.tta.ui.course.CourseScormViewActivity;

public class JSInterfaceTincan {

    CourseScormViewActivity ctx;

    public JSInterfaceTincan(CourseScormViewActivity activity) {
        ctx = activity;
    }

    @JavascriptInterface
    public void sendDataToAndroid(String data) {
        ctx.ReceiveTinCanStatement(data);
    }

    @JavascriptInterface
    public void sendResumeDataToAndroid(String resume_info) {
        ctx.ReceiveTinCanResumePayload(resume_info);
    }
    @JavascriptInterface
    public void sendTincanObject(String tincan_obj) {
        ctx.ReceiveTincanObject(tincan_obj);
    }

}

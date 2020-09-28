package org.humana.mobile.tta.scorm;

import android.webkit.JavascriptInterface;

import org.humana.mobile.tta.ui.course.CourseScormViewActivity;

public class JSInterfaceTincan {

    CourseScormViewActivity ctx;
    org.humana.mobile.view.CourseScormViewActivity ctxNew;

    public JSInterfaceTincan(CourseScormViewActivity activity) {
        ctx = activity;
    }

    public JSInterfaceTincan(org.humana.mobile.view.CourseScormViewActivity activity) {
        ctxNew = activity;
    }

    @JavascriptInterface
    public void sendDataToAndroid(String data) {
        if (ctx != null)
            ctx.ReceiveTinCanStatement(data);
        else
            ctxNew.ReceiveTinCanStatement(data);
    }

    @JavascriptInterface
    public void sendResumeDataToAndroid(String resume_info) {
        if (ctx != null)
            ctx.ReceiveTinCanResumePayload(resume_info);
        else
            ctxNew.ReceiveTinCanResumePayload(resume_info);
    }

    @JavascriptInterface
    public void sendTincanObject(String tincan_obj) {
        if (ctx != null)
            ctx.ReceiveTincanObject(tincan_obj);
        else
            ctxNew.ReceiveTincanObject(tincan_obj);
    }

}

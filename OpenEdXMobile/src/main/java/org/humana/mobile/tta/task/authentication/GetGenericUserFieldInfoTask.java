package org.humana.mobile.tta.task.authentication;

import android.content.Context;

import com.google.inject.Inject;

import org.humana.mobile.authentication.LoginAPI;
import org.humana.mobile.task.Task;
import org.humana.mobile.tta.data.model.authentication.FieldInfo;

public class GetGenericUserFieldInfoTask extends Task<FieldInfo> {

    @Inject
    LoginAPI loginAPI;

    public GetGenericUserFieldInfoTask(Context context) {
        super(context);
    }

    @Override
    public FieldInfo call() throws Exception {
        return loginAPI.getCustomStateFieldAttributes();
    }

}

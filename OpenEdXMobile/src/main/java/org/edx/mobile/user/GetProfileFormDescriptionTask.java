package org.edx.mobile.user;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.edx.mobile.R;
import org.edx.mobile.task.Task;

import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class GetProfileFormDescriptionTask extends
        Task<FormDescription> {


    public GetProfileFormDescriptionTask(@NonNull Context context) {
        super(context);
    }

    // Try-with-resources is actually from API 14, but lint says it isn't: https://code.google.com/p/android/issues/detail?id=73483
    @TargetApi(android.os.Build.VERSION_CODES.KITKAT)
    public FormDescription call() throws Exception {
        try (InputStream in = context.getResources().openRawResource(R.raw.profiles)) {
            return new Gson().fromJson(new InputStreamReader(in), FormDescription.class);
        }
    }
}

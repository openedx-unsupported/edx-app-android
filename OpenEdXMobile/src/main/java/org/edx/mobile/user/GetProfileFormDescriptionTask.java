package org.edx.mobile.user;

import android.content.Context;
import androidx.annotation.NonNull;

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

    public FormDescription call() throws Exception {
        try (InputStream in = context.getResources().openRawResource(R.raw.profiles)) {
            return new Gson().fromJson(new InputStreamReader(in), FormDescription.class);
        }
    }
}

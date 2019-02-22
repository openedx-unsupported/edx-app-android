package org.edx.mobile.user;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.task.Task;
import org.edx.mobile.util.UiUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public abstract class GetFormOptionsTask extends
        Task<List<FormOption>> {

    @NonNull
    private final Context context;
    @NonNull
    private final String fileName;

    public GetFormOptionsTask(@NonNull Context context, @NonNull String fileName) {
        super(context);
        this.context = context;
        this.fileName = fileName;
    }

    public List<FormOption> call() throws Exception {
        try (InputStream in = context.getResources().openRawResource(
                UiUtil.getRawFile(context, fileName))) {
            return new Gson().fromJson(new InputStreamReader(in), new TypeToken<List<FormOption>>() {
            }.getType());
        }
    }
}

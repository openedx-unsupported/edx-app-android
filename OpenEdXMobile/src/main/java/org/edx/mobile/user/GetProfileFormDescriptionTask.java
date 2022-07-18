package org.edx.mobile.user;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.edx.mobile.R;
import org.edx.mobile.model.user.FormDescription;
import org.edx.mobile.task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class GetProfileFormDescriptionTask extends
        Task<FormDescription> {


    public GetProfileFormDescriptionTask(@NonNull Context context) {
        super(context);
    }

    @Override
    protected FormDescription doInBackground(Void... voids) {
        try (InputStream in = context.get().getResources().openRawResource(R.raw.profiles)) {
            return new Gson().fromJson(new InputStreamReader(in), FormDescription.class);
        } catch (IOException e) {
            e.printStackTrace();
            handleException(e);
        }
        return null;
    }

    @Override
    public void onException(Exception ex) {
        // nothing to do
    }
}

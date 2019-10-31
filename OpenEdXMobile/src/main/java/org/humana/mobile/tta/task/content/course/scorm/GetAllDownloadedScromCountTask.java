package org.humana.mobile.tta.task.content.course.scorm;

import android.content.Context;
import android.support.annotation.NonNull;

import org.humana.mobile.task.Task;

public class GetAllDownloadedScromCountTask extends Task<Integer> {

    public GetAllDownloadedScromCountTask(@NonNull Context context) {
        super(context);
    }

    @Override
    public Integer call() throws Exception {
        return environment.getStorage().getDownloadedScromCount();
    }

}

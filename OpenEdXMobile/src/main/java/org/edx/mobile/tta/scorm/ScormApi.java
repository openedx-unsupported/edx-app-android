package org.edx.mobile.tta.scorm;

import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

@Singleton
public class ScormApi {

    @Inject
    private ScormService scormService;

    @Inject
    public ScormApi() {
    }

    public Call<ScormStartResponse> scormStart(String courseId, String blockId){
        Map<String, String> parameters=new HashMap<>();
        parameters.put("course_id",courseId);
        parameters.put("block_id",blockId);
        return scormService.scormStart(parameters);
    }

}

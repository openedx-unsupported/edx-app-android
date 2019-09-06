package org.edx.mobile.tta.task;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.task.Task;
import org.edx.mobile.tta.data.model.UpdateResponse;
import org.edx.mobile.tta.data.remote.api.TaAPI;

public class GetVersionUpdatedTask
    extends Task<UpdateResponse> {

        @Inject
        private TaAPI taAPI;
        String v_name;
        Long v_code;

    public GetVersionUpdatedTask(Context context,String version_name ,Long version_code) {
            super(context);
            this.v_name=version_name;
            this.v_code=version_code;
        }

        @Override
        public UpdateResponse call() throws Exception {
            return taAPI.getVersionUpdate(v_name,v_code).execute().body();
        }

}

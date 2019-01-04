package org.edx.mobile.tta.data.remote.api;

import android.support.annotation.NonNull;

import com.google.inject.Singleton;

import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.ModificationResponse;
import org.edx.mobile.tta.data.remote.service.TaService;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;

@Singleton
public class TaAPI {

    @NonNull
    private final TaService taService;

    @Inject
    public TaAPI(@NonNull TaService taService) {
        this.taService = taService;
    }

    public Call<ConfigurationResponse> getConfiguration(){
        return taService.getConfiguration();
    }

    public Call<ModificationResponse> getModification(){
        return taService.getModification();
    }

    public Call<List<Content>> getContents(){
        return taService.getContents();
    }
}

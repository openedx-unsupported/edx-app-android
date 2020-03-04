package org.humana.mobile.tta.ui.base.mvvm;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import org.humana.mobile.tta.data.DataManager;

public class TaViewModel extends AndroidViewModel {
    protected DataManager mDataManager;

    public TaViewModel(@NonNull Application application) {
        super(application);
        mDataManager = DataManager.getInstance(application);
    }
}
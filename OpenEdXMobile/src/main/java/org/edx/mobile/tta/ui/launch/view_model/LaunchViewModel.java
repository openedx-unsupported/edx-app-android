package org.edx.mobile.tta.ui.launch.view_model;

import android.content.Context;
import android.databinding.ObservableField;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class LaunchViewModel extends BaseViewModel {

    public ObservableField<Integer> image = new ObservableField<>(R.drawable.tta_onboarding_01);
    public ObservableField<String> text = new ObservableField<>("");

    public LaunchViewModel(Context context, TaBaseFragment fragment, int imageId, String text) {
        super(context, fragment);
        this.image.set(imageId);
        this.text.set(text);
    }
}

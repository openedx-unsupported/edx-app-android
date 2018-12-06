package org.edx.mobile.tta.ui.launch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.launch.view_model.LaunchViewModel;

public class LaunchFragment extends TaBaseFragment {

    private static final String IMAGE_ID = "imageId";
    private static final String TEXT = "text";

    private int imageId;
    private String text;

    public static LaunchFragment newInstance(int imageId, String text){
        LaunchFragment fragment = new LaunchFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(IMAGE_ID, imageId);
        bundle.putString(TEXT, text);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageId = getArguments().getInt(IMAGE_ID);
        text = getArguments().getString(TEXT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding(inflater, container, R.layout.t_fragment_launch, new LaunchViewModel(getActivity(), this, imageId, text))
                .getRoot();
    }
}

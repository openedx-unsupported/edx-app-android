package org.edx.mobile.tta.ui.image;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;

import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.image.view_model.ImageEditModule;


public class ImageEditFragment extends TaBaseFragment {
    public static final String TAG = ImageEditFragment.class.getCanonicalName();
    private ImageEditModule viewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel =  new ImageEditModule(getActivity(),this);
    }


    public static ImageEditFragment newInstance(){
        ImageEditFragment fragment = new ImageEditFragment();
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.fragment_edit_user_image, viewModel)
                .getRoot();

        return view;
    }
}

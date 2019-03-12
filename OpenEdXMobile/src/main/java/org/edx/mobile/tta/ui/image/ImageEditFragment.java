package org.edx.mobile.tta.ui.image;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.image.view_model.ImageEditModule;


public class ImageEditFragment extends TaBaseFragment {
    public static final String TAG = ImageEditFragment.class.getCanonicalName();
    private ImageEditModule viewModel;
    private Spinner spinner_classes;


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
        spinner_classes =  view.findViewById(R.id.spinner_classes);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spinner_classes, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner_classes.setAdapter(adapter);

        return view;
    }
}

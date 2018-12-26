package com.maurya.mx.mxlib.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maurya.mx.mxlib.R;


/**
 * Created by mukesh on 8/8/18.
 */

public class MxBottomSheetFragment extends BottomSheetDialogFragment {
    private static final String TAG = MxBottomSheetFragment.class.getName();
    private static MxBottomSheetFragment bottomSheetFragment;

    public static MxBottomSheetFragment newInstance() {

//        Bundle args = new Bundle();

        MxBottomSheetFragment fragment = new MxBottomSheetFragment();
//        fragment.setArguments(args);
        return fragment;
    }

    public static void show(FragmentManager manager){
        bottomSheetFragment=MxBottomSheetFragment.newInstance();
        bottomSheetFragment.show(manager,TAG);
    }
    public static void hide(){
        if (bottomSheetFragment!=null)
            bottomSheetFragment.dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mx_common_recyclerview_layout,container,false);
    }
}

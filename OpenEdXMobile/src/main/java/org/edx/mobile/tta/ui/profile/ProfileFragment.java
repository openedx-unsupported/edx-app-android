package org.edx.mobile.tta.ui.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.edx.mobile.R;

import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.image.ImageEditFragment;
import org.edx.mobile.tta.ui.profile.view_model.ProfileViewModel;
import org.edx.mobile.tta.utils.ActivityUtil;


public class ProfileFragment extends TaBaseFragment {
    public static final String TAG = ProfileFragment.class.getCanonicalName();
    ImageView imageprofile;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_profile, new ProfileViewModel(getActivity(), this))
                .getRoot();
        imageprofile = view.findViewById(R.id.imageProfile);
        imageprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.replaceFragmentInActivity(
                        getActivity().getSupportFragmentManager(),
                        ImageEditFragment.newInstance(),
                        R.id.dashboard_fragment,
                        ImageEditFragment.TAG,
                        true,
                        null
                );
            }
        });
        return view;

    }
}

package org.edx.mobile.tta.ui.assistant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.ui.assistant.view_model.AssistantViewModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.util.PermissionsUtil;

public class AssistantFragment extends TaBaseFragment {
    public static final String TAG = AssistantFragment.class.getCanonicalName();
    private int RANK;

    private AssistantViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RANK = BreadcrumbUtil.getCurrentRank() + 1;
        viewModel = new AssistantViewModel(getActivity(), this);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_assistant, viewModel).getRoot();
//        viewModel.getAgenda();
        if (!PermissionsUtil.checkPermissions(Manifest.permission.RECORD_AUDIO,getActivity())){
          PermissionsUtil.requestPermissions(119,new String[]{Manifest.permission.RECORD_AUDIO},this);
        }
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }else{
            showShortSnack("Record audio permission is required for conversation.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.assistant.name()));
    }
}

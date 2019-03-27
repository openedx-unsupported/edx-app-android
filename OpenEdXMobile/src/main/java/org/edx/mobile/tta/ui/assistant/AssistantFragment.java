package org.edx.mobile.tta.ui.assistant;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.assistant.view_model.AssistantViewModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;

public class AssistantFragment extends TaBaseFragment {
    public static final String TAG = AssistantFragment.class.getCanonicalName();

    private AssistantViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new AssistantViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_assistant, viewModel).getRoot();
//        viewModel.getAgenda();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.onResume();
    }
}

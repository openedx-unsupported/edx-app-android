package org.edx.mobile.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentAuthPanelBinding;

public class LogInSignUpFragment extends BaseFragment {

    @Inject
    protected IEdxEnvironment environment;

    FragmentAuthPanelBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_auth_panel, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (environment.getConfig().isNewLogistrationEnabled()) {
            binding.logIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(environment.getRouter().getLogInIntent());
                }
            });
            binding.signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    environment.getSegment().trackUserSignUpForAccount();
                    startActivity(environment.getRouter().getRegisterIntent());
                }
            });
        } else {
            binding.container.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (environment.getLoginPrefs().getUsername() != null) {
            binding.container.setVisibility(View.GONE);
        }
    }
}

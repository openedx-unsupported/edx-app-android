package org.edx.mobile.view;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentAccountBinding;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.tta.data.DataManager;
import org.edx.mobile.tta.data.constants.Constants;
import org.edx.mobile.tta.data.local.db.table.Program;
import org.edx.mobile.tta.data.local.db.table.Section;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.ui.programs.selectSection.SelectSectionActivity;
import org.edx.mobile.tta.ui.programs.selectSection.viewModel.SelectSectionViewModel;
import org.edx.mobile.tta.ui.programs.selectprogram.SelectProgramActivity;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.util.Config;

import java.util.List;

public class AccountFragment extends BaseFragment {
    private static final String TAG = AccountFragment.class.getCanonicalName();
    private FragmentAccountBinding binding;

    @Inject
    private Config config;

    @Inject
    private IEdxEnvironment environment;

    @Inject
    private LoginPrefs loginPrefs;

    private DataManager mDataManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false);
        mDataManager = DataManager.getInstance(getActivity());

        if (config.isUserProfilesEnabled()) {
            binding.profileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    environment.getRouter().showUserProfile(getActivity(),
                            loginPrefs.getUsername());
                }
            });
        } else {
            binding.profileBtn.setVisibility(View.GONE);
        }

        binding.settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showSettings(getActivity());
            }
        });

        binding.feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().showFeedbackScreen(getActivity(), getString(R.string.email_subject));
            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                environment.getRouter().performManualLogout(getActivity(),
                        environment.getAnalyticsRegistry(), environment.getNotificationDelegate());
            }
        });

//        binding.activateTutorialBtn.setOnClickListener(v -> {
//
//        });

        binding.changeProgBtn.setOnClickListener(v -> {
//            if (!Constants.isSinglePrg) {
//                ActivityUtil.gotoPage(getActivity(), SelectProgramActivity.class);
//                Constants.isSinglePrg = false;
//            }
//            else if (!Constants.isSingleRow){
//                ActivityUtil.gotoPage(getActivity(), SelectSectionActivity.class);
//                Constants.isSingleRow = false;
//            } else {
//                Toast.makeText(getActivity(),"Only single program & section exist", Toast.LENGTH_SHORT).show();
//            }
            mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
                @Override
                public void onSuccess(List<Program> data) {
                    if (data.size() == 0){
                        ActivityUtil.gotoPage(getActivity(), LandingActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }
                    else if (data.size() == 1) {
                        mDataManager.getLoginPrefs().setProgramTitle(data.get(0).getTitle());
                        mDataManager.getLoginPrefs().setProgramId(data.get(0).getId());
                        Constants.isSinglePrg = true;
                        mDataManager.getLoginPrefs().setParentId(data.get(0).getParent_id());

                        getSection();
                    } else {

                        ActivityUtil.gotoPage(getActivity(), SelectProgramActivity.class,
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    }

                }

                @Override
                public void onFailure(Exception e) {

                    ActivityUtil.gotoPage(getActivity(), LandingActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
            });
        });




        binding.tvVersionNo.setText(String.format("%s %s %s", getString(R.string.label_version),
                BuildConfig.VERSION_NAME, environment.getConfig().getEnvironmentDisplayName()));

        return binding.getRoot();
    }
    private void getSection() {
        mDataManager.getSections(mDataManager.getLoginPrefs().getProgramId(), new OnResponseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> data) {

                if (data.size() == 1) {
                    mDataManager.getLoginPrefs().setSectionId(data.get(0).getId());
                    mDataManager.getLoginPrefs().setRole(data.get(0).getRole());

                    Constants.isSingleRow = true;


                    Snackbar.make(binding.clMain, "Single program and section exists for this user",
                            Snackbar.LENGTH_LONG).show();
//                    ActivityUtil.gotoPage(getActivity(), LandingActivity.class,
//                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                } else {
                    ActivityUtil.gotoPage(getActivity(), SelectSectionActivity.class,
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                }

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
}

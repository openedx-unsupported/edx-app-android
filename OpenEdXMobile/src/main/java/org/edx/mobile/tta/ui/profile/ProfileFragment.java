package org.edx.mobile.tta.ui.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.maurya.mx.mxlib.view.MxFiniteRecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.profile.view_model.ProfileViewModel;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.tta.utils.BreadcrumbUtil;

public class ProfileFragment extends TaBaseFragment {
    public static final String TAG = ProfileFragment.class.getCanonicalName();
    private static final int RANK = 2;

    private ProfileViewModel viewModel;
    private ProfileOptionsBottomSheet bottomSheet;
    private ImageButton optionsBtn;
    private MxFiniteRecyclerView recyclerView;
    private LinearLayout llPoints;
    private ProgressBar progressBar;
    private LinearLayout llPadaav;

    private boolean bottomSheetOpened;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ProfileViewModel(getActivity(), this);
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.profile.name()));

        analytic.addMxAnalytics_db(null, Action.ViewProfile, Nav.profile.name(), Source.Mobile, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_profile, viewModel)
                .getRoot();

        optionsBtn = view.findViewById(R.id.profile_options_btn);
        optionsBtn.setOnClickListener(v -> {
            if (!bottomSheetOpened){
                bottomSheet.show(getActivity().getSupportFragmentManager(), ProfileOptionsBottomSheet.TAG);
                bottomSheetOpened = true;
            }
        });

        /*progressBar = view.findViewById(R.id.progressBar);
        progressBar.setOnClickListener( v -> {
            viewModel.onCliCkLl();
        });

        llPadaav = view.findViewById(R.id.llPadaav);
        llPadaav.setOnClickListener(v -> {
            viewModel.onCliCkLl();
        });

        llPoints =  view.findViewById(R.id.llPoints);
        llPoints.setOnClickListener(v -> {
            viewModel.onCliCkLl();
        });

        recyclerView = view.findViewById(R.id.badge_finite_list);
        recyclerView.setOnMoreButtonClickListener(v ->  {
            viewModel.onCliCkMoreButton();
        });*/

        bottomSheet = ProfileOptionsBottomSheet.newInstance(v -> {
            if (v == null){
                bottomSheetOpened = false;
                return;
            }

            switch (v.getId()){
                case R.id.ivClose:
                    bottomSheet.dismiss();
                    bottomSheetOpened = false;
                    break;

                case R.id.edit_profile_layout:
                    bottomSheet.dismiss();
                    bottomSheetOpened = false;
                    ActivityUtil.replaceFragmentInActivity(
                            getActivity().getSupportFragmentManager(),
                            EditProfileFragment.newInstance(viewModel.profileModel, viewModel.profileImage,
                                    viewModel.account, viewModel.searchFilter),
                            R.id.dashboard_fragment,
                            EditProfileFragment.TAG,
                            true,
                            null
                    );
                    break;

                case R.id.change_password_layout:
                    bottomSheet.dismiss();
                    bottomSheetOpened = false;
                    ActivityUtil.replaceFragmentInActivity(
                            getActivity().getSupportFragmentManager(),
                            new ChangePasswordFragment(),
                            R.id.dashboard_fragment,
                            ChangePasswordFragment.TAG,
                            true,
                            null
                    );
                    break;

                case R.id.contact_us_layout:
                    bottomSheet.dismiss();
                    bottomSheetOpened = false;
                    ActivityUtil.replaceFragmentInActivity(
                            getActivity().getSupportFragmentManager(),
                            new ContactUsFragment(),
                            R.id.dashboard_fragment,
                            ContactUsFragment.TAG,
                            true,
                            null
                    );
                    break;

                case R.id.btn_yes:
                    viewModel.logout();
                    break;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.profile.name()));
    }
}

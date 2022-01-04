package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentCourseUnitGradeBinding;
import org.edx.mobile.inapppurchases.BillingProcessor;
import org.edx.mobile.model.api.AuthorizationDenialReason;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.UiUtils;

public class CourseUnitMobileNotSupportedFragment extends CourseUnitFragment {
    private FragmentCourseUnitGradeBinding binding;
    private BillingProcessor billingProcessor;

    public static CourseUnitMobileNotSupportedFragment newInstance(@NonNull CourseComponent unit, @NonNull boolean isSelfPaced, @NonNull String price) {
        final CourseUnitMobileNotSupportedFragment fragment = new CourseUnitMobileNotSupportedFragment();
        final Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
        args.putBoolean(Router.EXTRA_IS_SELF_PACED, isSelfPaced);
        args.putString(Router.EXTRA_PRICE, price);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_course_unit_grade, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (unit.getAuthorizationDenialReason() == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS) {
            if (environment.getRemoteFeaturePrefs().isValuePropEnabled()) {
                boolean isSelfPaced = getBooleanArgument(Router.EXTRA_IS_SELF_PACED, false);
                String price = getStringArgument(Router.EXTRA_PRICE);
                binding.containerLayoutNotAvailable.setVisibility(View.GONE);
                binding.llGradedContentLayout.setVisibility(View.VISIBLE);
                setUpUpgradeButton(isSelfPaced, price);
                binding.toggleShow.setOnClickListener(v -> {
                    boolean showMore = binding.layoutUpgradeFeature.containerLayout.getVisibility() == View.GONE;
                    binding.layoutUpgradeFeature.containerLayout.setVisibility(showMore ? View.VISIBLE : View.GONE);
                    binding.toggleShow.setText(getText(showMore ? R.string.course_modal_graded_assignment_show_less : R.string.course_modal_graded_assignment_show_more));
                    environment.getAnalyticsRegistry().trackValuePropShowMoreLessClicked(unit.getCourseId(),
                            unit.getId(), price, isSelfPaced, showMore);
                });
            } else {
                binding.containerLayoutNotAvailable.setVisibility(View.VISIBLE);
                binding.llGradedContentLayout.setVisibility(View.GONE);
                binding.contentErrorIcon.setImageDrawable(UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.ic_lock));
                binding.notAvailableMessage.setText(R.string.not_available_on_mobile);
                binding.notAvailableMessage2.setVisibility(View.GONE);
            }
        } else {
            binding.containerLayoutNotAvailable.setVisibility(View.VISIBLE);
            binding.llGradedContentLayout.setVisibility(View.GONE);
            binding.contentErrorIcon.setImageDrawable(UiUtils.INSTANCE.getDrawable(requireContext(), R.drawable.ic_laptop));
            binding.notAvailableMessage.setText(unit.getType() == BlockType.VIDEO ?
                    R.string.video_only_on_web_short : R.string.assessment_not_available);
            binding.notAvailableMessage2.setVisibility(View.VISIBLE);
        }

        binding.viewOnWebButton.setOnClickListener(v -> {
            BrowserUtil.open(getActivity(), unit.getWebUrl(), true);
            environment.getAnalyticsRegistry().trackOpenInBrowser(unit.getId(), unit.getCourseId(),
                    unit.isMultiDevice(), unit.getBlockId());
        });
    }

    private void setUpUpgradeButton(boolean isSelfPaced, String price) {
        if (environment.getConfig().isIAPEnabled()) {
            binding.layoutUpgradeBtn.getRoot().setVisibility(View.VISIBLE);
            binding.layoutUpgradeBtn.btnUpgrade.setOnClickListener(v -> {
                enableUpgradeButton(false);
                purchaseProduct("org.edx.mobile.test_product");
                environment.getAnalyticsRegistry().trackUpgradeNowClicked(unit.getCourseId(),
                        price, unit.getId(), isSelfPaced);
            });

            billingProcessor = new BillingProcessor(requireContext(), new BillingProcessor.BillingFlowListeners() {
                @Override
                public void onPurchaseCancel() {
                    enableUpgradeButton(true);
                }

                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                    // Nothing do here
                }

                @Override
                public void onBillingServiceDisconnected() {
                    // Nothing do here
                }

                @Override
                public void onPurchaseComplete(@NonNull Purchase purchase) {
                    enableUpgradeButton(true);
                }
            });
        } else {
            binding.layoutUpgradeBtn.getRoot().setVisibility(View.GONE);
        }
    }

    private void enableUpgradeButton(boolean enable) {
        binding.layoutUpgradeBtn.btnUpgrade.setVisibility(enable ? View.VISIBLE : View.GONE);
        binding.layoutUpgradeBtn.loadingIndicator.setVisibility(!enable ? View.VISIBLE : View.GONE);
    }

    private void purchaseProduct(String productId) {
        if (billingProcessor != null) {
            billingProcessor.purchaseItem(requireActivity(), productId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (unit.getAuthorizationDenialReason() == AuthorizationDenialReason.FEATURE_BASED_ENROLLMENTS && environment.getRemoteFeaturePrefs().isValuePropEnabled()) {
            environment.getAnalyticsRegistry().trackLockedContentTapped(unit.getCourseId(), unit.getBlockId());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billingProcessor != null) {
            billingProcessor.disconnect();
        }
    }
}

package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentCourseUnitGradeBinding;
import org.edx.mobile.model.api.AuthorizationDenialReason;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.UiUtils;
import org.edx.mobile.view.dialog.CourseModalDialogFragment;

public class CourseUnitMobileNotSupportedFragment extends CourseUnitFragment {
    private FragmentCourseUnitGradeBinding binding;

    public static CourseUnitMobileNotSupportedFragment newInstance(@NonNull CourseComponent unit) {
        final CourseUnitMobileNotSupportedFragment fragment = new CourseUnitMobileNotSupportedFragment();
        final Bundle args = new Bundle();
        args.putSerializable(Router.EXTRA_COURSE_UNIT, unit);
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
                binding.containerLayoutNotAvailable.setVisibility(View.GONE);
                binding.llGradedContentLayout.setVisibility(View.VISIBLE);
                binding.btnLearnMore.setOnClickListener(v ->
                        CourseModalDialogFragment.newInstance(environment.getConfig().getPlatformName(),
                                false, unit.getCourseId(), unit.getBlockId())
                                .show(getChildFragmentManager(), CourseModalDialogFragment.TAG));
                environment.getAnalyticsRegistry().trackLockedContentTapped(unit.getCourseId(), unit.getBlockId());
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
}

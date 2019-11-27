package org.edx.mobile.view;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentCourseUnitGradeBinding;
import org.edx.mobile.model.api.AuthorizationDenialReason;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.util.BrowserUtil;

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
            binding.contentErrorIcon.setIcon(FontAwesomeIcons.fa_lock);
            binding.notAvailableMessage.setText(R.string.not_available_on_mobile);
            binding.notAvailableMessage2.setVisibility(View.GONE);
        } else {
            binding.contentErrorIcon.setIcon(FontAwesomeIcons.fa_laptop);
            binding.notAvailableMessage.setText(unit.getType() == BlockType.VIDEO ?
                    R.string.video_only_on_web_short : R.string.assessment_not_available);
            binding.notAvailableMessage2.setVisibility(View.VISIBLE);
        }

        binding.viewOnWebButton.setOnClickListener(v -> {
            BrowserUtil.open(getActivity(), unit.getWebUrl());
            environment.getAnalyticsRegistry().trackOpenInBrowser(unit.getId(), unit.getCourseId(),
                    unit.isMultiDevice(), unit.getBlockId());
        });
    }
}

package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class CertificateActivity extends BaseSingleFragmentActivity {

    public static Intent newIntent(@NonNull Context context, @NonNull EnrolledCoursesResponse courseData) {
        return new Intent(context, CertificateActivity.class)
                .putExtra(CertificateFragment.ENROLLMENT, courseData);
    }

    @Override
    public Fragment getFirstFragment() {
        return new CertificateFragment();
    }
}

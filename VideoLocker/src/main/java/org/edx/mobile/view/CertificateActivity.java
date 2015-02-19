package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

public class CertificateActivity extends BaseSingleFragmentActivity {

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(getString(R.string.tab_label_certificate));
    }

    @Override
    public Fragment getFirstFragment() {

        Fragment frag = new CertificateFragment();

        EnrolledCoursesResponse courseData = (EnrolledCoursesResponse) getIntent().getSerializableExtra(CertificateFragment.ENROLLMENT);
        if (courseData != null) {

            Bundle bundle = new Bundle();
            bundle.putSerializable(CertificateFragment.ENROLLMENT, courseData);
            frag.setArguments(bundle);

        }

        return frag;
    }

}

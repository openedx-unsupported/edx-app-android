package org.edx.mobile.tta.ui.course.view_model;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.ui.base.BasePagerAdapter;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class CourseDashboardViewModel extends BaseViewModel {

    public CourseDashboardPagerAdapter adapter;

    public Content course;

    public CourseDashboardViewModel(Context context, TaBaseFragment fragment, Content course) {
        super(context, fragment);
        this.course = course;
        adapter = new CourseDashboardPagerAdapter(mActivity.getSupportFragmentManager());
    }

    public class CourseDashboardPagerAdapter extends BasePagerAdapter {

        public CourseDashboardPagerAdapter(FragmentManager fm) {
            super(fm);
        }
    }

    public void getCourseData(){

    }

}

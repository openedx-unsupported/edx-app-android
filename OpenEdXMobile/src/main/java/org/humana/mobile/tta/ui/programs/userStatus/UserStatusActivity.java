package org.humana.mobile.tta.ui.programs.userStatus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import org.humana.mobile.R;
import org.humana.mobile.model.api.EnrolledCoursesResponse;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.programs.userStatus.viewModel.UserStatusViewModel;
import org.humana.mobile.view.Router;

public class UserStatusActivity extends BaseVMActivity {
    UserStatusViewModel viewModel;
    private EnrolledCoursesResponse course;
    private String username;
    private Boolean showCurrentPeriod;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null){
            getBundledData(getIntent().getExtras());
        } else if (savedInstanceState != null){
            getBundledData(savedInstanceState);
        }
        viewModel = new UserStatusViewModel(this, username, course, showCurrentPeriod);
        binding(R.layout.t_activity_user_status, viewModel);

        setSupportActionBar(findViewById(R.id.toolbar));
    }
    private void getBundledData(Bundle parameters){

        if (parameters.containsKey(Router.EXTRA_COURSE_DATA)){
            course = (EnrolledCoursesResponse) parameters.getSerializable(Router.EXTRA_COURSE_DATA);
        }

        if (parameters.containsKey(Router.EXTRA_USERNAME)){
            username =  parameters.getString(Router.EXTRA_USERNAME);
        }
        if (parameters.containsKey("isCurrent")){
            showCurrentPeriod =  parameters.getBoolean("isCurrent");
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}

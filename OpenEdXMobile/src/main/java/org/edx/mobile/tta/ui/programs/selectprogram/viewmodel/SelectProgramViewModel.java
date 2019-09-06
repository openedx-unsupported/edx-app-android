package org.edx.mobile.tta.ui.programs.selectprogram.viewmodel;

import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.data.local.db.table.Program;
import org.edx.mobile.tta.data.local.db.table.Section;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.landing.LandingActivity;
import org.edx.mobile.tta.utils.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

public class SelectProgramViewModel extends BaseViewModel {

    public List<RegistrationOption> programs;
    public List<RegistrationOption> sections;
    public String programId, sectionId;

    public SelectProgramViewModel(BaseVMActivity activity) {
        super(activity);
        programId = mDataManager.getLoginPrefs().getProgramId();
        sectionId = mDataManager.getLoginPrefs().getSectionId();
        programs = new ArrayList<>();
        sections = new ArrayList<>();
    }

    public void fetchPrograms(OnResponseCallback<List<Program>> callback) {
        programs.clear();

        mActivity.showLoading();
        mDataManager.getPrograms(new OnResponseCallback<List<Program>>() {
            @Override
            public void onSuccess(List<Program> data) {
                for (Program program : data) {
                    programs.add(new RegistrationOption(program.getTitle(), program.getId()));
                }
                mActivity.hideLoading();
                callback.onSuccess(data);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                callback.onFailure(e);
            }
        });

    }



    public void save() {
        mDataManager.getLoginPrefs().setProgramId(programId);
        mDataManager.getLoginPrefs().setSectionId(sectionId);

        mActivity.finish();
        ActivityUtil.gotoPage(mActivity, LandingActivity.class);
    }

}

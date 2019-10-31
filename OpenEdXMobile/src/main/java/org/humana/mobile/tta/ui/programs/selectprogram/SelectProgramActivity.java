package org.humana.mobile.tta.ui.programs.selectprogram;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.humana.mobile.R;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.programs.selectprogram.viewmodel.SelectProgramViewModel2;

public class SelectProgramActivity extends BaseVMActivity {

    private SelectProgramViewModel2 viewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new SelectProgramViewModel2(this);
        savedInstanceState = new Bundle();
        if (savedInstanceState != null) {
            savedInstanceState = getIntent().getExtras();
            if (savedInstanceState != null) {
                boolean prev = savedInstanceState.getBoolean("isPrev", false);
                viewModel.isPrev.set(prev);
            }
        }

        binding(R.layout.t_activity_select_program_section, viewModel);



//        fieldsLayout = findViewById(R.id.fields_layout);
//        fabSave = findViewById(R.id.fab_save);
//
//        setupForm();
//        getPrograms();
    }

 /*   private void getPrograms() {

        viewModel.fetchPrograms(new OnResponseCallback<List<Program>>() {
            @Override
            public void onSuccess(List<Program> data) {
                programSpinner.setItems(viewModel.programs, viewModel.programId == null ? null :
                        new RegistrationOption(viewModel.programId, viewModel.programId));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void getSections() {
        if (viewModel.programId == null) {
            viewModel.sections.clear();
            sectionSpinner.setItems(viewModel.sections, null);
            return;
        }

        viewModel.fetchSections(new OnResponseCallback<List<Section>>() {
            @Override
            public void onSuccess(List<Section> data) {
                sectionSpinner.setItems(viewModel.sections, viewModel.sectionId == null ? null :
                        new RegistrationOption(viewModel.sectionId, viewModel.sectionId));
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void setupForm() {

        programSpinner = ViewUtil.addOptionSpinner(fieldsLayout, "Programs", viewModel.programs,
                viewModel.programId == null ? null :
                        new RegistrationOption(viewModel.programId, viewModel.programId));
        programSpinner.setMandatory(true);

        ViewUtil.addEmptySpace(fieldsLayout, (int) getResources().getDimension(R.dimen._60dp));

        sectionSpinner = ViewUtil.addOptionSpinner(fieldsLayout, "Sections", viewModel.sections,
                viewModel.sectionId == null ? null :
                        new RegistrationOption(viewModel.sectionId, viewModel.sectionId));
        sectionSpinner.setMandatory(true);

//        setListeners();

    }

    private void setListeners() {

        fabSave.setOnClickListener(v -> {
            if (!validate()) {
                return;
            }

            viewModel.save();
        });

        programSpinner.setOnItemSelectedListener((view, item) -> {
            if (item == null) {
                viewModel.programId = null;
            } else {
                viewModel.programId = item.getValue();
            }

            getSections();
        });

        sectionSpinner.setOnItemSelectedListener((view, item) -> {
            if (item == null) {
                viewModel.sectionId = null;
            } else {
                viewModel.sectionId = item.getValue();
            }
        });

    }*/

//    private boolean validate() {
//        boolean valid = true;
//        if (!programSpinner.validate()) {
//            valid = false;
//            programSpinner.setError("Required");
//        }
//        if (!sectionSpinner.validate()) {
//            valid = false;
//            sectionSpinner.setError("Required");
//        }
//
//        return valid;
//    }
}

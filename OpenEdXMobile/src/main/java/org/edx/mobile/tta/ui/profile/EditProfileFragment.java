package org.edx.mobile.tta.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.joanzapata.iconify.internal.Animation;

import org.edx.mobile.R;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.task.Task;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.model.search.FilterTag;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.custom.FormEditText;
import org.edx.mobile.tta.ui.custom.FormMultiSpinner;
import org.edx.mobile.tta.ui.custom.FormSpinner;
import org.edx.mobile.tta.ui.custom.NonScrollListView;
import org.edx.mobile.tta.ui.profile.view_model.EditProfileViewModel;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.tta.utils.DataUtil;
import org.edx.mobile.tta.utils.ViewUtil;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.FormField;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.user.SetAccountImageTask;
import org.edx.mobile.util.PermissionsUtil;
import org.edx.mobile.util.images.ImageCaptureHelper;
import org.edx.mobile.util.images.ImageUtils;
import org.edx.mobile.view.CropImageActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EditProfileFragment extends TaBaseFragment {
    public static final String TAG = EditProfileFragment.class.getCanonicalName();
    private static final int RANK = 3;

    private static final int EDIT_FIELD_REQUEST = 1;
    private static final int CAPTURE_PHOTO_REQUEST = 2;
    private static final int CHOOSE_PHOTO_REQUEST = 3;
    private static final int CROP_PHOTO_REQUEST = 4;

    @NonNull
    private final ImageCaptureHelper helper = new ImageCaptureHelper();

    private EditProfileViewModel viewModel;

    private ProfileModel profileModel;
    private ProfileImage profileImage;
    private Account account;
    private SearchFilter searchFilter;
    /*private List<String> classTags, skillTags;
    private List<String> selectedClassTags, selectedSkillTags;

    public ArrayAdapter<String> classesAdapter;
    public ArrayAdapter<String> skillsAdapter;

    private NonScrollListView classesList, skillsList;
    private LinearLayout classesExpandedLayout, skillsExpandedLayout;
    private LinearLayout classesLayout, skillsLayout;
    private Button btnSave;*/
    private ImageView userImage;

    private LinearLayout userInfoLayout;
    private FormEditText etFirstName;
    private FormSpinner stateSpinner;
    private FormSpinner districtSpinner;
    private FormSpinner blockSpinner;
    private FormSpinner professionSpinner;
    private FormSpinner genderSpinner;
    private FormMultiSpinner classTaughtSpinner;
    private FormMultiSpinner skillsSpinner;
    private FormSpinner dietSpinner;
    private FormEditText etPmis;
    private Button btn;

    private String tagLabel;

    private boolean classesOpened, skillsOpened;

    public static EditProfileFragment newInstance(ProfileModel profileModel, ProfileImage profileImage,
                                                  Account account, SearchFilter searchFilter){
        EditProfileFragment fragment = new EditProfileFragment();
        fragment.profileModel = profileModel;
        fragment.profileImage = profileImage;
        fragment.account = account;
        fragment.searchFilter = searchFilter;

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel =  new EditProfileViewModel(getActivity(),this,
                profileModel, profileImage, account, searchFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding(inflater, container, R.layout.t_fragment_edit_profile, viewModel)
                .getRoot();

        /*classesList = view.findViewById(R.id.classes_multi_choice_list);
        skillsList = view.findViewById(R.id.skills_multi_choice_list);
        classesExpandedLayout = view.findViewById(R.id.classes_expanded);
        skillsExpandedLayout = view.findViewById(R.id.skills_expanded);
        classesLayout = view.findViewById(R.id.classes_layout);
        skillsLayout = view.findViewById(R.id.skills_layout);
        btnSave = view.findViewById(R.id.btn_save);*/
        userImage = view.findViewById(R.id.user_image);

        userInfoLayout = view.findViewById(R.id.user_info_fields_layout);

        if (profileModel != null && profileModel.getTagLabel() != null){
            tagLabel = profileModel.getTagLabel().trim();
        }

        viewModel.getData();
        setupForm();
        getBlocks();
        getClassesAndSkills();

        /*classTags = new ArrayList<>();
        skillTags = new ArrayList<>();
        selectedClassTags = new ArrayList<>();
        selectedSkillTags = new ArrayList<>();

        setupLists();*/

        /*classesList.setOnItemClickListener((parent, view1, position, id) -> {
            String c = (String) parent.getItemAtPosition(position);
            if (selectedClassTags.contains(c)){
                selectedClassTags.remove(c);
            } else {
                selectedClassTags.add(c);
            }
        });

        skillsList.setOnItemClickListener((parent, view1, position, id) -> {
            String s = (String) parent.getItemAtPosition(position);
            if (selectedSkillTags.contains(s)){
                selectedSkillTags.remove(s);
            } else {
                selectedSkillTags.add(s);
            }
        });

        classesLayout.setOnClickListener(this);
        skillsLayout.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        view.setOnClickListener(this);*/

        return view;
    }

    private void getBlocks() {
        if (viewModel.currentState == null || viewModel.currentDistrict == null) {
            return;
        }
        viewModel.getActivity().showLoading();
        viewModel.getBlocks(
                new OnResponseCallback<List<RegistrationOption>>() {
                    @Override
                    public void onSuccess(List<RegistrationOption> data) {
                        viewModel.getActivity().hideLoading();
                        blockSpinner.setItems(viewModel.blocks,
                                profileModel.block == null ? null : new RegistrationOption(profileModel.block, profileModel.block));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        viewModel.getActivity().hideLoading();
                    }
                });
    }

    private void getClassesAndSkills() {

        viewModel.getClassesAndSkills(new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                List<RegistrationOption> selectedOptions = null;
                if (tagLabel != null && tagLabel.length() > 0){
                    selectedOptions = new ArrayList<>();
                    for (String chunk: tagLabel.split(" ")){
                        String[] duet = chunk.split("_");
                        if (duet[0].equals(viewModel.classesSectionName)){
                            selectedOptions.add(new RegistrationOption(duet[1], duet[1]));
                        }
                    }
                }
                classTaughtSpinner.setItems(data, selectedOptions);
            }

            @Override
            public void onFailure(Exception e) {

            }
        }, new OnResponseCallback<List<RegistrationOption>>() {
            @Override
            public void onSuccess(List<RegistrationOption> data) {
                List<RegistrationOption> selectedOptions = null;
                if (tagLabel != null && tagLabel.length() > 0){
                    selectedOptions = new ArrayList<>();
                    for (String chunk: tagLabel.split(" ")){
                        String[] duet = chunk.split("_");
                        if (duet[0].equals(viewModel.skillSectionName)){
                            selectedOptions.add(new RegistrationOption(duet[1], duet[1]));
                        }
                    }
                }
                skillsSpinner.setItems(data, selectedOptions);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void setupForm() {

        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._14dp));

        etFirstName = ViewUtil.addFormEditText(userInfoLayout, "Name/नाम");
        etFirstName.setText(profileModel.name);
        etFirstName.setMandatory(true);

        stateSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "State/राज्य", viewModel.states,
                profileModel.state == null ? null : new RegistrationOption(profileModel.state, profileModel.state));
        stateSpinner.setMandatory(true);

        districtSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "District/" + "जिला", viewModel.districts,
                profileModel.district == null ? null : new RegistrationOption(profileModel.district, profileModel.district));
        districtSpinner.setMandatory(true);

        blockSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Block/तहसील", viewModel.blocks,
                profileModel.block == null ? null : new RegistrationOption(profileModel.block, profileModel.block));
        blockSpinner.setMandatory(true);

        professionSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Profession/व्यवसाय", viewModel.professions,
                profileModel.title == null ? null : new RegistrationOption(profileModel.title, profileModel.title));

        genderSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "Gender/लिंग", viewModel.genders,
                profileModel.gender == null ? null : new RegistrationOption(profileModel.gender, profileModel.gender));

        classTaughtSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "Classes Taught/पढ़ाई गई कक्षा",
                viewModel.classesTaught, null);
        skillsSpinner = ViewUtil.addMultiOptionSpinner(userInfoLayout, "Skills/कौशल",
                viewModel.skills, null);

        etPmis = ViewUtil.addFormEditText(userInfoLayout, "PMIS Code/पी इम आइ इस कोड");
        if (profileModel.pmis_code != null) {
            etPmis.setText(profileModel.pmis_code);
        }
        etPmis.setShowTv(getActivity().getString(R.string.please_insert_valide_pmis_code));

        dietSpinner = ViewUtil.addOptionSpinner(userInfoLayout, "DIET Code/डी आइ इ टी कोड", viewModel.dietCodes,
                profileModel.diet_code == null ? null : new RegistrationOption(profileModel.diet_code, profileModel.diet_code));

        btn = ViewUtil.addButton(userInfoLayout, "Submit");
        ViewUtil.addEmptySpace(userInfoLayout, (int) getResources().getDimension(R.dimen._50px));

        setListeners();
    }

    private void setListeners() {
        btn.setOnClickListener(v -> {
            if (!validate()) {
                return;
            }
            Bundle parameters = new Bundle();
            parameters.putString("name", etFirstName.getText().trim());
            parameters.putString("state", stateSpinner.getSelectedOption().getValue());
            parameters.putString("district", viewModel.currentDistrict);
            parameters.putString("block", blockSpinner.getSelectedOption().getName());
            parameters.putString("title", professionSpinner.getSelectedOption().getValue());
            parameters.putString("gender", genderSpinner.getSelectedOption().getName());

            StringBuilder builder = new StringBuilder();
            if (classTaughtSpinner.getSelectedOptions() != null) {
                for (RegistrationOption option: classTaughtSpinner.getSelectedOptions()){
                    builder.append(viewModel.classesSectionName).append("_").append(option.getName()).append(" ");
                }
            }
            if (skillsSpinner.getSelectedOptions() != null) {
                for (RegistrationOption option: skillsSpinner.getSelectedOptions()){
                    builder.append(viewModel.skillSectionName).append("_").append(option.getName()).append(" ");
                }
            }
            if (builder.length() > 0){
                builder.deleteCharAt(builder.length() - 1);
            }
            parameters.putString("tag_label", builder.toString());

            parameters.putString("pmis_code", etPmis.getText());
            parameters.putString("diet_code", dietSpinner.getSelectedOption().getName());
            viewModel.submit(parameters);
        });

        stateSpinner.setOnItemSelectedListener((view, item) -> {
            viewModel.currentState = item.getName();

            viewModel.districts.clear();
            viewModel.districts = DataUtil.getDistrictsByStateName(viewModel.currentState);
            districtSpinner.setItems(viewModel.districts,
                    profileModel.district == null ? null : new RegistrationOption(profileModel.district, profileModel.district));

            viewModel.dietCodes.clear();
            viewModel.dietCodes = DataUtil.getAllDietCodesOfState(viewModel.currentState);
            dietSpinner.setItems(viewModel.dietCodes,
                    profileModel.diet_code == null ? null : new RegistrationOption(profileModel.diet_code, profileModel.diet_code));
        });

        districtSpinner.setOnItemSelectedListener((view, item) -> {
            viewModel.currentDistrict = item.getName();
            getBlocks();
        });
    }

    private boolean validate(){
        boolean valid = true;
        if (!etFirstName.validate()){
            valid = false;
            etFirstName.setError("Required");
        }
        if (!stateSpinner.validate()){
            valid = false;
            stateSpinner.setError("Required");
        }
        if (!districtSpinner.validate()){
            valid = false;
            districtSpinner.setError("Required");
        }
        if (!blockSpinner.validate()){
            valid = false;
            blockSpinner.setError("Required");
        }
        if (!professionSpinner.validate()){
            valid = false;
            professionSpinner.setError("Required");
        }
        if (!genderSpinner.validate()){
            valid = false;
            genderSpinner.setError("Required");
        }
        if (!classTaughtSpinner.validate()){
            valid = false;
            classTaughtSpinner.setError("Required");
        }
        if (!etPmis.validate()){
            valid = false;
            etPmis.setError("Required");
        }
        if (!dietSpinner.validate()){
            valid = false;
            dietSpinner.setError("Required");
        }

        return valid;
    }

    /*private void setupLists() {

        if (searchFilter == null){
            return;
        }

        String[] section_tag_list = new String[]{};
        String tagLabel = null;

        if (profileModel != null && profileModel.getTagLabel() != null){
            tagLabel = profileModel.getTagLabel().trim();
        }

        if (tagLabel != null && tagLabel.length() > 0) {
            section_tag_list = tagLabel.split(" ");

            Map<String, List<String>> sectionTagsMap = new HashMap<>();
            for (String section_tag: section_tag_list){
                String[] duet = section_tag.split("_");
                if (!sectionTagsMap.containsKey(duet[0])){
                    sectionTagsMap.put(duet[0], new ArrayList<>());
                }
                sectionTagsMap.get(duet[0]).add(duet[1]);
            }

            for (FilterSection section: searchFilter.getResult()){
                if (section.isIn_profile()){
                    if (sectionTagsMap.containsKey(section.getName())){
                        if (section.getName().contains("कक्षा")){
                            selectedClassTags.addAll(sectionTagsMap.get(section.getName()));
                        } else if (section.getName().contains("कौशल")){
                            selectedSkillTags.addAll(sectionTagsMap.get(section.getName()));
                        }
                    }
                }
            }
        }

        if (searchFilter != null && searchFilter.getResult() != null){
            for (FilterSection section: searchFilter.getResult()){
                if (section.isIn_profile() && section.getTags() != null){
                    if (section.getName().contains("कक्षा")){
                        for (FilterTag tag: section.getTags()){
                            classTags.add(tag.getValue());
                        }
                    } else if (section.getName().contains("कौशल")){
                        for (FilterTag tag: section.getTags()){
                            skillTags.add(tag.getValue());
                        }
                    }
                }
            }
        }

        classesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, classTags);
        skillsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_multiple_choice, skillTags);

        classesList.setAdapter(classesAdapter);
        skillsList.setAdapter(skillsAdapter);

        for (String c: selectedClassTags){
            classesList.setItemChecked(classTags.indexOf(c), true);
        }

        for (String s: selectedSkillTags){
            skillsList.setItemChecked(skillTags.indexOf(s), true);
        }

    }*/

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        super.onPermissionGranted(permissions, requestCode);
        switch (requestCode){
            case PermissionsUtil.CAMERA_PERMISSION_REQUEST:
                startActivityForResult(helper.createCaptureIntent(getActivity()), CAPTURE_PHOTO_REQUEST);
                break;

            case PermissionsUtil.READ_STORAGE_PERMISSION_REQUEST:
                final Intent galleryIntent = new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, CHOOSE_PHOTO_REQUEST);
                break;
        }
    }

    @Override
    public void onPermissionDenied(String[] permissions, int requestCode) {
        super.onPermissionDenied(permissions, requestCode);
        viewModel.getActivity().showLongSnack("Permission denied");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CAPTURE_PHOTO_REQUEST: {
                Uri imageUri = helper.getImageUriFromResult();
                if (null != imageUri) {
                    // Rotate image according to exif tag, because exif rotation is creating rotation issues
                    // in thirdparty libraries used for zooming and cropping in this project. [MA-3175]
                    final Uri rotatedImageUri = ImageUtils.rotateImageAccordingToExifTag(getContext(), imageUri);
                    if (null != rotatedImageUri) {
                        imageUri = rotatedImageUri;
                    }
                    startActivityForResult(CropImageActivity.newIntent(getActivity(), imageUri, true), CROP_PHOTO_REQUEST);
                }
                break;
            }
            case CHOOSE_PHOTO_REQUEST: {
                final Uri imageUri = data.getData();
                if (null != imageUri) {
                    startActivityForResult(CropImageActivity.newIntent(getActivity(), imageUri, false), CROP_PHOTO_REQUEST);
                }
                break;
            }
            case CROP_PHOTO_REQUEST: {
                viewModel.setImageUri(CropImageActivity.getImageUriFromResult(data));
                viewModel.setCropRect(CropImageActivity.getCropRectFromResult(data));
                Glide.with(getActivity())
                        .load(CropImageActivity.getImageUriFromResult(data))
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(userImage);
                break;
            }
        }
    }

    /*private void collapseTags(){
        classesExpandedLayout.setVisibility(View.GONE);
        skillsExpandedLayout.setVisibility(View.GONE);
        classesOpened = false;
        skillsOpened = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.classes_layout:
                if (classesOpened || skillsOpened){
                    collapseTags();
                } else {
                    classesExpandedLayout.setVisibility(View.VISIBLE);
                    classesOpened = true;
                }
                break;

            case R.id.skills_layout:
                if (classesOpened || skillsOpened){
                    collapseTags();
                } else {
                    skillsExpandedLayout.setVisibility(View.VISIBLE);
                    skillsOpened = true;
                }
                break;

            case R.id.btn_save:
                collapseTags();
                viewModel.setSelectedClassTags(selectedClassTags);
                viewModel.setSelectedSkillTags(selectedSkillTags);
                viewModel.save();
                break;

            default:
                collapseTags();
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        logD("TTA Nav ======> " + BreadcrumbUtil.setBreadcrumb(RANK, Nav.edit.name()));
    }
}

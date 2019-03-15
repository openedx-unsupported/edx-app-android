package org.edx.mobile.tta.ui.profile.view_model;

import android.Manifest;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;


import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.tta.Constants;
import org.edx.mobile.tta.data.model.profile.UpdateMyProfileResponse;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.model.search.FilterTag;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.ProfileImage;
import org.edx.mobile.util.PermissionsUtil;

import java.util.List;

public class EditProfileViewModel extends BaseViewModel  {

    public ProfileModel profileModel;
    public ProfileImage profileImage;
    private Account account;
    private SearchFilter searchFilter;
    private List<String> selectedClassTags, selectedSkillTags;
    private String tagLabel;
    private Uri imageUri;
    private Rect cropRect;

    public ObservableField<String> name = new ObservableField<>("");
    public ObservableBoolean nameValid = new ObservableBoolean();
    public ObservableBoolean imageAddVisible = new ObservableBoolean();

    private boolean imageChanged;
    private boolean profileReceived, imageReceived;
    private boolean profileSuccessful, imageSuccessful;

    public TextWatcher nameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null){
                name.set(s.toString());
                if (name.get().trim().length() > 0){
                    nameValid.set(true);
                } else {
                    nameValid.set(false);
                }
            } else {
                name.set("");
                nameValid.set(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };



    public EditProfileViewModel(Context context, TaBaseFragment fragment,
                                ProfileModel profileModel, ProfileImage profileImage,
                                Account account, SearchFilter searchFilter) {
        super(context, fragment);

        this.profileModel = profileModel;
        this.profileImage = profileImage;
        this.account = account;
        this.searchFilter = searchFilter;

        if (this.profileModel != null){
            name.set(profileModel.name);
        }

        if (profileImage == null || profileImage.getImageUrlLarge() == null){
            imageAddVisible.set(true);
        } else {
            imageAddVisible.set(false);
        }

    }

    public void takePhoto(){
        mFragment.askForPermissions(new String[]{Manifest.permission.CAMERA},
                PermissionsUtil.CAMERA_PERMISSION_REQUEST);
    }

    public void choosePhoto(){
        mFragment.askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PermissionsUtil.READ_STORAGE_PERMISSION_REQUEST);
    }

    public void setSelectedClassTags(List<String> selectedClassTags) {
        this.selectedClassTags = selectedClassTags;
    }

    public void setSelectedSkillTags(List<String> selectedSkillTags) {
        this.selectedSkillTags = selectedSkillTags;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
        imageChanged = true;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
        imageChanged = true;
    }

    public void save(){
        mActivity.showLoading();
        profileReceived = false;
        profileSuccessful = false;
        imageReceived = false;
        imageSuccessful = false;

        StringBuilder builder = new StringBuilder();
        if (searchFilter != null && searchFilter.getResult() != null){
            for (FilterSection section: searchFilter.getResult()){
                if (section.isIn_profile() && section.getTags() != null){
                    for (FilterTag tag: section.getTags()){
                        if (selectedClassTags.contains(tag.getValue()) || selectedSkillTags.contains(tag.getValue())){
                            builder.append(section.getName())
                                    .append("_")
                                    .append(tag.getValue())
                                    .append(" ");
                        }
                    }
                }
            }
        }

        if (builder.length() > 0){
            builder.deleteCharAt(builder.length() - 1);
        }
        tagLabel = builder.toString();
        if (tagLabel == null){
            tagLabel = "";
        }

        Bundle parameters = new Bundle();
        parameters.putString(Constants.KEY_NAME, name.get());
        parameters.putString(Constants.KEY_TAG_LABEL, tagLabel);

        if (imageUri != null && cropRect != null && imageChanged) {
            mDataManager.updateProfileImage(imageUri, cropRect, new OnResponseCallback<ProfileImage>() {
                @Override
                public void onSuccess(ProfileImage data) {
                    imageChanged = false;
                    imageReceived = true;
                    imageSuccessful = true;
                    hideLoading();
                    goBack();
                }

                @Override
                public void onFailure(Exception e) {
                    imageReceived = true;
                    hideLoading();
                    mActivity.showLongSnack(e.getLocalizedMessage());
                }
            });
        } else {
            imageReceived = true;
            imageSuccessful = true;
        }

        mDataManager.updateProfile(parameters, new OnResponseCallback<UpdateMyProfileResponse>() {
            @Override
            public void onSuccess(UpdateMyProfileResponse data) {
                profileReceived = true;
                profileSuccessful = true;
                hideLoading();
                goBack();
            }

            @Override
            public void onFailure(Exception e) {
                profileReceived = true;
                hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void hideLoading(){
        if (imageReceived && profileReceived){
            mActivity.hideLoading();
        }
    }

    private void goBack(){
        if (profileSuccessful && imageSuccessful){
            mActivity.showLongSnack("Profile updated successfully");
            mActivity.onBackPressed();
        }
    }
}

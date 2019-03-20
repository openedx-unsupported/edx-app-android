package org.edx.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import org.edx.mobile.R;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.tta.data.local.db.table.Certificate;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.profile.MyCertificatesFragment;
import org.edx.mobile.tta.utils.ActivityUtil;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.ProfileImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.edx.mobile.util.BrowserUtil.loginPrefs;

public class ProfileViewModel extends BaseViewModel {

    public ProfileModel profileModel;
    public ProfileImage profileImage;
    public Account account;
    public SearchFilter searchFilter;

    private boolean accountReceived, filtersReceived;
    private String tagLabel;

    public ObservableInt userImagePlaceholder = new ObservableInt(R.drawable.profile_photo_placeholder);
    public ObservableField<String> classes = new ObservableField<>();
    public ObservableField<String> skills = new ObservableField<>();
    public ObservableField<String> following = new ObservableField<>();
    public ObservableField<String> followers = new ObservableField<>();
    public ObservableField<String> userImageUrl = new ObservableField<>();
    public ObservableField<String> nCertificates = new ObservableField<String>("0");

    public ProfileViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        fetchAccount();
        fetchFilters();
        fetchCertificates();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFromLocal();
        setDetails();
    }

    private void refreshFromLocal() {
        profileModel = loginPrefs.getCurrentUserProfile();
        profileImage = loginPrefs.getProfileImage();
        if (profileModel != null) {
            tagLabel = profileModel.getTagLabel();
            followers.set(String.valueOf(profileModel.getFollowers()));
            following.set(String.valueOf(profileModel.getFollowing()));
        }
        if (profileImage != null){
            userImageUrl.set(profileImage.getImageUrlLarge());
        }
    }

    private void fetchFilters() {

        mDataManager.getSearchFilter(new OnResponseCallback<SearchFilter>() {
            @Override
            public void onSuccess(SearchFilter data) {
                filtersReceived = true;
                hideLoading();
                searchFilter = data;
                if (accountReceived){
                    setDetails();
                }
            }

            @Override
            public void onFailure(Exception e) {
                filtersReceived = true;
                hideLoading();
                if (accountReceived){
                    setDetails();
                }
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void fetchAccount() {
        mActivity.showLoading();
        mDataManager.getAccount(new OnResponseCallback<Account>() {
            @Override
            public void onSuccess(Account data) {
                accountReceived = true;
                hideLoading();
                account = data;
                refreshFromLocal();
                if (account != null){
                    tagLabel = account.getTagLabel();
                }
                if (filtersReceived) {
                    setDetails();
                }
            }

            @Override
            public void onFailure(Exception e) {
                accountReceived = true;
                if (filtersReceived) {
                    setDetails();
                }
                hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void fetchCertificates(){

        mDataManager.getMyCertificates(new OnResponseCallback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> data) {
                nCertificates.set(String.valueOf(data.size()));
            }

            @Override
            public void onFailure(Exception e) {
                //Do nothing
            }
        });

    }

    public void showCertificates(){
        ActivityUtil.replaceFragmentInActivity(mActivity.getSupportFragmentManager(),
                new MyCertificatesFragment(),
                R.id.dashboard_fragment,
                MyCertificatesFragment.TAG,
                true,
                null);
    }

    private void setDetails() {
        classes.set("");
        skills.set("");
        if ((account == null && profileModel == null) ||
                searchFilter == null || searchFilter.getResult() == null){
            return;
        }

        String[] section_tag_list;

        if (tagLabel == null || tagLabel.length() == 0){
            return;
        }

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
                    StringBuilder builder = new StringBuilder();
                    for (String tag: sectionTagsMap.get(section.getName())){
                        builder.append(tag + ", ");
                    }
                    if (builder.length() > 0){
                        builder.deleteCharAt(builder.length() - 1);
                        builder.deleteCharAt(builder.length() - 1);
                    }

                    if (section.getName().contains("कक्षा")){
                        classes.set(builder.toString());
                    } else if (section.getName().contains("कौशल")){
                        skills.set(builder.toString());
                    }
                }
            }
        }

    }

    public void logout(){
        mDataManager.logout();
    }

    private void hideLoading(){
        if (accountReceived && filtersReceived){
            mActivity.hideLoading();
        }
    }

}

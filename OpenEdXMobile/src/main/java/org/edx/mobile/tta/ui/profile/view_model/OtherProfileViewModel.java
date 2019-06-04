package org.edx.mobile.tta.ui.profile.view_model;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import org.edx.mobile.R;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import org.edx.mobile.tta.analytics.analytics_enums.Page;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.model.StatusResponse;
import org.edx.mobile.tta.data.model.feed.SuggestedUser;
import org.edx.mobile.tta.data.model.profile.FollowStatus;
import org.edx.mobile.tta.data.model.search.FilterSection;
import org.edx.mobile.tta.data.model.search.SearchFilter;
import org.edx.mobile.tta.event.UserFollowingChangedEvent;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.user.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class OtherProfileViewModel extends BaseViewModel {

    private String username;
    private Account account;
    public SearchFilter searchFilter;
    private boolean followed;

    public ObservableInt userImagePlaceholder = new ObservableInt(R.drawable.profile_photo_placeholder);
    public ObservableField<String> classes = new ObservableField<>();
    public ObservableField<String> skills = new ObservableField<>();
    public ObservableField<String> following = new ObservableField<>();
    public ObservableField<String> followers = new ObservableField<>();
    public ObservableField<String> userImageUrl = new ObservableField<>();
    public ObservableField<String> nCertificates = new ObservableField<>("0");
    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> followBtnText = new ObservableField<>();
    public ObservableInt followBtnBackground = new ObservableInt();
    public ObservableInt followTextColor = new ObservableInt();

    private boolean accountReceived, filtersReceived;
    private String tagLabel;

    public OtherProfileViewModel(BaseVMActivity activity, String username) {
        super(activity);

        this.username = username;
        toggleFollowBtn();
        fetchFollowStatus();
        fetchUserAccount();
        fetchFilters();
    }

    private void fetchFollowStatus() {

        mDataManager.getFollowStatus(username, new OnResponseCallback<FollowStatus>() {
            @Override
            public void onSuccess(FollowStatus data) {
                followed = data.is_followed();
                toggleFollowBtn();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

    }

    private void fetchUserAccount(){
        mActivity.showLoading();
        mDataManager.getOtherUserAccount(username, new OnResponseCallback<Account>() {
            @Override
            public void onSuccess(Account data) {
                mActivity.hideLoading();
                account = data;
                if (account != null) {
                    tagLabel = account.getTagLabel();
                }
                setDetails();
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                setDetails();
            }
        });

    }

    private void fetchFilters() {

        mDataManager.getSearchFilter(new OnResponseCallback<SearchFilter>() {
            @Override
            public void onSuccess(SearchFilter data) {
                filtersReceived = true;
                hideLoading();
                searchFilter = data;
                setDetails();
            }

            @Override
            public void onFailure(Exception e) {
                filtersReceived = true;
                hideLoading();
                setDetails();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    public void followUnfollow(){
        mActivity.showLoading();
        mDataManager.followUnfollowUser(username, new OnResponseCallback<StatusResponse>() {
            @Override
            public void onSuccess(StatusResponse data) {
                mActivity.hideLoading();
                followed = data.getStatus();
                toggleFollowBtn();
                SuggestedUser user = new SuggestedUser();
                user.setUsername(username);
                user.setName(name.get());
                user.setFollowed(followed);
                if (account != null) {
                    user.setProfileImage(account.getProfileImage());
                    account.setFollowers(followed ? account.getFollowers()+1 : account.getFollowers()-1);
                }
                EventBus.getDefault().post(new UserFollowingChangedEvent(user));
                setDetails();

                if (data.getStatus()){
                    mActivity.analytic.addMxAnalytics_db(username, Action.FollowUser,
                            Page.ProfilePage.name(), Source.Mobile, username);
                } else {
                    mActivity.analytic.addMxAnalytics_db(username, Action.UnfollowUser,
                            Page.ProfilePage.name(), Source.Mobile, username);
                }
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        });

    }

    private void setDetails() {

        if (account == null){
            return;
        }

        userImageUrl.set(account.getProfileImage().getImageUrlFull());
        name.set(account.getName());
        followers.set(String.valueOf(account.getFollowers()));
        following.set(String.valueOf(account.getFollowing()));
        nCertificates.set(String.valueOf(account.getCertificateCount()));

        classes.set("");
        skills.set("");
        if (searchFilter == null || searchFilter.getResult() == null ||
                tagLabel == null || tagLabel.length() == 0) {
            return;
        }

        String[] section_tag_list;
        section_tag_list = tagLabel.split(" ");

        Map<String, List<String>> sectionTagsMap = new HashMap<>();
        for (String section_tag : section_tag_list) {
            String[] duet = section_tag.split("_");
            if (!sectionTagsMap.containsKey(duet[0])) {
                sectionTagsMap.put(duet[0], new ArrayList<>());
            }
            sectionTagsMap.get(duet[0]).add(duet[1]);
        }

        for (FilterSection section : searchFilter.getResult()) {
            if (section.isIn_profile()) {
                if (sectionTagsMap.containsKey(section.getName())) {
                    StringBuilder builder = new StringBuilder();
                    for (String tag : sectionTagsMap.get(section.getName())) {
                        builder.append(tag + ", ");
                    }
                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                        builder.deleteCharAt(builder.length() - 1);
                    }

                    if (section.getName().contains("कक्षा")) {
                        classes.set(builder.toString());
                    } else if (section.getName().contains("कौशल")) {
                        skills.set(builder.toString());
                    }
                }
            }
        }

    }

    private void toggleFollowBtn(){

        if (followed){
            followBtnText.set(mActivity.getString(R.string.following));
            followBtnBackground.set(R.drawable.btn_selector_filled);
            followTextColor.set(R.color.white);
        } else {
            followBtnText.set(mActivity.getString(R.string.follow));
            followBtnBackground.set(R.drawable.btn_selector_hollow);
            followTextColor.set(R.color.primary_cyan);
        }

    }

    private void hideLoading() {
        if (accountReceived && filtersReceived) {
            mActivity.hideLoading();
        }
    }

}

package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.inject.Inject;
import com.google.inject.Injector;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.NewUserProfileBinding;
import org.edx.mobile.http.callback.Callback;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.profiles.ScrollingPreferenceParent;
import org.edx.mobile.profiles.UserProfileActivity;
import org.edx.mobile.profiles.UserProfileImageViewModel;
import org.edx.mobile.profiles.UserProfileInteractor;
import org.edx.mobile.profiles.UserProfilePresenter;
import org.edx.mobile.profiles.UserProfileTab;
import org.edx.mobile.profiles.UserProfileTabsInteractor;
import org.edx.mobile.profiles.UserProfileViewModel;
import org.edx.mobile.user.PreferedLangList;
import org.edx.mobile.user.UserInfo;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.PreferedLanguageAdapter;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;

import static com.facebook.FacebookSdk.getApplicationContext;

public class NewUserProfileFragment extends PresenterFragment<UserProfilePresenter, UserProfilePresenter.ViewInterface>
        implements ScrollingPreferenceParent, PreferedLanguageAdapter.OnUpdateLanguage {

    @Inject
    private IEdxEnvironment environment;

    private PreferedLanguageAdapter preferedLanguageAdapter;
    @Inject
    private UserService userService;
    private UserInfo userInfo;

    @Override
    public void onChildScrollingPreferenceChanged() {

    }

    @NonNull
    public static NewUserProfileFragment newInstance(@NonNull String username, @NonNull String userType) {
        final NewUserProfileFragment fragment = new NewUserProfileFragment();
        fragment.setArguments(createArguments(username, userType));
        return fragment;
    }

    @NonNull
    @VisibleForTesting
    public static Bundle createArguments(@NonNull String username, @NonNull String userType) {
        final Bundle bundle = new Bundle();
        bundle.putString(UserProfileActivity.EXTRA_USERNAME, username);
        bundle.putString(UserProfileActivity.EXTRA_USERTYPE, userType);
        return bundle;
    }

    @NonNull
    @Override
    protected UserProfilePresenter createPresenter() {
        final Injector injector = RoboGuice.getInjector(getActivity());
        final String username = getUsername();
        return new UserProfilePresenter(
                injector.getInstance(AnalyticsRegistry.class),
                new UserProfileInteractor(
                        username,
                        injector.getInstance(UserService.class),
                        injector.getInstance(EventBus.class),
                        injector.getInstance(UserPrefs.class)),
                new UserProfileTabsInteractor(
                        username,
                        injector.getInstance(UserService.class),
                        injector.getInstance(Config.class)
                ));
    }

    @NonNull
    private String getUsername() {
        return getArguments().getString(UserProfileActivity.EXTRA_USERNAME);
    }

    @NonNull
    private String getUserType() {
        return getArguments().getString(UserProfileActivity.EXTRA_USERTYPE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return DataBindingUtil.inflate(inflater, R.layout.new_user_profile, container,
                false).getRoot();
    }

    NewUserProfileBinding viewHolder;

    @NonNull
    @Override
    protected UserProfilePresenter.ViewInterface createView() {
        viewHolder = DataBindingUtil.getBinding(getView());
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        if (getUsername() != null) {
            viewHolder.userName.setText(getUsername());
        }
        viewHolder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        //     loadProfileInfo();
        viewHolder.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*environment.getRouter().performManualLogout(getActivity(),
                        environment.getAnalyticsRegistry(), environment.getNotificationDelegate());*/
                logoutConfirmation();
            }
        });
        if (selectedLanguage.equals("en")) {
            viewHolder.english.setSelected(true);
            viewHolder.hindi.setSelected(false);
            viewHolder.kannada.setSelected(false);
            viewHolder.tamil.setSelected(false);
        } else if (selectedLanguage.equals("hi")) {
            viewHolder.hindi.setSelected(true);
            viewHolder.english.setSelected(false);
            viewHolder.kannada.setSelected(false);
            viewHolder.tamil.setSelected(false);
        } else if (selectedLanguage.equals("kn")) {
            viewHolder.kannada.setSelected(true);
            viewHolder.hindi.setSelected(false);
            viewHolder.english.setSelected(false);
            viewHolder.tamil.setSelected(false);
        } else if (selectedLanguage.equals("ta")) {
            viewHolder.tamil.setSelected(true);
            viewHolder.hindi.setSelected(false);
            viewHolder.kannada.setSelected(false);
            viewHolder.english.setSelected(false);
        }
        viewHolder.english.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (!viewHolder.english.isSelected()) {
                    LocaleManager.setNewLocale(getContext(), "en");
                    viewHolder.english.setSelected(true);
                    viewHolder.hindi.setSelected(false);
                    viewHolder.kannada.setSelected(false);
                    viewHolder.tamil.setSelected(false);
                    Intent intent
                            = new Intent(getActivity(), MainBottomDashboardFragment.class);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                } /*else {
                    viewHolder.english.setSelected(false);
                    viewHolder.hindi.setSelected(false);
                    viewHolder.kannada.setSelected(false);
                    viewHolder.tamil.setSelected(false);
                }*/
            }
        });
        viewHolder.hindi.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (!viewHolder.hindi.isSelected()) {
                    LocaleManager.setNewLocale(getContext(), "hi");
                    viewHolder.hindi.setSelected(true);
                    viewHolder.english.setSelected(false);
                    viewHolder.kannada.setSelected(false);
                    viewHolder.tamil.setSelected(false);
                    Intent intent
                            = new Intent(getActivity(), MainBottomDashboardFragment.class);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                } /*else {
                    LocaleManager.setNewLocale(getContext(), "en");
                    viewHolder.hindi.setSelected(false);
                    viewHolder.english.setSelected(false);
                    viewHolder.kannada.setSelected(false);
                    viewHolder.tamil.setSelected(false);
                }*/
            }
        });
        viewHolder.kannada.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (!viewHolder.kannada.isSelected()) {
                    LocaleManager.setNewLocale(getContext(), "kn");
                    viewHolder.kannada.setSelected(true);
                    viewHolder.hindi.setSelected(false);
                    viewHolder.english.setSelected(false);
                    viewHolder.tamil.setSelected(false);
                    Intent intent
                            = new Intent(getActivity(), MainBottomDashboardFragment.class);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                } /*else {
                    LocaleManager.setNewLocale(getContext(), "en");
                    viewHolder.kannada.setSelected(false);
                    viewHolder.english.setSelected(false);
                    viewHolder.hindi.setSelected(false);
                    viewHolder.tamil.setSelected(false);
                }*/
            }
        });
        viewHolder.tamil.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                if (!viewHolder.tamil.isSelected()) {
                    LocaleManager.setNewLocale(getContext(), "ta");
                    viewHolder.tamil.setSelected(true);
                    viewHolder.hindi.setSelected(false);
                    viewHolder.kannada.setSelected(false);
                    viewHolder.english.setSelected(false);
                    Intent intent
                            = new Intent(getActivity(), MainBottomDashboardFragment.class);
                    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                }/* else {
                    LocaleManager.setNewLocale(getContext(), "en");
                    viewHolder.tamil.setSelected(false);
                    viewHolder.hindi.setSelected(false);
                    viewHolder.kannada.setSelected(false);
                    viewHolder.english.setSelected(false);
                }*/
            }
        });
        if (getUserType() != null) {
            String userType = getUserType();
            viewHolder.noUserType.setVisibility(View.GONE);
            if (userType.equals("teacher")) {
                viewHolder.student.setVisibility(View.GONE);
                viewHolder.teacher.setSelected(true);
                viewHolder.student.setSelected(false);
            } else {
                viewHolder.teacher.setVisibility(View.GONE);
                viewHolder.teacher.setSelected(false);
                viewHolder.student.setSelected(true);
            }
        }else{
            viewHolder.lnUserAccountType.setVisibility(View.GONE);
            viewHolder.noUserType.setVisibility(View.VISIBLE);
            viewHolder.txtUserAccountType.setVisibility(View.GONE);
        }
        return new UserProfilePresenter.ViewInterface() {

            @Override
            public void setEditProfileMenuButtonVisible(boolean visible) {

            }

            @Override
            public void showProfile(@NonNull UserProfileViewModel profile) {

            }

            @Override
            public void showLoading() {

            }

            @Override
            public void showTabs(@NonNull List<UserProfileTab> tab) {

            }

            @Override
            public void showError(@NonNull Throwable error) {

            }

            @Override
            public void setPhotoImage(@NonNull UserProfileImageViewModel model) {

            }

            @Override
            public void setUsername(@NonNull String username) {

            }

            @Override
            public void navigateToProfileEditor(@NonNull String username) {

            }
        };
    }

    private void loadProfileInfo() {
/*
        userService.getUserInfo(getUsername()).enqueue(new Callback<UserInfo>() {
            @Override
            protected void onResponse(@NonNull UserInfo responseBody) {
                userInfo = responseBody;
                if (userInfo != null) {
                    List<PreferedLangList> preferedLangLists = new ArrayList<>();
                    PreferedLangList preferedLangList = new PreferedLangList();
                    preferedLangList.setName(getContext().getString(R.string.english));
                    preferedLangList.setShort_name("en");

                    PreferedLangList preferedLangList1 = new PreferedLangList();
                    preferedLangList1.setName(getContext().getString(R.string.hindi));
                    preferedLangList1.setShort_name("hi");

                    PreferedLangList preferedLangList2 = new PreferedLangList();
                    preferedLangList2.setName(getContext().getString(R.string.kannada));
                    preferedLangList2.setShort_name("kn");

                    PreferedLangList preferedLangList3 = new PreferedLangList();
                    preferedLangList3.setName(getContext().getString(R.string.tamil));
                    preferedLangList3.setShort_name("ta");

                    preferedLangLists.add(preferedLangList);
                    preferedLangLists.add(preferedLangList1);
                    preferedLangLists.add(preferedLangList2);
                    preferedLangLists.add(preferedLangList3);

                    userInfo.setPrefered_lang_list(preferedLangLists);
                    String selectedLanguage = "";
                    userInfo.setSelected_preferd_language(preferedLangList);
                    if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()){
                        selectedLanguage = LocaleManager.getLanguagePref(getActivity());
                    }
                    preferedLanguageAdapter = new PreferedLanguageAdapter(getActivity(), NewUserProfileFragment.this::executeUpdate);
                    viewHolder.preferedLangList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                    viewHolder.preferedLangList.setAdapter(preferedLanguageAdapter);
                    if (preferedLanguageAdapter != null) {
                        preferedLanguageAdapter.setLanguages(userInfo.getPrefered_lang_list(), selectedLanguage);
                    }
                    if (userInfo.getUser_type() != null) {
                        if (userInfo.getUser_type().equals("teacher")) {
                            viewHolder.student.setVisibility(View.GONE);
                            viewHolder.teacher.setSelected(true);
                            viewHolder.student.setSelected(false);
                        } else {
                            viewHolder.teacher.setVisibility(View.GONE);
                            viewHolder.teacher.setSelected(false);
                            viewHolder.student.setSelected(true);
                        }
                    }
                }
            }

            @Override
            protected void onFailure(@NonNull Throwable error) {
                super.onFailure(error);
            }
        });*/
        userInfo = new UserInfo();
        userInfo.setUser_type(getUserType());
        List<PreferedLangList> preferedLangLists = new ArrayList<>();
        PreferedLangList preferedLangList = new PreferedLangList();
        preferedLangList.setName(getContext().getString(R.string.english));
        preferedLangList.setShort_name("en");
        preferedLangList.setContent_description(getString(R.string.english));

        PreferedLangList preferedLangList1 = new PreferedLangList();
        preferedLangList1.setName(getContext().getString(R.string.hindi));
        preferedLangList1.setShort_name("hi");
        preferedLangList.setContent_description(getContext().getString(R.string.only_hindi));

        PreferedLangList preferedLangList2 = new PreferedLangList();
        preferedLangList2.setName(getContext().getString(R.string.kannada));
        preferedLangList2.setShort_name("kn");
        preferedLangList.setContent_description(getString(R.string.only_kannada));

        PreferedLangList preferedLangList3 = new PreferedLangList();
        preferedLangList3.setName(getContext().getString(R.string.tamil));
        preferedLangList3.setShort_name("ta");
        preferedLangList.setContent_description(getString(R.string.only_tamil));

        preferedLangLists.add(preferedLangList);
        preferedLangLists.add(preferedLangList1);
        preferedLangLists.add(preferedLangList2);
        preferedLangLists.add(preferedLangList3);

        userInfo.setPrefered_lang_list(preferedLangLists);
        String selectedLanguage = "";
        userInfo.setSelected_preferd_language(preferedLangList);
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        preferedLanguageAdapter = new PreferedLanguageAdapter(getActivity(), NewUserProfileFragment.this::executeUpdate);
        viewHolder.preferedLangList.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        viewHolder.preferedLangList.setAdapter(preferedLanguageAdapter);
        if (preferedLanguageAdapter != null) {
            preferedLanguageAdapter.setLanguages(userInfo.getPrefered_lang_list(), selectedLanguage);
        }
        if (userInfo.getUser_type() != null) {
            if (userInfo.getUser_type().equals("teacher")) {
                viewHolder.student.setVisibility(View.GONE);
                viewHolder.teacher.setSelected(true);
                viewHolder.student.setSelected(false);
            } else {
                viewHolder.teacher.setVisibility(View.GONE);
                viewHolder.teacher.setSelected(false);
                viewHolder.student.setSelected(true);
            }
        }

    }

    private void executeUpdate(String language) {
        if (language != null) {
            if (LocaleManager.getLanguagePref(getActivity()).equals(language)) {
                return;
            }
            LocaleManager.setNewLocale(getActivity(), language);
        }
        Intent intent
                = new Intent(getActivity(), MainBottomDashboardFragment.class);
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));

     /*   final Object valueObject;
        if (TextUtils.isEmpty(fieldValue)) {
            valueObject = Collections.emptyList();
        } else {
            valueObject = Collections.singletonList(new LanguageProficiency(fieldValue));
        }

        userService.updateAccount(getUsername(), Collections.singletonMap("language_proficiencies", valueObject))
                .enqueue(new UserAPI.AccountDataUpdatedCallback(getActivity(), getUsername(),
                        new DialogErrorNotification(this)) {
                    @Override
                    protected void onResponse(@NonNull final Account account) {
                        super.onResponse(account);
                        Toast.makeText(getActivity(), "UPDATED", Toast.LENGTH_LONG).show();
                    }
                });*/
    }

    @Override
    public void onClick(String fieldValue) {
        executeUpdate(fieldValue);
    }
    private void logoutConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Uncomment the below code to Set the message and title from the strings.xml file
        builder.setMessage(R.string.are_you_sure_you_want_to_logout) .setTitle(R.string.logout_confirmation);

        //Setting message manually and performing action on button click
        builder.setMessage(R.string.are_you_sure_you_want_to_logout)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        /*Toast.makeText(getApplicationContext(),"you choose yes action for alertbox",
                                Toast.LENGTH_SHORT).show();*/
                        environment.getRouter().performManualLogout(getActivity(),
                                environment.getAnalyticsRegistry(), environment.getNotificationDelegate());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  Action for 'NO' Button
                        dialog.cancel();
                        /*Toast.makeText(getApplicationContext(),"you choose no action for alertbox",
                                Toast.LENGTH_SHORT).show();*/
                    }
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle(R.string.logout_confirmation);
        alert.show();
    }
}

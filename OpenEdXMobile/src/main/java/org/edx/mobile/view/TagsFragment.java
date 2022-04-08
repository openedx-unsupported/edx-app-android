package org.edx.mobile.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentTagsScreenBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.model.TagModel;
import org.edx.mobile.discovery.model.TagTermResult;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.TagsAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static android.app.Activity.RESULT_OK;
import static org.edx.mobile.util.links.WebViewLink.Param.PROGRAMS;
import static org.edx.mobile.view.ProgramActivity.PROGRAM;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_UUID;
import static org.edx.mobile.view.TagsFragmentActivity.COLOR_CODE;
import static org.edx.mobile.view.TagsFragmentActivity.SUBJECT;

public class TagsFragment extends BaseFragment implements OnRecyclerItemClickListener {
    public static final String TAG = TagsFragment.class.getCanonicalName();
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    private LoginAPI loginAPI;
    @Inject
    CourseApi courseApi;
    private FragmentTagsScreenBinding binding;
    private static String subject;
    private static String colorCode;
    private TagsAdapter tagsAdapter;
    @Inject
    protected IEdxEnvironment environment;

    public static TagsFragment newInstance(@Nullable Bundle bundle) {
        final TagsFragment fragment = new TagsFragment();
        subject = bundle.getString(TagsFragmentActivity.SUBJECT);
        colorCode = bundle.getString(TagsFragmentActivity.COLOR_CODE);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subject = getArguments().getString(TagsFragmentActivity.SUBJECT);
        colorCode = getArguments().getString(TagsFragmentActivity.COLOR_CODE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tags_screen, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.announceForAccessibility("Tags Screen");
        binding.subjectName.setText(subject);
        if (loginPrefs.getUserType() != null) {
            if (loginPrefs.getUserType().toLowerCase().equals("teacher")) {
                binding.userType.setText(getContext().getString(R.string.for_teacher));
            } else if (loginPrefs.getUserType().toLowerCase().equals("student")) {
                binding.userType.setText(getContext().getString(R.string.for_student));
            }
        } else {
            binding.userType.setText(getContext().getString(R.string.no_user_type));
        }
        tagsAdapter = new TagsAdapter(getActivity(), TagsFragment.this::onItemClick, Integer.valueOf(colorCode));
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        binding.rvTags.setLayoutManager(mLayoutManager);
        binding.rvTags.setAdapter(tagsAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        binding.shimmerLayout.startShimmer();
        super.onResume();
        try {
            checkToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        binding.shimmerLayout.stopShimmer();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkToken() throws Exception {
        AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
        long millis = System.currentTimeMillis();
        long tokenTime = millis - responseJwt.creation_time;
        if (tokenTime > responseJwt.expires_in) {
            createToken();
        } else {
            getTopics();
        }
    }

    private void createToken() throws Exception {
        DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                getTopics();
            }

            @Override
            public void onException(Exception ex) {
                if (ex instanceof HttpStatusException &&
                        ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                } else {

                }
            }
        };
        discoveryTask.execute();
    }

    private void getTopics() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        Call<TagModel> discoveryTag = courseApi.getTopicsWithSubjectName(token, selectedLanguage, subject);
        discoveryTag.enqueue(new DiscoveryCallback<TagModel>() {
            @Override
            protected void onResponse(@NonNull TagModel responseBody) {
                binding.shimmerLayout.setVisibility(View.GONE);
                String userType = loginPrefs.getUserType();
                List<TagTermResult> accordingToTeacher = new ArrayList<>();
                List<TagTermResult> accordingToStudent = new ArrayList<>();
                List<TagTermResult> accordingNoUserType = new ArrayList<>();
                if (responseBody != null) {
                    if (responseBody.getTerms() != null) {
                        for (TagTermResult tagTermResult : responseBody.getTerms()) {
                            if (tagTermResult.getTerm().toLowerCase().contains("teacher")) {
                                accordingToTeacher.add(tagTermResult);
                            }
                            if (tagTermResult.getTerm().toLowerCase().contains("student")) {
                                accordingToStudent.add(tagTermResult);
                            }
                            if (!tagTermResult.getTerm().toLowerCase().contains("student") &&
                                    !tagTermResult.getTerm().toLowerCase().contains("teacher")) {
                                accordingNoUserType.add(tagTermResult);
                            }
                        }
                        if (userType != null) {
                            if (userType.contains("teacher")) {
                                accordingToTeacher.addAll(accordingNoUserType);
                                tagsAdapter.setTags(accordingToTeacher, userType);
                                if (accordingToTeacher == null) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                } else if (accordingToTeacher.size() == 0) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                }
                            } else {
                                accordingToStudent.addAll(accordingNoUserType);
                                tagsAdapter.setTags(accordingToStudent, userType);
                                if (accordingToStudent == null) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                } else if (accordingToStudent.size() == 0) {
                                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                                }
                            }
                        }else{
                            tagsAdapter.setTags(accordingNoUserType, userType);
                            if (accordingNoUserType == null) {
                                binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                binding.errorMsgTv.setVisibility(View.VISIBLE);
                                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                            } else if (accordingNoUserType.size() == 0) {
                                binding.errorMsgTv.setText(getString(R.string.no_program_found));
                                binding.errorMsgTv.setVisibility(View.VISIBLE);
                                binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                            }
                        }

                    }
                } else {
                    binding.errorMsgTv.setText(getString(R.string.no_program_found));
                    binding.errorMsgTv.setVisibility(View.VISIBLE);
                    binding.errorMsgTv.sendAccessibilityEvent(AccessibilityEvent.WINDOWS_CHANGE_ACCESSIBILITY_FOCUSED);
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });

    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof TagTermResult) {
            TagTermResult tagTermResult = (TagTermResult) item;
            //environment.getRouter().showProgramsActivity(getActivity(), tagTermResult.getTerm(), "");

       /*   MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);*/

            ProgramFragment programFragment = new ProgramFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(PROGRAM, tagTermResult.getTerm());
            bundle1.putString(PROGRAM_UUID, "");
            programFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, programFragment, ProgramFragment.TAG).addToBackStack(ProgramFragment.TAG)
                    .commit();
        }
    }
}

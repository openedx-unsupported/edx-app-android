package org.edx.mobile.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import org.edx.mobile.authentication.AuthResponseJwt;
import org.edx.mobile.authentication.DiscoveryTask;
import org.edx.mobile.authentication.LoginAPI;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentExploreCourseBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.DiscoverySubject;
import org.edx.mobile.discovery.model.DiscoverySubjectResult;
import org.edx.mobile.discovery.model.OrganisationList;
import org.edx.mobile.discovery.model.OrganisationModel;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.NewSubjectAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.OrganisationAdapter;

import retrofit2.Call;

import static android.app.Activity.RESULT_OK;
import static org.edx.mobile.view.TagsFragmentActivity.COLOR_CODE;
import static org.edx.mobile.view.TagsFragmentActivity.SUBJECT;

public class ExploreFragment extends BaseFragment implements OnRecyclerItemClickListener {
    public static final String TAG = ExploreFragment.class.getCanonicalName();
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    private LoginAPI loginAPI;
    @Inject
    CourseApi courseApi;
    private FragmentExploreCourseBinding binding;
    private NewSubjectAdapter newSubjectAdapter;
    private OrganisationAdapter organisationAdapter;
    private TagsFragment tagsFragment;
    private ExploreFragment exploreFragment;
    @Inject
    protected IEdxEnvironment environment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_explore_course, container,
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
       // tagsFragment = new TagsFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        binding.shimmerLayout.startShimmer();
        super.onResume();
        binding.progressBar.setVisibility(View.VISIBLE);
        try {
            getExploreSubjects();
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
    private void getExploreSubjects() throws Exception {
        AuthResponseJwt responseJwt = loginPrefs.getCurrentAuthJwt();
        long millis = System.currentTimeMillis();
        long tokenTime = millis - responseJwt.creation_time;
        if (tokenTime > responseJwt.expires_in) {
            createToken();
        } else {
            getSubjects();
            getOrganisations();
        }
    }

    private void createToken() throws Exception {
        DiscoveryTask discoveryTask = new DiscoveryTask(getContext()) {
            @Override
            public void onSuccess(@NonNull AuthResponseJwt result) {
                getSubjects();
                getOrganisations();
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

    private void getSubjects() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (getActivity()!=null){
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }

        Call<DiscoverySubject> discoveryCourse = courseApi.getDiscoverySubjects(token,selectedLanguage);
        discoveryCourse.enqueue(new DiscoveryCallback<DiscoverySubject>() {
            @Override
            protected void onResponse(@NonNull DiscoverySubject responseBody) {
                binding.progressBar.setVisibility(View.GONE);
                if (responseBody != null) {
                    Log.d("DiscoveryApi", String.valueOf(responseBody.getCount()));
                    newSubjectAdapter = new NewSubjectAdapter(getActivity(), ExploreFragment.this::onItemClick);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                    binding.rvExploreCourse.setLayoutManager(mLayoutManager);
                    binding.rvExploreCourse.setAdapter(newSubjectAdapter);
                    newSubjectAdapter.setSubjects(responseBody.getResults());
                    binding.shimmerLayout.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });
    }

    private void getOrganisations() {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (getActivity()!=null){
            if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
                selectedLanguage = LocaleManager.getLanguagePref(getActivity());
            }
        }

        Call<OrganisationList> discoveryOrganisation = courseApi.getOrganisations(token,selectedLanguage);
        discoveryOrganisation.enqueue(new DiscoveryCallback<OrganisationList>() {
            @Override
            protected void onResponse(@NonNull OrganisationList responseBody) {
                if (responseBody != null) {
                    organisationAdapter = new OrganisationAdapter(getActivity(), ExploreFragment.this::onItemClick);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                    binding.rvOrganisation.setLayoutManager(mLayoutManager);
                    binding.rvOrganisation.setAdapter(organisationAdapter);
                    organisationAdapter.setOrganisation(responseBody.getResults());
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
        if (item instanceof DiscoverySubjectResult) {
            DiscoverySubjectResult discoverySubjectResult = (DiscoverySubjectResult) item;
           /* environment.getRouter().showTagsActivity(getActivity(), discoverySubjectResult.getName(),
                    discoverySubjectResult.getCardColorName());*/
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
            TagsFragment tagsFragment = new TagsFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(SUBJECT, discoverySubjectResult.getName());
            bundle1.putString(COLOR_CODE, String.valueOf(discoverySubjectResult.getCardColorName()));
            tagsFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, tagsFragment, TagsFragment.TAG).addToBackStack(TagsFragment.TAG)
                    .commit();
        }
    }
}

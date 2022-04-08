package org.edx.mobile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentProgramListBinding;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.exception.AuthException;
import org.edx.mobile.http.HttpStatus;
import org.edx.mobile.http.HttpStatusException;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.CoursesAsyncLoader;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.programs.MyProgramListModel;
import org.edx.mobile.programs.ProgramTask;
import org.edx.mobile.programs.Programs;
import org.edx.mobile.programs.ResumeCourse;
import org.edx.mobile.view.adapters.MyProgramListAdapter;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.view.ProgramActivity.PROGRAM;
import static org.edx.mobile.view.ProgramActivity.PROGRAM_UUID;

public class MyProgramListFragment extends OfflineSupportBaseFragment
        implements RefreshListener, OnRecyclerItemClickListener,
        LoaderManager.LoaderCallbacks<AsyncTaskResult<List<EnrolledCoursesResponse>>> {
    public static final String TAG = MyCoursesListFragment.class.getCanonicalName();
    private FragmentProgramListBinding binding;
    private MyProgramListAdapter myProgramListAdapter;
    private static final int REQUEST_SHOW_COURSE_UNIT_DETAIL = 0;
    @Inject
    LoginPrefs loginPrefs;
    private MyCoursesListFragment.OnExploreButtonClick onExploreButtonClick;
    @Inject
    protected IEdxEnvironment environment;
    private ResumeCourse resumeCourse;

    private final Logger logger = new Logger(getClass().getSimpleName());
    ArrayList<EnrolledCoursesResponse> enrolledCoursesResponses = new ArrayList<>();
    private static final int MY_COURSE_LOADER_ID = 0x905000;
    private EnrolledCoursesResponse courseData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public MyProgramListFragment setExploreButtonClick(MyCoursesListFragment.OnExploreButtonClick answerChangeListener) {
        this.onExploreButtonClick = answerChangeListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program_list, container, false);
        binding.hello.setText(getString(R.string.hello) + " " + loginPrefs.getUsername());
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.exploreCourse.setVisibility(View.GONE);
        binding.btnExploreCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onExploreButtonClick.onClick();
            }
        });
        binding.resumeCourseContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (courseData != null) {
                   /* environment.getRouter().showCourseUnitDetail(MyProgramListFragment.this,
                            REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,resumeCourse.getBlock_id(), false);*/
                    //   final CourseComponent component = adapter.getItem(position).component;
                    if (resumeCourse.getBlock_id().contains("sequential")) {
                        environment.getRouter().showCourseContainerOutline(MyProgramListFragment.this,
                                REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,
                                resumeCourse.getBlock_id(), null, false);
                    } else {
                        environment.getRouter().showCourseUnitDetail(MyProgramListFragment.this,
                                REQUEST_SHOW_COURSE_UNIT_DETAIL, courseData, null,
                                resumeCourse.getBlock_id(), false);
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_course_info), Toast.LENGTH_LONG).show();
                }
            }
        });
        myProgramListAdapter = new MyProgramListAdapter(getActivity(), MyProgramListFragment.this::onItemClick);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        binding.myProgramList.setLayoutManager(mLayoutManager);
        binding.myProgramList.setAdapter(myProgramListAdapter);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData(false);
        try {
            getMyPrograms();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {

    }

    private void getMyPrograms() throws Exception {
        ProgramTask discoveryTask = new ProgramTask(getContext(), loginPrefs.getUsername()) {
            @Override
            public void onSuccess(@NonNull List<Programs> result) {
                binding.progressBar.setVisibility(View.GONE);
                if (result != null) {
                    String userType = loginPrefs.getUserType();
                    List<MyProgramListModel> myProgramListModels = new ArrayList<>();

                    List<MyProgramListModel> newProgramsListforTeacher = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforStudent = new ArrayList<>();
                    List<MyProgramListModel> newProgramsListforBoth = new ArrayList<>();
                    for (Programs programs : result) {
                        if (programs.getTags() != null) {
                            for (String tag : programs.getTags()) {
                                if (tag.toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(tag);
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforTeacher.add(myProgramListModel);
                                }
                                if (tag.toLowerCase().contains("student")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(tag);
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforStudent.add(myProgramListModel);
                                }
                                if (!tag.toLowerCase().contains("student") && !tag.toLowerCase().contains("teacher")) {
                                    MyProgramListModel myProgramListModel = new MyProgramListModel();
                                    myProgramListModel.setTagName(tag);
                                    myProgramListModel.setProgramName(programs.getProgram_title());
                                    myProgramListModel.setProgramUUid(programs.getProgram_uuid());
                                    myProgramListModel.setResume_program(programs.getResumePrograms());
                                    newProgramsListforBoth.add(myProgramListModel);
                                }
                            }
                        }
                    }
                    if (userType != null) {
                        if (userType.contains("teacher")) {
                            myProgramListModels.addAll(newProgramsListforTeacher);
                            newProgramsListforTeacher.addAll(newProgramsListforBoth);
                        } else {
                            myProgramListModels.addAll(newProgramsListforStudent);
                            newProgramsListforStudent.addAll(newProgramsListforBoth);
                        }
                    } else {
                        newProgramsListforStudent.addAll(newProgramsListforBoth);
                    }
                    //myProgramListModels.clear();
                    resumeCourse = null;
                    if (myProgramListModels != null && myProgramListModels.size() > 0) {
                        binding.txtYourEnrolledProgram.setVisibility(View.VISIBLE);
                        for (MyProgramListModel programListModel : myProgramListModels) {
                            if (programListModel.getResume_program() != null) {
                                if (resumeCourse == null) {
                                    resumeCourse = new ResumeCourse();
                                    resumeCourse.setBlock_id(programListModel.getResume_program().getBlock_id());
                                    resumeCourse.setCourse_id(programListModel.getResume_program().getCourse_id());
                                    resumeCourse.setCourse_name(programListModel.getResume_program().getCourse_name());
                                    resumeCourse.setProgramName(programListModel.getProgramName());
                                    resumeCourse.setTagName(programListModel.getTagName());
                                } else {
                                    if (resumeCourse.getBlock_id().equals(resumeCourse.getBlock_id())) {
                                        String programName = resumeCourse.getProgramName();
                                        if (!programName.contains(programListModel.getProgramName())) {
                                            programName = programName + ", " + programListModel.getProgramName();
                                        }
                                        resumeCourse.setProgramName(programName);
                                        String tagNme = resumeCourse.getTagName();
                                        if (!tagNme.contains(programListModel.getTagName())) {
                                            tagNme = tagNme + ", " + programListModel.getTagName();
                                        }
                                        resumeCourse.setTagName(tagNme);
                                    }
                                }
                            }
                        }

                        if (resumeCourse != null) {
                            binding.tagName.setText(resumeCourse.getTagName());
                            binding.programName.setText(context.getString(R.string.program_name) + " - " +
                                    resumeCourse.getProgramName());
                            binding.courseName.setText(resumeCourse.getCourse_name());
                            binding.lnResumeCourse.setVisibility(View.VISIBLE);
                            if (enrolledCoursesResponses != null) {
                                for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                                    if (enrolledCoursesResponse.getCourse() != null) {
                                        if (enrolledCoursesResponse.getCourse().getId() != null) {
                                            if (enrolledCoursesResponse.getCourse().getId().equals(resumeCourse.getCourse_id())) {
                                                courseData = enrolledCoursesResponse;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (myProgramListModels != null) {
                        binding.myProgramList.setVisibility(View.VISIBLE);
                    }
                    myProgramListAdapter.setMyProgramList(myProgramListModels);
                    if (myProgramListModels == null) {
                        binding.exploreCourse.setVisibility(View.VISIBLE);
                        binding.myProgramList.setVisibility(View.GONE);
                    } else if (myProgramListModels.size() == 0) {
                        binding.exploreCourse.setVisibility(View.VISIBLE);
                        binding.myProgramList.setVisibility(View.GONE);
                    }
                } else {
                    binding.exploreCourse.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onException(Exception ex) {
                binding.progressBar.setVisibility(View.GONE);
                if (ex instanceof HttpStatusException &&
                        ((HttpStatusException) ex).getStatusCode() == HttpStatus.UNAUTHORIZED) {
                } else {

                }
            }
        };
        discoveryTask.execute();
    }


    @Override
    protected boolean isShowingFullScreenError() {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        if (getActivity() != null) {
            onNetworkConnectivityChangeEvent(event);
        }
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof MyProgramListModel) {
            MyProgramListModel myProgramListModel = (MyProgramListModel) item;
         /*   environment.getRouter().showProgramsActivity(getActivity(), myProgramListModel.getTagName(),
                    myProgramListModel.getProgramUUid());*/
            MainBottomDashboardFragment.suodhaIcon().setVisibility(View.GONE);
            MainBottomDashboardFragment.backIcon().setVisibility(View.VISIBLE);
            ProgramFragment programFragment = new ProgramFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString(PROGRAM, myProgramListModel.getTagName());
            bundle1.putString(PROGRAM_UUID, myProgramListModel.getProgramUUid());
            programFragment.setArguments(bundle1);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment, programFragment, ProgramFragment.TAG).addToBackStack(ProgramFragment.TAG)
                    .commit();
        }
    }

    @Override
    public Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> onCreateLoader(int i, Bundle bundle) {
        return new CoursesAsyncLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader, AsyncTaskResult<List<EnrolledCoursesResponse>> result) {
        final Exception exception = result.getEx();
        if (exception != null) {
            if (exception instanceof AuthException) {
                loginPrefs.clear();
                getActivity().finish();
            } else if (exception instanceof HttpStatusException) {
                final HttpStatusException httpStatusException = (HttpStatusException) exception;
                switch (httpStatusException.getStatusCode()) {
                    case HttpStatus.UNAUTHORIZED: {
                        environment.getRouter().forceLogout(getContext(),
                                environment.getAnalyticsRegistry(),
                                environment.getNotificationDelegate());
                        break;
                    }
                }
            } else {
                logger.error(exception);
            }

        } else if (result.getResult() != null) {
            enrolledCoursesResponses = new ArrayList<EnrolledCoursesResponse>(result.getResult());
            if (resumeCourse != null) {
                for (EnrolledCoursesResponse enrolledCoursesResponse : enrolledCoursesResponses) {
                    if (enrolledCoursesResponse.getCourse() != null) {
                        if (enrolledCoursesResponse.getCourse().getId() != null) {
                            if (enrolledCoursesResponse.getCourse().getId().equals(resumeCourse.getCourse_id())) {
                                courseData = enrolledCoursesResponse;
                            }
                        }
                    }
                }
            }
        } else if (result.getResult() == null) {

        }
    }


    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<EnrolledCoursesResponse>>> asyncTaskResultLoader) {
    }

    protected void loadData(boolean showProgress) {
        getLoaderManager().restartLoader(MY_COURSE_LOADER_ID, null, this);
    }
}

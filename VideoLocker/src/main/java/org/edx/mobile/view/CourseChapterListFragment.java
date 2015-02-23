package org.edx.mobile.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.NetworkObserver;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.task.EnqueueDownloadTask;
import org.edx.mobile.task.GetCourseHierarchyTask;
import org.edx.mobile.task.GetLastAccessedTask;
import org.edx.mobile.task.SyncLastAccessedTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.UiUtil;
import org.edx.mobile.view.adapters.ChapterAdapter;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.ProgressDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CourseChapterListFragment extends CourseDetailBaseFragment implements NetworkObserver{


    private static final String TAG = CourseChapterListFragment.class.getCanonicalName();
    private static final String SECTION_ENTRIES = TAG + ".sectionEntryMap";

    private ChapterAdapter adapter;
    private String courseId;
    private String openInBrowserUrl;
    private DownloadSizeExceedDialog downloadFragment;
    private ListView chapterListView;
    private static final int MSG_UPDATE_PROGRESS = 1025;
    private boolean isActivityStarted;
    private String lastAccessed_subSectionId;
    private GetLastAccessedTask getLastAccessedTask;
    private EnrolledCoursesResponse enrollment;
    private ETextView courseScheduleTv;
    private String startDate;
    private boolean isTaskRunning = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.debug("created: " + getClass().getName());

        final Bundle bundle = getArguments();
        if(bundle!=null){
            enrollment = (EnrolledCoursesResponse) bundle
                    .getSerializable(BaseFragmentActivity.EXTRA_ENROLLMENT);
            if(enrollment!=null) {
                courseId = enrollment.getCourse().getId();
                try {
                    segIO.screenViewsTracking(enrollment.getCourse().getName()
                            + " - Courseware");
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_list, container,
                false);

        //Initialize the Course not started text view.
        if (!enrollment.getCourse().isStarted()) {
            startDate = DateUtil.formatCourseNotStartedDate(enrollment.getCourse().getStart());
            if (startDate != null) {
                startDate = "<font color='" + getString(R.color.grey_text_course_not_started) + "'>" + startDate + "</font>";
                String courseScheduledText = getString(R.string.course_content_available_text);
                courseScheduledText = courseScheduledText.replace("START_DATE", startDate);
                courseScheduleTv = (ETextView) view.findViewById(R.id.course_content_available_tv);
                courseScheduleTv.setText(Html.fromHtml(courseScheduledText));
            }
        }

        ArrayList<SectionEntry> savedEntries = null;
        if (savedInstanceState != null) {

            try {
                savedEntries = (ArrayList<SectionEntry>) savedInstanceState.getSerializable(SECTION_ENTRIES);
                adapter.setItems(savedEntries);
            } catch (Exception ex) {
                logger.error(ex);
            }

        }

        chapterListView = (ListView) view
                .findViewById(R.id.chapter_list);

        initializeAdapter();
        chapterListView.setAdapter(adapter);
        chapterListView.setOnItemClickListener(adapter);
        lastClickTime = 0;

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityStarted = true;
        adapter.setStore(db, storage);
        handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);

        fetchLastAccessed(getView());
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityStarted = false;
        //We need to cancel the getLastAccessed task if the fragment is stopped
        if(getLastAccessedTask!=null){
            getLastAccessedTask.cancel(true);
            isFetchingLastAccessed = false;
        }
    }

    private void initializeAdapter(){
        if (adapter == null) {
            // creating adapter just once

            adapter = new ChapterAdapter(getActivity(), courseId) {

                @Override
                public void onItemClicked(final SectionEntry model) {
                    // handle click
                    try {
                        if (AppConstants.offline_flag) {
                            boolean isVideoDownloaded = db.isVideoDownloadedInChapter(courseId,
                                    model.chapter, null);
                            if (isVideoDownloaded) {
                                Intent videoIntent = new Intent(getActivity(),
                                        VideoListActivity.class);
                                videoIntent.putExtra(BaseFragmentActivity.EXTRA_ENROLLMENT, enrollment);
                                videoIntent.putExtra("chapter", model.chapter);
                                videoIntent.putExtra("FromMyVideos", false);
                                startActivity(videoIntent);
                            } else {
                                UiUtil.showOfflineAccessMessage(CourseChapterListFragment.this.getView());
                            }
                        } else {
                            Intent lectureIntent = new Intent(getActivity(),
                                    CourseLectureListActivity.class);
                            lectureIntent.putExtra(BaseFragmentActivity.EXTRA_ENROLLMENT, enrollment);
                            lectureIntent.putExtra("lecture", model);
                            getActivity().startActivity(lectureIntent);
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                }

                @Override
                public void download(final SectionEntry model) {
                    try {
                        IDialogCallback dialogCallback = new IDialogCallback() {
                            @Override
                            public void onPositiveClicked() {
                                startChapterDownload(model);
                            }

                            @Override
                            public void onNegativeClicked() {
                                //
                            }
                        };
                        MediaConsentUtils.consentToMediaDownload(getActivity(), dialogCallback);

                    } catch (Exception e) {
                        logger.error(e);
                    }

                }
            };
        }

        if (!(NetworkUtil.isConnected(getActivity()))) {
            AppConstants.offline_flag = true;
        } else {
            AppConstants.offline_flag = false;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateOpenInBrowserPanel();

        if (adapter.isEmpty()) {
            logger.debug("adapter is empty, loading data ...");
            loadData(getView());
        }
    }

    private void startChapterDownload(SectionEntry model) {
        long downloadSize = 0;
        ArrayList<DownloadEntry> downloadList = new ArrayList<DownloadEntry>();
        int downloadCount = 0;
        for (VideoResponseModel v : model.getAllVideos()) {
            DownloadEntry de = (DownloadEntry) storage
                    .getDownloadEntryfromVideoResponseModel(v);
            if (de.downloaded == DownloadEntry.DownloadedState.DOWNLOADING
                    || de.downloaded == DownloadEntry.DownloadedState.DOWNLOADED) {
                continue;
            } else {
                downloadSize = downloadSize
                        + v.getSummary().getSize();
                downloadList.add(de);
                downloadCount++;
            }
        }
        if (downloadSize > MemoryUtil
                .getAvailableExternalMemory(getActivity())) {
            ((CourseDetailTabActivity) getActivity())
                    .showInfoMessage(getString(R.string.file_size_exceeded));
            updateList();
        } else {
            if (downloadSize < MemoryUtil.GB) {
                startDownload(downloadList, downloadCount);
            } else {
                showDownloadSizeExceedDialog(downloadList, downloadCount);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<SectionEntry> saveEntries = new ArrayList<SectionEntry>();
        for(int i = 0; i < adapter.getCount(); i++)
            saveEntries.add(adapter.getItem(i));

        outState.putSerializable(SECTION_ENTRIES, saveEntries);
    }

    //Loading data to the Adapter
    private void loadData(final View view) {
        if (isTaskRunning) {
            return;
        }

        GetCourseHierarchyTask task = new GetCourseHierarchyTask(getActivity()) {

            @Override
            public void onFinish(Map<String, SectionEntry> chapterMap) {
                // display these chapters
                if (chapterMap != null) {
                    logger.debug("Start displaying on UI "+ DateUtil.getCurrentTimeStamp());
                    adapter.clear();
                    for (Entry<String, SectionEntry> entry : chapterMap
                            .entrySet()) {
                        adapter.add(entry.getValue());
                        if (openInBrowserUrl == null || openInBrowserUrl.equalsIgnoreCase("")) {
                            // pick up browser link
                            openInBrowserUrl = entry.getValue().section_url;
                        }
                    }
                    if(adapter.getCount()==0){
                        view.findViewById(R.id.no_chapter_tv).setVisibility(View.VISIBLE);
                        chapterListView.setEmptyView(view.findViewById(R.id.no_chapter_tv));
                    }
                    adapter.notifyDataSetChanged();
                    updateOpenInBrowserPanel();

                    if ( !AppConstants.offline_flag) {
                        fetchLastAccessed(getView());
                    }
                }

                //Notify the adapter as contents of the adapter might have changed.
                adapter.notifyDataSetChanged();

                if (adapter.getCount() == 0) {
                    if (startDate != null) {
                        showCourseNotStartedMessage(view);
                    } else {
                        view.findViewById(R.id.no_chapter_tv).setVisibility(View.VISIBLE);
                        chapterListView.setEmptyView(view.findViewById(R.id.no_chapter_tv));
                    }
                }

                logger.debug("Completed displaying data on UI "+ DateUtil.getCurrentTimeStamp());
                isTaskRunning = false;
            }

            @Override
            public void onException(Exception ex) {
                if(adapter.getCount()==0) {
                    // calling setEmptyView requires adapter to be notified
                    adapter.notifyDataSetChanged();
                    view.findViewById(R.id.no_chapter_tv).setVisibility(View.VISIBLE);
                    chapterListView.setEmptyView(view.findViewById(R.id.no_chapter_tv));
                }

                isTaskRunning = false;
            }
        };

        ProgressBar progressBar = (ProgressBar) view
                .findViewById(R.id.api_spinner);
        task.setProgressDialog(progressBar);
        //Initializing task call
        logger.debug("Initializing Chapter Task"+ DateUtil.getCurrentTimeStamp());
        isTaskRunning = true;
        task.execute(courseId);
    }

    private void updateOpenInBrowserPanel() {
        if (AppConstants.offline_flag || adapter.isEmpty()) {
            hideOpenInBrowserPanel();
        } else {
            showOpenInBrowserPanel(openInBrowserUrl);
        }
    }

    @Override
    public void onOffline() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        hideLastAccessedView(getView());
        if (chapterListView != null) {
            updateOpenInBrowserPanel();
        }
    }

    @Override
    public void onOnline() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (chapterListView != null && openInBrowserUrl != null) {
            fetchLastAccessed(getView());
            updateOpenInBrowserPanel();
        }
    }

    public void updateList() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Dialog fragment to display message to user regarding 
    protected void showDownloadSizeExceedDialog(final ArrayList<DownloadEntry> de,
            final int noOfDownloads) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.download_exceed_title));
        dialogMap.put("message_1", getString(R.string.download_exceed_message));
        downloadFragment = DownloadSizeExceedDialog.newInstance(dialogMap,
                new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                startDownload(de, noOfDownloads);
            }

            @Override
            public void onNegativeClicked() {
                updateList();
                downloadFragment.dismiss();
            }
        });
        downloadFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        downloadFragment.show(getFragmentManager(), "dialog");
        downloadFragment.setCancelable(false);
    }

    public void startDownload(ArrayList<DownloadEntry> downloadList,
            int noOfDownloads) {
        try{
            segIO.trackSectionBulkVideoDownload(downloadList.get(0).getEnrollmentId(),
                    downloadList.get(0).getChapterName(), noOfDownloads);
        }catch(Exception e){
            logger.error(e);
        }

        EnqueueDownloadTask downloadTask = new EnqueueDownloadTask(getActivity()) {
            @Override
            public void onFinish(Long result) {
                try {
                    hideProgressDialog();
                    if(isActivityStarted) {
                        adapter.notifyDataSetChanged();
                        (getActivity()).invalidateOptionsMenu();
                        if (result > 1) {
                            String msg = String.format(getString(R.string.downloading_multiple), result);
                            UiUtil.showMessage(CourseChapterListFragment.this.getView(), msg);
                        } else if (result == 1) {
                            String msg = String.format(getString(R.string.downloading_single), result);
                            UiUtil.showMessage(CourseChapterListFragment.this.getView(), msg);
                        } else {
                            UiUtil.showMessage(CourseChapterListFragment.this.getView(),
                                    getString(R.string.msg_video_not_downloaded));
                        }
                    }
                }catch(Exception e){
                    logger.error(e);
                }
            }

            @Override
            public void onException(Exception ex) {
                hideProgressDialog();
                UiUtil.showMessage(CourseChapterListFragment.this.getView(), getString(R.string.msg_video_not_downloaded));
            }
        };

        // it is better to show progress before executing the task
        // this ensures task will hide the progress after it is shown
        if(downloadList.size()>=3) {
            showProgressDialog();
        }
        
        downloadTask.execute(downloadList);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            updateOpenInBrowserPanel();

            if(AppConstants.offline_flag){
                hideLastAccessedView(getView());
            }else{
                fetchLastAccessed(getView());
            }
        }
    }

    public boolean isActivityStarted() {
        return isActivityStarted;
    }

    private final Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_UPDATE_PROGRESS) {
                if (isActivityStarted()) {
                    if (!AppConstants.offline_flag) {
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, 3000);
                    }
                }
            }
        }
    };

    private ProgressDialogFragment progressDialog;
    private boolean isFetchingLastAccessed;
    private synchronized void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }
        synchronized (progressDialog) {
            try {
                if ( !progressDialog.isVisible() && isActivityStarted) {
                    final String tag = "progress_dialog_chapter";
                    
                    progressDialog.dismiss();
                    Fragment f = getFragmentManager().findFragmentByTag(tag);
                    if (f != null) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.remove(f);
                        ft.commit();
                        logger.debug("Removed progress dialog fragment");
                    }
                    
                    if ( !progressDialog.isAdded()) {
                        progressDialog.show(getFragmentManager(), tag);
                        progressDialog.setCancelable(false);
                        logger.debug("Showing activity indicator");
                    }
                }
            } catch(Exception ex) {
                logger.error(ex);
            }
        }
    }

    private void hideProgressDialog(){
        if(progressDialog!=null) {
            synchronized (progressDialog) {
                progressDialog.dismiss();
                logger.debug("hiding activity indicator");
            }
        }
    }

    private long lastClickTime;
    
    protected void showLastAccessedView(View v) {
        if (v != null && isActivityStarted()) {
            if (!AppConstants.offline_flag) {
                try {
                    if(courseId!=null && lastAccessed_subSectionId!=null){
                        final Api api = new Api(getActivity());
                        final VideoResponseModel videoModel = api.getSubsectionById(courseId, 
                                lastAccessed_subSectionId);
                        if (videoModel != null) {
                            LinearLayout lastAccessedLayout = (LinearLayout) v
                                    .findViewById(R.id.last_viewed_layout);
                            lastAccessedLayout.setVisibility(View.VISIBLE);

                            TextView lastAccessedVideoTv = (TextView) v
                                    .findViewById(R.id.last_viewed_tv);
                            lastAccessedVideoTv.setText(" "
                                    + videoModel.getSection().name);

                            lastAccessedLayout.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //This has been used so that if user clicks continuously on the screen, 
                                    //two activities should not be opened
                                    long currentTime = SystemClock.elapsedRealtime();
                                    if (currentTime - lastClickTime > 1000) {
                                        lastClickTime = currentTime;
                                        Bundle bundle = getArguments();
                                        EnrolledCoursesResponse enrollment = (EnrolledCoursesResponse) 
                                                bundle.getSerializable(BaseFragmentActivity.EXTRA_ENROLLMENT);
                                        try {
                                            LectureModel lecture = api.getLecture(courseId,
                                                    videoModel.getChapterName(), 
                                                    videoModel.getSequentialName());
                                            SectionEntry chapter = new SectionEntry();
                                            chapter.chapter = videoModel.getChapterName();
                                            lecture.chapter = chapter;
                                            Intent videoIntent = new Intent(
                                                    getActivity(),
                                                    VideoListActivity.class);
                                            videoIntent.putExtra(BaseFragmentActivity.EXTRA_ENROLLMENT, enrollment);
                                            videoIntent.putExtra("lecture", lecture);
                                            videoIntent.putExtra("FromMyVideos", false);
                                            
                                            startActivity(videoIntent);
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                    }
                                }
                            });
                        } else {
                            hideLastAccessedView(v);
                        }   
                    }
                } catch (Exception e) {
                    hideLastAccessedView(v);
                    logger.error(e);
                }
            } else {
                hideLastAccessedView(v);
            }
        }

    }

    //Hide Last Accessed
    private void hideLastAccessedView(View v) {
        try{
            if (v != null) {
                v.findViewById(R.id.last_viewed_layout).setVisibility(View.GONE);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void fetchLastAccessed(final View view){
        try{
            if(!isFetchingLastAccessed) {
                if(courseId!=null && getProfile()!=null && getProfile().username!=null){
                    String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
                            .username, courseId);
                    final PrefManager prefManager = new PrefManager(getActivity(), prefName);
                    final String prefModuleId = prefManager.getLastAccessedSubsectionId();

                    logger.debug("Last Accessed Module ID from Preferences "
                            +prefModuleId);

                    lastAccessed_subSectionId = prefModuleId;
                    showLastAccessedView(view);
                    getLastAccessedTask = new GetLastAccessedTask(getActivity()) {
                        @Override
                        public void onFinish(SyncLastAccessedSubsectionResponse result) {
                            String server_moduleId = null;
                            if(result!=null && result.getLastVisitedModuleId()!=null){
                                //Handle the last Visited Module received from Sever
                                server_moduleId = result.getLastVisitedModuleId();
                                logger.debug("Last Accessed Module ID from Server Get "
                                        +server_moduleId);
                                if(prefManager.isSyncedLastAccessedSubsection()){
                                    //If preference last accessed flag is true, put the last access fetched 
                                    //from server in Prefernces and display it on Last Accessed. 
                                    prefManager.putLastAccessedSubsection(server_moduleId, true);
                                    lastAccessed_subSectionId = server_moduleId;
                                    showLastAccessedView(view);
                                }else{
                                    //Preference's last accessed is not synched with server, 
                                    //Sync with server and display the result from server on UI.
                                    if(prefModuleId!=null && prefModuleId.length()>0){
                                        syncLastAccessedWithServer(prefManager, view, prefModuleId);
                                    }
                                }
                            }else{
                                //There is no Last Accessed module on the server
                                if(prefModuleId!=null && prefModuleId.length()>0){
                                    syncLastAccessedWithServer(prefManager,view, prefModuleId);
                                }
                            }
                            isFetchingLastAccessed = false;
                        }
                        @Override
                        public void onException(Exception ex) {
                            isFetchingLastAccessed = false;
                            logger.error(ex);
                        }
                    };

                    isFetchingLastAccessed = true;
                    getLastAccessedTask.execute(courseId);
                }   
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void syncLastAccessedWithServer(final PrefManager prefManager,
            final View view, String prefModuleId){
        try{
            SyncLastAccessedTask syncLastAccessTask = new SyncLastAccessedTask(
                    getActivity()) {
                @Override
                public void onFinish(SyncLastAccessedSubsectionResponse result) {
                    if(result!=null && result.getLastVisitedModuleId()!=null){
                        prefManager.putLastAccessedSubsection(result.getLastVisitedModuleId(), true);
                        logger.debug("Last Accessed Module ID from Server Sync "
                                +result.getLastVisitedModuleId());
                        lastAccessed_subSectionId = result.getLastVisitedModuleId();
                        showLastAccessedView(view);
                    }
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                }
            };
            syncLastAccessTask.execute(courseId, prefModuleId);
        }catch(Exception e){
            logger.error(e);
        }
    }

    /**
     * This function attaches the course not started message as Empty view to Chapter List
     * @param view
     */
    private void showCourseNotStartedMessage(View view){
        try{
            if(courseScheduleTv!=null){
                view.findViewById(R.id.no_chapter_tv).setVisibility(View.GONE);
                courseScheduleTv.setVisibility(View.VISIBLE);
                chapterListView.setEmptyView(courseScheduleTv);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }
}

package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.http.Api;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.task.GetLastAccessedTask;
import org.edx.mobile.util.DateUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.custom.ETextView;
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog;
import org.edx.mobile.view.dialog.ProgressDialogFragment;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.task.EnqueueDownloadTask;
import org.edx.mobile.task.GetCourseHierarchyTask;
import org.edx.mobile.task.SyncLastAccessedTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.LogUtil;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.view.adapters.ChapterAdapter;
import org.edx.mobile.view.dialog.IDialogCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CourseChapterListFragment extends CourseDetailBaseFragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter_list, container,
                false);
        final Bundle bundle = getArguments();
        if(bundle!=null){
            enrollment = (EnrolledCoursesResponse) bundle
                    .getSerializable("enrollment");
            if(enrollment!=null){
                courseId = enrollment.getCourse().getId();
                try{
                    segIO.screenViewsTracking(enrollment.getCourse().getName()
                            + " - Courseware");
                }catch(Exception e){
                    e.printStackTrace();
                }

                //Initialize the text view and
                if(!enrollment.getCourse().isStarted()){
                    startDate = DateUtil.formatCourseNotStartedDate(enrollment.getCourse().getStart());
                    if(startDate!=null){
                        startDate =  "<font color='"+ getString(R.color.grey_text_course_not_started)+"'>"+startDate+"</font>";
                        String courseScheduledText = getString(R.string.course_content_available_text);
                        courseScheduledText = courseScheduledText.replace("START_DATE",startDate);
                        courseScheduleTv = (ETextView) view.findViewById(R.id.course_content_available_tv);
                        courseScheduleTv.setText(Html.fromHtml(courseScheduledText));
                    }
                }
            }
        }

        if (!(NetworkUtil.isConnected(getActivity()))) {
            AppConstants.offline_flag = true;
        }else{
            AppConstants.offline_flag = false;
        }

        chapterListView = (ListView) view
                .findViewById(R.id.chapter_list);

        initializeAdapter();
        chapterListView.setAdapter(adapter);
        chapterListView.setOnItemClickListener(adapter);
        loadData(view);
        lastClickTime = 0;

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityStarted = true;
        adapter.setStore(db, storage);
        handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        if (!adapter.isEmpty()) {
            fetchLastAccessed(getView());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityStarted = false;
        //We need to cancel the getLastAccessed task if the fragment is stopped
        if(getLastAccessedTask!=null){
            getLastAccessedTask.cancel(true);
        }
    }

    private void initializeAdapter(){
        adapter = new ChapterAdapter(getActivity(), courseId) {

            @Override
            public void onItemClicked(final SectionEntry model) {
                // handle click
                try{
                    if (AppConstants.offline_flag) {
                        boolean isVideoDownloaded = db.isVideoDownloadedInChapter(courseId,
                                model.chapter, null);
                        if(isVideoDownloaded){
                            Intent videoIntent = new Intent(getActivity(),
                                    VideoListActivity.class);
                            videoIntent.putExtra("enrollment", enrollment);
                            videoIntent.putExtra("chapter", model.chapter);
                            videoIntent.putExtra("FromMyVideos", false);
                            startActivity(videoIntent);
                        } else {
                            ((CourseDetailTabActivity) getActivity())
                                    .showOfflineAccessMessage();
                        }
                    } else {
                        Intent lectureIntent = new Intent(mActivity,
                                CourseLectureListActivity.class);
                        lectureIntent.putExtra("enrollment", enrollment);
                        lectureIntent.putExtra("chapter", model);
                        mActivity.startActivity(lectureIntent);
                    }
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void download(SectionEntry model) {
                try {
                    // check if download is only allowed over wifi
                    PrefManager wifiPrefManager = new PrefManager(
                            context, PrefManager.Pref.WIFI);
                    boolean onlyWifi = wifiPrefManager.getBoolean(
                            PrefManager.Key.DOWNLOAD_ON_WIFI, true);
                    Context context = getActivity().getBaseContext();
                    boolean startDownloadFlag = false;
                    if (onlyWifi && NetworkUtil.isConnectedWifi(context)) {
                        startDownloadFlag = true;
                    }else if(!onlyWifi && (NetworkUtil.isConnectedWifi(context)
                            ||NetworkUtil.isConnectedMobile(context))) {
                        startDownloadFlag = true;
                    }else{
                        startDownloadFlag = false;
                    }

                    if(startDownloadFlag){
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
                                .getAvailableExternalMemory(context)) {
                            ((CourseDetailTabActivity) getActivity())
                                    .showMessage(getString(R.string.file_size_exceeded));
                            updateList();
                        } else {
                            if (downloadSize < MemoryUtil.GB) {
                                startDownload(downloadList, downloadCount);
                            } else {
                                showDownloadSizeExceedDialog(downloadList, downloadCount);
                            }
                        }
                    } else {
                        ((CourseDetailTabActivity) getActivity())
                                .showMessage(getString(R.string.wifi_off_message));
                        updateList();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    //Loading data to the Adapter
    private void loadData(final View view) {
        GetCourseHierarchyTask task = new GetCourseHierarchyTask(getActivity()) {
            @Override
            public void onFinish(Map<String, SectionEntry> chapterMap) {
                // display these chapters
                if (chapterMap != null) {
                    LogUtil.log("Start displaying on UI", DateUtil.getCurrentTimeStamp());
                    adapter.clear();
                    for (Entry<String, SectionEntry> entry : chapterMap
                            .entrySet()) {
                        adapter.add(entry.getValue());
                        if (openInBrowserUrl == null || openInBrowserUrl.equalsIgnoreCase("")) {
                            // pick up browser link
                            openInBrowserUrl = entry.getValue().section_url;
                        }
                    }

                    if (AppConstants.offline_flag) {
                        hideOpenInBrowserPanel();
                    } else {
                        fetchLastAccessed(getView());
                        showOpenInBrowserPanel(openInBrowserUrl);
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

                LogUtil.log("Completed displaying data on UI", DateUtil.getCurrentTimeStamp());
            }

            @Override
            public void onException(Exception ex) {
                if(adapter.getCount()==0) {
                    // calling setEmptyView requires adapter to be notified
                    adapter.notifyDataSetChanged();
                    view.findViewById(R.id.no_chapter_tv).setVisibility(View.VISIBLE);
                    chapterListView.setEmptyView(view.findViewById(R.id.no_chapter_tv));
                }
            }
        };

        ProgressBar progressBar = (ProgressBar) view
                .findViewById(R.id.api_spinner);
        task.setProgressDialog(progressBar);
        //Initializing task call
        LogUtil.log("Initializing Chapter Task", DateUtil.getCurrentTimeStamp());
        task.execute(courseId);

    }

    public void fragmentOffline() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        hideLastAccessedView(getView());
        if (chapterListView != null) {
            hideOpenInBrowserPanel();
        }
    }

    public void fragmentOnline() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (chapterListView != null && openInBrowserUrl != null) {
            fetchLastAccessed(getView());
            showOpenInBrowserPanel(openInBrowserUrl);
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
            segIO.trackSectionBulkVideoDownload(downloadList.get(0).eid, 
                    downloadList.get(0).chapter, noOfDownloads);
        }catch(Exception e){
            e.printStackTrace();
        }

        EnqueueDownloadTask downloadTask = new EnqueueDownloadTask(getActivity()) {
            @Override
            public void onFinish(Long result) {
                try {
                    hideProgressDialog();
                    if(isActivityStarted) {
                        adapter.notifyDataSetChanged();
                        ((CourseDetailTabActivity) getActivity()).invalidateOptionsMenu();
                        if (result > 1) {
                            ((CourseDetailTabActivity) getActivity())
                            .showMessage(getString(R.string.started_downloading) + " "
                                    + result + " " + getString(R.string.label_videos));
                        } else if (result == 1) {
                            ((CourseDetailTabActivity) getActivity())
                            .showMessage(getString(R.string.started_downloading) + " "
                                    + result + " " + getString(R.string.label_video));
                        } else {
                            ((CourseDetailTabActivity) getActivity())
                            .showMessage(getString(R.string.msg_video_not_downloaded));
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onException(Exception ex) {
                hideProgressDialog();
                
                ((CourseDetailTabActivity) getActivity())
                .showMessage(getString(R.string.msg_video_not_downloaded));
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
        if(!hidden){
            if(AppConstants.offline_flag){
                hideLastAccessedView(getView());
                hideOpenInBrowserPanel();
            }else{
                fetchLastAccessed(getView());
                if(chapterListView!=null && openInBrowserUrl!=null){
                    showOpenInBrowserPanel(openInBrowserUrl);
                }
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
                        LogUtil.log(getClass().getName(), "removed progress dialog fragment");
                    }
                    
                    if ( !progressDialog.isAdded()) {
                        progressDialog.show(getFragmentManager(), tag);
                        progressDialog.setCancelable(false);
                        LogUtil.log(getClass().getName(), "showing activity indicator");
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void hideProgressDialog(){
        if(progressDialog!=null) {
            synchronized (progressDialog) {
                progressDialog.dismiss();
                LogUtil.log(getClass().getName(), "hiding activity indicator");
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
                                                bundle.getSerializable("enrollment");
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
                                            videoIntent.putExtra("enrollment", enrollment);
                                            videoIntent.putExtra("lecture", lecture);
                                            videoIntent.putExtra("FromMyVideos", false);
                                            
                                            startActivity(videoIntent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
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
                    e.printStackTrace();
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
            e.printStackTrace();
        }
    }



    private void fetchLastAccessed(final View view){
        try{
            if(!isFetchingLastAccessed){
                isFetchingLastAccessed = true;
                if(courseId!=null && getProfile()!=null && getProfile().username!=null){
                    String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
                            .username, courseId);
                    final PrefManager prefManager = new PrefManager(getActivity(), prefName);
                    final String prefModuleId = prefManager.getLastAccessedSubsectionId();
                    LogUtil.log("Last Accessed", "Last Accessed Module ID from Preferences "
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
                                LogUtil.log("Last Accessed", "Last Accessed Module ID from Server Get"
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
                            ex.printStackTrace();
                        }
                    };
                    getLastAccessedTask.execute(courseId);
                }   
            }
        }catch(Exception e){
            e.printStackTrace();
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
                        LogUtil.log("Last Accessed", "Last Accessed Module ID from Server Sync "
                                +result.getLastVisitedModuleId());
                        lastAccessed_subSectionId = result.getLastVisitedModuleId();
                        showLastAccessedView(view);
                    }
                    isFetchingLastAccessed = false;
                }
                @Override
                public void onException(Exception ex) {
                    ex.printStackTrace();
                    isFetchingLastAccessed = false;
                }
            };
            syncLastAccessTask.execute(courseId, prefModuleId);
        }catch(Exception e){
            e.printStackTrace();
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
            //TODO - Remove comment while merging with master
            //logger.error(e);
        }
    }
}

package org.edx.mobile.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.LectureModel;
import org.edx.mobile.model.api.SectionEntry;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.task.EnqueueDownloadTask;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.MediaConsentUtils;
import org.edx.mobile.util.MemoryUtil;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.LectureAdapter;
import org.edx.mobile.view.dialog.DownloadSizeExceedDialog;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.ProgressDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CourseLectureListActivity extends BaseFragmentActivity {

    private View offlineBar;
    private LectureAdapter adapter;
    private DownloadSizeExceedDialog downloadFragment;
    private String openInBrowserUrl;
    private boolean isActivityVisible;
    private static final int MSG_UPDATE_PROGRESS = 1026;
    private EnrolledCoursesResponse enrollment;
    private String activityTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_list);

        offlineBar = (View) findViewById(R.id.offline_bar);
        if (!(NetworkUtil.isConnected(this))) {
            AppConstants.offline_flag = true;
            invalidateOptionsMenu();

            if (offlineBar != null) 
                offlineBar.setVisibility(View.VISIBLE);
        }


        enrollment = (EnrolledCoursesResponse) getIntent().getSerializableExtra(BaseFragmentActivity.EXTRA_ENROLLMENT);

        ArrayList<LectureModel> lectureList = new ArrayList<LectureModel>();

        ListView lectureListView = (ListView) findViewById(R.id.lecture_list);
        initalizeAdaptor();
        adapter.setItems(lectureList);
        lectureListView.setAdapter(adapter);
        lectureListView.setOnItemClickListener(adapter);

        enableOfflineCallback();
    } 

    @Override
    protected void onRestart() {
        super.onRestart();
        if(AppConstants.offline_flag){
            finish();
        }
    }

    private void initalizeAdaptor(){
        adapter = new LectureAdapter(this) {
            @Override
            public void onItemClicked(LectureModel model) {
                try {
                    if (model.videos != null && model.videos.size() > 0 && enrollment != null) {
                        String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
                                .username, enrollment.getCourse().getId());
                        PrefManager prefManager = new PrefManager(CourseLectureListActivity.this, prefName);
                        prefManager.putLastAccessedSubsection(model.videos.get(0).getSection().id, false);
                    }

                    Intent videoIntent = new Intent(CourseLectureListActivity.this,
                            VideoListActivity.class);
                    videoIntent.putExtra(BaseFragmentActivity.EXTRA_ENROLLMENT, enrollment);
                    videoIntent.putExtra("lecture", model);
                    videoIntent.putExtra("FromMyVideos", false);
                    startActivity(videoIntent);
                } catch (Exception e) {
                    logger.error(e);
                }
            }

            @Override

            public void download(final LectureModel lecture) {
                if (!NetworkUtil.isConnected(getContext())) {
                    showInfoMessage(getString(R.string.need_data));
                    updateList();
                } else {
                    IDialogCallback dialogCallback = new IDialogCallback() {
                        @Override
                        public void onPositiveClicked() {
                            startLectureDownload(lecture);
                        }

                        @Override
                        public void onNegativeClicked() {
                            //
                        }
                    };
                    MediaConsentUtils.consentToMediaDownload(CourseLectureListActivity.this, dialogCallback);
                }
            };

        };
    }
    private void startLectureDownload(LectureModel lecture) {

        long downloadSize = 0;
        int downloadCount = 0;
        ArrayList<DownloadEntry> downloadList = new ArrayList<DownloadEntry>();
        for (VideoResponseModel v : lecture.videos) {
        DownloadEntry de = (DownloadEntry) storage
                .getDownloadEntryfromVideoResponseModel(v);

        if(de.downloaded == DownloadEntry.DownloadedState.DOWNLOADING
                || de.downloaded == DownloadEntry.DownloadedState.DOWNLOADED){
                continue;
            }else{
                downloadSize = downloadSize +  v.getSummary().getSize();
                downloadList.add(de);
                downloadCount++;
            }
        }
        if(downloadSize > MemoryUtil.getAvailableExternalMemory(this)){
            showInfoMessage(getString(R.string.file_size_exceeded));
            updateList();
        }else{
            if(downloadSize < MemoryUtil.GB){
                startDownload(downloadList, downloadCount);
            }else{
                showDownloadSizeExceedDialog(downloadList, downloadCount);
            }
        }

    }


    private void loadData() {
        SectionEntry chapter = (SectionEntry) getIntent().getSerializableExtra("lecture");
        setTitle(chapter.chapter);
        activityTitle = chapter.chapter;

        if(chapter.sections.entrySet().size()>0){
            adapter.clear();
            for (Map.Entry<String, ArrayList<VideoResponseModel>> entry : chapter.sections.entrySet()) {
                LectureModel m = new LectureModel();
                m.chapter = chapter;
                m.name = entry.getKey();
                m.videos = entry.getValue();
                if(openInBrowserUrl==null||openInBrowserUrl.equalsIgnoreCase("")){
                    openInBrowserUrl = m.videos.get(0).section_url;
                }
                adapter.add(m);
            }
            adapter.notifyDataSetChanged();
        }

        showOpenInBrowserPanel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try{
            if(adapter==null){
                //If adapter is null, reinitialize the adapter
                initalizeAdaptor();
            }
            if(enrollment!=null){
                adapter.setStore(db, storage, enrollment.getCourse().getId());
            }
            loadData();
            handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
            setTitle(activityTitle);
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableOfflineCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
        isActivityVisible = true;
    }

    @Override
    protected void onOffline() {
        AppConstants.offline_flag = true;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
        if(isActivityVisible){
            finish();
        }
    }

    @Override
    protected void onOnline() {
        AppConstants.offline_flag = false;
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }
        invalidateOptionsMenu();
    }

    private void showOpenInBrowserPanel() {
        try {
            final StringBuffer urlStringBuffer = new StringBuffer();
            if (!openInBrowserUrl.contains("http://") && !openInBrowserUrl.contains("https://")){
                urlStringBuffer.append("http://");
                urlStringBuffer.append(openInBrowserUrl);
            }else{
                urlStringBuffer.append(openInBrowserUrl);
            }
            findViewById(R.id.open_in_browser_panel).setVisibility(
                    View.VISIBLE);
            TextView openInBrowserTv = (TextView) findViewById
                    (R.id.open_in_browser_btn);
            openInBrowserTv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    BrowserUtil.open(CourseLectureListActivity.this, 
                            urlStringBuffer.toString());
                }
            });

        } catch (Exception ex) {
            logger.debug("error in showing player");
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
                downloadFragment.dismiss();
                updateList();
            }
        });
        downloadFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        downloadFragment.show(getSupportFragmentManager(), "dialog");
        downloadFragment.setCancelable(false);
    }

    public void startDownload(ArrayList<DownloadEntry> downloadList, int noOfDownloads){
        try{
            segIO.trackSubSectionBulkVideoDownload(downloadList.get(0).getChapterName(),
                    downloadList.get(0).getSectionName(), downloadList.get(0).getEnrollmentId(), noOfDownloads);
        }catch(Exception e){
            logger.error(e);
        }

        EnqueueDownloadTask downloadTask = new EnqueueDownloadTask(this) {
            @Override
            public void onFinish(Long result) {
                try{
                    hideProgressDialog();
                    if(isActivityStarted()){
                        adapter.notifyDataSetChanged();
                        invalidateOptionsMenu();
                        if(result>1){
                            showInfoMessage(getString(R.string.started_downloading)+" "+result+
                                    " "+getString(R.string.label_videos));
                        }else if (result==1){
                            showInfoMessage(getString(R.string.started_downloading)+" "+result+
                                    " "+getString(R.string.label_video));
                        } else {
                            showInfoMessage(getString(R.string.msg_video_not_downloaded));
                        }
                    }
                }catch(Exception e){
                    logger.error(e);
                }
            }

            @Override
            public void onException(Exception ex) {
                hideProgressDialog();
                showInfoMessage(getString(R.string.msg_video_not_downloaded));
            }
        };
        downloadTask.execute(downloadList);
        if(downloadList.size()>=3){
            showProgressDialog();
        }

    }

    private void updateList(){
        adapter.notifyDataSetChanged();
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

    //Broadcast Receiver to notify all activities to finish if user logs out
    private BroadcastReceiver offlineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    protected void enableOfflineCallback() {
        // register for logout click listener
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.VIDEOLIST_BACK_PRESSED);
        registerReceiver(offlineReceiver, filter);
    } 

    protected void disableOfflineCallback() {
        // un-register logoutReceiver
        unregisterReceiver(offlineReceiver);
    }

    private ProgressDialogFragment progressDialog;
    private synchronized void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialogFragment.newInstance();
        }
        synchronized (progressDialog) {
            try {
                if ( !progressDialog.isVisible()) {
                    final String tag = "progress_dialog_lecture";

                    progressDialog.dismiss();
                    Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
                    if (f != null) {
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.remove(f);
                        ft.commit();
                        logger.debug("removed progress dialog fragment");
                    }

                    if ( !progressDialog.isAdded()) {
                        progressDialog.show(getSupportFragmentManager(), tag);
                        progressDialog.setCancelable(false);
                        logger.debug("showing activity indicator");
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

}

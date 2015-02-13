package org.edx.mobile.view;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.prefs.UserPrefs;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.module.storage.Storage;
import org.edx.mobile.player.PlayerFragment;
import org.edx.mobile.player.VideoListFragment.VideoListCallback;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.view.adapters.MyRecentVideoAdapter;
import org.edx.mobile.view.dialog.DeleteVideoDialogFragment;
import org.edx.mobile.view.dialog.IDialogCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyRecentVideosFragment extends Fragment {

    private MyRecentVideoAdapter adapter;
    private ListView videoListView;
    protected IDatabase db;
    protected IStorage storage;
    private DeleteVideoDialogFragment deleteDialogFragment;
    private int playingVideoIndex = -1;
    private VideoListCallback callback;
    private MyVideosTabActivity containerActivity;
    private DownloadEntry videoModel;
    private Button deleteButton = null;
    private final Handler handler = new Handler();
    private ISegment segIO;
    protected final Logger logger = new Logger(getClass().getName());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initDB();
        View view = inflater.inflate(R.layout.fragment_video_list_with_player_container,
                null);
        try{
            segIO.screenViewsTracking("My Videos - Recent Videos");
        }catch(Exception e){
            logger.error(e);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restore(savedInstanceState);

        try{
            videoListView = (ListView) getView().findViewById(R.id.list_video);

            if (videoListView != null) {
                adapter = new MyRecentVideoAdapter(getActivity(), db) {

                    @Override
                    public void onItemClicked(SectionItemInterface model, int position) {
                        if (model.isDownload()) {

                            if ( !isPlayerVisible()) {
                                // don't try to showPlayer() if already shown here
                                // this will cause player to freeze
                                showPlayer();
                            }
                            // initialize index for this model
                            playingVideoIndex = position;

                            play(model);
                            notifyAdapter();
                        }
                    }

                    @Override
                    public void onSelectItem() {
                        handleCheckboxSelection();
                    }
                };
                // videoAdaptor.setItems(sectionList);
                adapter.setSelectedPosition(playingVideoIndex);
                videoListView.setEmptyView(getView().findViewById(R.id.empty_list_view));
                videoListView.setAdapter(adapter);

                showDeletePanel(getView());
                if (!(NetworkUtil.isConnected(getActivity().getBaseContext()))) {
                    AppConstants.offline_flag = true;
                } else {
                    AppConstants.offline_flag = false;
                }

                videoListView.setOnItemClickListener(adapter);
            } else {
                // probably the landscape player view, so hide action bar
                ActionBar bar = getActivity().getActionBar();
                if(bar!=null){
                    bar.hide();
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            addToRecentAdapter(getView());
        } catch (Exception ex) {
            logger.error(ex);
        }

        if (containerActivity.playerFragment != null) {
            showPlayer();
        }
        notifyAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        hideConfirmDeleteDialog();
        if (getActivity() instanceof MyVideosTabActivity) {
            ((MyVideosTabActivity) getActivity()).invalidateOptionsMenu();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void addToRecentAdapter(View view) {
        try {
            logger.debug("reloading adapter...");

            ArrayList<SectionItemInterface> list = storage.getRecentDownloadedVideosList();
            if (list != null) {
                adapter.clear();
                for (SectionItemInterface m : list) {
                    adapter.add(m);
                }
                logger.debug("reload done");
            }
            if(adapter.getCount()<=0){
                hideDeletePanel(view);
            }
            videoListView.setOnItemClickListener(adapter);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void play(SectionItemInterface model) {
        if (model instanceof DownloadEntry) {
            Api api = new Api(getActivity());
            DownloadEntry v = (DownloadEntry) model;
            try {
                String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
                        .username, v.eid);
                PrefManager prefManager = new PrefManager(getActivity(), prefName);
                VideoResponseModel vrm = api.getVideoById(v.eid, v.videoId);
                prefManager.putLastAccessedSubsection(vrm.getSection().id, false);

                if (callback != null) {
                    videoModel = v;
                    if (getActivity() instanceof MyVideosTabActivity) {
                        ((MyVideosTabActivity)getActivity())
                        .setRecentNextPrevListeners(getNextListener(), getPreviousListener());
                    }
                    callback.playVideoModel(v);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    private void showPlayer() {
        hideDeletePanel(getView());
        try {
            View container = getView().findViewById(R.id.container_player);
            if (container.getVisibility() != View.VISIBLE) {
                container.setVisibility(View.VISIBLE);
            } 
            // add and display player fragment
            if (containerActivity.playerFragment == null) {
                containerActivity.playerFragment = new PlayerFragment();
            }

            // this is for INLINE player only
            if (isResumed() && !isLandscape()) {
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                if (containerActivity.playerFragment.isAdded()) {
                    ft.detach(containerActivity.playerFragment);
                    logger.debug("removing player from view ...");
                    ft.attach(containerActivity.playerFragment);
                    logger.debug("adding player to view ...");
                } else {
                    ft.replace(R.id.container_player, containerActivity.playerFragment, "player");
                }
                ft.commit();

                logger.debug("showing player ...");
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.warn("Error in showing player");
        }
    }

    public void onOffline() {
        try{
            AppConstants.offline_flag = true;
            notifyAdapter();
            videoListView.setOnItemClickListener(adapter);
        }catch(Exception e){
            logger.error(e);
        }
    }

    protected void onOnline() {
        try{
            AppConstants.offline_flag = false;
            notifyAdapter();
            videoListView.setOnItemClickListener(adapter);
        }catch(Exception e){
            logger.error(e);
        }
    }


    private void hideDeletePanel(View view) {
        try {
            // hide delete button panel at bottom
            view.findViewById(R.id.delete_button_panel).setVisibility(View.GONE);

            handler.postDelayed(new Runnable() {
                public void run() {
                    try {
                        // hide checkbox in action bar
                        ((MyVideosTabActivity) getActivity()).hideCheckBox();

                        // hide checkboxes in list
                        notifyAdapter();
                    } catch(Exception ex) {
                        logger.error(ex);
                    }
                }
            }, 300);
        } catch (Exception ex) {
            logger.error(ex);
            logger.warn("error in hiding delete button Panel");
        }
    }


    public void showDeletePanel(View view) {
        try {
            if (!isPlayerVisible()) {
                LinearLayout deletePanel = (LinearLayout) view
                        .findViewById(R.id.delete_button_panel);
                deletePanel.setVisibility(View.VISIBLE);

                deleteButton = (Button) view
                        .findViewById(R.id.delete_btn);
                final Button editButton = (Button) view
                        .findViewById(R.id.edit_btn);
                editButton.setVisibility(View.VISIBLE);
                final Button cancelButton = (Button) view
                        .findViewById(R.id.cancel_btn);

                if(AppConstants.myVideosDeleteMode){
                    deleteButton.setVisibility(View.VISIBLE);
                    cancelButton.setVisibility(View.VISIBLE);
                    editButton.setVisibility(View.GONE);
                }else{
                    deleteButton.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    editButton.setVisibility(View.VISIBLE);
                }


                deleteButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<SectionItemInterface> list = adapter
                                .getSelectedItems();
                        if (list != null && list.size() > 0) 
                            showConfirmDeleteDialog(list.size());
                    }
                });

                cancelButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editButton.setVisibility(View.VISIBLE);
                        videoListView.setOnItemClickListener(adapter);
                        AppConstants.myVideosDeleteMode = false;
                        if (getActivity() instanceof MyVideosTabActivity) {
                            ((MyVideosTabActivity) getActivity()).hideCheckBox();
                        }
                        adapter.unselectAll();
                        AppConstants.myVideosDeleteMode = false;
                        notifyAdapter();
                        deleteButton.setVisibility(View.GONE);
                        cancelButton.setVisibility(View.GONE);
                    }
                });

                editButton.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        editButton.setVisibility(View.GONE);
                        AppConstants.myVideosDeleteMode = true;
                        notifyAdapter();
                        videoListView.setOnItemClickListener(null);
                        disableDeleteButton();
                        deleteButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                        if (getActivity() instanceof MyVideosTabActivity) {
                            ((MyVideosTabActivity) getActivity()).showCheckBox();
                        }
                    }
                });
            }else{
                hideDeletePanel(view);
            }
        } catch (Exception ex) {
            logger.error(ex);
            logger.debug("error in showing delete panel");
        }

    }

    protected void showConfirmDeleteDialog(int itemCount) {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.delete_dialog_title_help));
        if (itemCount == 1) {
            dialogMap.put("message_1",  getString(R.string.delete_single_video_dialog));
        } else {
            dialogMap.put("message_1",  getString(R.string.delete_multiple_video_dialog));
        }
        dialogMap.put("yes_button", getString(R.string.label_delete));
        dialogMap.put("no_button",  getString(R.string.label_cancel));
        deleteDialogFragment = DeleteVideoDialogFragment.newInstance(dialogMap,
                new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                onConfirmDelete();
                deleteDialogFragment.dismiss();
            }

            @Override
            public void onNegativeClicked() {
                deleteDialogFragment.dismiss();
            }
        });
        deleteDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        deleteDialogFragment.show(getFragmentManager(), "dialog");
        deleteDialogFragment.setCancelable(false);
    }

    protected void hideConfirmDeleteDialog() {
        try{
            if(deleteDialogFragment!=null){
                deleteDialogFragment.dismiss();
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    //Deleting Downloaded videos on getting confirmation
    private void onConfirmDelete() {
        try{
            ArrayList<SectionItemInterface> list = adapter.getSelectedItems();
            if (list != null) {
                for (SectionItemInterface section : list) {
                    if (section.isDownload()) {
                        DownloadEntry de = (DownloadEntry) section;
                        storage.removeDownload(de);
                    }
                }
            }
            addToRecentAdapter(getView());
            notifyAdapter();
            videoListView.setOnItemClickListener(adapter);
            AppConstants.myVideosDeleteMode = false;
            ((MyVideosTabActivity) getActivity()).hideCheckBox();
            getView().findViewById(R.id.delete_btn).setVisibility(View.GONE);
            getView().findViewById(R.id.edit_btn).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.cancel_btn).setVisibility(View.GONE);
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    //Handle check box selection in Delete mode
    private void handleCheckboxSelection(){
        try{
            int selectedItemsNo = adapter.getSelectedVideoItemsCount();
            int totalVideos = adapter.getTotalVideoItemsCount();

            if(selectedItemsNo==0){
                disableDeleteButton();
            }else{
                enableDeleteButton();
            }

            if(selectedItemsNo==totalVideos){
                ((MyVideosTabActivity) getActivity()).setMyVideosCheckBoxSelected();
            }else if(selectedItemsNo<totalVideos){
                ((MyVideosTabActivity) getActivity()).unsetMyVideosCheckBoxSelected();
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    //Selecting all downloaded videos in the list for Delete    
    public void setAllVideosSectionChecked() {
        try{
            adapter.selectAll();
            notifyAdapter();
            enableDeleteButton();
        }catch(Exception e){
            logger.error(e);
        }
    }

    //DeSelecting all downloaded videos in the list for Delete
    public void unsetAllVideosSectionChecked() {
        try{
            adapter.unselectAll();
            notifyAdapter();
            disableDeleteButton();
        }catch(Exception e){
            logger.error(e);
        }
    }

    //Disabling Delete button until no video is checked
    private void disableDeleteButton(){
        try{
            deleteButton.setEnabled(false);
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    //Enabling Delete button until no video is checked
    private void enableDeleteButton(){
        try{
            deleteButton.setEnabled(true);
        }catch(Exception ex){
            logger.error(ex);
        }
    }


    public void markPlaying() {
        try {
            DownloadEntry v = videoModel;
            if (v != null) {
                if (v.watched == DownloadEntry.WatchedState.UNWATCHED) {
                    videoModel.watched = DownloadEntry.WatchedState.PARTIALLY_WATCHED;
                    // mark this as partially watches, as playing has started
                    db.updateVideoWatchedState(v.videoId, DownloadEntry.WatchedState.PARTIALLY_WATCHED,
                            setWatchedStateCallback);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void saveCurrentPlaybackPosition(int offset) {
        try {
            DownloadEntry v = videoModel;
            if (v != null) {
                // mark this as partially watches, as playing has started
                db.updateVideoLastPlayedOffset(v.videoId, offset, 
                        setCurrentPositionCallback);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void onPlaybackComplete() {
        try {
            DownloadEntry v = videoModel;
            if (v != null) {
                if (v.watched == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                    videoModel.watched = DownloadEntry.WatchedState.WATCHED;
                    // mark this as partially watches, as playing has started
                    db.updateVideoWatchedState(v.videoId, DownloadEntry.WatchedState.WATCHED,
                            setWatchedStateCallback);
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    private boolean isPlayerVisible() {
        try{
            if (getActivity() == null) {
                return false;
            }
            View container = getActivity().findViewById(R.id.container_player);
            return (container != null && container.getVisibility() == View.VISIBLE);
        }catch(Exception ex){
            logger.error(ex);
        }
        return false;
    }

    public void setCallback(VideoListCallback callback) {
        this.callback = callback;
    }

    public void setContainerActivity(MyVideosTabActivity activity) {
        this.containerActivity = activity;
    }

    public void notifyAdapter() {
        adapter.setSelectedPosition(playingVideoIndex);
        adapter.notifyDataSetChanged();
    }

    /**
     * Returns true if current orientation is LANDSCAPE, false otherwise.
     * @return
     */
    protected boolean isLandscape() {
        return (getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        logger.debug("In onSaveInstance State");
        try{
            outState.putInt("playingVideoIndex", playingVideoIndex);
            outState.putSerializable("model", videoModel);
            super.onSaveInstanceState(outState);
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    /**
     * Container tab will call this method to restore the saved data
     * @param savedInstanceState
     */
    public void restore(Bundle savedInstanceState) {
        try{
            if (savedInstanceState != null) {
                playingVideoIndex = savedInstanceState.getInt("playingVideoIndex", -1);
                videoModel = (DownloadEntry) savedInstanceState.getSerializable("model");
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    public void setSelectionEmpty() {
        try{
            playingVideoIndex = -1;
            if (adapter != null) {
                adapter.setSelectedPosition(playingVideoIndex);
                notifyAdapter();
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    //Play the nextVideo if user selects next from Dialog
    public void playNext() {
        try{
            playingVideoIndex++;
            // check next playable video entry
            while (playingVideoIndex < adapter.getCount()) {
                SectionItemInterface i = adapter.getItem(playingVideoIndex);
                if (i!=null && i instanceof DownloadEntry) {
                    videoModel = (DownloadEntry) i;
                    if (callback != null) {
                        adapter.setSelectedPosition(playingVideoIndex);
                        play(videoModel);
                        break;
                    }
                }
                // try next
                playingVideoIndex++;
            }
        }catch(Exception ex){
            logger.error(ex);
        }
    }

    //Check if next video is available in the Video List
    public boolean hasNextVideo(int index) {
        try{
            if(index==-1){
                index = playingVideoIndex;
            }
            for (int i=(index+1) ; i<adapter.getCount(); i++) {
                SectionItemInterface d = adapter.getItem(i);
                if (d!=null && d instanceof DownloadEntry) {
                    return true;
                }
            }
        }catch(Exception ex){
            logger.error(ex);
        }

        return false;
    }

    public void playPrevious() {
        try{
            playingVideoIndex--;
            // check next playable video entry
            while (playingVideoIndex >= 0) {
                SectionItemInterface i = adapter.getItem(playingVideoIndex);
                if (i!=null && i instanceof DownloadEntry) {
                    videoModel = (DownloadEntry) i;
                    if (callback != null) {
                        adapter.setSelectedPosition(playingVideoIndex);
                        play(videoModel);
                        //callback.playVideoModel(videoModel);
                        break;
                    }
                }
                // try next
                playingVideoIndex--;
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    public boolean hasPreviousVideo(int playingIndex) {
        try{
            for (int i=(playingIndex-1) ; i>=0; i--) {
                SectionItemInterface d = adapter.getItem(i);
                if (d!=null && d instanceof DownloadEntry) {
                    return true;
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
        return false;
    }

    public View.OnClickListener getNextListener(){
        if(hasNextVideo(playingVideoIndex)){
            return new NextClickListener();
        }
        return null;
    }

    public View.OnClickListener getPreviousListener(){
        if(hasPreviousVideo(playingVideoIndex)){
            return new PreviousClickListener();
        }
        return null;
    }

    private class NextClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            playNext();
        }
    }

    private class PreviousClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            playPrevious();
        }
    }

    private void initDB() {
        storage = new Storage(getActivity());

        UserPrefs userprefs = new UserPrefs(getActivity());
        String username = null;
        if (userprefs != null) {
            ProfileModel profile = userprefs.getProfile();
            if(profile!=null){
                username =profile.username;
            }
        }
        db = DatabaseFactory.getInstance(getActivity(), 
                DatabaseFactory.TYPE_DATABASE_NATIVE, username);

        segIO = SegmentFactory.getInstance();
    }

    private DataCallback<Integer> setWatchedStateCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
            logger.debug("Watched State Updated");
        }

        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

    private DataCallback<Integer> setCurrentPositionCallback = new DataCallback<Integer>() {
        @Override
        public void onResult(Integer result) {
            logger.debug("Current Playback Position Updated");
        }

        @Override
        public void onFail(Exception ex) {
            logger.error(ex);
        }
    };

    /**
     * Returns user's profile.
     * @return
     */
    protected ProfileModel getProfile() {
        PrefManager prefManager = new PrefManager(getActivity(), PrefManager.Pref.LOGIN);
        return prefManager.getCurrentUserProfile();
    }

}

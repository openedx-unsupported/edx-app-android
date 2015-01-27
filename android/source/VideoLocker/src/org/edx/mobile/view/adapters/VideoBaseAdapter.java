package org.edx.mobile.view.adapters;

import android.content.Context;

import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.db.DownloadEntry.DownloadedState;

import java.util.ArrayList;

public abstract class VideoBaseAdapter<T> extends BaseListAdapter<T> {

    protected int selectedPosition = -1;
    protected String videoId;

    protected static boolean isPlayerOn = false;
    public VideoBaseAdapter(Context context) {
        super(context);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    /**
     * Getting isPlayerOn Flag
     */
    public boolean getIsPlayerOn() {
        return isPlayerOn;
    }

    /**
     * Setting isPlayerOn Flag
     */
    public void setIsPlayerOn(boolean flag) {
        isPlayerOn = flag;
    }

    /**
     * Returns no of selected items of DownloadEntry Objects.
     * @return
     */
    public int getSelectedVideoItemsCount() {
        try{
            ArrayList<T> selectedItems = getSelectedItems();
            int selectedVideoCount=0;
            for (int i=0; i<selectedItems.size(); i++) {
                T obj= selectedItems.get(i);
                if(obj instanceof DownloadEntry){
                    DownloadEntry de = (DownloadEntry) obj;
                    if(de.downloaded == DownloadedState.DOWNLOADED){
                        selectedVideoCount++;
                    }
                }
            }
            return selectedVideoCount;
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;


    }

    /**
     * Returns no of selected items of DownloadEntry Objects.
     * @return
     */
    public int getTotalVideoItemsCount() {
        try{
            ArrayList<T> items = getItems();
            int videoCount=0;
            for (int i=0; i<items.size(); i++) {
                T obj= items.get(i);
                if(obj instanceof DownloadEntry){
                    DownloadEntry de = (DownloadEntry) obj;
                    if(de.downloaded == DownloadedState.DOWNLOADED){
                        videoCount++;
                    }
                }
            }
            return videoCount;
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getPositionByVideoId(String videoId) {
        ArrayList<T> items = getItems();
        int pos=-1;
        for (int i=0; i<items.size(); i++) {
            T obj= items.get(i);
            if(obj instanceof SectionItemInterface){
                if(((SectionItemInterface) obj).isDownload()){
                    DownloadEntry de = (DownloadEntry)obj; 
                    if(de.videoId.equalsIgnoreCase(videoId)){
                        pos = i;
                        break;
                    }
                }
            }
        }
        return pos;
    }


}

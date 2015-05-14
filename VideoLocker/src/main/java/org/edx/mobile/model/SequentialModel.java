package org.edx.mobile.model;

import android.text.TextUtils;

import org.edx.mobile.model.api.VideoResponseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanning on 4/29/15.
 */
public class SequentialModel extends CourseComponent implements ISequential{
    private List<IVertical> verticals;
    private IChapter chapter;
    private String sectionUrl;

    public SequentialModel(IChapter chapter,String id, String name){
        this.setName( name );
        this.setId(id);
        this.chapter = chapter;
        verticals = new ArrayList<>();
    }

    @Override
    public IChapter getChapter() {
        return chapter;
    }

    @Override
    public List<IVertical> getVerticals() {
        return verticals;
    }

    @Override
    public String getSectionUrl() {
        return sectionUrl;
    }

    @Override
    public void setSectionUrl(String sectionUrl) {
        this.sectionUrl = sectionUrl;
    }

    @Override
    public  IVertical getVerticalById(String vid){
        if (TextUtils.isEmpty(vid))
            return null;
        for(IVertical vertical : verticals){
            if ( vid.equalsIgnoreCase(vertical.getId()) )
                return vertical;
        }
        return null;
    }

    @Override
    public int getVideoCount(){
        int count = 0;
        for(IVertical vertical : verticals){
            count += vertical.getVideoCount();
        }
        return count;
    }

    @Override
    public  List<VideoResponseModel> getVideos(){
        List<VideoResponseModel> videos = new ArrayList<>();
        for(IVertical vertical : verticals){
            videos.addAll( vertical.getVideos() );
        }
        return videos;
    }
}

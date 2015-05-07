package org.edx.mobile.model;

import android.text.TextUtils;

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

    public  IVertical getVerticalById(String vid){
        if (TextUtils.isEmpty(vid))
            return null;
        for(IVertical vertical : verticals){
            if ( vid.equalsIgnoreCase(vertical.getId()) )
                return vertical;
        }
        return null;
    }
}

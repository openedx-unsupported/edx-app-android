package org.edx.mobile.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanning on 4/29/15.
 */
public class ChapterModel extends CourseComponent implements IChapter {

    private List<ISequential> sequentials;
    private ICourse course;


    public ChapterModel(){
        this(null, null, null);
    }

    public ChapterModel(ICourse course, String id, String name){
        sequentials = new ArrayList<>();
        this.course = course;
        setId(id);
        setName( name );
    }


    @Override
    public ICourse getCourse() {
        return course;
    }

    @Override
    public List<ISequential> getSequential() {
        return sequentials;
    }




    public void setCourse(ICourse course) {
        this.course = course;
    }

    public ISequential getSequentialById(String sid){
        if (TextUtils.isEmpty(sid))
            return null;
        for(ISequential sequential : sequentials){
            if ( sid.equalsIgnoreCase(sequential.getId()))
                return sequential;
        }
        return null;
    }

}

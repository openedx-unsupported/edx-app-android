package org.edx.mobile.model;

import org.edx.mobile.model.api.VideoResponseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * we use the term matches the one used in the web
 */
public class VerticalModel extends CourseComponent implements IVertical{

    private List<IUnit> units;
    private ISequential sequential;
    private String unitUrl;

    public VerticalModel(ISequential sequential, String id, String name){
        units = new ArrayList<>();
        this.sequential = sequential;
        this.setName( name );
        this.setId(id);
    }

    @Override
    public ISequential getSequential() {
        return sequential;
    }



    @Override
    public List<IUnit> getUnits() {
        return units;
    }

    public void setUnits(List<IUnit> units) {
        this.units = units;
    }

    public void setSequential(ISequential sequential) {
        this.sequential = sequential;
    }

    @Override
    public String getUnitUrl() {
        return unitUrl;
    }

    @Override
    public void setUnitUrl(String unitUrl) {
        this.unitUrl = unitUrl;
    }

    @Override
    public int getVideoCount(){
        int count = 0;
        for( IUnit unit :units ){
            //TODO - we may need to create enum type for categories
           if ("video".equals(unit.getCategory())  ){
               count ++;
           }
        }
        return count;
    }

    public  List<VideoResponseModel> getVideos(){
        List<VideoResponseModel> videos = new ArrayList<>();
        for(IUnit unit :units){
            if ( "video".equals(unit.getCategory()) )
                videos.add(unit.getVideoResponseModel());
        }
        return videos;
    }
}

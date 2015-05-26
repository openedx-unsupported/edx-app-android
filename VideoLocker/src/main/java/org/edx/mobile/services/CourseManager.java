package org.edx.mobile.services;

import org.edx.mobile.model.course.BlockModel;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.CourseStructureV1Model;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.model.course.IBlock;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.model.course.VideoData;

/**
 * no matter it is a smart phone, phablet, or tablet, app should show
 * the detail of only one course detail. so keep the logic at service
 * level is better than passing around between activities and fragments.
 *
 * TODO - the data will be persistent.
 *
 */
public class CourseManager {

    private static CourseManager instance;

    public static synchronized CourseManager getSharedInstance(){
        if ( instance == null )
            instance = new CourseManager();
        return instance;
    }


    private CourseManager(){

    }

    public static IBlock normalizeCourseStructure(CourseStructureV1Model courseStructureV1Model){
        BlockModel topBlock = courseStructureV1Model.getBlockById(courseStructureV1Model.root);
        CourseComponent course = new CourseComponent(topBlock, null);

        for (BlockModel m : courseStructureV1Model.getDescendants(topBlock)) {
            normalizeCourseStructure(courseStructureV1Model,m,course);
        }
        return course;
    }

    public static void normalizeCourseStructure(CourseStructureV1Model courseStructureV1Model,
                                                BlockModel block,
                                                CourseComponent parent){

        if ( block.isContainer() ){
            CourseComponent child = new CourseComponent(block, parent);
            for (BlockModel m : courseStructureV1Model.getDescendants(block)) {
                normalizeCourseStructure(courseStructureV1Model, m, child);
            }
        } else {
            if ( BlockType.VIDEO == block.type && block.data != null
                && (block.data  instanceof VideoData) ){
                new VideoBlockModel(block, parent);
            } else { //everything else.. we fallback to html component
                new HtmlBlockModel(block, parent);
            }
        }
    }
}

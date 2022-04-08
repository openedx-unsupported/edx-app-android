package org.edx.mobile.data.dao;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import org.edx.mobile.coursemultilingual.CourseMultilingualModel;

import java.util.List;

public interface CourseMultilingualDao {
    @Query("Select * from coursemultilingual where course_key in (:course_key)")
    List<CourseMultilingualModel> getCoursemultiLingualDataByCourseKey(String course_key);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CourseMultilingualModel courseMultilingualModel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<CourseMultilingualModel> courseMultilingualModelList
    );
}

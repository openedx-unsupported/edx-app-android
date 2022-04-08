package org.edx.mobile.data;

import org.edx.mobile.coursemultilingual.CourseMultilingualModel;

import java.util.List;

public interface ILocalDataSource {
    void clear();
    void insertMultilingualTranslation(List<CourseMultilingualModel> courseMultilingualModelList);
    void getCourseMultilingualModelByCourseKey(String courseKey, OnResult<List<CourseMultilingualModel>> listener);
}

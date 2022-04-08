package org.edx.mobile.data;

import org.edx.mobile.app.AppExecutors;
import org.edx.mobile.coursemultilingual.CourseMultilingualModel;

import java.util.List;

public class LocalDataSource implements ILocalDataSource {
    private final SubodhaDataBase mAppDatabase;
    private AppExecutors executors;

    public LocalDataSource(SubodhaDataBase mAppDatabase) {
        this.mAppDatabase = mAppDatabase;
        executors = new AppExecutors();
    }

    @Override
    public void clear() {
        executors.getDiskIo().execute(new Runnable() {
            @Override
            public void run() {
                mAppDatabase.clearAllTables();
            }
        });
    }

    @Override
    public void insertMultilingualTranslation(List<CourseMultilingualModel> courseMultilingualModelList) {
        executors.getDiskIo().execute(new Runnable() {
            @Override
            public void run() {
                mAppDatabase.courseMultilingualDao().insert(courseMultilingualModelList);
            }
        });
    }

    @Override
    public void getCourseMultilingualModelByCourseKey(String courseKey, OnResult<List<CourseMultilingualModel>> listener) {
        executors.getDiskIo().execute(new Runnable() {
            @Override
            public void run() {
                final List<CourseMultilingualModel> courseMultilingualModels = mAppDatabase.
                        courseMultilingualDao().getCoursemultiLingualDataByCourseKey(courseKey);
                executors.getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        listener.onResult(courseMultilingualModels);
                    }
                });
            }
        });
    }

}

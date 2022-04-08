package org.edx.mobile.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.edx.mobile.coursemultilingual.CourseMultilingualModel;
import org.edx.mobile.data.dao.CourseMultilingualDao;

@Database(
        entities = {
                CourseMultilingualModel.class
        },
        version = 1,
        exportSchema = false
)

@TypeConverters({DbTypeConverters.class})
public abstract class SubodhaDataBase extends RoomDatabase {
        public abstract CourseMultilingualDao courseMultilingualDao();

}

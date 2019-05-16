package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.UnitStatus;

import java.util.List;

@Dao
public interface UnitStatusDao {

    @Query("Select * from unit_status where username = :username and course_id = :courseId")
    List<UnitStatus> getAllByCourse(String username, String courseId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<UnitStatus> statuses);

}

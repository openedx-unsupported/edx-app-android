package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.Certificate;

import java.util.List;

@Dao
public interface CertificateDao {

    @Query("Select * from certificate where username = :username")
    List<Certificate> getAll(String username);

    @Query("Select * from certificate where course_id = :courseId and username = :username")
    Certificate getByCourseId(String courseId, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Certificate certificate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Certificate> certificates);

}

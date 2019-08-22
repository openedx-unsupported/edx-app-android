package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.Unit;

import java.util.List;

@Dao
public interface UnitDao {

    @Query("Select * from unit " +
            "where programId = :programId and sectionId = :sectionId " +
            "limit :take offset (:take*:skip)")
    List<Unit> getAll(String programId, String sectionId, int take, int skip);

    @Query("Select * from unit where id = :id")
    Unit getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Unit> units);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Unit unit);

}

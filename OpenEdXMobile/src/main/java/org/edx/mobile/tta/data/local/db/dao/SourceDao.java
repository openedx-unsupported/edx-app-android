package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.Source;

import java.util.List;

@Dao
public interface SourceDao {

    @Query("Select * from source")
    List<Source> getAll();

    @Query("Select * from source where id = :id")
    Source getById(long id);

    @Insert
    void insert(Source source);

    @Insert
    void insert(List<Source> sources);

}

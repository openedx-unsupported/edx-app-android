package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.Content;

import java.util.List;

@Dao
public interface ContentDao {

    @Query("Select * from content")
    List<Content> getAll();

    @Query("Select * from content where id = :id")
    Content getById(long id);

    @Query("Select * from content where source_identity = :sourceIdentity")
    Content getBySourceIdentity(String sourceIdentity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Content content);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Content> contents);
}

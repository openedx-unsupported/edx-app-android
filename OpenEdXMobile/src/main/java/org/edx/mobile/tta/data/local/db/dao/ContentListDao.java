package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.ContentList;

import java.util.List;

@Dao
public interface ContentListDao {

    @Query("Select * from content_list")
    List<ContentList> getAll();

    @Query("Select * from content_list where id = :id")
    ContentList getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContentList contentList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<ContentList> contentLists);

}

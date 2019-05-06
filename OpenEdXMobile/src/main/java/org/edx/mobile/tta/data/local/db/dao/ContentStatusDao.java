package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.ContentStatus;

import java.util.List;

@Dao
public interface ContentStatusDao {

    @Query("Select * from content_status where username = :username")
    List<ContentStatus> getAll(String username);

    @Query("Select * from content_status where id = :id and username = :username")
    ContentStatus getById(long id, String username);

    @Query("Select * from content_status where content_id = :contentId and username = :username")
    ContentStatus getByContentId(long contentId, String username);

    @Query("Select * from content_status where username = :username and content_id in (:contentIds)")
    List<ContentStatus> getAllByContentIds(List<Long> contentIds, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ContentStatus contentStatus);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<ContentStatus> statuses);

}

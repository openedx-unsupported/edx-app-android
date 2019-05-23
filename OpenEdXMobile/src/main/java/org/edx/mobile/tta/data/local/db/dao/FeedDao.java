package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.Feed;

import java.util.List;

@Dao
public interface FeedDao {

    @Query("Select * from feed where username = :username " +
            "order by action_on desc, `order` asc " +
            "limit :take offset (:take * :skip)")
    List<Feed> getAll(String username, int take, int skip);

    @Query("Select * from feed where id = :id and username = :username")
    Feed getById(String id, String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Feed content);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Feed> contents);

}

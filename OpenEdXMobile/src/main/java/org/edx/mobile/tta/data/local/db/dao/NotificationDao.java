package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import org.edx.mobile.tta.data.local.db.table.Notification;

import java.util.List;

@Dao
public interface NotificationDao {

    @Query("Select * from notification " +
            "where username = :username " +
            "order by created_time desc")
    List<Notification> getAll(String username);

    @Query("Select * from notification " +
            "where username = :username " +
            "order by created_time desc " +
            "limit :take offset (:take * :skip)")
    List<Notification> getAllInPage(String username, int take, int skip);

    @Query("Select * from notification " +
            "where username = :username and updated = 0 and seen = 1")
    List<Notification> getAllUnupdated(String username);

    @Query("Select * from notification where id = :id")
    Notification getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notification notification);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<Notification> notification);

    @Query("Update notification set updated = 1 where id = :id")
    void updateNotification(String id);

    @Update
    void update(List<Notification> notifications);

}

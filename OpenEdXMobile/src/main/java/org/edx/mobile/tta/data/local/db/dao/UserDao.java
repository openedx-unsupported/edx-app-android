package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.User;

import java.util.List;

@Dao
public interface  UserDao {
    @Query("SELECT * FROM users")
    List<User> loadAll();

    @Insert
    Long insert(User user);

}

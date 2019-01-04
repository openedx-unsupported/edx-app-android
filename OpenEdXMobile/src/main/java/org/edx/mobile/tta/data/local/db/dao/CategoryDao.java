package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("Select * from category")
    List<Category> getAll();

    @Query("Select * from category where id = :id")
    Category getById(long id);

    @Insert
    void insert(Category category);

    @Insert
    void insert(List<Category> categories);
}

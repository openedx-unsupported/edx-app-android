package org.edx.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.edx.mobile.tta.data.local.db.table.Program;

import java.util.List;

@Dao
public interface ProgramDao {

    @Query("Select * from program where username = :username")
    List<Program> getAll(String username);

    @Query("Select * from program where id = :id")
    Program getById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Program> programs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Program program);

}

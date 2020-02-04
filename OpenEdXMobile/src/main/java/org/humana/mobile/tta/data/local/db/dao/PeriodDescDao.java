package org.humana.mobile.tta.data.local.db.dao;

import android.app.DownloadManager;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.humana.mobile.tta.data.local.db.table.DownloadPeriodDesc;
import org.humana.mobile.tta.data.local.db.table.Period;

import java.util.List;
@Dao
public interface PeriodDescDao {
    @Query("Select * from periodDesc where about_url = :about_url")
    List<DownloadPeriodDesc> getAll(String about_url);

    @Query("Select * from periodDesc where id = :id")
    DownloadPeriodDesc getById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DownloadPeriodDesc periodDesc);
}

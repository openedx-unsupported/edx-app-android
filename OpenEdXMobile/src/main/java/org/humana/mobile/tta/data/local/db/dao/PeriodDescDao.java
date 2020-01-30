package org.humana.mobile.tta.data.local.db.dao;

import android.arch.persistence.room.Query;

import org.humana.mobile.tta.data.local.db.table.DownloadPeriodDesc;

import java.util.List;

public interface PeriodDescDao {
    @Query("Select * from periodDesc where about_url = :about_url")
    List<DownloadPeriodDesc> getAll(String about_url);

    @Query("Select * from periodDesc where id = :id")
    DownloadPeriodDesc getById(long id);
}

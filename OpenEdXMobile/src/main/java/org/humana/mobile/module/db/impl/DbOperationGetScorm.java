package org.humana.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.humana.mobile.model.VideoModel;
import org.humana.mobile.module.db.DatabaseModelFactory;
import org.humana.mobile.model.VideoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arjun Singh on 06,May,2020
 */
public class DbOperationGetScorm extends DbOperationSelect<VideoModel> {

    DbOperationGetScorm(boolean distinct, String table, String[] columns,
                        String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }

    @Override
    public VideoModel execute(SQLiteDatabase db) {
        List<VideoModel> list = new ArrayList<VideoModel>();

        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                VideoModel video = DatabaseModelFactory.getModel(c);
                list.add(video);
            } while (c.moveToNext());
        }
        c.close();

        return list.get(0);
    }

    @Override
    public VideoModel getDefaultValue() {
        return null;
    }
}

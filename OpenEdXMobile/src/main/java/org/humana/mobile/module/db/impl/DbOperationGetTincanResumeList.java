package org.humana.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.humana.mobile.module.db.DatabaseModelFactory;
import org.humana.mobile.tta.tincan.model.Resume;

import java.util.ArrayList;
import java.util.List;

public class DbOperationGetTincanResumeList extends DbOperationSelect<List<Resume>> {

    DbOperationGetTincanResumeList(boolean distinct, String table, String[] columns,
                                   String whereClause, String[] whereArgs, String orderBy, String limit) {
        super(distinct,table,columns,whereClause,whereArgs,orderBy,limit);
    }

    @Override
    public List<Resume> execute(SQLiteDatabase db) {
        List<Resume> resumeList=new ArrayList<>();

        Cursor cursor = getCursor(db);

        if(cursor==null) {
            return resumeList;
        }

        if (cursor.moveToFirst()) {
            do {
                resumeList.add(DatabaseModelFactory.getResumeModel(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return resumeList;
    }

    @Override
    public List<Resume> getDefaultValue() {
        return null;
    }
}

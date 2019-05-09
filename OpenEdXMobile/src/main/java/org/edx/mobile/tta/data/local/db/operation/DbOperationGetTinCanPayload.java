package org.edx.mobile.tta.data.local.db.operation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.module.db.DatabaseModelFactory;
import org.edx.mobile.module.db.impl.DbOperationSelect;
import org.edx.mobile.tta.tincan.model.Resume;

public class DbOperationGetTinCanPayload extends DbOperationSelect<Resume> {

    public DbOperationGetTinCanPayload(boolean distinct, String table, String[] columns,
                                String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }

    @Override
    public Resume execute(SQLiteDatabase db) {
        Resume resume = new Resume();

        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                resume = DatabaseModelFactory.getResumeModel(c);
            } while (c.moveToNext());
        }
        c.close();

        return resume;
    }

    @Override
    public Resume getDefaultValue() {
        return new Resume();
    }

}

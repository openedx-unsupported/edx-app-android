package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class DbOperationGetCount extends DbOperationSelect<Integer> {
    
    DbOperationGetCount(boolean distinct, String table, String[] columns,
            String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }
    
    @Override
    public Integer execute(SQLiteDatabase db) {
        Cursor c = getCursor(db);
        int count = c.getCount();
        c.close();
        
        return count;
    }
    
}

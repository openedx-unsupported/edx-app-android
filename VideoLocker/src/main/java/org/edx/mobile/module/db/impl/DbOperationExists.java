package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class DbOperationExists extends DbOperationSelect<Boolean> {
    
    DbOperationExists(boolean distinct, String table, String[] columns,
            String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }
    
    @Override
    public Boolean execute(SQLiteDatabase db) {
        Cursor c = getCursor(db);
        Boolean exists = (c.getCount() > 0);
        c.close();
        return (exists != null && exists);
    }
    
    @Override
    public Boolean getDefaultValue() {
        return false;
    }
    
}

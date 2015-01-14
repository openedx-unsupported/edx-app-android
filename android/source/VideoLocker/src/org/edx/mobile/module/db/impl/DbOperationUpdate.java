package org.edx.mobile.module.db.impl;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

class DbOperationUpdate extends DbOperationBase<Integer> {
    
    private String table;
    private ContentValues values;
    private String whereClause;
    private String[] whereArgs;
    
    DbOperationUpdate(String table, ContentValues values, String whereClause, String[] whereArgs) {
        this.table = table;
        this.values = values;
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
    }

    @Override
    public Integer execute(SQLiteDatabase db) {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided");
        }
        
        if (values == null) {
            throw new IllegalArgumentException("values must be provided");
        }
        
        int count = db.update(table, values, whereClause, whereArgs);
        
        return count;
    }

}

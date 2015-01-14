package org.edx.mobile.module.db.impl;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

class DbOperationInsert extends DbOperationBase<Long> {
    
    private String table;
    private ContentValues values;
    
    DbOperationInsert(String table, ContentValues values) {
        this.table = table;
        this.values = values;
    }

    @Override
    public Long execute(SQLiteDatabase db) {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided");
        }
        
        if (values == null) {
            throw new IllegalArgumentException("values must be provided");
        }
        
        long id = db.insert(table, null, values);
        
        return id;
    }

}

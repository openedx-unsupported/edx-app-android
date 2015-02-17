package org.edx.mobile.module.db.impl;

import android.database.sqlite.SQLiteDatabase;

class DbOperationDelete extends DbOperationBase<Integer> {

    private String table;
    private String whereClause;
    private String[] whereArgs;
    
    DbOperationDelete(String table, String whereClause, String[] whereArgs) {
        this.table = table;
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
    }
    
    @Override
    public Integer execute(SQLiteDatabase db) {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided");
        }
        
        int count = db.delete(table, whereClause, whereArgs);
        
        return count;
    }

}

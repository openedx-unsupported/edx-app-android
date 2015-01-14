package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

abstract class DbOperationSelect<T> extends DbOperationBase<T> {
    
    private String table;
    private String[] columns;
    private String whereClause;
    private String[] whereArgs;
    private String orderBy;
    private boolean distinct;
    
    DbOperationSelect(boolean distinct,String table, String[] columns, String whereClause, String[] whereArgs, String orderBy) {
        this.distinct = distinct;
        this.table = table;
        this.columns = columns;
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
        this.orderBy = orderBy;
    }
    
    public Cursor getCursor(SQLiteDatabase db) {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided");
        }
        
        Cursor c = db.query(distinct,table, columns, whereClause, whereArgs, null, null, orderBy, null);
        
        return c;
    }
    
}

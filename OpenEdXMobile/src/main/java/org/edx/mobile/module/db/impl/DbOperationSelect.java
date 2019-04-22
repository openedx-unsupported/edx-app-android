package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class DbOperationSelect<T> extends DbOperationBase<T> {
    
    private String table;
    private String[] columns;
    private String whereClause;
    private String[] whereArgs;
    private String orderBy;
    private boolean distinct;
    private String groupBy;
    private String limit;

    public DbOperationSelect(boolean distinct,String table, String[] columns, String whereClause, String[] whereArgs, String orderBy) {
        this.distinct = distinct;
        this.table = table;
        this.columns = columns;
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
        this.orderBy = orderBy;
    }

    public DbOperationSelect(boolean distinct, String table, String[] columns, String whereClause, String[] whereArgs, String groupBy, String orderBy, String limit) {
        this.distinct = distinct;
        this.table = table;
        this.columns = columns;
        this.whereClause = whereClause;
        this.whereArgs = whereArgs;
        this.groupBy = groupBy;
        this.orderBy = orderBy;
        this.limit = limit;
    }


    
    public Cursor getCursor(SQLiteDatabase db) {
        if (table == null) {
            throw new IllegalArgumentException("table must be provided");
        }
        try {
            Cursor c = db.query(distinct, table, columns, whereClause, whereArgs, groupBy, null, orderBy, limit);
            return c;
        }catch (Exception ex){
            logger.error(ex);
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
}

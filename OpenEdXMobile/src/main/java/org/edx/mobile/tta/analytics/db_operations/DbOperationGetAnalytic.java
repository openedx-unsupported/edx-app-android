package org.edx.mobile.tta.analytics.db_operations;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.module.db.DatabaseModelFactory;
import org.edx.mobile.module.db.impl.DbOperationSelect;
import org.edx.mobile.tta.analytics.AnalyticModel;

import java.util.ArrayList;

public class DbOperationGetAnalytic extends DbOperationSelect<ArrayList<AnalyticModel>> {

    public DbOperationGetAnalytic(boolean distinct, String table, String[] columns,
                           String whereClause, String[] whereArgs, String orderBy,String limit) {
        super(distinct, table, columns, whereClause, whereArgs, null, orderBy,limit);
    }

    @Override
    public ArrayList<AnalyticModel> execute(SQLiteDatabase db) {
        ArrayList<AnalyticModel> analyticlst=new ArrayList<>();

        Cursor cursor = getCursor(db);

        if(cursor==null) {
            return analyticlst;
        }

        if (cursor.moveToFirst()) {
            do {
                analyticlst.add(DatabaseModelFactory.getAnalyticModel(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return analyticlst;
    }

    @Override
    public ArrayList<AnalyticModel> getDefaultValue() {
        // Returning null should be fine here, as video should only be queried if
        // it exists, or at least there should be a null check on the client code
        // otherwise. If we want to return an empty object here, then we will
        // need an appropriate constructor or initializer in the default
        // VideoModel implementation as well.
        return null;
    }

}

package org.edx.mobile.tta.data.local.db;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import org.edx.mobile.tta.data.local.db.dao.UserDao;
import org.edx.mobile.tta.data.local.db.table.User;

@Database(entities = {User.class}, version = 2, exportSchema = false)
public abstract class TADatabase extends RoomDatabase {

    public abstract UserDao userDao();
}

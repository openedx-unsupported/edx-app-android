package org.edx.mobile.tta.data.local.db;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import org.edx.mobile.tta.data.local.db.dao.CategoryDao;
import org.edx.mobile.tta.data.local.db.dao.CertificateDao;
import org.edx.mobile.tta.data.local.db.dao.ContentDao;
import org.edx.mobile.tta.data.local.db.dao.ContentListDao;
import org.edx.mobile.tta.data.local.db.dao.FeedDao;
import org.edx.mobile.tta.data.local.db.dao.SourceDao;
import org.edx.mobile.tta.data.local.db.dao.UserDao;
import org.edx.mobile.tta.data.local.db.table.Certificate;
import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.data.local.db.table.User;
import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.local.db.table.Source;

@Database(
        entities = {
                User.class,
                Category.class,
                Content.class,
                ContentList.class,
                Source.class,
                Feed.class,
                Certificate.class
        },
        version = 4,
        exportSchema = false
)
@TypeConverters({DbTypeConverters.class})
public abstract class TADatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract ContentDao contentDao();
    public abstract ContentListDao contentListDao();
    public abstract SourceDao sourceDao();
    public abstract FeedDao feedDao();
    public abstract CertificateDao certificateDao();
}

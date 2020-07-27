package org.humana.mobile.tta.data.local.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;

import org.humana.mobile.tta.data.local.db.dao.CategoryDao;
import org.humana.mobile.tta.data.local.db.dao.CertificateDao;
import org.humana.mobile.tta.data.local.db.dao.ContentDao;
import org.humana.mobile.tta.data.local.db.dao.ContentListDao;
import org.humana.mobile.tta.data.local.db.dao.ContentStatusDao;
import org.humana.mobile.tta.data.local.db.dao.CurricullamDao;
import org.humana.mobile.tta.data.local.db.dao.FeedDao;
import org.humana.mobile.tta.data.local.db.dao.NotificationDao;
import org.humana.mobile.tta.data.local.db.dao.PeriodDao;
import org.humana.mobile.tta.data.local.db.dao.PeriodDescDao;
import org.humana.mobile.tta.data.local.db.dao.ProgramDao;
import org.humana.mobile.tta.data.local.db.dao.SectionDao;
import org.humana.mobile.tta.data.local.db.dao.SourceDao;
import org.humana.mobile.tta.data.local.db.dao.UnitDao;
import org.humana.mobile.tta.data.local.db.dao.UnitStatusDao;
import org.humana.mobile.tta.data.local.db.dao.UserDao;
import org.humana.mobile.tta.data.local.db.table.Category;
import org.humana.mobile.tta.data.local.db.table.Certificate;
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.data.local.db.table.ContentList;
import org.humana.mobile.tta.data.local.db.table.ContentStatus;
import org.humana.mobile.tta.data.local.db.table.CurricullamChaptersModel;
import org.humana.mobile.tta.data.local.db.table.CurricullamModel;
import org.humana.mobile.tta.data.local.db.table.DownloadPeriodDesc;
import org.humana.mobile.tta.data.local.db.table.Feed;
import org.humana.mobile.tta.data.local.db.table.Notification;
import org.humana.mobile.tta.data.local.db.table.Period;
import org.humana.mobile.tta.data.local.db.table.Program;
import org.humana.mobile.tta.data.local.db.table.Section;
import org.humana.mobile.tta.data.local.db.table.Source;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.local.db.table.UnitStatus;
import org.humana.mobile.tta.data.local.db.table.User;

import java.io.IOException;

@Database(
        entities = {
                User.class,
                Category.class,
                Content.class,
                ContentList.class,
                Source.class,
                Feed.class,
                Certificate.class,
                Notification.class,
                ContentStatus.class,
                UnitStatus.class,
                Program.class,
                Section.class,
                Period.class,
                Unit.class,
                CurricullamChaptersModel.class,
                DownloadPeriodDesc.class,
        },
        version = 9,
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

    public abstract NotificationDao notificationDao();

    public abstract ContentStatusDao contentStatusDao();

    public abstract UnitStatusDao unitStatusDao();

    public abstract ProgramDao programDao();

    public abstract SectionDao sectionDao();

    public abstract PeriodDao periodDao();

    public abstract PeriodDescDao periodDescDao();

    public abstract UnitDao unitDao();

    public abstract CurricullamDao curricullamDao();

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE unit ADD COLUMN periodName TEXT");
        }
    };

    public static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE unit ADD COLUMN unit_id TEXT");
        }
    };

    public static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE period ADD COLUMN total_points TEXT");
            database.execSQL("ALTER TABLE period ADD COLUMN completed_points TEXT");
        }
    };
    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE unit ADD COLUMN disablecheck BOOLEAN");

        }
    };
}

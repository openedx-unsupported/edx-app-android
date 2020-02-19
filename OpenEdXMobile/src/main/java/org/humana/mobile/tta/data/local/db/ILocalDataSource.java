package org.humana.mobile.tta.data.local.db;

import org.humana.mobile.tta.data.local.db.table.Category;
import org.humana.mobile.tta.data.local.db.table.Certificate;
import org.humana.mobile.tta.data.local.db.table.ContentList;
import org.humana.mobile.tta.data.local.db.table.ContentStatus;
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
import org.humana.mobile.tta.data.local.db.table.Content;
import org.humana.mobile.tta.data.model.library.CollectionConfigResponse;
import org.humana.mobile.tta.data.model.program.NotificationCountResponse;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */
public interface ILocalDataSource {
    Observable<List<User>> getAllUsers();
    Observable<Boolean> insertUser(final User user);

    void clear();

    CollectionConfigResponse getConfiguration();
    void insertConfiguration(CollectionConfigResponse response);

    List<Content> getContents();
    void insertContents(List<Content> contents);
    Content getContentById(long id);
    void insertContent(Content content);
    Content getContentBySourceIdentity(String sourceIdentity);

    List<Feed> getFeeds(String username, int take, int skip);
    void insertFeeds(List<Feed> feeds);

    Category getCategoryBySourceId(long sourceId);

    List<ContentList> getContentListsByCategoryId(long categoryId);
    List<ContentList> getContentListsByCategoryIdAndMode(long categoryId, String mode);
    List<ContentList> getContentListsByRootCategory(String rootCategory);

    List<Source> getSources();

    List<Certificate> getAllCertificates(String username);
    Certificate getCertificate(String courseId, String username);
    void insertCertificates(List<Certificate> certificates);
    void insertCertificate(Certificate certificate);

    List<Notification> getAllNotifications(String username);
    List<Notification> getAllNotificationsInPage(String username, int take, int skip);
    List<Notification> getAllUnupdatedNotifications(String username);
    void insertNotification(Notification notification);
    void insertNotifications(List<Notification> notifications);
    void updateNotifications(List<Notification> notifications);

    List<ContentStatus> getMyContentStatuses(String username);
    List<ContentStatus> getContentStatusesByContentIds(List<Long> contentIds, String username);
    ContentStatus getContentStatusByContentId(long contentId, String username);
    void insertContentStatus(ContentStatus contentStatus);
    void insertContentStatuses(List<ContentStatus> statuses);

    List<UnitStatus> getUnitStatusByCourse(String username, String courseId);
    void insertUnitStatuses(List<UnitStatus> statuses);

    List<Program> getPrograms(String username);
    Program getProgram(String id);
    void insertPrograms(List<Program> programs);
    void insertProgram(Program program);

    List<Section> getSections(String username);
    Section getSection(String id);
    void insertSections(List<Section> sections);
    void insertSection(Section section);

    List<Period> getPeriods(String username);
    Period getPeriod(long id);
    void insertPeriods(List<Period> periods);
    void insertPeriod(Period period);

    List<Unit> getUnits(String programId, String sectionId, int take, int skip);
    Unit getUnit(String id);
    void insertUnits(List<Unit> units);
    void insertUnit(Unit unit);

}
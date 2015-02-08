package org.edx.mobile.test.module;

import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.db.DownloadEntry.DownloadedState;
import org.edx.mobile.model.db.DownloadEntry.WatchedState;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.db.impl.DatabaseFactory;
import org.edx.mobile.test.BaseTestCase;

import java.util.List;

public class DbTests extends BaseTestCase {

    final Object lock = new Object();
    private IDatabase db;
    private final String username = "unittest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        db = DatabaseFactory.getInstance(getInstrumentation()
                .getTargetContext().getApplicationContext(),
                DatabaseFactory.TYPE_DATABASE_NATIVE, username);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        db.release();
    }

    public void testDeleteVideo() throws Exception {
        String videoId = "testVideoId";

        db.clearDataByUser(username);

        DownloadEntry de = getDummyVideoModel();
        de.videoId = videoId;
        Long rowId = db.addVideoData(de, null);
        assertNotNull(rowId);
        assertTrue("Row Id must be non zero positive number", rowId > 0);

        IVideoModel video = db.getVideoEntryByVideoId(videoId, null);
        assertNotNull("Shoud have got one video object", video);

        Integer count = db.deleteVideoByVideoId(video, null);
        assertNotNull(count);
        assertTrue("Should have deleted ONE video only", count == 1);
    }

    public void testInsert() throws Exception {
        DownloadEntry de = getDummyVideoModel();
        de.isCourseActive = 1;
        de.downloaded = DownloadedState.DOWNLOADING;

        db.addVideoData(de, new DataCallback<Long>() {
            @Override
            public void onResult(Long result) {
                print( "inserted id: " + result);
                assertNotNull(result);
                assertTrue(result > 0);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testPrintTable() throws Exception {
        db.getAllVideos("shahid", new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                print( "got results for all videos");
                for (IVideoModel v : result) {
                    print( v.getTitle());
                    print( "ID : " + v.getVideoId()+ " isDownloaded : "
                            + v.getDownloadedStateOrdinal());
                }

                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetVideoEntryByVideoId() throws Exception {
        db.clearDataByUser(username);
        String videoid="videoid";
        db.getVideoEntryByVideoId(videoid, new DataCallback<IVideoModel>() {
            @Override
            public void onResult(IVideoModel result) {
                assertTrue(result == null);
                print( "result for get VideoEntryByVideoId is:" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });

        lock();
        DownloadEntry de=getDummyVideoModel();
        de.videoId="videoid";
        db.addVideoData(de, null);

        db.getVideoEntryByVideoId(videoid, new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertTrue(result != null);
                print( "result for get VideoEntryByVideoId in AssertTrue:" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });

        lock();
    }

    public void testisAnyVideoDownloading() throws Exception {
        db.clearDataByUser(username);
        db.isAnyVideoDownloading(new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertFalse("something is downloading", result);
                print( "result for Video AnyVideoDownloading is:" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();

        DownloadEntry de = getDummyVideoModel();
        // avoid duplicate videoId
        de.downloaded = DownloadedState.DOWNLOADING;
        db.addVideoData(de, null);

        db.isAnyVideoDownloading(new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertTrue("Result for Any Video Downloading  is:" +result.toString(), result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });

        lock();
    }
    public void testgetAllDownloadingVideosDmidList() throws Exception {
        db.clearDataByUser(username);
        db.getAllDownloadingVideosDmidList(new DataCallback<List<Long>>() {

            @Override
            public void onResult(List<Long> result) {
                assertNotNull(result);
                assertTrue(result.size() == 0);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.downloaded=DownloadedState.DOWNLOADING;
        db.addVideoData(de, null);
        db.getAllDownloadingVideosDmidList(new DataCallback<List<Long>>() {

            @Override
            public void onResult(List<Long> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testupdateAllVideosAsDeactivated() throws Exception {
        db.clearDataByUser(username);
        db.updateAllVideosAsDeactivated(new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 0);
                // assertFalse("something is downloading", result);
                print( "Result for update All Videos As Deactivated is"
                        + result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });

        lock();
        DownloadEntry de=getDummyVideoModel();
        db.addVideoData(de, null);
        db.updateAllVideosAsDeactivated(new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                // assertFalse("something is downloading", result);
                print( "Result for update All Videos As Deactivated is"
                        + result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });

        lock();
    }

    public void testupdateVideosActivatedForCourse() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        db.updateVideosActivatedForCourse(enrollmentId,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 0);
                // assertFalse("something is downloading", result);
                print("Result for update Videos Activated For Course is:"+ result.intValue());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.eid="enrollmentId";
        db.addVideoData(de, null);
        db.updateVideosActivatedForCourse(enrollmentId,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                // assertFalse("something is downloading", result);
                print("Result for update Videos Activated For Course is:"+ result.intValue());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();


    }

    public void testgetAllDeactivatedVideos() throws Exception {
        db.clearDataByUser(username);
        db.getAllDeactivatedVideos(new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                //assertNotNull(result);
                assertTrue(result.isEmpty());
                print( "Result for getAllDeactivatedVideos for size 0 is" + result);
                unlock();
            }
            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.isCourseActive = 0; // inactive video
        db.addVideoData(de, null);

        db.getAllDeactivatedVideos(new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                assertNotNull(result);
                assertTrue("result size = " + result.size(), result.size() == 1);
                print( "Result for getAllDeactivatedVideos for size 1 is" + result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();

    }

    public void testupdateVideoAsOnlineByVideoId() throws Exception {
        db.clearDataByUser(username);
        String videoId="videoId";
        //String videoId = "videoId-" + System.currentTimeMillis();
        db.updateVideoAsOnlineByVideoId(videoId, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertTrue(result == 0);
                print( "Result for updateVideoAsOnlineByVideoId for 0 is" + result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de1=getDummyVideoModel();
        de1.videoId = "videoId";
        de1.downloaded=DownloadedState.ONLINE;
        db.addVideoData(de1, null);
        //String videoId1="" + System.currentTimeMillis();
        db.updateVideoAsOnlineByVideoId(videoId,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print("Result for update Video As Online By VideoId "+ result.intValue());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetVideoCountBydmId() throws Exception {
        db.clearDataByUser(username);
        long dmId = 1;
        db.getVideoCountBydmId(dmId, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 0);
                // assertFalse("something is downloading", result);
                print( "Video Count By dmId is:" + result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.dmId=1;
        db.addVideoData(de, null);
        db.getVideoCountBydmId(dmId, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                // assertFalse("something is downloading", result);
                print( "Video Count By dmId for result 1 is:" + result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();

    }

    public void testisVideoDownloadedInChapter() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        final String chapter = "chapter";
        db.isVideoDownloadedInChapter(enrollmentId, chapter,
                new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                // assertTrue(result);
                assertFalse("Video should not be downloaded in chapter "+ chapter, result);
                print("Result for VideoDownloaded In Chapter is:"+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });
        lock();

        DownloadEntry de = getDummyVideoModel();
        de.chapter="chapter";
        de.eid="enrollmentId";
        de.downloaded=DownloadedState.DOWNLOADED;
        db.addVideoData(de, null);
        db.isVideoDownloadedInChapter(enrollmentId, chapter, new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertTrue(result);
                //assertFalse("Video should not be downloaded in chapter "+ chapter, result);
                print("Result for VideoDownloaded In Chapter is:"+ result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetVideosCountByChapter() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        String chapter = "chapter";
        db.getVideosCountByChapter(enrollmentId, chapter,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertTrue(result == 0);
                print("Videos Count By Chapter is: "+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });

        lock();
        DownloadEntry de=getDummyVideoModel();
        de.chapter="chapter";
        de.eid="enrollmentId";
        db.addVideoData(de, null);
        db.getVideosCountByChapter(enrollmentId, chapter,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertTrue(result == 1);
                print("Videos Count By Chapter is: "+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());

            }
        });

        lock();

    }

    public void testisVideoDownloadingInChapter() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        final String chapter = "chapter";
        db.isVideoDownloadingInChapter(enrollmentId, chapter,
                new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                // assertTrue(result);
                assertFalse("Video should not be present for chapter"+ chapter, result);
                print("Boolean result for Video Downloading InChapter is:"+ result.booleanValue());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.chapter="chapter";
        de.eid="enrollmentId";
        db.addVideoData(de, null);
        db.isVideoDownloadingInChapter(enrollmentId, chapter,
                new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertTrue(result);
                //assertFalse("Video should not be present for chapter"+ chapter, result);
                print("Boolean result for Video Downloading InChapter is:"+ result.booleanValue());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {
                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testgetDownloadingVideoDmIdsForChapter() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        String chapter = "chapter";
        db.getDownloadingVideoDmIdsForChapter(enrollmentId, chapter,
                new DataCallback<List<Long>>() {

            @Override
            public void onResult(List<Long> result) {
                assertNotNull(result);
                //assertTrue(result.length >= 0);
                assertTrue(result.size() == 0);
                // assertFalse("something is downloading", result);
                print( "get All Downloading Videos DmidList :"
                        + result.size());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.chapter="chapter";

        de.eid="enrollmentId";
        db.addVideoData(de, null);
        db.getDownloadingVideoDmIdsForChapter(enrollmentId, chapter,
                new DataCallback<List<Long>>() {

            @Override
            public void onResult(List<Long> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                // assertFalse("something is downloading", result);
                print( "get All Downloading Videos DmidList :"
                        + result.size());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();


    }

    public void testisVideoDownloadingInSection() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        String chapter = "chapter";
        final String section = "section";
        db.isVideoDownloadingInSection(enrollmentId, chapter, section,
                new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertFalse("Video should not be downloading in section "+ section, result);
                print("got result for Video Downloading In assertfalse "+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.chapter="chapter";
        de.section="section";
        de.eid="enrollmentId";
        db.addVideoData(de, null);
        db.isVideoDownloadingInSection(enrollmentId, chapter, section,
                new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertTrue(result);
                //assertFalse("Video should not be downloading in section "+ section, result);
                print("got result for Video Downloading In assertTrue "+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetDownloadingVideoDmIdsForSection() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        String chapter = "chapter";
        String section = "section";
        db.getDownloadingVideoDmIdsForSection(enrollmentId, chapter, section,
                new DataCallback<List<Long>>() {
            @Override
            public void onResult(List<Long> result) {
                assertNotNull(result);
                //assertTrue(result.length >= 0);
                assertTrue(result.size() == 0);
                print("get DownloadingVideo DmIds For Section :"+ result.size());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.chapter="chapter";
        de.section="section";
        de.eid="enrollmentId";
        db.addVideoData(de, null);
        db.getDownloadingVideoDmIdsForSection(enrollmentId, chapter, section,
                new DataCallback<List<Long>>() {

            @Override
            public void onResult(List<Long> result) {
                assertNotNull(result);
                //assertTrue(result.length >= 0);
                assertTrue(result.size() == 1);
                // assertFalse("something is downloading", result);
                print("get DownloadingVideo DmIds For Section :"+ result.size());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetVideosCountBySection() throws Exception {
        db.clearDataByUser(username);
        String enrollmentId = "enrollmentId";
        String chapter = "chapter";
        String section = "section";
        db.getVideosCountBySection(enrollmentId, chapter, section,
                new DataCallback<Integer>() {
            @Override
            public void onResult(Integer result) {
                assertTrue(result == 0);
                print( "get the VideosCountBySection is: "+ result);
                unlock();
            }
            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.chapter="fake_chapter";
        de.section="fake_section";
        de.eid="fake_eid";
        db.addVideoData(de, null);

        String enrollmentid = "fake_eid";
        String Chapter = "fake_chapter";
        String Section = "fake_section";

        db.getVideosCountBySection(enrollmentid, Chapter, Section,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print( "get the VideosCountBySection is: "+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();

    }

    public void testupdateVideoWatchedState() throws Exception {
        db.clearDataByUser(username);
        String videoId = "videoId";
        WatchedState state = WatchedState.PARTIALLY_WATCHED;
        db.updateVideoWatchedState(videoId, state, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 0);
                print( "updated VideoWatchedState for 0 is" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        String videoid = de.videoId = "videoId-" + System.currentTimeMillis();
        WatchedState State = WatchedState.WATCHED;
        db.addVideoData(de, null);
        db.updateVideoWatchedState(videoid, State, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print( "updated VideoWatchedState for 1 is :" + result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testupdateVideoLastPlayedOffset() throws Exception {
        db.clearDataByUser(username);
        String videoId = "videoId";
        int offset = 1;
        db.updateVideoLastPlayedOffset(videoId, offset,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertTrue(result == 0);
                print("Result for updated the VideoLastPlayedOffset "+ result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.id=1;
        de.videoId=videoId;
        int Offset=2;
        db.addVideoData(de, null);
        db.updateVideoLastPlayedOffset(videoId, Offset,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print("Result for updated the VideoLastPlayedOffset "+ result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testaddVideoData() throws Exception {
        db.clearDataByUser(username);
        DownloadEntry de1 = getDummyVideoModel();
        db.addVideoData(de1, new DataCallback<Long>() {

            @Override
            public void onResult(Long result) {
                //assertNotNull(result);
                //assertTrue(result >= 0);
                assertTrue(result > 0);
                print( "addVideoData" + result);
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
    }

    public void testGetVideoEntryByVideoId() throws Exception {
        db.clearDataByUser(username);
        String videoId = "videoId";
        //  String videoId = "videoId-" + System.currentTimeMillis();

        db.getVideoEntryByVideoId(videoId, new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertTrue(result == null);
                print("Result for Video Entry By VideoId= "+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        //      de.videoId = "videoId-" + System.currentTimeMillis();
        de.videoId="videoId";
        db.addVideoData(de, null);
        db.getVideoEntryByVideoId(videoId, new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertTrue(result != null);
                print("Result for Video Entry By VideoId in AssertTrue= "+ result.getVideoId());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testisVideoFilePresentByUrl() throws Exception {
        db.clearDataByUser(username);
        final String url = "fakeURL";
        db.isVideoFilePresentByUrl(url, new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertFalse("Video should not be present for " + url, result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.url="http://fake/url";
        db.addVideoData(de, null);
        db.isVideoFilePresentByUrl(url, new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertTrue(result!=null);
                //assertFalse("Video should not be present for " + url, result);
                print("Result for is VideoFilePresentByUrl in AssertTrue= "+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
    }

    public void testupdateDownloadingVideoInfoByVideoId() throws Exception {
        db.clearDataByUser(username);
        DownloadEntry model = getDummyVideoModel();
        db.updateDownloadingVideoInfoByVideoId(model,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 0);
                print("Result for update Downloading Video Info By VideoId for 0= "+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.downloaded=DownloadedState.DOWNLOADING;
        de.id=1;
        db.addVideoData(de, null);
        db.updateDownloadingVideoInfoByVideoId(de,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print("Result for update Downloading Video Info By VideoId for 1= "+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testupdateAsDownloadingByVideoId() throws Exception {
        db.clearDataByUser(username);
        DownloadEntry model = getDummyVideoModel();
        db.updateAsDownloadingByVideoId(model, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                //assertTrue(result >= 0);
                assertTrue(result == 0);
                print( "Result for update As Downloading ByVideoId is for size 0= "+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.videoId="videoId";
        de.downloaded=DownloadedState.DOWNLOADING;
        db.addVideoData(de, null);
        db.updateAsDownloadingByVideoId(de, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                //assertTrue(result >= 0);
                assertTrue(result == 1);
                print( "Result for update As Downloading ByVideoId is= "+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testgetListOfOngoingDownloads() throws Exception {
        db.clearDataByUser(username);
        db.getListOfOngoingDownloads(new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                assertNotNull(result);
                assertTrue(result.size() == 0);
                for (IVideoModel model : result) {
                    print( model.getChapterName());
                    print( "ID : " + model.getVideoId());
                    print( "result for get ListOfOngoingDownloads is:"
                            + result.size());
                }

                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        db.addVideoData(de, null);
        db.getListOfOngoingDownloads(new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                for (IVideoModel model : result) {
                    print( model.getChapterName());
                    print( "ID : " + model.getVideoId());
                    print( "result for get ListOfOngoingDownloads is:"
                            + result.size());
                }

                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testgetVideosDownloadedCount() throws Exception {
        db.clearDataByUser(username);
        db.getVideosDownloadedCount(new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                //assertTrue(result >= 0);
                assertTrue(result == 0);
                print( "Result for get Videos Downloaded Count is= "+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.downloaded=DownloadedState.DOWNLOADED;
        db.addVideoData(de, null);
        db.getVideosDownloadedCount(new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                //assertTrue(result >= 0);
                assertTrue(result == 1);
                print( "Result for get Videos Downloaded Count is= "+ result);
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testgetDownloadedVideoCountByCourse() throws Exception {
        db.clearDataByUser(username);
        String courseId = "courseId";
        db.getDownloadedVideoCountByCourse(courseId,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                //assertTrue(result >= 0);
                assertTrue(result==0);
                print("Result for get Downloaded Video Count ByCourse is:"+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.eid=courseId;
        de.downloaded=DownloadedState.DOWNLOADED;

        db.addVideoData(de, null);
        db.getDownloadedVideoCountByCourse(courseId,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print("Result for get Downloaded Video Count ByCourse is:"+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetDownloadedVideosSizeByCourse() throws Exception {
        db.clearDataByUser(username);
        String courseId = "courseId";
        db.getDownloadedVideosSizeByCourse(courseId, new DataCallback<Long>() {

            @Override
            public void onResult(Long result) {
                print( "Result for get Downloaded Videos SizeByCourse for 0 is:"+ result);
                assertNotNull(result);
                //assertTrue(result >= 0);
                assertTrue(result==0);
                // assertFalse("something is downloading", result);
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();

        DownloadEntry de=getDummyVideoModel();
        de.eid = courseId;
        de.downloaded=DownloadedState.DOWNLOADED;
        de.size = 20000;
        db.addVideoData(de, null);

        de=getDummyVideoModel();
        de.eid = courseId;
        de.downloaded=DownloadedState.DOWNLOADED;
        de.size = 2200;
        db.addVideoData(de, null);

        db.getDownloadedVideosSizeByCourse(de.eid, new DataCallback<Long>() {

            @Override
            public void onResult(Long result) {
                assertNotNull(result);
                assertTrue("found result=" + result, result == 22200);
                // assertFalse("something is downloading", result);
                print( "result for get Downloaded Videos SizeByCourse for more than 0:"+ result);
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
    }
    public void testgetIVideoModelByVideoUrl() throws Exception {
        db.clearDataByUser(username);
        final String videoUrl ="url";
        db.getIVideoModelByVideoUrl(videoUrl, new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertNull("result should be null", result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {
                logger.error(ex);
                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.url="http://fake/url";
        db.addVideoData(de, null);
        db.getIVideoModelByVideoUrl(de.url, new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertNotNull(result);
                print( "VideoModel  present for url= "+ result.getVideoUrl());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testisDmIdExists() throws Exception {
        db.clearDataByUser(username);
        final long dmId = 1;
        db.isDmIdExists(dmId, new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertFalse("DmId should not be present for " + dmId, result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.dmId = 1;
        db.addVideoData(de, null);
        db.isDmIdExists(dmId, new DataCallback<Boolean>() {

            @Override
            public void onResult(Boolean result) {
                assertNotNull(result);
                assertTrue("DmId  should be present for " + dmId, result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();

    }

    public void testupdateDownloadCompleteInfoByDmId() throws Exception {
        db.clearDataByUser(username);
        long dmId = 1;
        DownloadEntry de = getDummyVideoModel();
        db.updateDownloadCompleteInfoByDmId(dmId, de,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 0);
                print("Result for update DownloadComplete InfoByDmId in assertfail:"+ result.toString());
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de1=getDummyVideoModel();
        de1.dmId = 1;
        db.addVideoData(de1, null);
        db.updateDownloadCompleteInfoByDmId(dmId, de1,
                new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print("Result for update DownloadComplete InfoByDmId is in AssertTrue:"+ result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetAllVideos() throws Exception {
        db.clearDataByUser(username);
        db.getAllVideos(username, new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                assertNotNull(result);
                assertTrue("there should not be any video present in cleared database", result.size()==0);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        db.addVideoData(de, null);
        db.getAllVideos(username, new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                assertNotNull(result);
                assertTrue(result.size() == 1);
                // assertFalse("something is downloading", result);
                print( "result for getAllVideos :" + result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }

    public void testgetDownloadEntryByDmId() throws Exception {
        db.clearDataByUser(username);
        long dmId=1;
        db.getDownloadEntryByDmId(dmId, new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertNull("result should be null", result);
                unlock();

            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.dmId=1;
        db.addVideoData(de, null);
        db.getDownloadEntryByDmId(de.dmId, new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertTrue(result!=null);
                print( "result for getDownloadEntryByDmId for not null is:" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }
    public void testgetVideoCountByVideoUrl() throws Exception {
        db.clearDataByUser(username);
        String videoUrl="url";
        db.getVideoCountByVideoUrl(videoUrl, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                //assertNull("result should be null", result);
                assertTrue(result==0);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();

        DownloadEntry de=getDummyVideoModel();
        de.url="http://fake/url";
        db.addVideoData(de, null);
        db.getVideoCountByVideoUrl(de.url, new DataCallback<Integer>() {

            @Override
            public void onResult(Integer result) {
                assertNotNull(result);
                assertTrue(result == 1);
                print("Result for testgetVideoCountByVideoUrl is in 1:"+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }
    public void testgetWatchedStateForVideoId() throws Exception {
        db.clearDataByUser(username);
        String videoId="videoId";
        db.getWatchedStateForVideoId(videoId, new DataCallback<DownloadEntry.WatchedState>() {

            @Override
            public void onResult(WatchedState result) {
                assertNotNull(result);
                assertTrue(result == WatchedState.UNWATCHED);
                print( "result for getWatchedStateForVideoId :" + result.toString());
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.watched=WatchedState.WATCHED;
        de.videoId="videoid";
        db.addVideoData(de, null);
        db.getWatchedStateForVideoId(de.videoId, new DataCallback<DownloadEntry.WatchedState>() {

            @Override
            public void onResult(WatchedState result) {
                assertNotNull(result);
                assertTrue(result == WatchedState.WATCHED);
                print("Result for getWatchedStateForVideoId is in 1:"+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
    }
    public void testgetVideoByVideoUrl() throws Exception {
        db.clearDataByUser(username);
        String videoUrl="url";
        db.getVideoByVideoUrl(videoUrl,new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertNull("result should be null", result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.url="http://fake/url";
        db.addVideoData(de, null);
        db.getVideoByVideoUrl(de.url,new DataCallback<IVideoModel>() {

            @Override
            public void onResult(IVideoModel result) {
                assertNotNull(result);
                //assertTrue(result == 1);
                print("Result for getVideoByVideoUrl for not null:"+ result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());
            }
        });

        lock();
    }
    public void testgetDownloadedVideoListForCourse() throws Exception {
        db.clearDataByUser(username);
        String courseId="courseId";
        db.getDownloadedVideoListForCourse(courseId, new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                assertNotNull(result);
                assertTrue(result.isEmpty());
                print( "Result for getDownloadedVideoListForCourse for size 0 is" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());              }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.eid ="courseid"; // inactive video
        de.downloaded = DownloadedState.DOWNLOADED;
        db.addVideoData(de, null);

        db.getDownloadedVideoListForCourse(de.eid, new DataCallback<List<IVideoModel>>() {

            @Override
            public void onResult(List<IVideoModel> result) {
                assertNotNull(result);
                assertTrue("result size = " + result.size(), result.size() == 1);
                print( "Result for getAllDeactivatedVideos for size 1 is" + result.toString());

                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });

        lock();

    }
    public void testgetDownloadedStateForVideoId() throws Exception {
        db.clearDataByUser(username);
        String videoId="videoId";

        db.getDownloadedStateForVideoId(videoId, new DataCallback<DownloadEntry.DownloadedState>() {

            @Override
            public void onResult(DownloadedState result) {

                assertTrue(result==DownloadedState.ONLINE);
                print( "Result for getDownloadedStateForVideoId for not downloaded" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
        DownloadEntry de=getDummyVideoModel();
        de.videoId = "videoId-" + System.currentTimeMillis();
        de.downloaded = DownloadedState.DOWNLOADED;
        db.addVideoData(de, null);
        db.getDownloadedStateForVideoId(de.videoId, new DataCallback<DownloadEntry.DownloadedState>() {

            @Override
            public void onResult(DownloadedState result) {
                assertNotNull(result);
                assertTrue(result == DownloadedState.DOWNLOADED);
                print( "Result for getDownloadedStateForVideoId for downloaded is" + result);
                unlock();
            }

            @Override
            public void onFail(Exception ex) {

                fail(ex.getMessage());

            }
        });
        lock();
    }

    private void lock() throws InterruptedException {
        synchronized (lock) {
            lock.wait(60 * 1000);
        }
    }

    private void unlock() {
        synchronized (lock) {
            lock.notify();
        }
    }

    private DownloadEntry getDummyVideoModel() {
        DownloadEntry de = new DownloadEntry();
        de.username = username;
        de.title = "title";
        de.videoId = "videoId-" + System.currentTimeMillis();
        de.size = 1024;
        de.duration = 3600;
        de.filepath = "/fakepath";
        de.url = "http://fake/url";
        de.eid = "fake_eid";
        de.chapter = "fake_chapter";
        de.section = "fake_section";
        de.lastPlayedOffset = 0;
        de.lmsUrl = "http://fake/lms/url";
        de.isCourseActive = 1;
        de.downloaded = DownloadedState.DOWNLOADING;

        return de;
    }
}

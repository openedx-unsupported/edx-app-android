package org.edx.mobile.test.module;

import android.app.Application;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.download.DownloadFactory;
import org.edx.mobile.module.download.IDownloadManager;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.util.FileUtil;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DownloadTests extends BaseTestCase {

    private IDownloadManager dm;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dm = DownloadFactory.getInstance(RuntimeEnvironment.application);
    }

    @Test
    public void testAddDownload() throws Exception {
        File dir = null;
        try {
            final Application application = RuntimeEnvironment.application;
            final IEdxEnvironment environment = MainApplication.getEnvironment(application);
            dir = FileUtil.getDownloadDirectory(application, environment);
        } catch (Exception ex) {
            // it happens in CI environment and we should skip the test.
            print("dir is null, it happens in CI environment and we should skip the test.");
        }
        if ( dir == null )
            return ;

        String url = "https://s3.amazonaws.com/edx-course-videos/edx-edx101/EDXSPCPJSP13-H010000_100.mp4";

        // test add new download
        long dmid = dm.addDownload(dir, url, true, "Video 1 title");
        long dmid2 = dm.addDownload(dir, url, false, "Video 2 title");
        assertTrue("invalid dmid=" + dmid, dmid > 0);
        assertTrue("invalid dmid2=" + dmid2, dmid2 > 0);
        print( "new download dmid: " + dmid);
        print( "new download dmid2: " + dmid2);

        // wait for downloads to begin
        Thread.sleep(20 * 1000);

        // test get download info
        NativeDownloadModel model = dm.getDownload(dmid);
        assertNotNull(model);
        print( "downloading [dmid=" + dmid + "] to: " + model.filepath);

        int progress = dm.getProgressForDownload(dmid);
        assertTrue(progress >= 0 && progress <= 100);
        print( "progress=" + progress);

        int averageProgress = dm.getAverageProgressForDownloads(new long[] {dmid, dmid2});
        assertTrue(averageProgress >= 0 && averageProgress <= 100);
        print( "averageProgress=" + averageProgress);

        // test remove downloads
        boolean removed = dm.removeDownloads(dmid) > 0;
        assertTrue("failed to remove dmid: " + dmid, removed);
        removed = dm.removeDownloads(dmid2) > 0;
        assertTrue("failed to remove dmid: " + dmid2, removed);
        model = dm.getDownload(dmid);
        assertNull("download not removed, dmid: " +dmid, model);
        print( "removed the downloads");
    }

}

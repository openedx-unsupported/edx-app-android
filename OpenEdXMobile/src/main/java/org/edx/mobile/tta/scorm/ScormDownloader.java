package org.edx.mobile.tta.scorm;

import android.content.Context;

import com.google.inject.Inject;

import org.edx.mobile.course.CourseAPI;
import org.edx.mobile.logger.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import roboguice.RoboGuice;

public abstract class ScormDownloader implements Runnable {

    private String srtUrl;
    private String strFile;

    private File f;

    @Inject
    CourseAPI localApi;
    private final Logger logger = new Logger(ScormDownloader.class.getName());

    public ScormDownloader(Context context, String url, String file) {
        this.srtUrl = url;
        this.strFile = file;
        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public void run() {
        try {
            String response = localApi.downloadScorm(srtUrl,strFile);
            if(strFile.equals(response)){

                //unpackZip(response)
                //to handel both PDF and scrom xblock
                if(response.indexOf(".pdf") != -1 ||  unpackZip(response)){
                    onDownloadComplete(response);
                }else {
                    handle(null);
                }
            }else {
                handle(null);
            }
        } catch (Exception localException) {
            handle(localException);
            logger.error(localException);
        }
    }

    private boolean unpackZip(String file)
    {
        InputStream is;
        ZipInputStream zis;

        File arch = new File(file);
        File withExt = new File(arch.getAbsolutePath()+".zip");
        try
        {

            arch.renameTo(withExt);

            String filename;
            String folder = arch.getParent();
            String name = arch.getName();

            is = new FileInputStream(withExt.getAbsoluteFile());
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {

                filename = ze.getName();


                if (ze.isDirectory()) {
                    File fmd = new File(folder+"/" +name+"/"+ filename);
                    ScormDownloader.this.f = fmd;

                    fmd.mkdirs();
                    continue;
                }


                File tmp = new File(folder+"/" +name+"/"+ filename);

                File foldertmp = tmp.getParentFile();
                if(!foldertmp.exists()){
                    foldertmp.mkdirs();
                }

                FileOutputStream fout = new FileOutputStream(folder+"/" +name+"/"+ filename);


                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();

            withExt.delete();
        }
        catch(IOException e)
        {
            arch.delete();
            withExt.delete();
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public abstract void handle(Exception ex);

    public abstract void onDownloadComplete(String response);
}

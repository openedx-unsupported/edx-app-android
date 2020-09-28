package org.humana.mobile.scrom_migration;

import android.content.Context;
import android.os.Environment;

import org.humana.mobile.logger.Logger;
import org.humana.mobile.model.course.CourseComponent;

import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.tta.scorm.ScormBlockModel;
import org.humana.mobile.util.Sha1Util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static org.humana.mobile.util.BrowserUtil.environment;
import static org.humana.mobile.util.BrowserUtil.loginPrefs;

/**
 * Created by Arjun Chauhan on 4/21/2017.
 */

public class ScormMigrationScript
{
    private File oldScormFolderPath;
    private File newuserSpecificFolderPath;
    private String hash="";
    private Context context;
    private boolean isOldFolderExist=false;
    private final Logger logger = new Logger(getClass().getName());

    public ScormMigrationScript(Context context)
    {
        try {
            this.context = context;
            File android = new File(Environment.getExternalStorageDirectory(), "Android");
            File downloadsDir = new File(android, "data");
            File packDir = new File(downloadsDir, context.getPackageName());
            File scormFolder = new File(packDir, "scormFolder");

            if (scormFolder.exists()) {
                oldScormFolderPath=scormFolder;
                isOldFolderExist=true;

                //add user specific folder
                newuserSpecificFolderPath = new File(scormFolder, loginPrefs.getUsername());

                if(!newuserSpecificFolderPath.exists()) {
                    newuserSpecificFolderPath.mkdirs();
                }
            }

        } catch (Exception e)
        {
            logger.error(e);
        }
    }

  /*  public void doMigrate(ScormBlockModel download)
    {
        if(has(download.getId()))
        {
            boolean ismoved=false;
            //get encripted folder path first
          File Sha1EncriptedFolderPath= getSHAoneEncriptedFolderPath(download.getId());
            try {
                ismoved=true;
                //Create same folder inside userspecific folder with SHA1 code
              File hashFolder = new File(newuserSpecificFolderPath, hash);

                //move data to new folder
                moveDirectory(Sha1EncriptedFolderPath,hashFolder);
            } catch (IOException e) {
                e.printStackTrace();
                ismoved=false;
            }

            //if file moved successfully do entry in DB too..
            if(ismoved)
            {
               doDownloadEntery_db(download);
            }
        }
    }

    public boolean has(String url) {

        try {
            hash = Sha1Util.SHA1(url);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }
        File file = new File(oldScormFolderPath, hash);
        return file.exists() && file.isDirectory();
    }*/

    public boolean isOldFolderexist()
    {
        return isOldFolderExist;
    }

    //it will provide you with folder containing scorm data which is associated with that particular scorm id
/*    private File getSHAoneEncriptedFolderPath(String url) {
        String hash = "";
        try {
            hash = Sha1Util.SHA1(url);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File file = new File(oldScormFolderPath, hash);
        return file;
    }*/

    private void doDownloadEntery_db(CourseComponent mUnit)
    {
        String downladedFilePath="Scrom";
        //set download url to null because are downloading file from scrom manager not from edx vedio download manager.
        DownloadEntry model=new DownloadEntry();
        model.setDownloadEntryForScrom(loginPrefs.getUsername(),mUnit.getDisplayName(),downladedFilePath,mUnit.getId(),"",mUnit.getRoot().getCourseId()
                ,mUnit.getParent().getDisplayName()
                ,mUnit.getParent().getParent().getDisplayName(), (long) 0,"");

        environment.getStorage().addDownload(model);
    }
}

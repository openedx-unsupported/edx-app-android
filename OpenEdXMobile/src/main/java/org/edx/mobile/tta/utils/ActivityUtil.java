package org.edx.mobile.tta.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import org.edx.mobile.tta.ui.logistration.SigninRegisterActivity;

import java.io.File;


/**
 * Created by Arjun on 2018/3/9.
 */

public class ActivityUtil {
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static void replaceFragmentInActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId, String tag,
                                                 boolean addToBackStack, String stackName) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(frameId, fragment, tag);
        if (addToBackStack){
            transaction.addToBackStack(stackName);
        }
        transaction.commit();
    }

    public static void gotoPage(Context context, Class<?> activityClass) {
        context.startActivity(new Intent(context, activityClass));
    }

    public static void gotoPage(Context context, Class<?> activityClass, Bundle bundle) {
        Intent intent = new Intent(context, activityClass);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    public static void gotoPage(Context context, Class<?> activityClass, int flags) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(flags);
        context.startActivity(intent);
    }

    public static void gotoLogin(Context context) {
        Intent intent = new Intent(context, SigninRegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void viewPDF(Context ctx, File filePath)
    {
        /** PDF reader code */
        // File file = new File(Environment.getExternalStorageDirectory() + "/" + "abc.pdf");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(filePath),"application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            ctx.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Toast.makeText(ctx, "NO Pdf Viewer", Toast.LENGTH_SHORT).show();
        }
    }

    public static void playVideo(String filePath, Context context){

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(filePath), "video/*");
        context.startActivity(Intent.createChooser(intent, "Complete action using"));

    }
}

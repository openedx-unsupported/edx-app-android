package org.humana.mobile.tta.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;

public class AppUtil {
    private static final String TAG = "Permissions";

    public static boolean appInstalledOrNot(String uri, PackageManager pm) {
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

// Provide device unique key
    @SuppressLint("HardwareIds")
    public static String generateDeviceIdentifier(Context context) {

        String pseudoId = "35" +
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10;

        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String btId = "";

        if (bluetoothAdapter != null) {
            btId = bluetoothAdapter.getAddress();
        }

        String longId = pseudoId + androidId + btId;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(longId.getBytes(), 0, longId.length());

            // get md5 bytes
            byte md5Bytes[] = messageDigest.digest();

            // creating a hex string
            String identifier = "";

            for (byte md5Byte : md5Bytes) {
                int b = (0xFF & md5Byte);

                // if it is a single digit, make sure it have 0 in front (proper padding)
                if (b <= 0xF) {
                    identifier += "0";
                }

                // add number to string
                identifier += Integer.toHexString(b);
            }

            // hex string to uppercase
            identifier = identifier.toUpperCase();
            return identifier;
        } catch (Exception e) {
            Log.e("TAG", e.toString());
        }
        return "";
    }
    public static boolean isReadStoragePermissionGranted(AppCompatActivity context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public static boolean isWriteStoragePermissionGranted(AppCompatActivity context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }

    public static String encode(String url) {

        try {

            String encodeURL = URLEncoder.encode(url, "UTF-8");

            return encodeURL;

        } catch (UnsupportedEncodingException e) {

            return "Issue while encoding" + e.getMessage();

        }

    }
}

package org.humana.mobile.tta.interfaces;

public interface PermissionListener {
    void onPermissionGranted(String[] permissions, int requestCode);

    void onPermissionDenied(String[] permissions, int requestCode);
}
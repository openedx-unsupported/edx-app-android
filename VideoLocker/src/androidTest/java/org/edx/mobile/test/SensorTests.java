package org.edx.mobile.test;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import org.edx.mobile.util.OrientationDetector;

public class SensorTests extends BaseTestCase {
    
    private final Object lock = new Object();
    
    public void testRotation() throws Exception {
        print("landscape=" + isDeviceLandscape());
    }

    private boolean isDeviceLandscape() throws Exception {
        WindowManager wm = (WindowManager) getInstrumentation().getTargetContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point outSize = new Point();
        display.getSize(outSize);
        int r = display.getRotation();
        print( "rotation=" + r);
        
        if (r == Surface.ROTATION_0) {
            // portrait
            return false;
        }
        if (r == Surface.ROTATION_90) {
            // landscape
            return true;
        }
        if (r == Surface.ROTATION_180) {
            // portrait
            return false;
        }
        if (r == Surface.ROTATION_270) {
            // landscape
            return true;
        }
        
        return false;
    }

    public void testOrientation() throws Exception {
        OrientationDetector d = new OrientationDetector(getInstrumentation().getContext()) {
            @Override
            protected void onLandscapeLeft() {
                super.onLandscapeLeft();
                print("landscape left");
            }
            
            protected void onLandscapeRight() {
                super.onLandscapeRight();
                print("landscape right");
            }
            
            @Override
            protected void onPortrait() {
                super.onPortrait();
                print("landscape portrait");
            }
        };
        d.start();
        
        synchronized (lock) {
            lock.wait(10 * 1000);
        }
        
        d.stop();
    }
}

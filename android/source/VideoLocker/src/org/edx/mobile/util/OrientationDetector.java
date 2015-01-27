package org.edx.mobile.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.edx.mobile.logger.Logger;

public class OrientationDetector implements SensorEventListener {

    public static final int LYING = 0;
    public static final int LANDSCAPE_RIGHT = 1;
    public static final int PORTRAIT = 2;
    public static final int LANDSCAPE_LEFT = 3;
    public static final int UNKNOWN = -1;

    private int previousOrientation = UNKNOWN;
    private Context context;
    protected final Logger logger = new Logger(getClass().getName());

    public OrientationDetector(Context context) {
        this.context = context;
    }
    
    public void start() {
        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensorOrientation = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (mSensorOrientation != null) {
            mSensorManager.registerListener(this, mSensorOrientation,
                    SensorManager.SENSOR_DELAY_NORMAL);
            
            logger.debug("started sensor: " + mSensorOrientation.toString());
        } else {
            logger.warn("sensor NOT available");
        }
    }
    
    public void stop() {
        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.unregisterListener(this);
        
        logger.warn("stopped orientation sensor");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do
        logger.debug(String.format("accuracy changed to %d for %s", accuracy, sensor.getName()));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        if ((sensor.getType() == Sensor.TYPE_ORIENTATION)) {

            float[] eventValues = event.values;

            // current orientation of the phone
            float xAxis = eventValues[1];
            float yAxis = eventValues[2];
            
            if ((yAxis <= 25) && (yAxis >= -25) && (xAxis >= -160)) {

                if (previousOrientation != PORTRAIT) {
                    previousOrientation = PORTRAIT;

                    // CHANGED TO PORTRAIT
                    onPortrait();
                    onChanged();
                }

            } else if ((yAxis < -25) && (xAxis >= -20)) {

                if (previousOrientation != LANDSCAPE_RIGHT) {
                    previousOrientation = LANDSCAPE_RIGHT;

                    // CHANGED TO LANDSCAPE RIGHT
                    onLandscapeRight();
                    if (previousOrientation != UNKNOWN) {
                        onChanged();
                    }
                }

            } else if ((yAxis > 25) && (xAxis >= -20)) {

                if (previousOrientation != LANDSCAPE_LEFT) {
                    previousOrientation = LANDSCAPE_LEFT;

                    // CHANGED TO LANDSCAPE LEFT
                    onLandscapeLeft();
                    if (previousOrientation != UNKNOWN) {
                        onChanged();
                    }
                }
            } else {
                logger.debug("Orientation values unhandled: " + eventValues);
            }
            
            onUpdate();
        }
    }
    
    public boolean isLandscape() {
        if (previousOrientation == LANDSCAPE_LEFT) {
            return true;
        }
        if (previousOrientation == LANDSCAPE_RIGHT) {
            return true;
        }
        
        return false;
    }
    
    public boolean isPortrait() {
        return (previousOrientation == PORTRAIT);
    }
    
    protected void onPortrait() { logger.debug("now PORTRAIT"); }
    protected void onLandscapeRight() { logger.debug("now LANDSCAPE right"); }
    protected void onLandscapeLeft() { logger.debug("now LANDSCAPE left"); }
    protected void onUpdate() { /* nothing to do here */ }
    protected void onChanged() { /* nothing to do here */ logger.debug("orientation changed"); }
}

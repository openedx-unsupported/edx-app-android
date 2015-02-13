package org.edx.mobile.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class Preview extends SurfaceView implements Callback {

    public Preview(Context context) {
        super(context);
        getHolder().addCallback(this);
    }
    
    public Preview(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public Preview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // override to handle
    }
}

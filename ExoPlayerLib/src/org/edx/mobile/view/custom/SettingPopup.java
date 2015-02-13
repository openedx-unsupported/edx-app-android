package org.edx.mobile.view.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

import com.google.android.exoplayer.R;

public abstract class SettingPopup extends PopupWindow implements PopupWindow.OnDismissListener {

    /**
     * Height of the popup must match with the height of its layout (xml).
     */
    private static final float POPUP_HEIGHT = 81;
    private static final float POPUP_WIDTH = 150;

    public SettingPopup(Context context, Point p) {
        super(context);
        
        Resources r = context.getResources();
        float popupHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, POPUP_HEIGHT, r.getDisplayMetrics());

        float popupWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, POPUP_WIDTH , r.getDisplayMetrics());

        // Inflate the popup_layout.xml
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.panel_settings_popup, null);
        layout.findViewById(R.id.tv_closedcaption).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onCcClicked();
            }
        });
        layout.findViewById(R.id.tv_speedcontrol).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                onSpeedClicked();
            }

        });

        // setup popup properly
        setContentView(layout);
        setWidth((int)popupWidth);
        setHeight((int)popupHeight);
        setFocusable(true);
        
        // Clear the default translucent background
        setBackgroundDrawable(new BitmapDrawable());
        
        // Displaying the popup at the specified location, + offsets.
        showAtLocation(layout, Gravity.NO_GRAVITY, p.x-(int)popupWidth, p.y-(int)popupHeight);
        
        setOnDismissListener(this);
    }
    
    public abstract void onCcClicked();
    
    public abstract void onSpeedClicked();

    @Override
    public abstract void onDismiss();
}

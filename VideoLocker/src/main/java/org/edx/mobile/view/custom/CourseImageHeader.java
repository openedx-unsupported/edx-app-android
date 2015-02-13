package org.edx.mobile.view.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.*;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public class CourseImageHeader extends NetworkImageView {

    private Context context;

    public CourseImageHeader(Context context){
        super(context);
        this.context = context;
    }

    public CourseImageHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CourseImageHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm != null) {
            bm = blurBitmap(bm, 10f);
        }
        super.setImageBitmap(bm);

    }

    private Bitmap blurBitmap(Bitmap bitmap, float blurRadius) {

        Bitmap blurredBitmap =  Bitmap.createBitmap(bitmap);

        RenderScript rs = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SCRIPT);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        script.setInput(input);
        script.setRadius(blurRadius);
        script.forEach(output);

        output.copyTo(blurredBitmap);

        return blurredBitmap;

    }
}

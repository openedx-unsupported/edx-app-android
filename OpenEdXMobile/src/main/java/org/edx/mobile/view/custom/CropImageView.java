package org.edx.mobile.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.third_party.subscaleview.SubsamplingScaleImageView;

public class CropImageView extends org.edx.mobile.third_party.subscaleview.SubsamplingScaleImageView {

    private Paint borderPaint;
    private Paint backgroundPaint;

    final Path circleSelectionPath = new Path();
    final RectF mRectF = new RectF();

    private final float cropCirclePadding;

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        cropCirclePadding = getResources().getDimension(R.dimen.crop_circle_padding);
        setOrientation(ORIENTATION_USE_EXIF);
        setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        borderPaint = new Paint();
        borderPaint.setColor(getResources().getColor(R.color.crop_circle_border_color));
        borderPaint.setStrokeWidth(getResources().getDimension(R.dimen.crop_circle_border_width));
        borderPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(getResources().getColor(R.color.crop_circle_overlay_color));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int horizontalPadding = (int) cropCirclePadding;
        final int verticalPadding = (getHeight() - (getWidth() - horizontalPadding * 2)) / 2;
        setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        setPanLimit(PAN_LIMIT_INSIDE); // This will call `fitToBounds` with the new padding
    }

    public CropImageView(Context context) {
        this(context, null);
    }

    // Extend fitToBounds function to account for view padding
    @Override
    protected void fitToBounds(boolean center, ScaleAndTranslate sat) {
        if (panLimit == PAN_LIMIT_OUTSIDE && isReady()) {
            center = false;
        }

        PointF vTranslate = sat.vTranslate;
        float scale = limitedScale(sat.scale);
        float scaleWidth = scale * sWidth();
        float scaleHeight = scale * sHeight();

        if (panLimit == PAN_LIMIT_CENTER && isReady()) {
            vTranslate.x = Math.max(vTranslate.x, getWidth() / 2 - scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, getHeight() / 2 - scaleHeight);
        } else if (center) {
            vTranslate.x = Math.max(vTranslate.x, getWidth() - scaleWidth - getPaddingEnd());
            vTranslate.y = Math.max(vTranslate.y, getHeight() - scaleHeight - getPaddingBottom());
        } else {
            vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
            vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
        }

        float maxTx;
        float maxTy;
        if (panLimit == PAN_LIMIT_CENTER && isReady()) {
            maxTx = Math.max(0, getWidth() / 2);
            maxTy = Math.max(0, getHeight() / 2);
        } else if (center) {
            maxTx = Math.max(0, getPaddingStart());
            maxTy = Math.max(0, getPaddingTop());
        } else {
            maxTx = Math.max(0, getWidth());
            maxTy = Math.max(0, getHeight());
        }

        vTranslate.x = Math.min(vTranslate.x, maxTx);
        vTranslate.y = Math.min(vTranslate.y, maxTy);

        sat.scale = scale;
    }

    @NonNull
    public Rect getCropRect() {
        final PointF topLeft = this.viewToSourceCoord(getPaddingStart(), getPaddingTop());
        final PointF bottomRight = this.viewToSourceCoord(getWidth() - getPaddingEnd(), getHeight() - getPaddingBottom());
        return new Rect((int) topLeft.x, (int) topLeft.y, (int) bottomRight.x, (int) bottomRight.y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background with transparent circular hole
        final float radius = Math.min((float) canvas.getWidth(), canvas.getHeight()) / 2 - cropCirclePadding;
        mRectF.set((float) canvas.getWidth() / 2 - radius, (float) canvas.getHeight() / 2 - radius, (float) canvas.getWidth() / 2 + radius, (float) canvas.getHeight() / 2 + radius);
        circleSelectionPath.reset();
        circleSelectionPath.addOval(mRectF, Path.Direction.CW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutPath(circleSelectionPath);
        } else {
            canvas.clipPath(circleSelectionPath, Region.Op.XOR);
        }

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        // Canvas did not save and called restore due to which app crashes, so we have to save first then call restore
        canvas.save();
        canvas.restore();

        // Draw circle border
        canvas.drawCircle((float) canvas.getWidth() / 2, (float) canvas.getHeight() / 2, radius, borderPaint);
    }
}

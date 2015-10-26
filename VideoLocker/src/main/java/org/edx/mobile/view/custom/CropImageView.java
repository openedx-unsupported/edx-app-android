package org.edx.mobile.view.custom;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import org.edx.mobile.R;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class CropImageView extends ImageViewTouch {

    private float extraPadding;

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        extraPadding = context.getResources().getDimension(R.dimen.widget_margin);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context) {
        this(context, null);
    }

    @Override
    protected void updateRect(RectF bitmapRect, RectF scrollRect) {
        if (bitmapRect != null) {
            final int width = getWidth();
            final int height = getHeight();
            final float verticalPadding = getVerticalPadding();
            final float horizontalPadding = getHorizontalPadding();
            // TODO: account for padding in here and uncomment
            /*if (bitmapRect.top >= 0.0F && bitmapRect.bottom <= (float) height) {
                scrollRect.top = 0.0F;
            }

            if (bitmapRect.left >= 0.0F && bitmapRect.right <= (float) width) {
                scrollRect.left = 0.0F;
            }

            if (bitmapRect.top + scrollRect.top >= 0.0F && bitmapRect.bottom > (float) height) {
                scrollRect.top = (float) ((int) (0.0F - bitmapRect.top));
            }

            if (bitmapRect.bottom + scrollRect.top <= (float) (height - 0) && bitmapRect.top < 0.0F) {
                scrollRect.top = (float) ((int) ((float) (height - 0) - bitmapRect.bottom));
            }

            if (bitmapRect.left + scrollRect.left >= 0.0F) {
                scrollRect.left = (float) ((int) (0.0F - bitmapRect.left));
            }

            if (bitmapRect.right + scrollRect.left <= (float) (width - 0)) {
                scrollRect.left = (float) ((int) ((float) (width - 0) - bitmapRect.right));
            }*/
        }
    }

    protected RectF getCenter(Matrix supportMatrix, boolean horizontal, boolean vertical) {
        Drawable drawable = this.getDrawable();
        if (drawable == null) {
            return new RectF(0.0F, 0.0F, 0.0F, 0.0F);
        } else {
            this.mCenterRect.set(0.0F, 0.0F, 0.0F, 0.0F);
            RectF rect = this.getBitmapRect(supportMatrix);
            float height = rect.height();
            float width = rect.width();
            float deltaX = 0.0F;
            float deltaY = 0.0F;
            if (vertical) {
                int viewHeight = getHeight();
                final float padding = getVerticalPadding();
                if (height < (float) viewHeight) {
                    deltaY = ((float) viewHeight - height) / 2.0F - rect.top;
                } else if (rect.top > padding) {
                    deltaY = padding - rect.top;
                } else if (rect.bottom < (float) viewHeight - padding) {
                    deltaY = (float) viewHeight - padding - rect.bottom;
                }
            }

            if (horizontal) {
                int viewWidth = getWidth();
                final float padding = getHorizontalPadding();
                if (width < (float) viewWidth) {
                    deltaX = ((float) viewWidth - width) / 2.0F - rect.left;
                } else if (rect.left > padding) {
                    deltaX = padding - rect.left;
                } else if (rect.right < (float) viewWidth - padding) {
                    deltaX = (float) viewWidth - padding - rect.right;
                }
            }

            this.mCenterRect.set(deltaX, deltaY, 0.0F, 0.0F);
            return this.mCenterRect;
        }
    }

    private float getVerticalPadding() {
        if (getHeight() > getWidth()) {
            return (getHeight() - getWidth()) / 2 + extraPadding;
        } else {
            return extraPadding;
        }
    }

    private float getHorizontalPadding() {
        if (getWidth() > getHeight()) {
            return (getWidth() - getHeight()) / 2 + extraPadding;
        } else {
            return extraPadding;
        }
    }
}

/**
 *
 * Peter Organa
 * Dec 12, 2014
 *
 * This is a complete copy and minor modification of the Facebook ProfilePictureViewClass. I needed to modify this to allow for rounded avatar views.
 *
 *
 *
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edx.mobile.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.facebook.widget.ProfilePictureView;

public class RoundedProfilePictureView extends ProfilePictureView {

    private Path roundedPathMask;


    public RoundedProfilePictureView(Context context) {
        super(context);
        initialize(context);
    }

    public RoundedProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public RoundedProfilePictureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        for(int i = 0; i < getChildCount(); i++){
            if(getChildAt(i) instanceof ImageView){
                ((ImageView)getChildAt(i)).setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private float roundedCornerRadius;


    public float getRoundedCornerRadius() {
        return roundedCornerRadius;
    }

    public void setRoundedCornerRadius(float roundedCornerRadius) {
        this.roundedCornerRadius = roundedCornerRadius;
        roundedPathMask = calculateMaskPath();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        roundedPathMask = calculateMaskPath();
    }

    private Path calculateMaskPath(){
        Path roundedPath = new Path();
        int width = getWidth();
        int height = getHeight();

        roundedPath.moveTo(roundedCornerRadius, 0);
        roundedPath.lineTo(width - roundedCornerRadius, 0);

        roundedPath.cubicTo(width - roundedCornerRadius, 0, width, 0, width, roundedCornerRadius);
        roundedPath.lineTo(width, height - roundedCornerRadius);

        roundedPath.cubicTo(width, height - roundedCornerRadius, width, height, width - roundedCornerRadius, height);
        roundedPath.lineTo(roundedCornerRadius, height);

        roundedPath.cubicTo(roundedCornerRadius, height, 0, height, 0, height - roundedCornerRadius);
        roundedPath.lineTo(0, roundedCornerRadius);

        roundedPath.cubicTo(0, roundedCornerRadius, 0, 0, roundedCornerRadius, 0);
        roundedPath.close();
        return roundedPath;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(roundedPathMask != null)
            canvas.clipPath(roundedPathMask);
        super.onDraw(canvas);
    }

}
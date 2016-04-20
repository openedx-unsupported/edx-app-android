/*
 * This class is stripped down and slightly modified from the class of the
 * same name in the appcompat library, which is available from the AOSP.
 *
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.edx.mobile.util;

import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/*
 * Since this class is not included in the public API of the appcompat
 * library, it's copied into the project for usage in custom views etc.
 * Unneeded methods have been stripped out, and support annotations have
 * been added.
 */
public class DrawableUtils {

    private DrawableUtils() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static PorterDuff.Mode parseTintMode(@IntRange(from = 0, to = 15) int value,
                                                @NonNull PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3: return PorterDuff.Mode.SRC_OVER;
            case 5: return PorterDuff.Mode.SRC_IN;
            case 9: return PorterDuff.Mode.SRC_ATOP;
            case 14: return PorterDuff.Mode.MULTIPLY;
            case 15: return PorterDuff.Mode.SCREEN;
            case 16: return Build.VERSION.SDK_INT >= 11 ? PorterDuff.Mode.valueOf("ADD")
                    : defaultMode;
            default: return defaultMode;
        }
    }

}

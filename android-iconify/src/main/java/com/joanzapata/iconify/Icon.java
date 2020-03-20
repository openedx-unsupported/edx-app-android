package com.joanzapata.iconify;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

/**
 * Icon represents one icon in an icon font.
 */
public interface Icon {

    /** The key of icon, for example 'fa-ok' */
    @CheckResult
    @NonNull
    String key();

    /** The character matching the key in the font, for example '\u4354' */
    @CheckResult
    char character();

    /** Whether the icon can be mirrored in RTL mode */
    @CheckResult
    boolean supportsRtl();

}

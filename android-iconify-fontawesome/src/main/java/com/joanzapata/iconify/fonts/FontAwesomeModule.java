package com.joanzapata.iconify.fonts;

import androidx.annotation.NonNull;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

public class FontAwesomeModule implements IconFontDescriptor {

    @Override
    @NonNull
    public String ttfFileName() {
        return "iconify/android-iconify-fontawesome.ttf";
    }

    @Override
    @NonNull
    public Icon[] characters() {
        return FontAwesomeIcons.values();
    }
}

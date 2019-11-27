package com.joanzapata.iconify.internal;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconFontDescriptor;

import java.util.HashMap;
import java.util.Map;

public class IconFontDescriptorWrapper {

    @NonNull
    private final IconFontDescriptor iconFontDescriptor;

    @NonNull
    private final Map<String, Icon> iconsByKey;

    @Nullable
    private Typeface cachedTypeface;

    public IconFontDescriptorWrapper(@NonNull IconFontDescriptor iconFontDescriptor) {
        this.iconFontDescriptor = iconFontDescriptor;
        iconsByKey = new HashMap<String, Icon>();
        Icon[] characters = iconFontDescriptor.characters();
        for (int i = 0, charactersLength = characters.length; i < charactersLength; i++) {
            Icon icon = characters[i];
            iconsByKey.put(icon.key(), icon);
        }
    }

    @CheckResult
    @Nullable
    public Icon getIcon(@NonNull String key) {
        return iconsByKey.get(key);
    }

    @CheckResult
    @NonNull
    public IconFontDescriptor getIconFontDescriptor() {
        return iconFontDescriptor;
    }

    @CheckResult
    @NonNull
    public Typeface getTypeface(@NonNull Context context) {
        if (cachedTypeface != null) return cachedTypeface;
        synchronized (this) {
            if (cachedTypeface != null) return cachedTypeface;
            cachedTypeface = Typeface.createFromAsset(context.getAssets(), iconFontDescriptor.ttfFileName());
            return cachedTypeface;
        }
    }

    @CheckResult
    public boolean hasIcon(@NonNull Icon icon) {
        return iconsByKey.values().contains(icon);
    }
}

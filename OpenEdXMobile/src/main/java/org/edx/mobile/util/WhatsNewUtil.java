package org.edx.mobile.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.whatsnew.WhatsNewItemModel;
import org.edx.mobile.whatsnew.WhatsNewModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class WhatsNewUtil {
    private static final Logger logger = new Logger(WhatsNewUtil.class);

    /**
     * Filter whats new messages according to the provided build version.
     *
     * @param buildVersion       Provided build version.
     * @param whatsNewModelsList List which needs to be filtered.
     * @return Filtered list.
     */
    @Nullable
    public static List<WhatsNewItemModel> getWhatsNewItems(@NonNull String buildVersion,
                                                           @NonNull List<WhatsNewModel> whatsNewModelsList) {
        final Version version;
        Version whatsNewVersion;
        try {
            version = new Version(buildVersion);
        } catch (ParseException e) {
            logger.error(e, true);
            return null;
        }
        for (WhatsNewModel whatsNewModel : whatsNewModelsList) {
            try {
                whatsNewVersion = new Version(whatsNewModel.getVersion());
            } catch (ParseException e) {
                logger.error(e, true);
                continue;
            }
            if (version.hasSameMajorMinorVersion(whatsNewVersion)) {
                return filterAndroidItems(whatsNewModel.getWhatsNewItems());
            }
        }
        return null;
    }

    @Nullable
    public static List<WhatsNewItemModel> filterAndroidItems(@NonNull List<WhatsNewItemModel> whatsNewItems) {
        final List<WhatsNewItemModel> androidItems = new ArrayList<>();
        for (WhatsNewItemModel item : whatsNewItems) {
            if (item.isAndroidMessage()) {
                androidItems.add(item);
            }
        }
        return androidItems.size() > 0 ? androidItems : null;
    }
}

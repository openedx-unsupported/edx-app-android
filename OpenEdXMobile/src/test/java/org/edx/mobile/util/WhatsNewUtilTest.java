package org.edx.mobile.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.R;
import org.edx.mobile.test.BaseTest;
import org.edx.mobile.whatsnew.WhatsNewItemModel;
import org.edx.mobile.whatsnew.WhatsNewModel;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class WhatsNewUtilTest extends BaseTest {
    private final String IMAGE_NAME_REGEX = "screen_\\d+";

    @Test
    public void testWhatsNewJsonParsing() throws IOException, ParseException {
        final Context context = RuntimeEnvironment.application;
        final String whatsNewJson = FileUtil.loadTextFileFromResources(context, R.raw.whats_new);
        final Type type = new TypeToken<List<WhatsNewModel>>() {
        }.getType();
        final List<WhatsNewModel> whatsNewModels = new Gson().fromJson(whatsNewJson, type);

        assertThat(whatsNewModels).isNotEmpty();

        // Check validity of all what's new content
        List<WhatsNewItemModel> whatsNewItems;
        for (WhatsNewModel whatsNewModel : whatsNewModels) {
            // Validate version name
            assertThat(new Version(whatsNewModel.getVersion())).isNotNull();
            // Validate all whatsNewItems
            whatsNewItems = whatsNewModel.getWhatsNewItems();
            assertThat(whatsNewItems).isNotEmpty();
            validateWhatsNewItems(whatsNewItems);
            whatsNewItems = WhatsNewUtil.getWhatsNewItems(whatsNewModel.getVersion(), whatsNewModels);
            assertThat(whatsNewItems).isNotEmpty();
        }

        // Case when what's new content doesn't exist
        whatsNewItems = WhatsNewUtil.getWhatsNewItems("0.0.00", whatsNewModels);
        assertThat(whatsNewItems).isNull();
    }

    public void validateWhatsNewItems(List<WhatsNewItemModel> whatsNewItems) {
        for (WhatsNewItemModel item : whatsNewItems) {
            assertThat(item).isNotNull();
            assertThat(item.getTitle()).isNotEmpty();
            assertThat(item.getMessage()).isNotEmpty();
            assertThat(item.getImage()).isNotEmpty();

            if (item.isAndroidMessage()) {
                // Check image name is valid
                assertThat(item.getImage().matches(IMAGE_NAME_REGEX)).isTrue();
            }

            final List<String> platforms = item.getPlatforms();
            assertThat(platforms).isNotEmpty();

            // Check platform name is valid
            for (String platform : platforms) {
                assertThat(WhatsNewItemModel.Platform.isSupportedPlatform(platform)).isTrue();
            }
        }
    }

    @Test
    public void testFilterAndroidItems() {
        List<WhatsNewItemModel> whatsNewItems = new ArrayList<>();
        WhatsNewItemModel item;
        item = new WhatsNewItemModel();
        item.setPlatforms(Arrays.asList("android"));
        whatsNewItems.add(item);
        item = new WhatsNewItemModel();
        item.setPlatforms(Arrays.asList("ios"));
        whatsNewItems.add(item);
        item = new WhatsNewItemModel();
        item.setPlatforms(Arrays.asList("android", "ios"));
        whatsNewItems.add(item);
        assertThat(WhatsNewUtil.filterAndroidItems(whatsNewItems)).hasSize(2);
    }
}

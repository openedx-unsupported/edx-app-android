package org.edx.mobile.whatsnew;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

public class WhatsNewItemModel implements Parcelable {
    public enum Platform {
        ANDROID("android"),
        IOS("ios")
        ;

        private final String key;

        Platform(@NonNull String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public static boolean isSupportedPlatform(@NonNull String platform) {
            if (TextUtils.isEmpty(platform)) {
                return false;
            }
            for (Platform supportedPlatform : Platform.values()) {
                if (platform.trim().equalsIgnoreCase(supportedPlatform.key)) {
                    return true;
                }
            }
            return false;
        }
    }

    @NonNull
    private String title;
    @NonNull
    private String message;
    @NonNull
    private String image;
    @NonNull
    private List<String> platforms;

    public WhatsNewItemModel() {
    }

    protected WhatsNewItemModel(Parcel in) {
        this.title = in.readString();
        this.message = in.readString();
        this.image = in.readString();
        this.platforms = in.createStringArrayList();
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public String getImage() {
        return image;
    }

    @NonNull
    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(@NonNull List<String> platforms) {
        this.platforms = platforms;
    }

    public boolean isAndroidMessage() {
        for (String platform : platforms) {
            if (platform.trim().equalsIgnoreCase(Platform.ANDROID.key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.message);
        dest.writeString(this.image);
        dest.writeStringList(this.platforms);
    }

    public static final Creator<WhatsNewItemModel> CREATOR = new Creator<WhatsNewItemModel>() {
        @Override
        public WhatsNewItemModel createFromParcel(Parcel source) {
            return new WhatsNewItemModel(source);
        }

        @Override
        public WhatsNewItemModel[] newArray(int size) {
            return new WhatsNewItemModel[size];
        }
    };
}

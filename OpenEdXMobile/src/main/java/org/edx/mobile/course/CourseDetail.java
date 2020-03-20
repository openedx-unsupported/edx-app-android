package org.edx.mobile.course;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import org.edx.mobile.model.api.StartType;
import org.edx.mobile.util.UrlUtil;

public class CourseDetail implements Parcelable {
    public String course_id;
    public String name;
    public String number;
    public String org;
    public String short_description;
    public String start;
    public StartType start_type;
    public String start_display;
    public String end;
    public String enrollment_start;
    public String enrollment_end;
    public String blocks_url;
    public Media media;
    public String effort;
    public String overview;
    public Boolean invitation_only;

    public static class Media implements Parcelable {
        public Image course_image;
        public Video course_video;

        protected Media(Parcel in) {
            course_image = (Image) in.readValue(Image.class.getClassLoader());
            course_video = (Video) in.readValue(Video.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(course_image);
            dest.writeValue(course_video);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
            @Override
            public Media createFromParcel(Parcel in) {
                return new Media(in);
            }

            @Override
            public Media[] newArray(int size) {
                return new Media[size];
            }
        };
    }

    public static class Image implements Parcelable {
        private String uri;

        protected Image(Parcel in) {
            uri = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(uri);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
            @Override
            public Image createFromParcel(Parcel in) {
                return new Image(in);
            }

            @Override
            public Image[] newArray(int size) {
                return new Image[size];
            }
        };

        @Nullable
        public String getUri(String baseURL) {
            return UrlUtil.makeAbsolute(uri, baseURL);
        }
    }

    public static class Video implements Parcelable {
        public String uri;

        protected Video(Parcel in) {
            uri = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(uri);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Video> CREATOR = new Parcelable.Creator<Video>() {
            @Override
            public Video createFromParcel(Parcel in) {
                return new Video(in);
            }

            @Override
            public Video[] newArray(int size) {
                return new Video[size];
            }
        };
    }

    protected CourseDetail(Parcel in) {
        course_id = in.readString();
        name = in.readString();
        number = in.readString();
        org = in.readString();
        short_description = in.readString();
        start = in.readString();
        start_type = (StartType) in.readValue(StartType.class.getClassLoader());
        start_display = in.readString();
        end = in.readString();
        enrollment_start = in.readString();
        enrollment_end = in.readString();
        blocks_url = in.readString();
        media = (Media) in.readValue(Media.class.getClassLoader());
        effort = in.readString();
        overview = in.readString();
        invitation_only = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(course_id);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(org);
        dest.writeString(short_description);
        dest.writeString(start);
        dest.writeValue(start_type);
        dest.writeString(start_display);
        dest.writeString(end);
        dest.writeString(enrollment_start);
        dest.writeString(enrollment_end);
        dest.writeString(blocks_url);
        dest.writeValue(media);
        dest.writeString(effort);
        dest.writeString(overview);
        dest.writeValue(invitation_only);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CourseDetail> CREATOR = new Parcelable.Creator<CourseDetail>() {
        @Override
        public CourseDetail createFromParcel(Parcel in) {
            return new CourseDetail(in);
        }

        @Override
        public CourseDetail[] newArray(int size) {
            return new CourseDetail[size];
        }
    };
}

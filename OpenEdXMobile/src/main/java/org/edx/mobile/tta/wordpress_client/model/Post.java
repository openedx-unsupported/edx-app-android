package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.tta.utils.MxHelper;
import org.edx.mobile.tta.wordpress_client.util.Validate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Arjun Singh
 *         Created on 2015/12/03.
 */
public class Post extends WPObject<Post> {

    public static final String POST_STATUS_DRAFT = "draft";
    public static final String POST_STATUS_PUBLISH = "publish";
    public static final String POST_STATUS_PUBLISHED_PRIVATE = "private";
    public static final String POST_STATUS_FUTURE = "future";
    public static final String POST_STATUS_PENDING = "pending";
    public static final String POST_STATUS_TRASH = "trash";
    public static final String POST_STATUS_AUTO_DRAFT = "auto-draft";
    public static final String POST_STATUS_UPLOADING = "uploading";

    public static final String JSON_FIELD_CONTENT = "content";
    public static final String JSON_FIELD_EXCERPT = "excerpt";
    public static final String JSON_FIELD_FEATURED_IMAGE = "featured_media";
    public static final String JSON_FIELD_STICKY = "sticky";
    public static final String JSON_FIELD_FORMAT = "format";
    public static final String JSON_FIELD_STATUS = "status";
    public static final String JSON_FIELD_CATEGORIES = "categories";
    public static final String JSON_FIELD_TAGS = "tags";


    /**
     * A password to protect access to the post.
     */
    @SerializedName("password")
    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public Post withPassword(String password) {
        setPassword(password);
        return this;
    }

    /**
     * The content for the object.
     */
    @SerializedName("content")
    private WPGeneric content;

    public void setContent(WPGeneric content) {
        this.content = content;
    }

    public WPGeneric getContent() {
        return content;
    }

    public Post withContent(String content) {
        WPGeneric generic = new WPGeneric();
        generic.setRaw(content);
        generic.setRendered(content);
        setContent(generic);
        return this;
    }

    /**
     * The excerpt for the object.
     */
    @SerializedName("excerpt")
    private WPGeneric excerpt;

    public void setExcerpt(WPGeneric excerpt) {
        this.excerpt = excerpt;
    }

    public WPGeneric getExcerpt() {
        return excerpt;
    }

    public Post withExcerpt(WPGeneric excerpt) {
        setExcerpt(excerpt);
        return this;
    }

    /**
     * ID of the featured image for the object.
     */
    @SerializedName("featured_media")
    private int featuredMedia = -1;

    public void setFeaturedMedia(int featuredMedia) {
        this.featuredMedia = featuredMedia;
    }

    public int getFeaturedMedia() {
        return featuredMedia;
    }

    public Post withFeaturedMedia(int featuredMedia) {
        setFeaturedMedia(featuredMedia);
        return this;
    }

    /**
     * Whether or not the object should be treated as sticky.
     */
    @SerializedName("sticky")
    private boolean sticky;

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    public boolean getSticky() {
        return sticky;
    }

    public Post withSticky(boolean sticky) {
        setSticky(sticky);
        return this;
    }

    /**
     * The format for the object.
     */
    @SerializedName("format")
    private String format;

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public Post withFormat(String format) {
        setFormat(format);
        return this;
    }


    /**
     * The Comment count for the object.
     */
    @SerializedName("total_comments")
    private int total_comments;

    public void setTotal_comments(int total_comments) {
        this.total_comments = total_comments;
    }

    public int getTotal_comments() {
        return total_comments;
    }

    /**
     * Weather the post is liked by login user or not object.
     */
    @SerializedName("is_liked")
    private boolean is_liked;

    public void setIsLiked(int is_liked) {
        if(is_liked==0)
        this.is_liked = false;
        else
            this.is_liked = true;
    }

    public boolean getIsLikes() {
        return is_liked;
    }

    private String filterinString;
    /**
     * The Filter faplicable for post object.
     */
    @SerializedName("custom_filters")
    private ArrayList<CustomFilter> filter;

    public void setFilterInString(String filter) {
        this.filterinString = filter;
    }

    public void setFilterFromJSON(String filter_json) {
        this.filter = getFilterFromJSON(filter_json);
    }

    public ArrayList<CustomFilter> getFilter() {
        return filter;
    }

    public String getFormatedFilter() {
        String formated_filter = "";
        if (filter == null || filter.size() == 0)
            return formated_filter;

        for (int filter_index = 0; filter_index < filter.size(); filter_index++) {
            String filter_name = filter.get(filter_index).getName();

            if (filter.get(filter_index).getChoices() == null)
                continue;

            for (int choice_index = 0; choice_index < filter.get(filter_index).getChoices().length; choice_index++) {
                formated_filter = formated_filter + "##" + filter_name + "_" + "" + filter.get(filter_index).getChoices()[choice_index];
            }

        }

        if (!formated_filter.equals(""))
            formated_filter= formated_filter+ "##";

        return formated_filter;
    }

    public ArrayList<CustomFilter> getFilterFromJSON(String filter_json)
    {
        MxHelper helper=new MxHelper();
        return helper.getCustomFilterObjectFromJson(filter_json);
    }

    public String geJSONFromFilter(ArrayList<CustomFilter> filters)
    {
        MxHelper helper=new MxHelper();
        return helper.getJSONStringfromCustomFilterObj(filters);
    }


    @SerializedName("likes")
    private String likes;

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getLikes() {
        return likes;
    }

    /**
     * A named status for the object.
     * <p/>
     * One of: publish, future, draft, pending, private
     */
    @SerializedName("status")
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Post withStatus(String status) {
        setStatus(status);
        return this;
    }

    @SerializedName(JSON_FIELD_CATEGORIES)
    private List<Long> categories;


    /**
     * The Likes count for the object.
     */
    @SerializedName("post_image")
    private String post_image;

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setCategories(List<Long> categories) {
        this.categories = categories;
    }

    public List<Long> getCategories() {
        return categories;
    }

    //formate categories in such a manner so that we can traverse it by using LIKE in sqlite
    public String getAsStringWithSeparater() {
        String cat_ids="#";

        if(categories==null || categories.size()==0)
            return "";

        for(int cat_index=0;cat_index < categories.size();cat_index++)
        {
            cat_ids=cat_ids+""+categories.get(cat_index)+"#";
        }

        return cat_ids;
    }

    public void setCategoriesFromSeparater(String ids) {
        if(ids==null || ids.equals(""))
        {
            categories=new ArrayList<>();
            return;
        }
        String[] parts = ids.split("#");

        if(parts.length==0|| (parts.length==1&& parts[0].equals("")) )
        {
            categories=new ArrayList<>();
            return;
        }

        categories=new ArrayList<>();

        for(int cat_index=0;cat_index < parts.length;cat_index++)
        {
            if(parts[cat_index].equals(""))
                continue;

            categories.add(new Long(parts[cat_index]));
        }
    }

    public void addCategory(long catId) {
        if (categories == null) {
            categories = new ArrayList<>();
        }

        categories.add(catId);
    }

    public Post withCategories(List<Long> categories) {
        setCategories(categories);
        return this;
    }

    public Post withCategory(long catId) {
        addCategory(catId);
        return this;
    }


    @SerializedName("tags")
    private List<Long> tags;

    public void setTags(List<Long> tags) {
        this.tags = tags;
    }

    public List<Long> getTags() {
        return tags;
    }

    public void addTag(long tagId) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tagId);
    }

    public Post withTags(List<Long> tags) {
        setTags(tags);
        return this;
    }

    public Post withTag(long tagId) {
        addTag(tagId);
        return this;
    }

    @Override
    public Post withId(long id) {
        setId(id);
        return this;
    }

    @Override
    public Post withDate(String date) {
        setDate(date);
        return this;
    }

    @Override
    public Post withDateGMT(String dateGMT) {
        setDateGMT(dateGMT);
        return this;
    }

    @Override
    public Post withGuid(WPGeneric guid) {
        setGuid(guid);
        return this;
    }

    @Override
    public Post withModified(String modified) {
        setModified(modified);
        return this;
    }

    @Override
    public Post withModifiedGMT(String modifiedGMT) {
        setModifiedGMT(modifiedGMT);
        return this;
    }

    @Override
    public Post withSlug(String slug) {
        setSlug(slug);
        return this;
    }

    @Override
    public Post withType(String type) {
        setType(type);
        return this;
    }

    @Override
    public Post withLink(String link) {
        setLink(link);
        return this;
    }

    @Override
    public Post withTitle(String title) {
        WPGeneric generic = new WPGeneric();
        generic.setRendered(title);
        setTitle(generic);
        return this;
    }

    @Override
    public Post withAuthor(int author) {
        setAuthor(author);
        return this;
    }

    @Override
    public Post withCommentStatus(WPStatus commentStatus) {
        setCommentStatus(commentStatus);
        return this;
    }

    @Override
    public Post withPingStatus(WPStatus pingStatus) {
        setPingStatus(pingStatus);
        return this;
    }

    @Override
    public Post withLinks(ArrayList<Link> links) {
        setLinks(links);
        return this;
    }

    @Override
    public Post withLink(Link link) {
        addLink(link);
        return this;
    }

    public Post() {
    }

    public Post(Parcel in) {
        super(in);

        password = in.readString();
        content = in.readParcelable(WPGeneric.class.getClassLoader());
        excerpt = in.readParcelable(WPGeneric.class.getClassLoader());
        featuredMedia = in.readInt();
        sticky = in.readByte() == 1;
        format = in.readString();
        status = in.readString();
        categories = new ArrayList<>();
        in.readList(categories, Long.class.getClassLoader());
        tags = new ArrayList<>();
        in.readList(tags, Long.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(password);
        dest.writeParcelable(content, flags);
        dest.writeParcelable(excerpt, flags);
        dest.writeInt(featuredMedia);
        dest.writeByte((byte) (sticky ? 1 : 0));
        dest.writeString(format);
        dest.writeString(status);
        dest.writeList(categories);
        dest.writeList(tags);
    }

    public static Map<String, Object> mapFromFields(Post post) {
        Map<String, Object> builder = new HashMap<>();

        WPObject.mapFromFields(post, builder);

        if (post.getContent() != null) {
            Validate.validateMapEntry(JSON_FIELD_CONTENT, post.getContent().getRaw(), builder);
        }
        Validate.validateMapEntry(JSON_FIELD_EXCERPT, post.getExcerpt(), builder);
        Validate.validateMapLongEntry(JSON_FIELD_FEATURED_IMAGE, post.getFeaturedMedia(), builder);
        Validate.validateMapEntry(JSON_FIELD_STICKY, post.getSticky(), builder);
        Validate.validateMapEntry(JSON_FIELD_FORMAT, post.getFormat(), builder);
        Validate.validateMapEntry(JSON_FIELD_LINKS, post.getLinks(), builder);
        Validate.validateMapEntry(JSON_FIELD_STATUS, post.getStatus(), builder);
        Validate.validateMapEntry(JSON_FIELD_CATEGORIES, post.getCategories(), builder);
        Validate.validateMapEntry(JSON_FIELD_TAGS, post.getTags(), builder);

        return builder;
    }

    public static final Parcelable.Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    @Override
    public String toString() {
        return super.toString() + "--> Post{" +
                ", content=" + content +
                ", excerpt=" + excerpt +
                ", featuredMedia=" + featuredMedia +
                ", sticky=" + sticky +
                ", format='" + format + '\'' +
                '}';
    }
}

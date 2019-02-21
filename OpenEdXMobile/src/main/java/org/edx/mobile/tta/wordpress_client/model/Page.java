package org.edx.mobile.tta.wordpress_client.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * @author Arjun Singh
 *         Created on 2016/01/14.
 */
public class Page extends WPObject<Page> {

    public static final String JSON_FIELD_CONTENT = "content";
    public static final String JSON_FIELD_EXCERPT = "excerpt";
    public static final String JSON_FIELD_FEATURED_IMAGE = "featured_image";
    public static final String JSON_FIELDS_PARENT = "parent";
    public static final String JSON_FIELDS_MENU_ORDER = "menu_order";
    public static final String JSON_FIELDS_TEMPLATE = "template";

    /**
     * The content for the object.
     */
    @SerializedName(JSON_FIELD_CONTENT)
    private WPGeneric content;

    public void setContent(WPGeneric content) {
        this.content = content;
    }

    public WPGeneric getContent() {
        return content;
    }

    public Page withContent(String content) {
        WPGeneric generic = new WPGeneric();
        generic.setRaw(content);
        setContent(generic);
        return this;
    }

    /**
     * The excerpt for the object.
     */
    @SerializedName(JSON_FIELD_EXCERPT)
    private WPGeneric excerpt;

    public void setExcerpt(WPGeneric excerpt) {
        this.excerpt = excerpt;
    }

    public WPGeneric getExcerpt() {
        return excerpt;
    }

    public Page withExcerpt(WPGeneric excerpt) {
        setExcerpt(excerpt);
        return this;
    }

    /**
     * ID of the featured image for the object.
     */
    @SerializedName(JSON_FIELD_FEATURED_IMAGE)
    private int featuredImage;

    public void setFeaturedImage(int featuredImage) {
        this.featuredImage = featuredImage;
    }

    public int getFeaturedImage() {
        return featuredImage;
    }

    public Page withFeaturedImage(int featuredImage) {
        setFeaturedImage(featuredImage);
        return this;
    }

    /**
     * The id for the parent of the object.
     */
    @SerializedName(JSON_FIELDS_PARENT)
    private long parent;

    public void setParent(long parent) {
        this.parent = parent;
    }

    public long getParent() {
        return parent;
    }

    public Page withParent(long parent) {
        setParent(parent);
        return this;
    }

    /**
     * The order of the object in relation to other object of its type.
     */
    @SerializedName(JSON_FIELDS_MENU_ORDER)
    private int menuOrder;

    public void setMenuOrder(int menuOrder) {
        this.menuOrder = menuOrder;
    }

    public int getMenuOrder() {
        return menuOrder;
    }

    public Page withMenuOrder(int menuOrder) {
        setMenuOrder(menuOrder);
        return this;
    }

    /**
     * The theme file to use to display the object.
     */
    @SerializedName(JSON_FIELDS_TEMPLATE)
    private String template;

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }

    public Page withTemplate(String template) {
        setTemplate(template);
        return this;
    }

    @Override
    public Page withId(long id) {
        setId(id);
        return this;
    }

    @Override
    public Page withDate(String date) {
        setDate(date);
        return this;
    }

    @Override
    public Page withDateGMT(String dateGMT) {
        setDateGMT(dateGMT);
        return this;
    }

    @Override
    public Page withGuid(WPGeneric guid) {
        setGuid(guid);
        return this;
    }

    @Override
    public Page withModified(String modified) {
        setModified(modified);
        return this;
    }

    @Override
    public Page withModifiedGMT(String modifiedGMT) {
        setModifiedGMT(modifiedGMT);
        return this;
    }

    @Override
    public Page withSlug(String slug) {
        setSlug(slug);
        return this;
    }

    @Override
    public Page withType(String type) {
        setType(type);
        return this;
    }

    @Override
    public Page withLink(String link) {
        setLink(link);
        return this;
    }

    @Override
    public Page withTitle(String title) {
        WPGeneric generic = new WPGeneric();
        generic.setRendered(title);
        setTitle(generic);
        return this;
    }

    @Override
    public Page withAuthor(int author) {
        setAuthor(author);
        return this;
    }

    @Override
    public Page withCommentStatus(WPStatus commentStatus) {
        setCommentStatus(commentStatus);
        return this;
    }

    @Override
    public Page withPingStatus(WPStatus pingStatus) {
        setPingStatus(pingStatus);
        return this;
    }

    @Override
    public Page withLinks(ArrayList<Link> links) {
        setLinks(links);
        return this;
    }

    @Override
    public Page withLink(Link link) {
        addLink(link);
        return this;
    }

    public Page() {
    }

    public Page(Parcel in) {
        super(in);

        content = in.readParcelable(WPGeneric.class.getClassLoader());
        excerpt = in.readParcelable(WPGeneric.class.getClassLoader());
        featuredImage = in.readInt();
        parent = in.readLong();
        menuOrder = in.readInt();
        template = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeParcelable(content, flags);
        dest.writeParcelable(excerpt, flags);
        dest.writeInt(featuredImage);
        dest.writeLong(parent);
        dest.writeInt(menuOrder);
        dest.writeString(template);
    }

    public static final Parcelable.Creator<Page> CREATOR = new Creator<Page>() {
        @Override
        public Page createFromParcel(Parcel source) {
            return new Page(source);
        }

        @Override
        public Page[] newArray(int size) {
            return new Page[size];
        }
    };
}

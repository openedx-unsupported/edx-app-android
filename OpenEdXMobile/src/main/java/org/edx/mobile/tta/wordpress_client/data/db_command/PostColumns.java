package org.edx.mobile.tta.wordpress_client.data.db_command;

/**
 * Created by JARVICE on 08-12-2017.
 */

public class PostColumns {



    /**
     * ID of the blog this record is linked to
     * <P>Type: INTEGER (long)</P>
     */
    public static final String BLOG_ID = "blog_id";

    /**
     * ID of the user on the WP site
     * <P>Type: TEXT</P>
     */
    public static final String WP_AUTHOR_ID = "wp_author_id";

    /**
     * Post id on WP
     * <P>Type: INTEGER (long)</P>
     */
    public static final String WP_POST_ID = "wp_post_id";

    /**
     * Flag to set if content has been updated locally
     * <P>Type: INTEGER (short)</P>
     */
    public static final String UPDATED = "updated";

    /**
     * Time local content was updated
     * <P>Type: INTEGER (long)</P>
     */
    public static final String UPDATED_TIME = "updated_time";












    /**
     * Date post was made
     * <P>Type: TEXT</P>
     */
    public static final  String DATE = "date";

    /**
     * Date post was made, in GMT
     * <P>Type: TEXT</P>
     */
    public static final String DATE_GMT = "date_gmt";

    /**
     * Globally unique identifier for object
     * <P>Type : TEXT</P>
     */
    public static final String GUID = "guid";

    /**
     * Date psot was last modified
     * <P>Type: TEXT</P>
     */
    public static final String MODIFIED = "modified";

    /**
     * Date psot was last modified, in GMT
     * <P>Type: TEXT</P>
     */
    public static final  String MODIFIED_GMT = "modified_gmt";

    /**
     * Password to protect access to post
     * <P>Type : TEXT</P>
     */
    public static final String PASSWORD = "password";

    /**
     * Alphanumeric identifier for the object unique to its type.
     * <P>Type : TEXT</P>
     */
    public static final String SLUG = "slug";

    /**
     * Named status for the object
     * <P>Type : TEXT</P>
     * <P>Valid values : publish, future, draft, pending, private</P>
     */
    public static final String STATUS = "status";

    /**
     * Type of post
     * <P>Type : TEXT</P>
     */
    public static final  String TYPE = "type";

    /**
     * URL to the object
     * <P>Type : TEXT</P>
     */
    public static final  String LINK = "link";

    /**
     * Title of the post
     * <P>Type: TEXT</P>
     */
    public static final String TITLE = "title";

    /**
     * Body of the post
     * <P>Type: TEXT</P>
     */
    public static final  String CONTENT = "content";

    /**
     * The excerpt for the object
     * <P>Type : TEXT</P>
     */
    public static final String EXCERPT = "excerpt";

    /**
     * ID of the featured media
     * <P>Type: INTEGER (long)</P>
     */
    public static final  String FEATURED_MEDIA = "featured_media";

    /**
     * Whether or not comments are open on the object
     * <P>Type : INTEGER (short)</P>
     *
     * @see org.edx.mobile.tta.wordpress_client.model.WPStatus
     */
    public static final String COMMENT_STATUS = "comment_status";

    /**
     * Whether or not the object can be pinged.
     * <P>Type : INTEGER (short)</P>
     *
     * @see org.edx.mobile.tta.wordpress_client.model.WPStatus
     */
    public static final String PING_STATUS = "ping_status";

    /**
     * Whether or not the object should be treated as sticky
     * <P>Type : INTEGER (short/boolean)</P>
     */
    public static final String STICKY = "sticky";

    /**
     * Format of the post
     * <P>Type: TEXT</P>
     */
    public static final   String FORMAT = "format";

    /**
     * +
     * Array of categories for this post
     * <P>Type: TEXT (array)</P>
     */
    public static final String CATEGORIES = "categories";

    /**
     * Array of tags for the post
     * <P>Type: TEXT (array)</P>
     */
    public static final  String TAGS = "tags";

    /**
     * Flag to specify if Post is being uploaded
     * <P>Type: INTEGER (short)</P>
     */
    public static final  String UPLOADING = "uploading";

    /**
     * specify total comment on post
     * <P>Type: INTEGER (short)</P>
     */
    public static final  String COMMENT_COUNT = "comment_count";

    /**
     * specify total likes on post
     * <P>Type: TEXT </P>
     */
    public static final String LIKE_COUNT = "like_count";

    /**
     * Public link to object
     * <P>Type: TEXT</P>
     */
    public static final String IMAGE = "image";

    /**
     * Public like to object
     * <P>Type: Boolean</P>
     */
    public static final String ISLIKE = "islike";

    /**
     * Public FILTER to object
     * <P>Type: TEXT</P>
     */
    public static final String FILTER = "filter";

    /**
     * Public FILTERJSON to object
     * <P>Type: TEXT</P>
     */
    public static final String FILTERJSON = "filterjson";
}

package org.edx.mobile.tta.wordpress_client.data;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.text.TextUtils;

import org.edx.mobile.tta.wordpress_client.model.MediaItem;
import org.edx.mobile.tta.wordpress_client.model.Meta;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.tta.wordpress_client.model.Taxonomy;
import org.edx.mobile.tta.wordpress_client.model.WPStatus;
import org.edx.mobile.tta.wordpress_client.util.DataConverters;

/**
 * @author Arjun Singh
 *         Created on 2016/02/11.
 */
public class WordPressContract {

    /**
     * Shared columns for tables to link records
     */
    interface BaseWPColumns {

        /**
         * ID of the blog this record is linked to
         * <P>Type: INTEGER (long)</P>
         */
        String BLOG_ID = "blog_id";

        /**
         * ID of the user on the WP site
         * <P>Type: TEXT</P>
         */
        String WP_AUTHOR_ID = "wp_author_id";

        /**
         * Post id on WP
         * <P>Type: INTEGER (long)</P>
         */
        String WP_POST_ID = "wp_post_id";

        /**
         * Flag to set if content has been updated locally
         * <P>Type: INTEGER (short)</P>
         */
        String UPDATED = "updated";

        /**
         * Time local content was updated
         * <P>Type: INTEGER (long)</P>
         */
        String UPDATED_TIME = "updated_time";
    }

    /**
     * Table for keeping track of multiple WP sites
     */
    interface BlogColumns {

        /**
         * Title of the blog
         * <P>Type: TEXT</P>
         */
        String TITLE = "title";

        /**
         * Url of the blog
         * <P>Type: TEXT</P>
         */
        String URL = "url";

        /**
         * Admin user name
         * <P>Type: TEXT</P>
         */
        String USER = "user";

        /**
         * Admin password
         * <P>Type: TEXT</P>
         */
        String PASS = "pass";

    }

    /**
     * WP author details
     */
    interface UsersColumns {

        /**
         * Username on WP blog
         * <P>Type: TEXT</P>
         */
        String USERNAME = "username";

        /**
         * User password for blog access
         * <P>Type: TEXT</P>
         */
        String PASS = "password";

        /**
         * Full name of the user
         * <P>Type: TEXT</P>
         */
        String FULL_NAME = "full_name";

        /**
         * First name of the user
         * <P>Type: TEXT</P>
         */
        String FIRST_NAME = "first_name";

        /**
         * Last name of the user
         * <P>Type: TEXT</P>
         */
        String LAST_NAME = "last_name";

        /**
         * Email of the user
         * <P>Type: TEXT</P>
         */
        String EMAIL = "email";

        /**
         * <P>Type: TEXT</P>
         */
        String URL = "url";

        /**
         * <P>Type: TEXT</P>
         */
        String DESCRIPTION = "description";

        /**
         * <P>Type: TEXT</P>
         */
        String LINK = "link";

        /**
         * <P>Type: TEXT</P>
         */
        String NICKNAME = "nickname";

        /**
         * <P>Type: TEXT</P>
         */
        String SLUG = "slug";

        /**
         * <P>Type: TEXT</P>
         */
        String REGISTERED_DATE = "registered_date";

        /**
         * <P>Type: TEXT</P>
         */
        String ROLES = "roles";

        /**
         * User icon
         * <P>Type: TEXT</P>
         */
        String AVATAR_URL = "avatar_url";
    }

    /**
     * WP Post columns
     */
    interface PostColumns {

        /**
         * Date post was made
         * <P>Type: TEXT</P>
         */
        String DATE = "date";

        /**
         * Date post was made, in GMT
         * <P>Type: TEXT</P>
         */
        String DATE_GMT = "date_gmt";

        /**
         * Globally unique identifier for object
         * <P>Type : TEXT</P>
         */
        String GUID = "guid";

        /**
         * Date psot was last modified
         * <P>Type: TEXT</P>
         */
        String MODIFIED = "modified";

        /**
         * Date psot was last modified, in GMT
         * <P>Type: TEXT</P>
         */
        String MODIFIED_GMT = "modified_gmt";

        /**
         * Password to protect access to post
         * <P>Type : TEXT</P>
         */
        String PASSWORD = "password";

        /**
         * Alphanumeric identifier for the object unique to its type.
         * <P>Type : TEXT</P>
         */
        String SLUG = "slug";

        /**
         * Named status for the object
         * <P>Type : TEXT</P>
         * <P>Valid values : publish, future, draft, pending, private</P>
         */
        String STATUS = "status";

        /**
         * Type of post
         * <P>Type : TEXT</P>
         */
        String TYPE = "type";

        /**
         * URL to the object
         * <P>Type : TEXT</P>
         */
        String LINK = "link";

        /**
         * Title of the post
         * <P>Type: TEXT</P>
         */
        String TITLE = "title";

        /**
         * Body of the post
         * <P>Type: TEXT</P>
         */
        String CONTENT = "content";

        /**
         * The excerpt for the object
         * <P>Type : TEXT</P>
         */
        String EXCERPT = "excerpt";

        /**
         * ID of the featured media
         * <P>Type: INTEGER (long)</P>
         */
        String FEATURED_MEDIA = "featured_media";

        /**
         * Whether or not comments are open on the object
         * <P>Type : INTEGER (short)</P>
         *
         * @see org.edx.mobile.tta.wordpress_client.model.WPStatus
         */
        String COMMENT_STATUS = "comment_status";

        /**
         * Whether or not the object can be pinged.
         * <P>Type : INTEGER (short)</P>
         *
         * @see org.edx.mobile.tta.wordpress_client.model.WPStatus
         */
        String PING_STATUS = "ping_status";

        /**
         * Whether or not the object should be treated as sticky
         * <P>Type : INTEGER (short/boolean)</P>
         */
        String STICKY = "sticky";

        /**
         * Format of the post
         * <P>Type: TEXT</P>
         */
        String FORMAT = "format";

        /**
         * +
         * Array of categories for this post
         * <P>Type: TEXT (array)</P>
         */
        String CATEGORIES = "categories";

        /**
         * Array of tags for the post
         * <P>Type: TEXT (array)</P>
         */
        String TAGS = "tags";

        /**
         * Flag to specify if Post is being uploaded
         * <P>Type: INTEGER (short)</P>
         */
        String UPLOADING = "uploading";

        /**
         * specify total comment on post
         * <P>Type: INTEGER (short)</P>
         */
        String COMMENT_COUNT = "comment_count";

        /**
         * specify total likes on post
         * <P>Type: TEXT </P>
         */
        String LIKE_COUNT = "like_count";

        /**
         * Public link to object
         * <P>Type: TEXT</P>
         */
        String IMAGE = "image";

        /**
         * Public like to object
         * <P>Type: Boolean</P>
         */
        String ISLIKE = "islike";

        /**
         * Public FILTER to object
         * <P>Type: TEXT</P>
         */
        String FILTER = "filter";
        /**
         * Public FILTERJSON to object
         * <P>Type: TEXT</P>
         */
        String FILTERJSON = "filterjson";
    }

    interface TaxonomyColumns {

        /**
         * ID of object on WP site
         * <P>Type: INTEGER (long)</P>
         */
        String WP_TAXONOMY_ID = "wp_taxonomy_id";

        /**
         * ID of parent object
         * <P>Type: INTEGER (long)</P>
         */
        String WP_PARENT_ID = "wp_parent_id";

        /**
         * Taxonomy type; category or tag
         * <P>Type: TEXT</P>
         */
        String TYPE = "type";

        /**
         * Object's label
         * <P>Type: TEXT</P>
         */
        String NAME = "name";

        /**
         * Object's description
         * <P>Type: TEXT</P>
         */
        String DESCRIPTION = "description";

        /**
         * Usage count for object
         * <P>Type: INTEGER</P>
         */
        String COUNT = "count";

        /**
         * Public link to object
         * <P>Type: TEXT</P>
         */
        String LINK = "link";

        /**
         * Public link to object
         * <P>Type: TEXT</P>
         */
        String IMAGE = "image";

        /**
         * Public Roles to object
         * <P>Type: TEXT</P>
         */
        String ROLES = "roles";

        /**
         * Public FILTERS to object
         * <P>Type: TEXT</P>
         */
        String FILTERS = "filters";
    }

    interface MetaColumns {

        /**
         * Row ID of Post item for use when Post hasn't been uploaded yet.
         * <P>Type: INTEGER (long)</P>
         */
        String POST_ROW_ID = "post_row_id";

        /**
         * ID of object on WP
         * <P>Type: INTEGER (long)</P>
         */
        String WP_META_ID = "wp_meta_id";

        /**
         * Key of the object
         * <P>Type: TEXT</P>
         */
        String KEY = "meta_key";

        /**
         * Value of the object
         * <P>Type: TEXT</P>
         */
        String VALUE = "meta_value";
    }

    interface MediaColumns {

        /**
         * Row ID of Post item for use when Post hasn't been uploaded yet.
         * <P>Type: INTEGER (long)</P>
         */
        String POST_ROW_ID = "post_row_id";

        /**
         * Id of the object on WP
         * <P>Type: INTEGER (long)</P>
         */
        String WP_MEDIA_ID = "wp_media_id";

        /**
         * Mime type of the media item
         * <P>Type: TEXT</P>
         */
        String TYPE = "type";

        /**
         * Uri pointing to media object
         * <P>Type: TEXT</P>
         */
        String URI = "uri";

        /**
         * Optional caption on media
         * <P>Type: TEXT</P>
         */
        String CAPTION = "caption";

        /**
         * External URL for the object
         * <P>Type: TEXT</P>
         */
        String EXTERNAL_URL = "external_url";

        /**
         * Flag to show upload state of object
         * <P>Type: INTEGER</P>
         */
        String UPLOAD_STATE = "upload_state";
    }

    interface References {
        String BLOG_ID = "REFERENCES " + Blogs.TABLE_NAME + "(" + Blogs.BLOG_ID + ")";
        String AUTHOR_ID = "REFERENCES " + Users.TABLE_NAME + "(" + Users.WP_AUTHOR_ID + ")";
        String POST_ID = "REFERENCES " + Posts.TABLE_NAME + "(" + Posts.WP_POST_ID + ")";
        String POST_ROW_ID = "REFERENCES " + Posts.TABLE_NAME + "(" + Posts._ID + ")";
    }

    public static class BaseWpTable implements BaseColumns, BaseWPColumns {
        public static final int IDX_ID = 0;
    }

    public static class Blogs extends BaseWpTable implements BlogColumns {
        public static final String TABLE_NAME = "blogs";

        public static final int IDX_TITLE = 1;
        public static final int IDX_URL = 2;
        public static final int IDX_USER = 3;
        public static final int IDX_PASS = 4;

        public static final String SCHEMA = "CREATE TABLE " + TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TITLE + " TEXT NOT NULL,"
                + URL + " TEXT NOT NULL,"
                + USER + " TEXT NOT NULL,"
                + PASS + " TEXT NOT NULL)";

        /**
         * Creates a {@link ContentValues} object for insert or updating a Blog entry.
         * <p/>
         * For <i>INSERT</i>, all values will be committed as is.
         * <br/>
         * For <i>UPDATE</i>, only values that are <B>NOT NULL</B> will be updated
         *
         * @param update True if this is an UPDATE action, false for INSERT
         * @param title  Title of the blog
         * @param url    Url of the blog
         * @param user   Admin user of the blog
         * @param pass   Password for the admin user
         * @return ContentValues for inserting/updating
         */
        public static ContentValues makeContentValues(boolean update, String title, String url, String user, String pass) {
            ContentValues values = new ContentValues();

            if (!update) {
                values.put(TITLE, title);
                values.put(URL, url);
                values.put(USER, user);
                values.put(PASS, pass);
            } else {
                if (!TextUtils.isEmpty(title)) {
                    values.put(TITLE, title);
                }
                if (!TextUtils.isEmpty(url)) {
                    values.put(URL, url);
                }
                if (!TextUtils.isEmpty(user)) {
                    values.put(USER, user);
                }
                if (!TextUtils.isEmpty(pass)) {
                    values.put(PASS, pass);
                }
            }

            return values;
        }

        /**
         * Inserts a blog entry
         *
         * @see Blogs#makeContentValues(boolean, String, String, String, String)
         */
        public static ContentValues insert(String title, String url, String user, String pass) {
            return makeContentValues(false, title, url, user, pass);
        }

        /**
         * Updates a blog entry
         *
         * @see Blogs#makeContentValues(boolean, String, String, String, String)
         */
        public static ContentValues update(String title, String url, String user, String pass) {
            return makeContentValues(true, title, url, user, pass);
        }
    }

    public static class Users extends BaseWpTable implements UsersColumns {
        public static final String TABLE_NAME = "users";

        public static final int IDX_BLOG_ID = 1;
        public static final int IDX_WP_AUTHOR_ID = 2;
        public static final int IDX_USERNAME = 3;
        public static final int IDX_PASSWORD = 4;
        public static final int IDX_FULL_NAME = 5;
        public static final int IDX_FIRST_NAME = 6;
        public static final int IDX_LAST_NAME = 7;
        public static final int IDX_EMAIL = 8;
        public static final int IDX_DESCRIPTION = 9;
        public static final int IDX_LINK = 10;
        public static final int IDX_NICKNAME = 11;
        public static final int IDX_SLUG = 12;
        public static final int IDX_REGISTERED_DATE = 13;
        public static final int IDX_ROLES = 14;
        public static final int IDX_AVATAR_URL = 15;

        public static final String SCHEMA = "CREATE TABLE " + TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BLOG_ID + " INTEGER " + References.BLOG_ID + ","
                + WP_AUTHOR_ID + " INTEGER,"
                + USERNAME + " TEXT,"
                + PASS + " TEXT,"
                + FULL_NAME + " TEXT,"
                + FIRST_NAME + " TEXT,"
                + LAST_NAME + " TEXT,"
                + EMAIL + " TEXT,"
                + URL + " TEXT,"
                + DESCRIPTION + " TEXT,"
                + LINK + " TEXT,"
                + NICKNAME + " TEXT,"
                + SLUG + " TEXT,"
                + REGISTERED_DATE + " TEXT,"
                + ROLES + " TEXT,"
                + AVATAR_URL + " TEXT)";

        private static ContentValues makeContentValues(boolean update, long blogId, long userId,
                                                       String username, String password, String fullName,
                                                       String firstName, String lastName, String email,
                                                       String url, String description, String link,
                                                       String nickname, String slug, String registeredDate,
                                                       String roles, String avatarUrl) {
            ContentValues values = new ContentValues();
            if (!update) {
                values.put(BLOG_ID, blogId);
                values.put(WP_AUTHOR_ID, userId);
                values.put(USERNAME, username);
                values.put(PASS, password);
                values.put(FULL_NAME, fullName);
                values.put(FIRST_NAME, firstName);
                values.put(LAST_NAME, lastName);
                values.put(EMAIL, email);
                values.put(URL, url);
                values.put(DESCRIPTION, description);
                values.put(LINK, link);
                values.put(NICKNAME, nickname);
                values.put(SLUG, slug);
                values.put(REGISTERED_DATE, registeredDate);
                values.put(ROLES, roles);
                values.put(AVATAR_URL, avatarUrl);
            } else {
                if (blogId != -1) {
                    values.put(BLOG_ID, blogId);
                }
                if (userId != -1) {
                    values.put(WP_AUTHOR_ID, userId);
                }
                if (!TextUtils.isEmpty(username)) {
                    values.put(USERNAME, username);
                }
                if (!TextUtils.isEmpty(password)) {
                    values.put(PASS, password);
                }
                if (!TextUtils.isEmpty(fullName)) {
                    values.put(FULL_NAME, fullName);
                }
                if (!TextUtils.isEmpty(firstName)) {
                    values.put(FIRST_NAME, firstName);
                }
                if (!TextUtils.isEmpty(lastName)) {
                    values.put(LAST_NAME, lastName);
                }
                if (!TextUtils.isEmpty(email)) {
                    values.put(EMAIL, email);
                }
                if (!TextUtils.isEmpty(url)) {
                    values.put(URL, url);
                }
                if (!TextUtils.isEmpty(description)) {
                    values.put(DESCRIPTION, description);
                }
                if (!TextUtils.isEmpty(link)) {
                    values.put(LINK, link);
                }
                if (!TextUtils.isEmpty(nickname)) {
                    values.put(NICKNAME, nickname);
                }
                if (!TextUtils.isEmpty(slug)) {
                    values.put(SLUG, slug);
                }
                if (!TextUtils.isEmpty(registeredDate)) {
                    values.put(REGISTERED_DATE, registeredDate);
                }
                if (!TextUtils.isEmpty(roles)) {
                    values.put(ROLES, roles);
                }
                if (!TextUtils.isEmpty(avatarUrl)) {
                    values.put(AVATAR_URL, avatarUrl);
                }
            }

            return values;
        }

        public static ContentValues insert(long blogId, long userId,
                                           String username, String password, String fullName,
                                           String firstName, String lastName, String email,
                                           String url, String description, String link,
                                           String nickname, String slug, String registeredDate,
                                           String roles, String avatarUrl) {
            return makeContentValues(false, blogId, userId, username, password, fullName,
                    firstName, lastName, email, url, description, link, nickname, slug, registeredDate,
                    roles, avatarUrl);
        }

        public static ContentValues update(long blogId, long userId,
                                           String username, String password, String fullName,
                                           String firstName, String lastName, String email,
                                           String url, String description, String link,
                                           String nickname, String slug, String registeredDate,
                                           String roles, String avatarUrl) {
            return makeContentValues(true, blogId, userId, username, password, fullName,
                    firstName, lastName, email, url, description, link, nickname, slug, registeredDate,
                    roles, avatarUrl);
        }
    }

    public static class Posts extends BaseWpTable implements PostColumns {
        public static final String TABLE_NAME = "posts";

        public static final String QUALIFIED_ID = TABLE_NAME + "." + _ID;

        public static final int IDX_BLOG_ID = 1;
        public static final int IDX_WP_AUTHOR_ID = 2;
        public static final int IDX_WP_POST_ID = 3;
        public static final int IDX_DATE = 4;
        public static final int IDX_DATE_GMT = 5;
        public static final int IDX_GUID = 6;
        public static final int IDX_MODIFIED = 7;
        public static final int IDX_MODIFIED_GMT = 8;
        public static final int IDX_PASSWORD = 9;
        public static final int IDX_SLUG = 10;
        public static final int IDX_STATUS = 11;
        public static final int IDX_TYPE = 12;
        public static final int IDX_LINK = 13;
        public static final int IDX_TITLE = 14;
        public static final int IDX_CONTENT = 15;
        public static final int IDX_EXCERPT = 16;
        public static final int IDX_FEATURED_MEDIA = 17;
        public static final int IDX_COMMENT_STATUS = 18;
        public static final int IDX_PING_STATUS = 19;
        public static final int IDX_STICKY = 20;
        public static final int IDX_FORMAT = 21;
        public static final int IDX_CATEGORIES = 22;
        public static final int IDX_TAGS = 23;
        public static final int IDX_UPDATED = 24;
        public static final int IDX_UPDATED_TIME = 25;
        public static final int IDX_UPLOADING = 26;
        public static final int IDX_COMMENT_COUNT = 27;
        public static final int IDX_LIKE_COUNT = 28;
        public static final int IDX_ISLIKE = 29;
        public static final int IDX_FILTER = 30;


        public static final String SCHEMA = "CREATE TABLE " + TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BLOG_ID + " INTEGER " + References.BLOG_ID + ","
                + WP_AUTHOR_ID + " INTEGER " + References.AUTHOR_ID + ","
                + WP_POST_ID + " INTEGER,"
                + DATE + " TEXT,"
                + DATE_GMT + " TEXT,"
                + GUID + " TEXT,"
                + MODIFIED + " TEXT,"
                + MODIFIED_GMT + " TEXT,"
                + PASSWORD + " TEXT,"
                + SLUG + " TEXT,"
                + STATUS + " TEXT,"
                + TYPE + " TEXT,"
                + LINK + " TEXT,"
                + TITLE + " TEXT,"
                + CONTENT + " TEXT,"
                + EXCERPT + " TEXT,"
                + FEATURED_MEDIA + " INTEGER,"
                + COMMENT_STATUS + " INTEGER,"
                + PING_STATUS + " INTEGER,"
                + STICKY + " INTEGER,"
                + FORMAT + " TEXT,"
                + CATEGORIES + " TEXT,"
                + TAGS + " TEXT,"
                + UPDATED + " INTEGER,"
                + UPDATED_TIME + " INTEGER,"
                + COMMENT_COUNT+" INTEGER,"
                + LIKE_COUNT + " TEXT,"
                + IMAGE + " TEXT,"
                + ISLIKE + " INTEGER ,"
                + FILTER + " TEXT,"
                + FILTERJSON + " TEXT,"
                + UPLOADING + " INTEGER DEFAULT 0)";

        private static ContentValues makeContentValues(boolean update, long blogId, long authorId, long postId,
                                                       String date, String dateGmt, String guid, String modified,
                                                       String modifiedGmt, String password, String slug, String status,
                                                       String type, String link, String title, String content,
                                                       String excerpt, long featuredMedia, int commentStatus,
                                                       int pingStatus, boolean sticky, String format,
                                                       String categories, String tags,int comment_count,String likes,String image,boolean islike,String filter,String filterjson) {
            ContentValues values = new ContentValues();
            if (!update) {
                values.put(BLOG_ID, blogId);
                values.put(WP_AUTHOR_ID, authorId);
                values.put(WP_POST_ID, postId);
                values.put(DATE, date);
                values.put(DATE_GMT, dateGmt);
                values.put(GUID, guid);
                values.put(MODIFIED, modified);
                values.put(MODIFIED_GMT, modifiedGmt);
                values.put(PASSWORD, password);
                values.put(SLUG, slug);
                values.put(STATUS, status);
                values.put(TYPE, type);
                values.put(LINK, link);
                values.put(TITLE, title);
                values.put(CONTENT, content);
                values.put(EXCERPT, excerpt);
                values.put(FEATURED_MEDIA, featuredMedia);
                values.put(COMMENT_STATUS, commentStatus);
                values.put(PING_STATUS, pingStatus);
                values.put(STICKY, sticky);
                values.put(FORMAT, format);
                values.put(CATEGORIES, categories);
                values.put(TAGS, tags);
                values.put(COMMENT_COUNT, comment_count);
                values.put(LIKE_COUNT, likes);
                values.put(IMAGE, image);
                values.put(ISLIKE,islike);
                values.put(FILTER,filter);
                values.put(FILTERJSON,filterjson);
            } else {
                if (blogId != -1) {
                    values.put(BLOG_ID, blogId);
                }
                if (authorId != -1) {
                    values.put(WP_AUTHOR_ID, authorId);
                }
                if (postId != -1) {
                    values.put(WP_POST_ID, postId);
                }
                if (!TextUtils.isEmpty(date)) {
                    values.put(DATE, date);
                }
                if (!TextUtils.isEmpty(dateGmt)) {
                    values.put(DATE_GMT, dateGmt);
                }
                if (!TextUtils.isEmpty(guid)) {
                    values.put(GUID, guid);
                }
                if (!TextUtils.isEmpty(modified)) {
                    values.put(MODIFIED, modified);
                }
                if (!TextUtils.isEmpty(modifiedGmt)) {
                    values.put(MODIFIED_GMT, modifiedGmt);
                }
                if (!TextUtils.isEmpty(password)) {
                    values.put(PASSWORD, password);
                }
                if (!TextUtils.isEmpty(slug)) {
                    values.put(SLUG, slug);
                }
                if (!TextUtils.isEmpty(status)) {
                    values.put(STATUS, status);
                }
                if (!TextUtils.isEmpty(type)) {
                    values.put(TYPE, type);
                }
                if (!TextUtils.isEmpty(link)) {
                    values.put(LINK, link);
                }
                if (!TextUtils.isEmpty(title)) {
                    values.put(TITLE, title);
                }
                if (content != null) {
                    values.put(CONTENT, content);
                }
                if (!TextUtils.isEmpty(excerpt)) {
                    values.put(EXCERPT, excerpt);
                }
                if (featuredMedia != -1) {
                    values.put(FEATURED_MEDIA, featuredMedia);
                }
                if (commentStatus != -1) {
                    values.put(COMMENT_STATUS, commentStatus);
                }
                if (pingStatus != -1) {
                    values.put(PING_STATUS, pingStatus);
                }
                values.put(STICKY, sticky);
                if (!TextUtils.isEmpty(format)) {
                    values.put(FORMAT, format);
                }
                if (!TextUtils.isEmpty(categories)) {
                    values.put(CATEGORIES, categories);
                }
                if (!TextUtils.isEmpty(tags)) {
                    values.put(TAGS, tags);
                }
                if (comment_count!= -1) {
                    values.put(COMMENT_COUNT, comment_count);
                }
                if (!TextUtils.isEmpty(likes)) {
                    values.put(LIKE_COUNT, likes);
                }
                if (!TextUtils.isEmpty(image)) {
                    values.put(IMAGE, image);
                }

                    values.put(FILTER, filter);

                    values.put(FILTERJSON,filterjson);

                    values.put(ISLIKE, islike);
            }

            return values;
        }

        public static ContentValues insert(long blogId, long authorId, long postId,
                                           String date, String dateGmt, String guid, String modified,
                                           String modifiedGmt, String password, String slug, String status,
                                           String type, String link, String title, String content,
                                           String excerpt, long featuredMedia, int commentStatus,
                                           int pingStatus, boolean sticky, String format,
                                           String categories, String tags,int comment_count,String likes,String image,boolean islike,String filter,String filterjson) {
            return makeContentValues(false, blogId, authorId, postId, date, dateGmt, guid, modified,
                    modifiedGmt, password, slug, status, type, link, title, content, excerpt,
                    featuredMedia, commentStatus, pingStatus, sticky, format, categories, tags,comment_count,likes,image,islike,filter,filterjson);
        }

        public static ContentValues insert(long blogId, long authorId, Post post) {
            return insert(blogId, authorId, post.getId(), post.getDate(), post.getDateGMT(), post.getGuid() != null ? post.getGuid().getRendered() : null,
                    post.getModified(), post.getModifiedGMT(), post.getPassword(), post.getSlug(), post.getStatus(),
                    post.getType(), post.getLink(), post.getTitle().getRendered(), post.getContent() != null ? post.getContent().getRendered() : null,
                    post.getExcerpt() != null ? post.getExcerpt().getRendered() : null, post.getFeaturedMedia(),
                    post.getCommentStatus() != null ? post.getCommentStatus().getStatus() : WPStatus.OPEN,
                    post.getPingStatus() != null ? post.getPingStatus().getStatus() : WPStatus.OPEN,
                    post.getSticky(), post.getFormat(),post.getAsStringWithSeparater(),
                    DataConverters.makeTagString(post.getTags()),post.getTotal_comments(),post.getLikes(),post.getPost_image(),post.getIsLikes(),post.getFormatedFilter(),post.geJSONFromFilter(post.getFilter()));
        }

        public static ContentValues update(long blogId, long authorId, long postId,
                                           String date, String dateGmt, String guid, String modified,
                                           String modifiedGmt, String password, String slug, String status,
                                           String type, String link, String title, String content,
                                           String excerpt, long featuredMedia, int commentStatus,
                                           int pingStatus, boolean sticky, String format,
                                           String categories, String tags,int comment_count,String likes,String image,boolean islike,String filter,String filterjson) {
            return makeContentValues(true, blogId, authorId, postId, date, dateGmt, guid, modified,
                    modifiedGmt, password, slug, status, type, link, title, content, excerpt,
                    featuredMedia, commentStatus, pingStatus, sticky, format, categories, tags,comment_count,likes,image,islike,filter,filterjson);
        }

        public static ContentValues update(long blogId, long authorId, Post post) {
            return update(blogId, authorId, post.getId(), post.getDate(), post.getDateGMT(), post.getGuid() != null ? post.getGuid().getRendered() : null,
                    post.getModified(), post.getModifiedGMT(), post.getPassword(), post.getSlug(), post.getStatus(),
                    post.getType(), post.getLink(), post.getTitle().getRendered(), post.getContent() != null ? post.getContent().getRendered() : null,
                    post.getExcerpt() != null ? post.getExcerpt().getRendered() : null, post.getFeaturedMedia(),
                    post.getCommentStatus() != null ? post.getCommentStatus().getStatus() : WPStatus.OPEN,
                    post.getPingStatus() != null ? post.getPingStatus().getStatus() : WPStatus.OPEN,
                    post.getSticky(), post.getFormat(), post.getAsStringWithSeparater(),
                    DataConverters.makeTagString(post.getTags()),
                    post.getTotal_comments(),
                    post.getLikes(),
                    post.getPost_image(),
                    post.getIsLikes(),
                    post.getFormatedFilter(),
                    post.geJSONFromFilter(post.getFilter()));
        }
    }

    public static class Taxonomies extends BaseWpTable implements TaxonomyColumns {
        public static final String TABLE_NAME = "taxonomies";

        public static final int IDX_BLOG_ID = 1;
        public static final int IDX_WP_TAXONOMY_ID = 2;
        public static final int IDX_WP_PARENT_ID = 3;
        public static final int IDX_TYPE = 4;
        public static final int IDX_NAME = 5;
        public static final int IDX_DESCRIPTION = 6;
        public static final int IDX_COUNT = 7;
        public static final int IDX_LINK = 8;
        public static final int IDX_IMAGE = 9;
        public static final int IDX_Roles= 10;
        public static final int IDX_FILTERS= 11;

        public static final String SCHEMA = "CREATE TABLE " + TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BLOG_ID + " INTEGER " + References.BLOG_ID + ","
                + WP_TAXONOMY_ID + " INTEGER,"
                + WP_PARENT_ID + " INTEGER,"
                + TYPE + " TEXT NOT NULL,"
                + NAME + " TEXT NOT NULL,"
                + DESCRIPTION + " TEXT,"
                + COUNT + " INTEGER,"
                + IMAGE + " TEXT,"
                + ROLES + " TEXT,"
                + FILTERS + " TEXT,"
                + LINK + " TEXT)";

        private static ContentValues makeContentValues(boolean update, long blogId, long taxId, long parentId,
                                                       String type, String name, String description,
                                                       int count, String link,String image,String roles,String filters) {
            ContentValues values = new ContentValues();
            if (!update) {
                values.put(BLOG_ID, blogId);
                values.put(WP_TAXONOMY_ID, taxId);
                values.put(WP_PARENT_ID, parentId);
                values.put(TYPE, type);
                values.put(NAME, name);
                values.put(DESCRIPTION, description);
                values.put(COUNT, count);
                values.put(LINK, link);
                values.put(IMAGE, image);
                values.put(IMAGE, roles);
                values.put(FILTERS, filters);
            } else {
                if (blogId != -1) {
                    values.put(BLOG_ID, blogId);
                }
                if (taxId != -1) {
                    values.put(WP_TAXONOMY_ID, taxId);
                }
                if (parentId != -1) {
                    values.put(WP_PARENT_ID, parentId);
                }
                if (!TextUtils.isEmpty(type)) {
                    values.put(TYPE, type);
                }
                if (!TextUtils.isEmpty(name)) {
                    values.put(NAME, name);
                }
                if (!TextUtils.isEmpty(DESCRIPTION)) {
                    values.put(DESCRIPTION, description);
                }
                if (count != -1) {
                    values.put(COUNT, count);
                }
                if (!TextUtils.isEmpty(link)) {
                    values.put(LINK, link);
                }
                if (!TextUtils.isEmpty(image)) {
                    values.put(IMAGE, image);
                }
                if (!TextUtils.isEmpty(roles)) {
                    values.put(ROLES, roles);
                }

                if (!TextUtils.isEmpty(filters)) {
                    values.put(FILTERS, filters);
                }
            }
            return values;
        }

        public static ContentValues insert(long blogId, long taxId, long parentId, String type, String name,
                                           String description, int count, String link,String image,String roles,String filters) {
            return makeContentValues(false, blogId, taxId, parentId, type, name, description, count, link,image,roles,filters);
        }

        public static ContentValues insert(long blogId, String type, Taxonomy taxonomy) {
            StringBuilder stringBuilder=new StringBuilder();
            if (taxonomy.getRoles() != null && taxonomy.getRoles().size() > 0)
            {
                for (String role:taxonomy.getRoles())
                {
                    stringBuilder.append(role+",");
                }
            }

            return insert(blogId, taxonomy.getId(), taxonomy.getParent(), type, taxonomy.getName(),
                    taxonomy.getDescription(), taxonomy.getCount(), taxonomy.getLink(), taxonomy.getCategory_image(), stringBuilder.toString(),taxonomy.getFormatedfilters());
        }

        public static ContentValues update(long blogId, long taxId, long parentId, String type, String name,
                                           String description, int count, String link,String image,String roles,String filters) {

            return makeContentValues(true, blogId, taxId, parentId, type, name, description, count, link,image,roles,filters);
        }
    }

    public static class Metas extends BaseWpTable implements MetaColumns {
        public static final String TABLE_NAME = "metas";

        public static final int IDX_BLOG_ID = 1;
        public static final int IDX_WP_POST_ID = 2;
        public static final int IDX_POST_ROW_ID = 3;
        public static final int IDX_WP_META_ID = 4;
        public static final int IDX_KEY = 5;
        public static final int IDX_VALUE = 6;

        public static final String SCHEMA = "CREATE TABLE " + TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BLOG_ID + " INTEGER " + References.BLOG_ID + ","
                + WP_POST_ID + " INTEGER " + References.POST_ID + ","
                + POST_ROW_ID + " INTEGER " + References.POST_ROW_ID + ","
                + WP_META_ID + " INTEGER,"
                + KEY + " TEXT NOT NULL,"
                + VALUE + " TEXT NOT NULL)";

        private static ContentValues makeContentValues(boolean update, long blogId, long postId, long postRowId, long metaId,
                                                       String key, String value) {
            ContentValues values = new ContentValues();
            if (!update) {
                values.put(BLOG_ID, blogId);
                values.put(WP_POST_ID, postId);
                values.put(POST_ROW_ID, postRowId);
                values.put(WP_META_ID, metaId);
                values.put(KEY, key);
                values.put(VALUE, value);
            } else {
                if (blogId != -1) {
                    values.put(BLOG_ID, blogId);
                }
                if (postId != -1) {
                    values.put(WP_POST_ID, postId);
                }
                if (postRowId != -1) {
                    values.put(POST_ROW_ID, postRowId);
                }
                if (metaId != -1) {
                    values.put(WP_META_ID, metaId);
                }
                if (!TextUtils.isEmpty(key)) {
                    values.put(KEY, key);
                }
                if (!TextUtils.isEmpty(value)) {
                    values.put(VALUE, value);
                }
            }
            return values;
        }

        public static ContentValues insert(long blogId, long postId, long postRowId, long metaId,
                                           String key, String value) {
            return makeContentValues(false, blogId, postId, postRowId, metaId, key, value);
        }

        public static ContentValues insert(long blogId, long postId, long postRowId, Meta meta) {
            return insert(blogId, postId, postRowId, meta.getId(), meta.getKey(), meta.getValue());
        }

        public static ContentValues update(long blogId, long postId, long postRowId, long metaId,
                                           String key, String value) {
            return makeContentValues(true, blogId, postId, postRowId, metaId, key, value);
        }

        public static ContentValues update(long blogId, long postId, long postRowId, Meta meta) {
            return update(blogId, postId, postRowId, meta.getId(), meta.getKey(), meta.getValue());
        }
    }

    public static class Medias extends BaseWpTable implements MediaColumns {
        public static final String TABLE_NAME = "medias";

        public static final int IDX_BLOG_ID = 1;
        public static final int IDX_WP_POST_ID = 2;
        public static final int IDX_POST_ROW_ID = 3;
        public static final int IDX_WP_MEDIA_ID = 4;
        public static final int IDX_TYPE = 5;
        public static final int IDX_URI = 6;
        public static final int IDX_CAPTION = 7;
        public static final int IDX_EXTERNAL_URL = 8;
        public static final int IDX_UPLOAD_STATE = 9;

        public static final String SCHEMA = "CREATE TABLE " + TABLE_NAME + "("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BLOG_ID + " INTEGER " + References.BLOG_ID + ","
                + WP_POST_ID + " INTEGER " + References.POST_ID + ","
                + POST_ROW_ID + " INTEGER " + References.POST_ROW_ID + ","
                + WP_MEDIA_ID + " INTEGER,"
                + TYPE + " TEXT NOT NULL,"
                + URI + " TEXT NOT NULL,"
                + CAPTION + " TEXT,"
                + EXTERNAL_URL + " TEXT,"
                + UPLOAD_STATE + " INTEGER)";

        private static ContentValues makeContentValues(boolean update, long blogId, long postId,
                                                       long postRowId, long mediaId, String type,
                                                       String uri, String caption, String externalUrl) {
            ContentValues values = new ContentValues();
            if (!update) {
                values.put(BLOG_ID, blogId);
                values.put(WP_POST_ID, postId);
                values.put(POST_ROW_ID, postRowId);
                values.put(WP_MEDIA_ID, mediaId);
                values.put(TYPE, type);
                values.put(URI, uri);
                values.put(CAPTION, caption);
                values.put(EXTERNAL_URL, externalUrl);
            } else {
                if (blogId != -1) {
                    values.put(BLOG_ID, blogId);
                }
                if (postId != -1) {
                    values.put(WP_POST_ID, postId);
                }
                if (postRowId != -1) {
                    values.put(POST_ROW_ID, postRowId);
                }
                if (mediaId != -1) {
                    values.put(WP_MEDIA_ID, mediaId);
                }
                if (!TextUtils.isEmpty(type)) {
                    values.put(TYPE, type);
                }
                if (!TextUtils.isEmpty(uri)) {
                    values.put(URI, uri);
                }
                if (!TextUtils.isEmpty(caption)) {
                    values.put(CAPTION, caption);
                }
                if (!TextUtils.isEmpty(externalUrl)) {
                    values.put(EXTERNAL_URL, externalUrl);
                }
            }
            return values;
        }

        public static ContentValues insert(long blogId, long postId, long postRowId, long mediaId,
                                           String type, String uri, String caption, String externalUrl) {
            return makeContentValues(false, blogId, postId, postRowId, mediaId, type, uri, caption, externalUrl);
        }

        public static ContentValues insert(long blogId, long postId, long postRowId, MediaItem mediaItem) {
            return insert(blogId, postId, postRowId, mediaItem.mediaId, mediaItem.type, mediaItem.uri, mediaItem.caption,
                    mediaItem.externalUrl);
        }

        public static ContentValues update(long blogId, long postId, long postRowId, long mediaId,
                                           String type, String uri, String caption, String externalUrl) {
            return makeContentValues(true, blogId, postId, postRowId, mediaId, type, uri, caption, externalUrl);
        }

        public static ContentValues update(long blogId, long postId, long postRowId, MediaItem mediaItem) {
            return update(blogId, postId, postRowId, mediaItem.mediaId, mediaItem.type, mediaItem.uri, mediaItem.caption,
                    mediaItem.externalUrl);
        }
    }
}

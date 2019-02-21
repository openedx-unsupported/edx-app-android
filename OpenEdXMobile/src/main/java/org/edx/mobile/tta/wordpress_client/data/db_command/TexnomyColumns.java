package org.edx.mobile.tta.wordpress_client.data.db_command;

/**
 * Created by JARVICE on 08-12-2017.
 */

public class TexnomyColumns {


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
    public static final  String UPDATED = "updated";

    /**
     * Time local content was updated
     * <P>Type: INTEGER (long)</P>
     */
    public static final  String UPDATED_TIME = "updated_time";














    /**
     * ID of object on WP site
     * <P>Type: INTEGER (long)</P>
     */
    public static final String WP_TAXONOMY_ID = "wp_taxonomy_id";

    /**
     * ID of parent object
     * <P>Type: INTEGER (long)</P>
     */
    public static final String WP_PARENT_ID = "wp_parent_id";

    /**
     * Taxonomy type; category or tag
     * <P>Type: TEXT</P>
     */
    public static final  String TYPE = "type";

    /**
     * Object's label
     * <P>Type: TEXT</P>
     */
    public static final String NAME = "name";

    /**
     * Object's description
     * <P>Type: TEXT</P>
     */
    public static final String DESCRIPTION = "description";

    /**
     * Usage count for object
     * <P>Type: INTEGER</P>
     */
    public static final String COUNT = "count";

    /**
     * Public link to object
     * <P>Type: TEXT</P>
     */
    public static final String LINK = "link";

    /**
     * Public link to object
     * <P>Type: TEXT</P>
     */
    public static final String IMAGE = "image";

    /**
     * Public Roles to object
     * <P>Type: TEXT</P>
     */
    public static final String ROLES = "roles";


    /**
     * Public FILTERS to object
     * <P>Type: TEXT</P>
     */
    public static final String FILTERS = "filters";
}

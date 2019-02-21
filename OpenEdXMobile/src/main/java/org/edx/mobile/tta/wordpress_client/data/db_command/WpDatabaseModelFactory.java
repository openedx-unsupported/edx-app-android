package org.edx.mobile.tta.wordpress_client.data.db_command;

import android.database.Cursor;

import com.google.gson.Gson;

import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.tta.wordpress_client.model.Taxonomy;
import org.edx.mobile.tta.wordpress_client.model.WPGeneric;
import org.edx.mobile.tta.wordpress_client.model.WPStatus;


/**
 * Created by JARVICE on 05-12-2017.
 */

public class WpDatabaseModelFactory {
    public static Taxonomy getTexonomyModel(Cursor c) {
        Taxonomy txnmy = new Taxonomy();
        txnmy.setId(c.getLong(c.getColumnIndex(TexnomyColumns.BLOG_ID)));
        txnmy.setDescription(c.getString(c.getColumnIndex(TexnomyColumns.DESCRIPTION)));
        txnmy.setName(c.getString(c.getColumnIndex(TexnomyColumns.NAME)));
        txnmy.setLink(c.getString(c.getColumnIndex(TexnomyColumns.LINK)));
        txnmy.setCategory_image(c.getString(c.getColumnIndex(TexnomyColumns.IMAGE)));
        txnmy.setRoles(c.getString(c.getColumnIndex(TexnomyColumns.ROLES)));
        txnmy.setParent(c.getLong(c.getColumnIndex(TexnomyColumns.WP_PARENT_ID)));
        txnmy.setCount(c.getInt(c.getColumnIndex(TexnomyColumns.COUNT)));
        txnmy.setTaxonomy(c.getString(c.getColumnIndex(TexnomyColumns.TYPE)));
        txnmy.setCustom_filterslist(c.getString(c.getColumnIndex(TexnomyColumns.FILTERS)));
        return txnmy;
    }

    public static Post getPostModel(Cursor c) {
        Gson gson = new Gson();
        WPGeneric wpGeneric = new WPGeneric();
        Post post = new Post();
        post.setId(c.getLong(c.getColumnIndex(PostColumns.WP_POST_ID)));

        post.setAuthor(c.getInt(c.getColumnIndex(PostColumns.WP_AUTHOR_ID)));
        post.setDate(c.getString(c.getColumnIndex(PostColumns.DATE)));

        post.setDateGMT(c.getString(c.getColumnIndex(PostColumns.DATE_GMT)));

        if (c.getString(c.getColumnIndex(PostColumns.GUID)) != null) {
            wpGeneric = new WPGeneric();
            wpGeneric.setRendered(c.getString(c.getColumnIndex(PostColumns.GUID)));

            post.setGuid(wpGeneric);
        }

        post.setModified(c.getString(c.getColumnIndex(PostColumns.MODIFIED)));
        post.setModifiedGMT(c.getString(c.getColumnIndex(PostColumns.MODIFIED_GMT)));
        post.setPassword(c.getString(c.getColumnIndex(PostColumns.PASSWORD)));
        post.setSlug(c.getString(c.getColumnIndex(PostColumns.SLUG)));
        post.setStatus(c.getString(c.getColumnIndex(PostColumns.STATUS)));
        post.setType(c.getString(c.getColumnIndex(PostColumns.TYPE)));

        if (c.getString(c.getColumnIndex(PostColumns.TITLE)) != null) {
            wpGeneric = new WPGeneric();
            wpGeneric.setRendered(c.getString(c.getColumnIndex(PostColumns.TITLE)));
            post.setTitle(wpGeneric);
        }

        if (c.getString(c.getColumnIndex(PostColumns.CONTENT)) != null) {
            wpGeneric = new WPGeneric();
            wpGeneric.setRendered(c.getString(c.getColumnIndex(PostColumns.CONTENT)));
            post.setContent(wpGeneric);
        }

        if (c.getString(c.getColumnIndex(PostColumns.EXCERPT)) != null) {
            wpGeneric = new WPGeneric();
            wpGeneric.setRendered(c.getString(c.getColumnIndex(PostColumns.EXCERPT)));
            post.setExcerpt(wpGeneric);
        }
        post.setFeaturedMedia(c.getInt(c.getColumnIndex(PostColumns.FEATURED_MEDIA)));


        if (c.getString(c.getColumnIndex(PostColumns.COMMENT_STATUS)) != null) {
            WPStatus wpStatus = new WPStatus();
            if (c.getString(c.getColumnIndex(PostColumns.COMMENT_STATUS)).equals("1"))
                wpStatus.setStatus(1);
            else
                wpStatus.setStatus(0);

            post.setCommentStatus(wpStatus);
        }


        if (c.getString(c.getColumnIndex(PostColumns.PING_STATUS)) != null) {
            WPStatus wpStatus = new WPStatus();

            if (c.getString(c.getColumnIndex(PostColumns.PING_STATUS)).equals("1"))
                wpStatus.setStatus(1);
            else
                wpStatus.setStatus(0);

            post.setPingStatus(wpStatus);
        }


        if (c.getInt(c.getColumnIndex(PostColumns.STICKY)) == 0)
            post.setSticky(false);
        else
            post.setSticky(true);

        post.setFormat(c.getString(c.getColumnIndex(PostColumns.FORMAT)));

        if (c.getString(c.getColumnIndex(PostColumns.CATEGORIES)) != null) {
            post.setCategoriesFromSeparater(c.getString(c.getColumnIndex(PostColumns.CATEGORIES)));
        }

        post.setLink(c.getString(c.getColumnIndex(PostColumns.LINK)));

        // post.setLinks(c.getString(c.getColumnIndex(PostColumns.LINK)));

        // post.setTags(c.getString(c.getColumnIndex(PostColumns.TAGS)));
        post.setTotal_comments(c.getInt(c.getColumnIndex(PostColumns.COMMENT_COUNT)));
        post.setLikes(c.getString(c.getColumnIndex(PostColumns.LIKE_COUNT)));
        if (c.getString(c.getColumnIndex(PostColumns.IMAGE))!=null)
            post.setPost_image(c.getString(c.getColumnIndex(PostColumns.IMAGE)));


            post.setIsLiked(c.getInt(c.getColumnIndex(PostColumns.ISLIKE)));

        if (c.getString(c.getColumnIndex(PostColumns.FILTER))!=null)
            post.setFilterInString(c.getString(c.getColumnIndex(PostColumns.FILTER)));

        if (c.getString(c.getColumnIndex(PostColumns.FILTERJSON))!=null)
            post.setFilterFromJSON(c.getString(c.getColumnIndex(PostColumns.FILTERJSON)));

        return post;
    }
}

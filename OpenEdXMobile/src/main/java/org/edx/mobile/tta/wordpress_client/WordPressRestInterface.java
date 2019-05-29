
package org.edx.mobile.tta.wordpress_client;

import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.Like;
import org.edx.mobile.tta.wordpress_client.model.Media;
import org.edx.mobile.tta.wordpress_client.model.Meta;
import org.edx.mobile.tta.wordpress_client.model.Page;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.tta.wordpress_client.model.Taxonomy;
import org.edx.mobile.tta.wordpress_client.model.User;
import org.edx.mobile.tta.wordpress_client.model.WPProfileModel;
import org.edx.mobile.tta.wordpress_client.model.WpAuthResponse;
import org.edx.mobile.tta.wordpress_client.model.dto.PostCount;
import org.edx.mobile.tta.wordpress_client.util.ContentUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * REST API interface for WP REST API plugin.
 *
 * @author Arjun Singh
 *         Created on 2016/01/12.
 */
public interface WordPressRestInterface {


    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are sending a user and password to get the AuthResponse.grant_type=password
     */
    @FormUrlEncoded
    @POST("wp-content/plugins/miniorange-oauth-server-premium/web/moserver/token")
    Call<WpAuthResponse> getAccessToken(@Field("grant_type") String grant_type,
                                        @Field("client_id") String client_id,
                                        @Field("username") String username,
                                        @Field("password") String password,
                                        @Field("client_secret") String client_secret);

/*    @POST("token")
    Call<WpAuthResponse> getAccessToken(@Body WPAuthRequest request);*/

    /**
     * Depending on the query parameters for this endpoint, a different action will be triggered
     * on the server side. In this case, we are using our refresh_token to get a new AuthResponse.
     */
    @FormUrlEncoded
    @POST("wp-content/plugins/miniorange-oauth-server-premium/web/moserver/token")
    Call<WpAuthResponse> refreshAccessToken(@Field("grant_type") String grant_type,
                                          @Field("client_id") String client_id,
                                          @Field("refresh_token") String refresh_token,
                                          @Field("client_secret") String client_secret);


    @GET("wp-content/plugins/miniorange-oauth-server-premium/web/moserver/resource")
    Call<WPProfileModel> getLoginUser();

    /*Like*/
    @FormUrlEncoded
    @POST("wp-json/wp/v2/likepost")
    Call<Like> setLike(@Field("action") String action,
                       @Field("id") String id,
                       @Field("type") String type);

    /* POSTS */

    /**
     * Creates a new Post.
     *
     * @param postFields Map of Post fields
     * @return The created Post object
     */
    @POST("wp-json/wp/v2/posts")
    Call<Post> createPost(@Body Map<String, Object> postFields);

    /**
     * Gets all Posts.
     *
     * @return List of Post objects
     */
    @GET("wp-json/wp/v2/posts")
    Call<List<Post>> getPosts();

    /**
     * Gets all Posts using provided query params
     *
     * @param map Optional query parameters
     * @return List of Post objects
     */
    @GET("wp-json/wp/v2/posts")
    Call<List<Post>> getPosts(@QueryMap Map<String, String> map);

    /**
     * Gets a single Post.
     *
     * @param postId Id of the Post
     * @param map    Optional query params
     * @return Post object
     */
    @GET("wp-json/wp/v2/posts/{id}")
    Call<Post> getPost(@Path("id") long postId, @QueryMap Map<String, String> map);

    /**
     * Gets all Posts created by a User.
     *
     * @param authorId Id of the User
     * @param status   The status of the post, eg. draft or publish
     * @return List of Post objects for the User
     */
    @GET("wp-json/wp/v2/posts")
    Call<List<Post>> getPostsForAuthor(@Query("author") long authorId, @Query("status") String status, @Query("context") String context);

    /**
     * Updates an existing Post.
     *
     * @param postId     Id of the Post
     * @param postFields Map of the fields to update
     * @return The updated Post object
     */
    @POST("wp-json/wp/v2/posts/{id}")
    Call<Post> updatePost(@Path("id") long postId, @Body Map<String, Object> postFields);

    /**
     * Deletes a Post.
     *
     * @param postId Id of the Post
     * @param force  Whether to bypass trash and force deletion.
     * @return Post object that was deleted
     */
    @DELETE("wp-json/wp/v2/posts/{id}")
    Call<Post> deletePost(@Path("id") long postId, @Query("force") boolean force, @Query("context") String context);

    /**
     * Creates new Meta objects for a Post
     *
     * @param postId Id of the Post
     * @param fields Map containing key/value pairs
     * @return The created PostMeta object
     */
    @POST("wp-json/wp/v2/posts/{id}/meta")
    Call<Meta> createPostMeta(@Path("id") long postId, @Body Map<String, Object> fields);

    @GET("wp-json/wp/v2/posts/{id}/meta")
    Call<List<Meta>> getPostMeta(@Path("id") long postId);

    @GET("wp-json/wp/v2/posts/{postId}/meta/{metaId}")
    Call<Meta> getPostMeta(@Path("postId") long postId, @Path("metaId") long metaId);

    @POST("wp-json/wp/v2/posts/{postId}/meta/{metaId}")
    Call<Meta> updatePostMeta(@Path("postId") long postId, @Path("metaId") long metaId, @Body Map<String, Object> fields);

    @DELETE("wp-json/wp/v2/posts/{postId}/meta/{metaId}?force=true")
    Call<Meta> deletePostMeta(@Path("postId") long postId, @Path("metaId") long metaId);


    @GET("wp-json/wp/v2/posts/{postId}/revisions")
    Call<List<Post>> getPostRevisions(@Path("postId") long postId);

    @GET("wp-json/wp/v2/posts/{postId}/revisions/{revId}")
    Call<Post> getPostRevision(@Path("postId") long postId, @Path("revId") long revId);

    @DELETE("wp-json/wp/v2/posts/{postId}/revisions/{revId}")
    Call<Post> deltePostRevision(@Path("postId") long postId, @Path("revId") long revId);


    @POST("wp-json/wp/v2/posts/{postId}/categories/{catId}")
    Call<Taxonomy> setPostCategory(@Path("postId") long postId, @Path("catId") long catId);

    @GET("wp-json/wp/v2/posts/{postId}/categories")
    Call<List<Taxonomy>> getPostCategories(@Path("postId") long postId);

    @GET("wp-json/wp/v2/posts/{postId}/categories/{catId}")
    Call<Taxonomy> getPostCategory(@Path("postId") long postId, @Path("catId") long catId);

    @DELETE("wp-json/wp/v2/posts/{postId}/categories/{catId}")
    Call<Taxonomy> deletePostCategory(@Path("postId") long postId, @Path("catId") long catId);


    @POST("wp-json/wp/v2/posts/{postId}/tags/{tagId}")
    Call<Taxonomy> setPostTag(@Path("postId") long postId, @Path("tagId") long tagId);

    //@GET("wp-json/wp/v2/posts/{postId}/tags")
    //Call<List<Taxonomy>> getPostTags(@Path("postId") long postId);

    @GET("wp-json/wp/v2/tags")
    Call<List<Taxonomy>> getPostTags(@Query("post") long postId);

    @GET("wp-json/wp/v2/posts/{postId}/tags/{tagId}")
    Call<Taxonomy> getPostTag(@Path("postId") long postId, @Path("tagId") long catId);

    @DELETE("wp-json/wp/v2/posts/{postId}/tags/{tagId}")
    Call<Taxonomy> deletePostTag(@Path("postId") long postId, @Path("tagId") long catId);


    /* PAGES */

    @POST("wp-json/wp/v2/pages")
    Call<Page> createPage(@Body Map<String, Object> fieldMap);

    @GET("wp-json/wp/v2/pages")
    Call<List<Page>> getPages();


    @GET("wp-json/wp/v2/pages/{pageId}")
    Call<Page> getPage(@Path("pageId") long pageId);

    @POST("wp-json/wp/v2/pages/{pageId}")
    Call<Page> updatePage(@Path("pageId") long pageId, @Body Map<String, Object> fieldMap);

    @DELETE("wp-json/wp/v2/pages/{pageId}")
    Call<Page> deletePage(@Path("pageId") long pageId);


    @POST("wp-json/wp/v2/pages/{pageId}/meta")
    Call<Meta> createPageMeta(@Path("pageId") long pageId, @Body Map<String, Object> fields);

    @GET("wp-json/wp/v2/pages/{pageId}/meta")
    Call<List<Media>> getPageMeta(@Path("pageId") long pageId);

    @GET("wp-json/wp/v2/pages/{pageId}/meta/{metaId}")
    Call<Meta> getPageMeta(@Path("pageId") long postId, @Path("metaId") long metaId);

    @POST("wp-json/wp/v2/pages/{pageId}/meta/{metaId}")
    Call<Meta> updatePageMeta(@Path("pageId") long postId, @Path("metaId") long metaId, @Body Map<String, Object> fields);

    @DELETE("wp-json/wp/v2/pages/{pageId}/meta/{metaId}")
    Call<Meta> deletePageMeta(@Path("pageId") long postId, @Path("metaId") long metaId);


    @GET("wp-json/wp/v2/pages/{pageId}/revisions")
    Call<List<Page>> getPageRevisions(@Path("pageId") long postId);

    @GET("wp-json/wp/v2/pages/{pageId}/revisions/{revId}")
    Call<Page> getPageRevision(@Path("pageId") long postId, @Path("revId") long revId);

    @DELETE("wp-json/wp/v2/pages/{pageId}/revisions/{revId}")
    Call<Page> deltePageRevision(@Path("pageId") long postId, @Path("revId") long revId);


    /* MEDIA */

    /**
     * Upload a new Media item into WordPress.
     *
     * @param header Content-Disposition header containing filename, eg "filename=file.jpg"
     * @param params Map containing all fields to upload
     * @return Media item created
     * @see ContentUtil#makeMediaItemUploadMap(Media, File)
     */
    @Multipart
    @POST("wp-json/wp/v2/media")
    Call<Media> createMedia(@Header("Content-Disposition") String header, @PartMap Map<String, RequestBody> params);

    /**
     * Gets all Media objects.
     *
     * @return List of Media objects
     */
    @GET("wp-json/wp/v2/media")
    Call<List<Media>> getMedia();

    /**
     * Returns a single Media item.
     *
     * @param mediaId Id of the Media item
     * @return The Media object
     */
    @GET("wp-json/wp/v2/media/{id}")
    Call<Media> getMedia(@Path("id") long mediaId);

    /**
     * Returns all Media items attached to a Post.
     *
     * @param postId Id of the Post
     * @param type   MIME type of Media
     * @return List of Media objects
     */
    @GET("wp-json/wp/v2/posts/{id}/media/{type}")
    Call<List<Media>> getMediaForPost(@Path("id") long postId, @Path("type") String type);


    @GET("wp-json/wp/v2/media")
    Call<List<Media>> getMediaForSlug(@QueryMap Map<String, Object> map);


    /**
     * Updates a Media item.
     *
     * @param mediaId Id the Media item
     * @param fields  Fields to update
     * @return The updated Media object
     */
    @POST("wp-json/wp/v2/media/{id}")
    Call<Media> updateMedia(@Path("id") long mediaId, @Body Map<String, Object> fields);

    /**
     * Deletes a Media item.
     *
     * @param mediaId Id of the Media item
     * @return The deleted Media object
     */
    @DELETE("wp-json/wp/v2/media/{id}?force=true")
    Call<Media> deleteMedia(@Path("id") long mediaId);


    /* TYPES */

    //@GET("wp-json/wp/v2/types")

    //@GET("wp-json/wp/v2/types/{typeId}")


    /* STATUSES */

    // @GET("wp-json/wp/v2/statuses")

    // @GET("wp-json/wp/v2/statuses/{statusId}")


    /* TAXONOMIES */

    // @GET("wp-json/wp/v2/taxonomies")

    // @GET("wp-json/wp/v2/taxonomies/{id}")


    /* CATEGORIES */

    @POST("wp-json/wp/v2/categories")
    Call<Taxonomy> createCategory(@Body Map<String, Object> fields);

    @GET("wp-json/wp/v2/categories")
    Call<List<Taxonomy>> getCategories();

    @GET("wp-json/wp/v2/categories/{id}")
    Call<Taxonomy> getCategory(@Path("id") long id);

    @GET("wp-json/wp/v2/categories")
    Call<List<Taxonomy>> getCategoryForSlug(@QueryMap Map<String, String> map);

    @GET("wp-json/wp/v2/categories")
    Call<List<Taxonomy>> getCategories(@QueryMap Map<String, Object> map);

    @POST("wp-json/wp/v2/categories/{id}")
    Call<Taxonomy> updateCategory(@Path("id") long id, Map<String, Object> fields);

    @DELETE("wp-json/wp/v2/categories/{id}")
    Call<Taxonomy> deleteCategory(@Path("id") long id);


    /* TAGS */

    @POST("wp-json/wp/v2/tags")
    Call<Taxonomy> createTag(@Body Map<String, Object> fields);

    @GET("wp-json/wp/v2/tags")
    Call<List<Taxonomy>> getTags();

    @GET("wp-json/wp/v2/tags")
    Call<List<Taxonomy>> getTagsOrdered(@QueryMap Map<String, String> map);

    @GET("wp-json/wp/v2/tags")
    Call<List<Taxonomy>> getTagForSlug(@QueryMap Map<String, String> map);

    @GET("wp-json/wp/v2/tags/{id}")
    Call<Taxonomy> getTag(@Path("id") long id);

    @POST("wp-json/wp/v2/tags/{id}")
    Call<Taxonomy> updateTag(@Path("id") long id, Map<String, Object> fields);

    @DELETE("wp-json/wp/v2/tags/{id}")
    Call<Taxonomy> deleteTag(@Path("id") long id);


    /* USERS */

    /**
     * Creates a new WordPress user.
     *
     * @param fields Map of fields
     * @return The created User object
     */
    @POST("wp-json/wp/v2/users")
    Call<User> createUser(@Body Map<String, Object> fields);

    @GET("wp-json/wp/v2/users")
    Call<List<User>> getUsers();

    @GET("wp-json/wp/v2/users/{id}")
    Call<User> getUser(@Path("id") long id);

    @POST("wp-json/wp/v2/users/{id}")
    Call<User> updateUser(@Path("id") long id, @Body Map<String, Object> fields);

    @DELETE("wp-json/wp/v2/users/{id}")
    Call<User> deleteUser(@Path("id") long id);

    /**
     * Gets existing User using username.
     *
     * @param username Login username of the User
     * @return The User object
     */
    @GET("wp-json/wp/v2/users/login/{username}")
    Call<User> getUserFromLogin(@Path("username") String username);

    @GET("wp-json/wp/v2/users/email/{email}")
    Call<User> getUserFromEmail(@Path("email") String email);

    @GET("wp-json/wp/v2/users/me")
    Call<User> getUserMe();


    /* COMMENTS */



    @FormUrlEncoded
    @POST("wp-json/wp/v2/comments")
    Call<Comment> createComment(@Field("author") Long author,
                                @Field("author_ip") String author_ip,
                                @Field("author_url") String author_url,
                                @Field("author_user_agent") String author_user_agent,
                                @Field("content") String content,
                                @Field(value = "date", encoded = true) String date,
                                @Field(value = "date_gmt",encoded = true) String date_gmt,
                                @Field("parent") int parent,
                                @Field("post") Long post);

    @GET("wp-json/wp/v2/comments")
    Call<List<Comment>> getComments();

    @GET("wp-json/wp/v2/comments")
    Call<List<Comment>> getCommentsByPost(@Query("post") long postId);

    @GET("wp-json/wp/v2/comments" + "?parent=0")
    Call<List<Comment>> getCommentsByPost(@Query("post") long postId,
                                          @Query("per_page") int take,
                                          @Query("page") int page);

    @GET("wp-json/wp/v2/comments" + "?per_page=100")
    Call<List<Comment>> getRepliesOnComment(@Query("post") long postId,
                                            @Query("parent") long commentId);

    @GET("wp-json/wp/v2/comments/{id}")
    Call<Comment> getComment(@Path("id") long id);

    @POST("wp-json/wp/v2/comments/{id}")
    Call<Comment> updateComment(@Path("id") long id, Map<String, Object> fields);

    @DELETE("wp-json/wp/v2/comments/{id}")
    Call<Comment> deleteComment(@Path("id") long id);

    /* OTHER */

    @GET("wp-json/wp/v2/posts")
    Call<List<Post>> getPostsForTags(@Query("filter[tag]") String tag);


    @GET("wp-json/wp/v2/posts")
    Call<List<Post>> getPostsForCategory(@Query("categories") Long id,@Query("per_page")  int per_page,@Query("page")  int page_number);

    @GET("wp-json/wp/v2/posts")
    Call<List<Post>> getPostsForCategoryWithFilters(@Query("categories") Long id,@Query("per_page")  int per_page,@Query("page")  double page_number,
                                                    @Query("custom_filters")  String custom_filters,@Query("orderby")  String orderby,@Query("order")  String order);
    /**
     * Returns the number of pages for each of the following post states:
     * publish, draft, private
     *
     * @return Number of pages for post states
     */
    @GET("wp-json/wp/v2/posts/counts")
    Call<PostCount> getPostCounts();


    //BY Arjun
    /**
     * Get Post created by a slug.
     *
     * @return POST ITEM
     */
    @GET("wp-json/wp/v2/posts/")
    Call<List<Post>> getPostBySlug(@QueryMap Map<String, String> map);
}

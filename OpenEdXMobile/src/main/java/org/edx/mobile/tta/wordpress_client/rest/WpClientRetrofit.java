package org.edx.mobile.tta.wordpress_client.rest;

import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.edx.mobile.tta.wordpress_client.WordPressRestInterface;
import org.edx.mobile.tta.wordpress_client.model.Comment;
import org.edx.mobile.tta.wordpress_client.model.CustomComment;
import org.edx.mobile.tta.wordpress_client.model.Like;
import org.edx.mobile.tta.wordpress_client.model.Media;
import org.edx.mobile.tta.wordpress_client.model.Meta;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.tta.wordpress_client.model.Taxonomy;
import org.edx.mobile.tta.wordpress_client.model.User;
import org.edx.mobile.tta.wordpress_client.model.WPProfileModel;
import org.edx.mobile.tta.wordpress_client.model.WpAuthResponse;
import org.edx.mobile.tta.wordpress_client.model.dto.PostCount;
import org.edx.mobile.tta.wordpress_client.rest.interceptor.OkHttpBasicAuthInterceptor;
import org.edx.mobile.tta.wordpress_client.rest.interceptor.OkHttpBearerTokenAuthInterceptor;
import org.edx.mobile.tta.wordpress_client.rest.interceptor.OkHttpDebugInterceptor;
import org.edx.mobile.tta.wordpress_client.util.ContentUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.edx.mobile.util.BrowserUtil.config;
import static org.edx.mobile.util.BrowserUtil.loginPrefs;

/**
 * @author Arjun Singh
 *         Created on 2016/01/12.
 */
public class WpClientRetrofit {

    private WordPressRestInterface mRestInterface;

    public WpClientRetrofit(String baseUrl, final String username, final String password) {
        this(baseUrl, username, password, false);
    }

    public WpClientRetrofit(String baseUrl, final String username, final String password, boolean debugEnabled) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);

        // add the Basic Auth header
        builder.addInterceptor(new OkHttpBasicAuthInterceptor(username, password));

        if (debugEnabled) {
            builder.addInterceptor(new OkHttpDebugInterceptor());
        }

        // setup retrofit with custom OkHttp client and Gson parser
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        // create instance of REST interface
        mRestInterface = retrofit.create(WordPressRestInterface.class);
    }

    //for refresh token client
    public WpClientRetrofit(boolean debugEnabled) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);

        if (debugEnabled) {
            builder.addInterceptor(new OkHttpDebugInterceptor());
        }

        // setup retrofit with custom OkHttp client and Gson parser
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(config.getConnectUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        // create instance of REST interface
        mRestInterface = retrofit.create(WordPressRestInterface.class);
    }

    //for all oauth 2.0 hits
    public WpClientRetrofit(boolean debugEnabled,boolean isTokenHit) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.writeTimeout(30, TimeUnit.SECONDS);

        if(!isTokenHit) {
            // add the OAuth 2.0 header
            if (loginPrefs.getWPAuthorizationHeader() != null)
                builder.addInterceptor(new OkHttpBearerTokenAuthInterceptor(loginPrefs.getWPAuthorizationHeader()));

            //builder.authenticator(new WPOauthRefreshTokenAuthenticator());
        }

        if (debugEnabled) {
            builder.addInterceptor(new OkHttpDebugInterceptor());
        }
        // setup retrofit with custom OkHttp client and Gson parser
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(config.getConnectUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

        // create instance of REST interface
        mRestInterface = retrofit.create(WordPressRestInterface.class);
    }

    private <T> void doRetrofitCall(Call<T> call, final WordPressRestResponse<T> callback) {
        Callback<T> retroCallback = new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful()) {
                    //Log.d("connect request",response.raw().request().url().toString());
                    //Log.d("connect response",response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure(HttpServerErrorResponse.from(response.errorBody()));

                    //Log.d("connect request",response.raw().request().url().toString());
                    //Log.d("connect response",response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure(HttpServerErrorResponse.from(t));
            }
        };
        call.enqueue(retroCallback);
    }

    // USER

    //get access token
    public void getAccessToken(@NonNull String username,
                                               @NonNull String password , WordPressRestResponse<WpAuthResponse> callback) {
        String grantType = "password";
        String clientID = config.getWPOAuthClientId();
        String clientSecret = config.getWPOAuthClientSecret();

        doRetrofitCall(mRestInterface.getAccessToken(grantType, clientID, username, password,clientSecret), callback);
        //return mRestInterface.getAccessToken(grantType, clientID, username, password,clientSecret);
    }

    //get refresh access-token
    public Call<WpAuthResponse>  refreshAccessToken(String refresh_token) {
        String grantType = "refresh_token";
        String clientID = config.getWPOAuthClientId();
        String clientSecret = config.getWPOAuthClientSecret();

        return mRestInterface.refreshAccessToken(grantType, clientID,refresh_token,clientSecret);
    }

    public void createUser(User user, WordPressRestResponse<User> callback) {
        doRetrofitCall(mRestInterface.createUser(User.mapFromFields(user)), callback);
    }

    public Call<User> getUser(long userId) {
        return mRestInterface.getUser(userId);
    }

    public void getUser(long userId, WordPressRestResponse<User> callback) {
        doRetrofitCall(mRestInterface.getUser(userId), callback);
    }

    public void getUserFromLogin(String login, WordPressRestResponse<User> callback) {
        doRetrofitCall(mRestInterface.getUserFromLogin(login), callback);
    }

    public void getUserFromEmail(String email, WordPressRestResponse<User> callback) {
        doRetrofitCall(mRestInterface.getUserFromEmail(email), callback);
    }

    public void getUserMe(WordPressRestResponse<User> callback) {
        doRetrofitCall(mRestInterface.getUserMe(), callback);
    }

    public void getLoginUser(WordPressRestResponse<WPProfileModel> callback) {
        doRetrofitCall(mRestInterface.getLoginUser(), callback);
    }

    public void updateUser(User user, WordPressRestResponse<User> callback) {
        Map<String, Object> map = User.mapFromFields(user);
        doRetrofitCall(mRestInterface.updateUser(user.getId(), map), callback);
    }

    // POSTS

    public void createPost(Post post, WordPressRestResponse<Post> callback) {
        // 201 CREATED on success
        doRetrofitCall(mRestInterface.createPost(Post.mapFromFields(post)), callback);
    }

    public Call<Post> createPost(Post post) {
        return mRestInterface.createPost(Post.mapFromFields(post));
    }

    public void getPost(long postId, WordPressRestResponse<Post> callback) {
        Map<String, String> map = new HashMap<>();
        //map.put("context", "edit");
        doRetrofitCall(mRestInterface.getPost(postId, map), callback);
    }

    public void getPostBySlug(String slug, WordPressRestResponse<List<Post>> callback) {
        Map<String, String> map = new HashMap<>();
        map.put("slug", slug);
        doRetrofitCall(mRestInterface.getPostBySlug(map), callback);
    }

    public Call<Post> getPost(long postId) {
        Map<String, String> map = new HashMap<>();
        map.put("context", "edit");
        return mRestInterface.getPost(postId, map);
    }

    public void getPostForEdit(long postId, WordPressRestResponse<Post> callback) {
        Map<String, String> map = new HashMap<>();
        map.put("context", "edit");
        doRetrofitCall(mRestInterface.getPost(postId, map), callback);
    }

    public void getPosts(WordPressRestResponse<List<Post>> callback) {
        doRetrofitCall(mRestInterface.getPosts(), callback);
    }

    public Call<List<Post>> getPosts() {
        return mRestInterface.getPosts();
    }

    public Call<List<Post>> getPostsForPage(int startPage) {
        Map<String, String> map = new HashMap<>();
        map.put("page", startPage + "");
        map.put("context", "edit");
        return mRestInterface.getPosts(map);
    }

    public Call<List<Post>> getPostsForPage(int startPage, int pageSize) {
        Map<String, String> map = new HashMap<>();
        map.put("page", startPage + "");
        map.put("per_page", pageSize + "");
        map.put("context", "edit");
        return mRestInterface.getPosts(map);
    }

    public Call<List<Post>> getPostsAfterDate(String date) {
        Map<String, String> map = new HashMap<>();
        map.put("after", date);
        map.put("context", "edit");
        return mRestInterface.getPosts(map);
    }

    public Call<List<Post>> getPostsForAuthor(long authorId, String status) {
        return mRestInterface.getPostsForAuthor(authorId, status, "edit");
    }

    public void getPostsForAuthor(long authorId, String status, WordPressRestResponse<List<Post>> callback) {
        doRetrofitCall(getPostsForAuthor(authorId, status), callback);
    }

    //added by Arjun to access all posts belongs to that category
    public void getPostsForCategory(long categoryId,int per_page,int page_number, WordPressRestResponse<List<Post>> callback) {
        doRetrofitCall(mRestInterface.getPostsForCategory(categoryId,per_page,page_number), callback);
    }

    //added by Arjun to access all posts belongs to that category with filter
    public void getPostsForCategoryWithFilters(long categoryId,int per_page,double page_number,String custom_filters,String orderby,String order,  WordPressRestResponse<List<Post>> callback) {
        doRetrofitCall(mRestInterface.getPostsForCategoryWithFilters(categoryId,per_page,page_number,custom_filters,orderby,order), callback);
    }

    public void getPostsForTag(String tag, WordPressRestResponse<List<Post>> callback) {
        doRetrofitCall(mRestInterface.getPostsForTags(tag), callback);
    }

    public void updatePost(Post post, WordPressRestResponse<Post> callback) {
        // 200 on success
        doRetrofitCall(mRestInterface.updatePost(post.getId(), Post.mapFromFields(post)), callback);
    }

    public Call<Post> updatePost(Post post) {
        return mRestInterface.updatePost(post.getId(), Post.mapFromFields(post));
    }

    public void deletePost(long postId, boolean force, WordPressRestResponse<Post> callback) {
        // 200 on success
        // 410 GONE on failure
        doRetrofitCall(mRestInterface.deletePost(postId, force, "edit"), callback);
    }

    public Call<Post> deletePost(long postId, boolean force) {
        return mRestInterface.deletePost(postId, force, "edit");
    }

    /* MEDIA */

    public void createMedia(Media media, File file, WordPressRestResponse<Media> callback) {
        Map<String, RequestBody> map = ContentUtil.makeMediaItemUploadMap(media, file);
        String header = "filename=" + file.getName();

        doRetrofitCall(mRestInterface.createMedia(header, map), callback);
    }

    public Call<Media> createMedia(Media media, File file) {
        Map<String, RequestBody> map = ContentUtil.makeMediaItemUploadMap(media, file);
        String header = "filename=" + file.getName();
        return mRestInterface.createMedia(header, map);
    }

    public void getMedia(long mediaId, WordPressRestResponse<Media> callback) {
        doRetrofitCall(mRestInterface.getMedia(mediaId), callback);
    }

    public void getMedia(WordPressRestResponse<List<Media>> callback) {
        doRetrofitCall(mRestInterface.getMedia(), callback);
    }

    public Call<Media> getMedia(long mediaId) {
        return mRestInterface.getMedia(mediaId);
    }

    public void getMediaForPost(long postId, String mimeType, WordPressRestResponse<List<Media>> callback) {
        doRetrofitCall(mRestInterface.getMediaForPost(postId, mimeType), callback);
    }

    public Call<List<Media>> getMediaForPost(long postId, String mimeType) {
        return mRestInterface.getMediaForPost(postId, mimeType);
    }

    public Call<List<Media>> getMediaForSlug(String slug) {
        Map<String, Object> map = new HashMap<>();
        map.put("slug", slug);
        return mRestInterface.getMediaForSlug(map);
    }

    public void getMediaForSlug(String slug, WordPressRestResponse<List<Media>> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put("slug", slug);

        doRetrofitCall(mRestInterface.getMediaForSlug(map), callback);
    }

    public void updateMedia(Media media, long mediaId, WordPressRestResponse<Media> callback) {
        doRetrofitCall(mRestInterface.updateMedia(mediaId, Media.mapFromFields(media)), callback);
    }

    public Call<Media> updateMedia(Media media, long mediaId) {
        return mRestInterface.updateMedia(mediaId, Media.mapFromFields(media));
    }

    public Call<Media> deleteMedia(long mediaId) {
        return mRestInterface.deleteMedia(mediaId);
    }

    /* TAXONOMIES */

    public void setTagForPost(long postId, long tagId, WordPressRestResponse<Taxonomy> callback) {
        doRetrofitCall(mRestInterface.setPostTag(postId, tagId), callback);
    }

    public void getTagsForPost(long postId, WordPressRestResponse<List<Taxonomy>> callback) {
        doRetrofitCall(mRestInterface.getPostTags(postId), callback);
    }

    public void getTags(WordPressRestResponse<List<Taxonomy>> callback) {
        doRetrofitCall(mRestInterface.getTags(), callback);
    }

    public void getTagsOrderedByCount(WordPressRestResponse<List<Taxonomy>> callback) {
        Map<String, String> map = new HashMap<>();
        map.put("orderby", "count");
        map.put("order", "desc");

        doRetrofitCall(mRestInterface.getTagsOrdered(map), callback);
    }

    public Call<List<Taxonomy>> getTagForSlug(String slug) {
        Map<String, String> map = new HashMap<>();
        map.put("search", slug);
        return mRestInterface.getTagForSlug(map);
    }

    public void setCategoryForPost(long postId, long catId, WordPressRestResponse<Taxonomy> callback) {
        doRetrofitCall(mRestInterface.setPostCategory(postId, catId), callback);
    }

    public Call<Taxonomy> setCategoryForPost(long postId, long catId) {
        return mRestInterface.setPostCategory(postId, catId);
    }

    public void getCategoriesForPost(long postId, WordPressRestResponse<List<Taxonomy>> callback) {
        doRetrofitCall(mRestInterface.getPostCategories(postId), callback);
    }

    public void getCategories(WordPressRestResponse<List<Taxonomy>> callback) {
        doRetrofitCall(mRestInterface.getCategories(), callback);
    }

    public Call<List<Taxonomy>> getCategoryForSlug(String slug) {
        Map<String, String> map = new HashMap<>();
        map.put("search", slug);
        return mRestInterface.getCategoryForSlug(map);
    }

    public Call<List<Taxonomy>> getCategories() {
        return mRestInterface.getCategories();
    }

    public void getCategoriesForParent(long parentId, WordPressRestResponse<List<Taxonomy>> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put("parent", parentId);

        doRetrofitCall(mRestInterface.getCategories(map), callback);
    }

    public Call<List<Taxonomy>> getCategoriesForParent(long parentId) {
        Map<String, Object> map = new HashMap<>();
        map.put("parent", parentId);
        return mRestInterface.getCategories(map);
    }

    /* META */

    public void createPostMeta(long postId, Meta meta, WordPressRestResponse<Meta> callback) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", meta.getKey());
        map.put("value", meta.getValue());

        doRetrofitCall(mRestInterface.createPostMeta(postId, map), callback);
    }

    public Call<Meta> createPostMeta(long postId, Meta meta) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", meta.getKey());
        map.put("value", meta.getValue());

        return mRestInterface.createPostMeta(postId, map);
    }

    public Call<Meta> updatePostMeta(long postId, Meta meta) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", meta.getKey());
        map.put("value", meta.getValue());

        return mRestInterface.updatePostMeta(postId, meta.getId(), map);
    }

    public Call<List<Meta>> getPostMetas(long postId) {
        return mRestInterface.getPostMeta(postId);
    }

    public Call<Meta> deletePostMeta(long postId, long metaId) {
        return mRestInterface.deletePostMeta(postId, metaId);
    }

    /* OTHER */

    public void getPostCounts(WordPressRestResponse<PostCount> callback) {
        doRetrofitCall(mRestInterface.getPostCounts(), callback);
    }

    public Call<PostCount> getPostCounts() {
        return mRestInterface.getPostCounts();
    }

            /* Comment*/

    public void getComments(WordPressRestResponse<List<Comment>> callback) {
        doRetrofitCall(mRestInterface.getComments(), callback);
    }

    public void getCommentsByPost(long postId, WordPressRestResponse<List<Comment>> callback) {
        doRetrofitCall(mRestInterface.getCommentsByPost(postId), callback);
    }

    public void getCommentsByPost(long postId, int take, int page, WordPressRestResponse<List<Comment>> callback) {
        doRetrofitCall(mRestInterface.getCommentsByPost(postId, take, page), callback);
    }

    public void getRepliesOnComment(long postId, long commentId, WordPressRestResponse<List<Comment>> callback) {
        doRetrofitCall(mRestInterface.getRepliesOnComment(postId, commentId), callback);
    }

    public void createComment(CustomComment cmt, WordPressRestResponse<Comment> callback) {
        doRetrofitCall(mRestInterface.createComment(cmt.author,cmt.author_ip,cmt.author_url,cmt.author_user_agent,cmt.content,cmt.date,cmt.date_gmt,cmt.parent,cmt.post), callback);
    }

    //like post
    public void setLike(String id,WordPressRestResponse<Like> callback) {
        doRetrofitCall(mRestInterface.setLike("wp_ulike_process",id,"likeThis"), callback);
    }
}

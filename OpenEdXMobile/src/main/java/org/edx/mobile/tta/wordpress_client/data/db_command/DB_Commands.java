package org.edx.mobile.tta.wordpress_client.data.db_command;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import org.edx.mobile.tta.wordpress_client.data.WordPressContract;
import org.edx.mobile.tta.wordpress_client.data.tasks.WpAsyncTask;
import org.edx.mobile.tta.wordpress_client.data.tasks.WpInsertTask;
import org.edx.mobile.tta.wordpress_client.data.tasks.WpQueryCursorSyncTask;
import org.edx.mobile.tta.wordpress_client.data.tasks.WpUpdateTask;
import org.edx.mobile.tta.wordpress_client.data.tasks.callback.WpTaskCallback;
import org.edx.mobile.tta.wordpress_client.data.wp_tables;
import org.edx.mobile.tta.wordpress_client.model.Post;
import org.edx.mobile.tta.wordpress_client.model.Taxonomy;
import org.edx.mobile.tta.wordpress_client.util.SortType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by JARVICE on 05-12-2017.
 */

public class DB_Commands {
    private Context context;
    private static final ExecutorService threadpool = Executors.newFixedThreadPool(3);

    public DB_Commands(Context ctx)
    {
        context=ctx;
    }

    //region Category_command
    public void updateCategoryCache(final List<Taxonomy> taxonomyList)
    {
        //isCategoryExist(taxonomyList.get(0));
        //if item allready exist in db please update it //otherwise go for a new insertion
        for(int i=0;i<taxonomyList.size();i++) {
            if(this.isCategoryExist(taxonomyList.get(i)))
            {
                updateCategory(taxonomyList.get(i));
            }
            else
            {
                addCategory(taxonomyList.get(i));
            }
        }
    }

    private void addCategory(final Taxonomy  taxonomy)
    {
            ContentValues contentValuesForInsert = WordPressContract.Taxonomies.insert(taxonomy.getId(),taxonomy.getTaxonomy(),taxonomy);
            WpInsertTask insertTask = new WpInsertTask(context, wp_tables.TABLE_TEXONOMIES,contentValuesForInsert, new WpTaskCallback<Long>() {
                @Override
                public void onTaskSuccess(Long aLong) {
                }

                @Override
                public void onTaskResultNull() {

                }

                @Override
                public void onTaskCancelled() {

                }

                @Override
                public void onTaskFailure(WpAsyncTask task, String error) {

                }
            });
            insertTask.execute();
    }

    private void updateCategory(Taxonomy taxonomy) {
        String whereClause = WordPressContract.Taxonomies.TYPE + " =? AND " + WordPressContract.Taxonomies.BLOG_ID + " =?";
        String[] whereArgs = new String[]{taxonomy.getTaxonomy(), String.valueOf(taxonomy.getId())};

        StringBuilder stringBuilder=new StringBuilder();
        if (taxonomy.getRoles() != null && taxonomy.getRoles().size() > 0)
        {
            for (String role:taxonomy.getRoles())
            {
                stringBuilder.append(role+",");
            }
        }

        ContentValues contentValues = WordPressContract.Taxonomies.update(taxonomy.getId(),
                taxonomy.getId(), taxonomy.getParent(), taxonomy.getTaxonomy()
                , taxonomy.getName(), taxonomy.getDescription(), taxonomy.getCount(),
                taxonomy.getLink(), taxonomy.getCategory_image(), stringBuilder.toString(),taxonomy.getFormatedfilters());

        WpUpdateTask updateTask = new WpUpdateTask(context, wp_tables.TABLE_TEXONOMIES, contentValues, whereClause, whereArgs, new WpTaskCallback<Integer>() {
            @Override
            public void onTaskSuccess(Integer aLong) {

            }

            @Override
            public void onTaskResultNull() {

            }

            @Override
            public void onTaskCancelled() {

            }

            @Override
            public void onTaskFailure(WpAsyncTask task, String error) {

            }
        });
        updateTask.execute();
    }

    public List<Taxonomy> getCategoryies()
    {
        List<Taxonomy> taxonomies=new ArrayList<>();

        String whereClause=null;
        String[] whereArgs = new String[]{ };
        String orderBy= WordPressContract.Taxonomies.BLOG_ID+" ASC";
        String limit="1000000000000";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_TEXONOMIES, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if(cursor==null) {
                return taxonomies;
            }

            if (cursor.moveToFirst()) {
                do {
                    taxonomies.add(WpDatabaseModelFactory.getTexonomyModel(cursor));
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return taxonomies;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return taxonomies;
        }
        return  taxonomies;
    }

    private boolean isCategoryExist(Taxonomy item)
    {
        boolean isExist=false;
        String whereClause=WordPressContract.Taxonomies.TYPE+" =? AND "+WordPressContract.Taxonomies.BLOG_ID+" =?";
        String[] whereArgs = new String[]{ item.getTaxonomy(),String.valueOf(item.getId())};
        String orderBy= WordPressContract.Taxonomies.BLOG_ID+" ASC";
        String limit="10";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_TEXONOMIES, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor == null || cursor.isClosed()) {
                return isExist;
            }
            else
            {
                if (cursor.moveToFirst()) {
                    Taxonomy category_Item = WpDatabaseModelFactory.getTexonomyModel(cursor);
                    if(category_Item==null)
                        isExist=false;
                    else
                        isExist=true;
                }

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return isExist;
    }

    public Taxonomy getCategoryById(Long cat_id)
    {
        Taxonomy category_item=new Taxonomy();
        String whereClause=WordPressContract.Taxonomies.TYPE+" =? AND "+WordPressContract.Taxonomies.BLOG_ID+" =?";
        String[] whereArgs = new String[]{ "category",String.valueOf(cat_id)};
        String orderBy= WordPressContract.Taxonomies.BLOG_ID+" ASC";
        String limit="10";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_TEXONOMIES, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor==null) {
                return category_item;
            }

            if (cursor.moveToFirst()) {
                do {
                    category_item=WpDatabaseModelFactory.getTexonomyModel(cursor);
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return category_item;
    }


    //endregion

    //region Post_Commands
    public void updatePostCache(final List<Post> postList)
    {
        //if item allready exist in db please update it //otherwise go for a new insertion
        for(int i=0;i<postList.size();i++) {
            if(this.isPostExist(postList.get(i)))
            {
                updatePost(postList.get(i));
            }
            else
            {
                addPost(postList.get(i));
            }
        }
    }

    private void addPost(final Post post)
    {
        ContentValues contentValuesForInsert = WordPressContract.Posts.insert(post.getId(),post.getAuthor(),post);
        WpInsertTask insertTask = new WpInsertTask(context, wp_tables.TABLE_POSTS,contentValuesForInsert, new WpTaskCallback<Long>() {
            @Override
            public void onTaskSuccess(Long aLong) {
            }

            @Override
            public void onTaskResultNull() {

            }

            @Override
            public void onTaskCancelled() {

            }

            @Override
            public void onTaskFailure(WpAsyncTask task, String error) {

            }
        });
        insertTask.execute();
    }

    private void updatePost(Post post)
    {
        String whereClause=WordPressContract.Posts.TYPE+" =? AND "+WordPressContract.Taxonomies.WP_POST_ID+" =?";
        String[] whereArgs = new String[]{ post.getType(),String.valueOf(post.getId())};

        ContentValues contentValues = WordPressContract.Posts.update( post.getId(),post.getId(),post);

        WpUpdateTask updateTask=new WpUpdateTask(context,wp_tables.TABLE_POSTS,contentValues,whereClause,whereArgs, new WpTaskCallback<Integer>() {
            @Override
            public void onTaskSuccess(Integer aLong) {

            }

            @Override
            public void onTaskResultNull() {

            }

            @Override
            public void onTaskCancelled() {

            }

            @Override
            public void onTaskFailure(WpAsyncTask task, String error) {

            }
        });
        updateTask.execute();
    }

    public List<Post> getPosts(long cat_id)
    {
        List<Post> posts=new ArrayList<>();

        String whereClause=WordPressContract.Posts.CATEGORIES+" LIKE ? AND "+WordPressContract.Posts.TYPE+" =?";
        String[] whereArgs = new String[]{ "%#"+cat_id+"#%","post"};
        String orderBy= WordPressContract.Posts.BLOG_ID+" ASC";
        String limit="1000000000000";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_POSTS, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor==null) {
                return posts;
            }

            if (cursor.moveToFirst()) {
                do {
                    posts.add(WpDatabaseModelFactory.getPostModel(cursor));
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return posts;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return posts;
        }
        return  posts;
    }

    private boolean isPostExist(Post post)
    {
        boolean isExist=false;
        String whereClause=WordPressContract.Posts.TYPE+" =? AND "+WordPressContract.Posts.BLOG_ID+" =?";
        String[] whereArgs = new String[]{ post.getType(),String.valueOf(post.getId())};
        String orderBy= WordPressContract.Posts.BLOG_ID+" ASC";
        String limit="10";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_POSTS, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor == null || cursor.isClosed()) {
                return isExist;
            }
            else
            {
                if (cursor.moveToFirst()) {
                    Post post_Item = WpDatabaseModelFactory.getPostModel(cursor);
                    if(post_Item==null)
                        isExist=false;
                    else
                        isExist=true;
                }

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return isExist;
    }

    public Post getByPostID(Long post_ID)
    {
        Post postItem=new Post();
        String whereClause=WordPressContract.Posts.TYPE+" =? AND "+WordPressContract.Posts.BLOG_ID+" =?";
        String[] whereArgs = new String[]{ "post",String.valueOf(post_ID)};
        String orderBy= WordPressContract.Posts.BLOG_ID+" ASC";
        String limit="10";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_POSTS, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor==null) {
                return postItem;
            }

            if (cursor.moveToFirst()) {
                do {
                    postItem=WpDatabaseModelFactory.getPostModel(cursor);
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return postItem;
    }

    public  List<Post> getByPostIDs(String[] ids)
    {
        List<Post> posts =new ArrayList<>();
        String whereClause=WordPressContract.Posts.BLOG_ID + " IN ("+getINQueryParams(ids)+")";
        String[] whereArgs = ids;
        String orderBy= WordPressContract.Posts.BLOG_ID+" ASC";
        String limit="1000000";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_POSTS, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor==null) {
                return posts;
            }

            if (cursor.moveToFirst()) {
                do {
                    posts.add(WpDatabaseModelFactory.getPostModel(cursor));
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return posts;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return posts;
        }
        return  posts;
    }
    //endregion


    public List<Post> getFilteredPosts(long cat_id,ArrayList<String> filter, SortType sortType)
    {
        List<Post> posts=new ArrayList<>();

        //Apply filters
        String whereClause=getwhereClauseQueryMultipleLike(filter,true,true);
        String[] whereArgs =getwhereClauseQueryMultipleLikeArgs(filter,cat_id,"post");

        //apply sort
        String orderBy="";
        if(sortType== SortType.NewestToOldest)
            orderBy=WordPressContract.Posts.DATE_GMT+" DESC";
        else if(sortType==SortType.OldestToNewest)
            orderBy=WordPressContract.Posts.DATE_GMT+" ASC";
        else if(sortType==SortType.Popularity)
            orderBy=WordPressContract.Posts.COMMENT_COUNT+" DESC";

        String limit="1000000000000";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_POSTS, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor==null) {
                return posts;
            }

            if (cursor.moveToFirst()) {
                do {

                    if(!posts.contains(WpDatabaseModelFactory.getPostModel(cursor)))
                    {
                        posts.add(WpDatabaseModelFactory.getPostModel(cursor));
                    }
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
            return posts;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return posts;
        }
        return  posts;
    }

    private String getwhereClauseQueryMultipleLike(ArrayList<String> query_list,boolean iscategoryBase,boolean isOrderByBase) {//"FILTER LIKE ? OR FILTER LIKE ?";

        if(query_list==null)
            query_list=new ArrayList<>();
        String whereClause = "";

        for (int index = 0; index < query_list.size(); index++) {
            if (index != 0)
                whereClause = " " + whereClause + WordPressContract.Posts.FILTER + " LIKE ?";
            else
                whereClause = whereClause + WordPressContract.Posts.FILTER + " LIKE ?";


            if (index != query_list.size() - 1)
                whereClause = whereClause + " OR ";
        }

        if (whereClause.trim().isEmpty() || whereClause.trim().equals(""))
            return WordPressContract.Posts.CATEGORIES + " LIKE ? AND " + WordPressContract.Posts.TYPE + " =?";
        else
            return whereClause.trim() + " AND " + WordPressContract.Posts.CATEGORIES + " LIKE ? AND " + WordPressContract.Posts.TYPE + " =?";

        // return whereClause.trim();
    }

    private String[] getwhereClauseQueryMultipleLikeArgs(ArrayList<String> query_list,long CategoryID,String Type) {
        if(query_list==null)
            query_list=new ArrayList<>();

        Object[] objQuerys = query_list.toArray();

        String[] strQuerys = Arrays.copyOf(objQuerys, objQuerys.length, String[].class);

        String[] whereArgs = new String[strQuerys.length + 2];

        for (int index = 0; index < strQuerys.length; index++) {
            whereArgs[index] = "%##" + String.valueOf(strQuerys[index]) + "##%";
        }

        whereArgs[strQuerys.length] = "%#" + CategoryID + "#%";
        whereArgs[strQuerys.length + 1] = Type;

        return whereArgs;
    }


    public Post getPostBySlug(String slug)
    {
        Post postItem=new Post();
        String whereClause=WordPressContract.Posts.SLUG+" =?";
        String[] whereArgs = new String[]{slug};
        String orderBy= WordPressContract.Posts.BLOG_ID+" ASC";
        String limit="10";

        WpQueryCursorSyncTask task=new WpQueryCursorSyncTask(context, true, wp_tables.TABLE_POSTS, null, whereClause, whereArgs, null, null, orderBy, limit);
        Future future = threadpool.submit(task);

        try {
            Cursor cursor  = (Cursor) future.get();

            if (cursor==null) {
                return postItem;
            }

            if (cursor.moveToFirst()) {
                do {
                    postItem=WpDatabaseModelFactory.getPostModel(cursor);
                } while (cursor.moveToNext());
            }

            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return postItem;
    }



    private String getINQueryParams(String[] ids)
    {
        StringBuilder sb=new StringBuilder();

        if(ids==null || ids.length==0)
            return "";

        for(int i=0;i< ids.length;i++) {

            if (i == ids.length-1)
                sb.append("?");
            else
                sb.append( "?,");
        }

        return sb.toString();
    }
}

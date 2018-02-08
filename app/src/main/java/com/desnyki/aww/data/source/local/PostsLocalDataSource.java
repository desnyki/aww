package com.desnyki.aww.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.desnyki.aww.data.Post;
import com.desnyki.aww.data.source.PostsDataSource;
import com.desnyki.aww.data.source.local.PostsPersistenceContract.PostEntry;
import com.desnyki.aww.util.schedulers.BaseSchedulerProvider;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import java.util.List;


import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by desnyki on 25/01/18.
 */

public class PostsLocalDataSource implements PostsDataSource {

    private static final String TAG = PostsLocalDataSource.class.getSimpleName();

    @Nullable
    private static PostsLocalDataSource INSTANCE;

    @NonNull
    private final BriteDatabase mDatabaseHelper;

    @NonNull
    private Function<Cursor, Post> mPostMapperFunction;

    private PostsLocalDataSource(@NonNull Context context,
                                 @NonNull BaseSchedulerProvider schedulerProvider){
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
        PostsDbHelper dbHelper = new PostsDbHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        mPostMapperFunction = this::getPost;
    }
    public static PostsLocalDataSource getInstance(@NonNull Context context,
                                                   @NonNull BaseSchedulerProvider schedulerProvider){
        if(INSTANCE == null){
            INSTANCE = new PostsLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @NonNull
    private Post getPost(@NonNull Cursor c) {
        String itemId = c.getString(c.getColumnIndexOrThrow(PostEntry.COLUMN_NAME_ENTRY_ID));
        String title = c.getString(c.getColumnIndexOrThrow(PostEntry.COLUMN_NAME_TITLE));
        String url =
                c.getString(c.getColumnIndexOrThrow(PostEntry.COLUMN_NAME_URL));
        int commentsCount = c.getInt(c.getColumnIndexOrThrow(PostEntry.COLUMN_NAME_COMMENT_COUNT));
        int upvotesCount = c.getInt(c.getColumnIndexOrThrow(PostEntry.COLUMN_NAME_COMMENT_COUNT));
        return new Post(title, url, commentsCount, upvotesCount, itemId);
    }

    @NonNull
    @Override
    public Flowable<List<Post>> getPosts() {
        String[] projection = {
                PostEntry.COLUMN_NAME_ENTRY_ID,
                PostEntry.COLUMN_NAME_TITLE,
                PostEntry.COLUMN_NAME_URL,
                PostEntry.COLUMN_NAME_COMMENT_COUNT,
                PostEntry.COLUMN_NAME_UPVOTE_COUNT,
        };
        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), PostEntry.TABLE_NAME);
        return mDatabaseHelper.createQuery(PostEntry.TABLE_NAME, sql)
                .mapToList(mPostMapperFunction)
                .toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public void savePost(@NonNull Post post) {
        checkNotNull(post);
        ContentValues values = toContentValues(post);
        mDatabaseHelper.insert(PostEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    private ContentValues toContentValues(Post post) {
        ContentValues values = new ContentValues();
        values.put(PostEntry.COLUMN_NAME_ENTRY_ID, post.getId());
        values.put(PostEntry.COLUMN_NAME_TITLE, post.getTitle());
        values.put(PostEntry.COLUMN_NAME_URL, post.getUrl());
        values.put(PostEntry.COLUMN_NAME_UPVOTE_COUNT, post.getUpvoteCount());
        values.put(PostEntry.COLUMN_NAME_COMMENT_COUNT, post.getCommentCount());
        return values;
    }

    public Flowable<List<Post>> refreshPosts() {
        return null;
    }

}

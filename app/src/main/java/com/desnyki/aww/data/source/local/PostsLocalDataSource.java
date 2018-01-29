package com.desnyki.aww.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.desnyki.aww.data.Post;
import com.desnyki.aww.data.source.PostsDataSource;
import com.desnyki.aww.data.source.local.PostsPersistenceContract.PostEntry;
import com.desnyki.aww.posts.PostsViewModel;
import com.desnyki.aww.util.schedulers.BaseSchedulerProvider;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

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
    private Func1<Cursor, Post> mPostMapperFunction;

    private PostsLocalDataSource(@NonNull Context context,
                                 @NonNull BaseSchedulerProvider schedulerProvider){
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
        PostsDbHelper dbHelper = new PostsDbHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        mPostMapperFunction = this::getPost;
    }
    public static PostsLocalDataSource getInstance(Context context, BaseSchedulerProvider schedulerProvider){
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
    public Observable<List<Post>> getPosts() {
        String[] projection = {
                PostEntry.COLUMN_NAME_ENTRY_ID,
                PostEntry.COLUMN_NAME_TITLE,
                PostEntry.COLUMN_NAME_URL,
                PostEntry.COLUMN_NAME_COMMENT_COUNT,
                PostEntry.COLUMN_NAME_UPVOTE_COUNT,
        };
        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), PostEntry.TABLE_NAME);
        return mDatabaseHelper.createQuery(PostEntry.TABLE_NAME, sql)
                .mapToList(mPostMapperFunction);
    }

    @NonNull
    @Override
    public Completable savePosts(@NonNull List<Post> posts) {
        checkNotNull(posts);
        Log.d(TAG, "savePosts");
        return Observable.using(mDatabaseHelper::newTransaction,
                transaction -> inTransactionInsert(posts, transaction),
                BriteDatabase.Transaction::end)
                .toCompletable();
    }

    @NonNull
    @Override
    public Completable savePost(@NonNull Post post) {
        checkNotNull(post);
        return Completable.fromAction(() -> {
            ContentValues values = toContentValues(post);
            mDatabaseHelper.insert(PostEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
        });
    }

    @NonNull
    private Observable<List<Post>> inTransactionInsert(@NonNull List<Post> posts,
                                                       @NonNull BriteDatabase.Transaction transaction) {
        checkNotNull(posts);
        checkNotNull(transaction);
        Log.d(TAG, "inTransactionInsert");

        return Observable.from(posts)
                .doOnNext(post -> {
                    ContentValues values = toContentValues(post);
                    mDatabaseHelper.insert(PostEntry.TABLE_NAME, values);
                })
                .doOnCompleted(transaction::markSuccessful)
                .toList();
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
    
    @NonNull
    @Override
    public Completable refreshPosts() {
        return Completable.complete();
    }

}

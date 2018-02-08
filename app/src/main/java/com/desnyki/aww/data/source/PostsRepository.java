package com.desnyki.aww.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import android.util.Log;

import com.desnyki.aww.data.Post;
import com.desnyki.aww.util.schedulers.BaseSchedulerProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by desnyki on 25/01/18.
 */

public class PostsRepository implements PostsDataSource {

    private static final String TAG = PostsRepository.class.getSimpleName();

    @Nullable
    private static PostsRepository INSTANCE = null;

    @NonNull
    private final PostsDataSource mPostsRemoteDataSource;

    @NonNull
    private final PostsDataSource mPostsLocalDataSource;

    @NonNull
    private final BaseSchedulerProvider mBaseSchedulerProvider;

    @VisibleForTesting
    @Nullable
    Map<String, Post> mCachedPosts;

    @VisibleForTesting
    boolean mCacheIsDirty = false;

    private PostsRepository(PostsDataSource postsDataSource,
                            PostsDataSource postsLocalDataSource,
                            BaseSchedulerProvider baseSchedulerProvider){
    
        mPostsRemoteDataSource = postsDataSource;
        mPostsLocalDataSource = postsLocalDataSource;
        mBaseSchedulerProvider = baseSchedulerProvider;
    }
    public static PostsRepository getInstance(@NonNull PostsDataSource postsRemoteDataSource,
                                              @NonNull PostsDataSource postsLocalDataSource,
                                              @NonNull BaseSchedulerProvider schedulerProvider){
        if (INSTANCE == null){
            INSTANCE = new PostsRepository(postsRemoteDataSource,
                    postsLocalDataSource,
                    schedulerProvider);

        }
        return INSTANCE;
    }


    @NonNull
    @Override
    public Flowable<List<Post>> getPosts() {
        if (mCachedPosts != null && !mCacheIsDirty) {
            return Flowable.fromIterable(mCachedPosts.values()).toList().toFlowable();
        } else if (mCachedPosts == null) {
            mCachedPosts = new LinkedHashMap<>();
        }

        Flowable<List<Post>> remotePosts = getAndSaveRemotePosts();

        if (mCacheIsDirty) {
            return remotePosts;
        } else {
            // Query the local storage if available. If not, query the network.
            Flowable<List<Post>> localPosts = getAndCacheLocalPosts();
            return Flowable.concat(localPosts, remotePosts)
                    .filter(posts -> !posts.isEmpty())
                    .firstOrError()
                    .toFlowable();
        }

    }

    private Flowable<List<Post>> getAndCacheLocalPosts() {
        return mPostsLocalDataSource.getPosts()
                .flatMap(posts -> Flowable.fromIterable(posts)
                        .doOnNext(post -> mCachedPosts.put(post.getId(), post))
                        .toList()
                        .toFlowable());
    }

    private Flowable<List<Post>> getAndSaveRemotePosts() {
        return mPostsRemoteDataSource
                .getPosts()
                .flatMap(posts -> Flowable.fromIterable(posts).doOnNext(post -> {
                    mPostsLocalDataSource.savePost(post);
                    mCachedPosts.put(post.getId(), post);
                }).toList().toFlowable())
                .doOnComplete(() -> mCacheIsDirty = false);
    }

    @Override
    public void savePost(@NonNull Post post) {
        checkNotNull(post);
        mPostsLocalDataSource.savePost(post);
        mPostsRemoteDataSource.savePost(post);
    }

    @NonNull
    @Override
    public Flowable<List<Post>> refreshPosts() {
        mCacheIsDirty = true;
        return getPosts();
    }
}

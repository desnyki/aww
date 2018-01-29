package com.desnyki.aww.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.desnyki.aww.data.Post;
import com.desnyki.aww.data.source.local.PostsLocalDataSource;
import com.desnyki.aww.data.source.remote.PostsRemoteDataSource;
import com.desnyki.aww.posts.PostsViewModel;
import com.desnyki.aww.util.schedulers.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.Observable;

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


    //    @NonNull
//    @Override
//    public Observable<List<Post>> getPosts() {
//        return mPostsLocalDataSource.getPosts();
//    }
    @NonNull
    @Override
    public Observable<List<Post>> getPosts() {
        return Observable.just(0)
                .subscribeOn(mBaseSchedulerProvider.io())
                .flatMap(__ -> mPostsRemoteDataSource.getPosts())
                        .subscribeOn(mBaseSchedulerProvider.io());
    }

    @NonNull
    @Override
    public Completable savePosts(@NonNull List<Post> posts) {
        checkNotNull(posts);
        return mPostsLocalDataSource.savePosts(posts)
                .andThen(mPostsRemoteDataSource.savePosts(posts));
    }

    @NonNull
    @Override
    public Completable savePost(@NonNull Post post) {
        checkNotNull(post);
        return mPostsLocalDataSource.savePost(post)
                .andThen(mPostsRemoteDataSource.savePost(post));
    }

    @Override
    public Completable refreshPosts() {
        Log.d(TAG, "refreshPosts");

        return Observable.just(0)
                .subscribeOn(mBaseSchedulerProvider.io())
                .flatMap(__ -> mPostsRemoteDataSource.getPosts()
                        .subscribeOn(mBaseSchedulerProvider.io())
                        .doOnNext(mPostsLocalDataSource::savePosts))
                .toCompletable();
    }
}

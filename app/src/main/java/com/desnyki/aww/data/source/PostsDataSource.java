package com.desnyki.aww.data.source;

import android.support.annotation.NonNull;

import com.desnyki.aww.data.Post;

import java.util.List;

import rx.Completable;
import rx.Observable;

/**
 * Created by desnyki on 25/01/18.
 */

public interface PostsDataSource {

    @NonNull
    Observable<List<Post>> getPosts();

    @NonNull
    Completable savePosts(@NonNull List<Post> posts);

    @NonNull
    Completable savePost(@NonNull Post post);

    @NonNull
    Completable refreshPosts();

}

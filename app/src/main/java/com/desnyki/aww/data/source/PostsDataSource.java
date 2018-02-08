package com.desnyki.aww.data.source;

import android.support.annotation.NonNull;

import com.desnyki.aww.data.Post;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by desnyki on 25/01/18.
 */

public interface PostsDataSource {

    @NonNull
    Flowable<List<Post>> getPosts();

    @NonNull
    Flowable<List<Post>> refreshPosts();

    @NonNull
    void savePost(@NonNull Post post);
}

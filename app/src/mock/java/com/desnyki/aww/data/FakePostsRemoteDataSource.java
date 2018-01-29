/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.desnyki.aww.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.desnyki.aww.data.source.PostsDataSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Completable;
import rx.Observable;

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
public class FakePostsRemoteDataSource implements PostsDataSource {

    private static final Map<String, Post> POSTS_SERVICE_DATA = new LinkedHashMap<>();
    private static FakePostsRemoteDataSource INSTANCE;

    // Prevent direct instantiation.
    private FakePostsRemoteDataSource() {
    }

    public static FakePostsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakePostsRemoteDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<Post>> getPosts() {
        List<Post> values = new ArrayList<>(POSTS_SERVICE_DATA.values());
        return Observable.just(values);
    }

//    @Override
//    public Observable<Post> getPost(@NonNull String postId) {
//        Post post = POSTS_SERVICE_DATA.get(postId);
//        return Observable.just(post);
//    }

    @Override
    public Completable savePost(@NonNull Post post) {
        return Completable.fromAction(() -> POSTS_SERVICE_DATA.put(post.getId(), post));
    }


    @Override
    public Completable savePosts(@NonNull List<Post> posts) {
        return Observable.from(posts)
                .doOnNext(this::savePost)
                .toCompletable();
    }

    public Completable refreshPosts() {
        return Completable.complete();
    }

//    @Override
//    public void deletePost(@NonNull String postId) {
//        POSTS_SERVICE_DATA.remove(postId);
//    }

//    @Override
//    public void deleteAllPosts() {
//        POSTS_SERVICE_DATA.clear();
//    }

    @VisibleForTesting
    public void addPosts(Post... posts) {
        for (Post post : posts) {
            POSTS_SERVICE_DATA.put(post.getId(), post);
        }
    }
}

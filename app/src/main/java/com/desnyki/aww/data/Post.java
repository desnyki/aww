package com.desnyki.aww.data;

/**
 * Created by desnyki on 24/01/18.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import java.util.UUID;

/**
 * Immutable model class for a Post.
 */
public class Post {

    @NonNull
    private final String mId;

    @Nullable
    private final String mTitle;

    @Nullable
    private final String mUrl;

    private final int mCommentCount;

    private final int mUpvoteCount;

    /**
     * Use this constructor to create a new active Post.
     *
     * @param title         title of the post
     * @param url           url to the image of the post
     * @param commentCount  comment count of the post
     * @param upvoteCount   upvote count of the post
     */

    public Post(@Nullable String title, @Nullable String url, int commentCount, int upvoteCount){
        this(title, url, commentCount, upvoteCount, UUID.randomUUID().toString());
    }

    /**
     * Use this constructor to create a new active Post.
     *
     * @param title         title of the post
     * @param url           url to the image of the post
     * @param commentCount  comment count of the post
     * @param upvoteCount   upvote count of the post
     * @param id            id of the post
     */

    public Post(@Nullable String title, @Nullable String url, int commentCount, int upvoteCount, @NonNull String id){
        mId = id;
        mTitle = title;
        mUrl = url;
        mCommentCount = commentCount;
        mUpvoteCount = upvoteCount;
    }

    @NonNull
    public String getId() {
        return mId;
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getUrl() {
        return mUrl;
    }

    @Nullable
    public int getCommentCount() {
        return mCommentCount;
    }

    @Nullable
    public int getUpvoteCount() {
        return mUpvoteCount;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equal(mId, post.mId) &&
                Objects.equal(mTitle, post.mTitle) &&
                Objects.equal(mUrl, post.mUrl) &&
                Objects.equal(mCommentCount, post.mCommentCount) &&
                Objects.equal(mUpvoteCount, post.mUpvoteCount);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mId, mTitle, mUrl, mCommentCount, mUpvoteCount);
    }

    @Override
    public String toString() {
        return "Post with title: " + mTitle + " url: " + mUrl + " comment count: " + mCommentCount + " upvote count: " + mUpvoteCount;
    }
}

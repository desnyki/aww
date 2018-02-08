package com.desnyki.aww.posts;


import com.desnyki.aww.data.Post;

import io.reactivex.functions.Action;


/**
 * A post that should be displayed as an item in a list of posts.
 * Contains the post, the action that should be triggered when taping on the post.
 * */
final class PostItem {

    private Post mPost;

    private Action mOnClickAction;

    public PostItem(Post post,
                    Action onClickAction) {
        mPost = post;
        mOnClickAction = onClickAction;
    }

    public Post getPost() {
        return mPost;
    }

}

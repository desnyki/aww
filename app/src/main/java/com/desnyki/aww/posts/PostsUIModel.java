package com.desnyki.aww.posts;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

/**
 * Created by desnyki on 25/01/18.
 */

public class PostsUIModel {

    private final boolean mIsPostsListVisible;

    private final List<PostItem> mItemList;

    private final boolean mIsNoPostsViewVisible;


    public PostsUIModel(boolean isPostsListVisible, List<PostItem> itemList,
                        boolean isNoPostsViewVisible) {
        mIsPostsListVisible = isPostsListVisible;
        mItemList = itemList;
        mIsNoPostsViewVisible = isNoPostsViewVisible;
    }

    public boolean isPostsListVisible() {
        return mIsPostsListVisible;
    }

    public List<PostItem> getItemList() {
        return mItemList;
    }

}

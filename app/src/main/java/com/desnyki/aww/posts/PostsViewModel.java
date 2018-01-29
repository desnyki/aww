package com.desnyki.aww.posts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;
import android.util.Log;

import com.desnyki.aww.R;
import com.desnyki.aww.data.Post;
import com.desnyki.aww.data.source.PostsRepository;
import com.desnyki.aww.util.schedulers.BaseSchedulerProvider;

import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by desnyki on 25/01/18.
 */

public class PostsViewModel {

    private static final String TAG = PostsViewModel.class.getSimpleName();

    @NonNull
    private final PostsRepository mPostsRepository;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    // using a BehaviourSubject because we are interested in the last object that was emitted before
    // subscribing. Like this we ensure that the loading indicator has the correct visibility.
    private final BehaviorSubject<Boolean> mLoadingIndicatorSubject;


    // using a PublishSubject because we are not interested in the last object that was emitted
    // before subscribing. Like this we avoid displaying the snackbar multiple times
    @NonNull
    private final PublishSubject<Integer> mSnackbarText;

    public PostsViewModel(@NonNull PostsRepository postsRepository,
                          @NonNull BaseSchedulerProvider schedulerProvider) {
        mPostsRepository = postsRepository;
        mSchedulerProvider = checkNotNull(schedulerProvider, "SchedulerProvider cannot be null");
        mLoadingIndicatorSubject = BehaviorSubject.create(false);
        mSnackbarText = PublishSubject.create();
    }

    @NonNull
    public Observable<PostsUIModel> getUIModel() {
        return getPostItems()
                .doOnSubscribe(() -> mLoadingIndicatorSubject.onNext(true))
                .doOnNext(__ -> mLoadingIndicatorSubject.onNext(false))
                .doOnError(__ -> mSnackbarText.onNext(R.string.loading_posts_error))
                .map(this::constructPostsModel);
    }

    @NonNull
    private PostsUIModel constructPostsModel(List<PostItem> posts) {
        Log.d(TAG, "constructPostsModel");

        boolean isPostsListVisible = !posts.isEmpty();
        boolean isNoPostsViewVisible = !isPostsListVisible;

        return new PostsUIModel(isPostsListVisible, posts, isNoPostsViewVisible);
    }
    private Observable<List<PostItem>> getPostItems() {
        Log.d(TAG, "getPostItems");
        return mPostsRepository.getPosts()
                .flatMap( post -> Observable.from(post)
                        .map(this::constructPostItem)
                        .toList());
    }

    @NonNull
    private PostItem constructPostItem(Post post) {
        return new PostItem(post,
                () -> handlePostTaped(post));
    }

    private void handlePostTaped(Post post) {
        Log.d(TAG, "Post Tapped");
    }

    /**
     * Trigger a force update of the posts.
     */
    public Completable forceUpdatePosts() {
        Log.d(TAG, "forceUpdatePosts");
        mLoadingIndicatorSubject.onNext(true);
        return mPostsRepository.refreshPosts()
                .doOnTerminate(() -> mLoadingIndicatorSubject.onNext(false));
    }

    /**
     * Handle the response received on onActivityResult.
     *
     * @param requestCode the request with which the Activity was opened.
     * @param resultCode  the result of the Activity.
     */
    public void handleActivityResult(int requestCode, int resultCode) {

    }


    /**
     * Restore the state of the view based on a bundle.
     *
     * @param bundle the bundle containing the state.
     */
    public void restoreState(@Nullable Bundle bundle) {
    }

    /**
     * @return the state of the view that needs to be saved.
     */
    @NonNull
    public Bundle getStateToSave() {
        Bundle bundle = new Bundle();
        return bundle;
    }

    /**
     * @return a stream of string ids that should be displayed in the snackbar.
     */
    @NonNull
    public Observable<Integer> getSnackbarMessage() {
        return mSnackbarText.asObservable();
    }

    /**
     * @return a stream that emits true if the progress indicator should be displayed, false otherwise.
     */
    @NonNull
    public Observable<Boolean> getLoadingIndicatorVisibility() {
        return mLoadingIndicatorSubject.asObservable();
    }
}

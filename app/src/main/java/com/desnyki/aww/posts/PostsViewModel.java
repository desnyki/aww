package com.desnyki.aww.posts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.desnyki.aww.R;
import com.desnyki.aww.data.Post;
import com.desnyki.aww.data.source.PostsRepository;
import com.desnyki.aww.util.schedulers.BaseSchedulerProvider;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;


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

    private final BehaviorSubject<Boolean> mLoadingIndicatorSubject;

    @NonNull
    private final PublishSubject<Integer> mSnackbarText;

    public PostsViewModel(@NonNull PostsRepository postsRepository,
                          @NonNull BaseSchedulerProvider schedulerProvider) {
        mPostsRepository = postsRepository;
        mSchedulerProvider = checkNotNull(schedulerProvider, "SchedulerProvider cannot be null");
        mLoadingIndicatorSubject = BehaviorSubject.create();
        mSnackbarText = PublishSubject.create();
    }

    @NonNull
    public Flowable<PostsUIModel> getUIModel() {
        return mPostsRepository.getPosts()
                .subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.ui())
                .doOnNext(__ -> mLoadingIndicatorSubject.onNext(true))
                .flatMap(Flowable::fromIterable)
                .map(this::constructPostItem).toList().toFlowable()
                .map(this::constructPostsModel)
                .doOnComplete(() -> mLoadingIndicatorSubject.onNext(false))
                .doOnError(error -> mSnackbarText.onNext(R.string.loading_posts_error));
    }

    @NonNull
    private PostsUIModel constructPostsModel(List<PostItem> posts) {
        boolean isPostsListVisible = !posts.isEmpty();
        boolean isNoPostsViewVisible = !isPostsListVisible;

        return new PostsUIModel(isPostsListVisible, posts, isNoPostsViewVisible);
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
    public Flowable<PostsUIModel> refreshPosts() {
        return mPostsRepository.refreshPosts()
                .subscribeOn(mSchedulerProvider.computation())
                .observeOn(mSchedulerProvider.ui())
                .doOnNext(__ -> mLoadingIndicatorSubject.onNext(true))
                .flatMap(Flowable::fromIterable)
                .map(this::constructPostItem).toList().toFlowable()
                .map(this::constructPostsModel)
                .doOnComplete(() -> mLoadingIndicatorSubject.onNext(false))
                .doOnError(error -> mSnackbarText.onNext(R.string.loading_posts_error));
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
        return mSnackbarText.hide();
    }

    /**
     * @return a stream that emits true if the progress indicator should be displayed, false otherwise.
     */
    @NonNull
    public Observable<Boolean> getLoadingIndicatorVisibility() {
        return mLoadingIndicatorSubject.hide();
    }
}

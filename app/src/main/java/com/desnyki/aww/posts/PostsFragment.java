package com.desnyki.aww.posts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.desnyki.aww.R;
import com.desnyki.aww.data.source.PostsRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.disposables.Disposable;

public class PostsFragment extends Fragment {
    private static final String TAG = PostsFragment.class.getSimpleName();

    private PostsViewModel mViewModel;

    private RecyclerView mRecyclerView;

    private RecyclerView.LayoutManager mLayoutManager;

    private PostsAdapter mListAdapter;

    private LinearLayout mPostsView;

    private CompositeDisposable mSubscription = new CompositeDisposable();

    public PostsFragment() {
        // Requires empty public constructor
    }

    public static PostsFragment newInstance() {
        return new PostsFragment();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mViewModel.handleActivityResult(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.posts_fragment, container, false);

        mRecyclerView = root.findViewById(R.id.posts_list);
        mRecyclerView.setAdapter(mListAdapter);

//        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mListAdapter = new PostsAdapter(new ArrayList<>(0), getContext());
        mRecyclerView.setAdapter(mListAdapter);
        mPostsView = root.findViewById(R.id.posts_ll);

        setupSwipeRefreshLayout(root, mRecyclerView);

        mViewModel = PostsModule.createPostsViewModel(getActivity());
        mViewModel.restoreState(savedInstanceState);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        bindViewModel();
    }

    @Override
    public void onPause() {
        unbindViewModel();
        super.onPause();
    }

    private void bindViewModel() {
        mSubscription = new CompositeDisposable();

        mSubscription.add(mViewModel.getUIModel()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onNext
                        this::updateView,
                        //onError
                        error -> Log.e(TAG, "Error loading posts", error)
                ));

        mSubscription.add(mViewModel.getSnackbarMessage()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onNext
                        this::showSnackbar,
                        //onError
                        error -> Log.d(TAG, "Error showing snackbar", error)
                ));

        mSubscription.add(mViewModel.getLoadingIndicatorVisibility()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onNext
                        this::setLoadingIndicatorVisibility,
                        //onError
                        error -> Log.d(TAG, "Error showing loading indicator", error)
                ));
    }

    private void forceUpdate() {
        Log.d(TAG, "forceUpdate");
        mSubscription.add(mViewModel.forceUpdatePosts()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onCompleted
                        () -> {
//                            Log.d(TAG, "forceUpdateComepleted: " +  mListAdapter.getItemCount());
                            // nothing to do here
                        },
                        //onError
                        error -> Log.d(TAG, "Error refreshing posts", error)
                ));
    }
    private void unbindViewModel() {
        // un subscribing from all the subscriptions to ensure we don't have any memory leaks
        mSubscription.unsubscribe();
    }

    private void updateView(PostsUIModel model) {
        Log.d(TAG, "updateView");
        int postsListVisibility = model.isPostsListVisible() ? View.VISIBLE : View.GONE;
        mPostsView.setVisibility(postsListVisibility);

        if (model.isPostsListVisible()) {
            showPosts(model.getItemList());
        }
    }

    private void setupSwipeRefreshLayout(View root, RecyclerView listView) {
        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
                root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView);

        swipeRefreshLayout.setOnRefreshListener(this::forceUpdate);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(mViewModel.getStateToSave());
        super.onSaveInstanceState(outState);
    }

    private void setLoadingIndicatorVisibility(final boolean isVisible) {
        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl = getView().findViewById(R.id.refresh_layout);
        srl.post(() -> srl.setRefreshing(isVisible));
    }

    private void showPosts(List<PostItem> posts) {
        mListAdapter.replaceItems(posts);
    }

    private void showSnackbar(@StringRes int message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }
}

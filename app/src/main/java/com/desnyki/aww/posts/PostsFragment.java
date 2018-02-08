package com.desnyki.aww.posts;

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

import com.desnyki.aww.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class PostsFragment extends Fragment {
    private static final String TAG = PostsFragment.class.getSimpleName();

    private PostsViewModel mViewModel;

    private RecyclerView mRecyclerView;

    private RecyclerView.LayoutManager mLayoutManager;

    private PostsAdapter mListAdapter;

    private LinearLayout mPostsView;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

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

        mRecyclerView.setHasFixedSize(true);

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
        mCompositeDisposable = new CompositeDisposable();

        mCompositeDisposable.add(mViewModel.getUIModel()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onNext
                        this::updateView,
                        //onError
                        error -> Log.e(TAG, "Error loading posts", error)
                ));

        mCompositeDisposable.add(mViewModel.getSnackbarMessage()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onNext
                        this::showSnackbar,
                        //onError
                        error -> Log.d(TAG, "Error showing snackbar", error)
                ));

        mCompositeDisposable.add(mViewModel.getLoadingIndicatorVisibility()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onNext
                        this::setLoadingIndicatorVisibility,
                        //onError
                        error -> Log.d(TAG, "Error showing loading indicator", error)
                ));
    }

    private void unbindViewModel() {
        mCompositeDisposable.clear();
    }

    private void updateView(PostsUIModel model) {
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

        swipeRefreshLayout.setScrollUpChild(listView);
        swipeRefreshLayout.setOnRefreshListener(this::refreshPosts);
    }
    
    private void refreshPosts() {
        mCompositeDisposable.add(mViewModel.refreshPosts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        //onNext
                        this::updateView,
                        //onError
                        error -> Log.d(TAG, "Error refreshing tasks", error)
                ));
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

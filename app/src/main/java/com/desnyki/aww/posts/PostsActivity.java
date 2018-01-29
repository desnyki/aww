package com.desnyki.aww.posts;

import android.content.Intent;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.desnyki.aww.R;
import com.desnyki.aww.util.ActivityUtils;
import com.desnyki.aww.util.EspressoIdlingResource;

public class PostsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.posts_activity);

        PostsFragment postsFragment =
                (PostsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (postsFragment == null) {
            // Create the fragment
            postsFragment = PostsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), postsFragment, R.id.contentFrame);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // forward the result to the fragment
        Fragment postsFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (postsFragment != null) {
            postsFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @VisibleForTesting
    public IdlingResource getCountingIdlingResource() {
        return EspressoIdlingResource.getIdlingResource();
    }
}

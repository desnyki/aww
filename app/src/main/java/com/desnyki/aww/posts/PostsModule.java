package com.desnyki.aww.posts;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.desnyki.aww.Injection;

/**
 * Created by desnyki on 26/01/18.
 */
/**
 * Enables inversion of control of the ViewModel class for posts screen.
 */
public class PostsModule {

    @NonNull
    public static PostsViewModel createPostsViewModel(@NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();

        return new PostsViewModel(Injection.providePostsRepository(appContext),
                                  Injection.provideSchedulerProvider());
    }
}

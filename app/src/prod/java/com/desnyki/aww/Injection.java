/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.desnyki.aww;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.desnyki.aww.data.source.PostsDataSource;
import com.desnyki.aww.data.source.PostsRepository;
import com.desnyki.aww.data.source.local.PostsLocalDataSource;
import com.desnyki.aww.data.source.remote.PostsRemoteDataSource;
import com.desnyki.aww.util.providers.BaseResourceProvider;
import com.desnyki.aww.util.providers.ResourceProvider;
import com.desnyki.aww.util.schedulers.BaseSchedulerProvider;
import com.desnyki.aww.util.schedulers.SchedulerProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Enables injection of production implementations for
 * {@link PostsDataSource} at compile time.
 */
public class Injection {

    @NonNull
    public static PostsRepository providePostsRepository(@NonNull Context context) {
        checkNotNull(context);
        return PostsRepository.getInstance(PostsRemoteDataSource.getInstance(),
                PostsLocalDataSource.getInstance(context, provideSchedulerProvider()),
                provideSchedulerProvider());
    }

    @NonNull
    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }

    @NonNull
    public static BaseResourceProvider createResourceProvider(@NonNull Context context) {
        return new ResourceProvider(context);
    }

}

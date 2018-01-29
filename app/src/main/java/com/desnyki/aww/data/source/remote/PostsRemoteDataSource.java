package com.desnyki.aww.data.source.remote;

import android.support.annotation.NonNull;
import android.util.Log;

import com.desnyki.aww.data.Post;
import com.desnyki.aww.data.source.PostsDataSource;
import com.desnyki.aww.data.source.PostsRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Created by desnyki on 25/01/18.
 */

public class PostsRemoteDataSource implements PostsDataSource {

    private static final String TAG = PostsRepository.class.getSimpleName();

    private static PostsRemoteDataSource INSTANCE;

    List<Post> posts = new ArrayList<>();

    private PostsRemoteDataSource(){

    }

    public static PostsRemoteDataSource getInstance(){
        if(INSTANCE == null){
            INSTANCE = new PostsRemoteDataSource();
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public Observable<List<Post>> getPosts() {
        if(posts.isEmpty())
            return getPostImageData();
        else
            return Observable.from(posts).toList();
    }

    @NonNull
    @Override
    public Completable savePosts(@NonNull List<Post> posts) {
        return Observable.from(posts)
                .doOnNext(this::savePost)
                .toCompletable();
    }

    @NonNull
    @Override
    public Completable savePost(@NonNull Post post) {
        return Completable.complete();
    }

    @NonNull
    @Override
    public Completable refreshPosts() {
        return Completable.complete();
    }

    OkHttpClient client;

    public Observable<List<Post>> getPostImageData(){
        Log.d(TAG, "getPostImageData");
        final String mURI = "https://www.reddit.com/r/aww/.json";
        String mURLreturn;
        client = new OkHttpClient();

        String id = "";
        String url = "";
        String title = "";
        int upvoteCount = 0;
        int commentCount = 0;
        boolean isLocked = false;

        try {
            mURLreturn = getURL(mURI);

            JSONObject data=new JSONObject(mURLreturn)
                    .getJSONObject("data");
            JSONArray children=data.getJSONArray("children");

            for(int i=0;i<children.length();i++){
                if(children.getJSONObject(i)
                        .getJSONObject("data").has("preview")){
                    JSONArray cur = children.getJSONObject(i)
                            .getJSONObject("data").getJSONObject("preview").getJSONArray("images");
                    for (int j = 0; j < cur.length(); j++) {
                        url = cur.getJSONObject(j).getJSONObject("source").getString("url");
                    }

                    if(children.getJSONObject(i) .getJSONObject("data").has("title")){
                        title = children.getJSONObject(i)
                                .getJSONObject("data").getString("title");
                    }
                    if(children.getJSONObject(i) .getJSONObject("data").has("ups")){
                        upvoteCount = children.getJSONObject(i)
                                .getJSONObject("data").getInt("ups");
                    }
                    if(children.getJSONObject(i) .getJSONObject("data").has("num_comments")){
                        commentCount = children.getJSONObject(i)
                                .getJSONObject("data").getInt("num_comments");
                    }
                    if(children.getJSONObject(i) .getJSONObject("data").has("id")){
                        id = children.getJSONObject(i)
                                .getJSONObject("data").getString("id");
                    }
                    if(children.getJSONObject(i) .getJSONObject("data").has("locked")){
                        isLocked = children.getJSONObject(i)
                                .getJSONObject("data").getBoolean("locked");
                    }
                }
                if(url.length()>0&&!isLocked)
                posts.add(new Post(title,url,commentCount,upvoteCount,id));
            }
            return Observable.from(posts).toList();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    String getURL(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}

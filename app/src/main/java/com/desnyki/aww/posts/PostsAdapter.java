package com.desnyki.aww.posts;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.desnyki.aww.R;
import com.desnyki.aww.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by desnyki on 25/01/18.
 */

public class PostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = PostsViewModel.class.getSimpleName();

    private Context mContext;
    private List<PostItem> mPostItems;
    private final int imgSize;

    public PostsAdapter(List<PostItem> postItems, Context context){
        setItems(postItems);
        mContext = context;
        imgSize = Utils.getScreenWidth(mContext);
    }

    public void setItems(List<PostItem> postItems){
        mPostItems = postItems;
    }

    public void replaceItems(List<PostItem> postItems){
        Log.d(TAG, "replaceItems");
        mPostItems = postItems;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        RecyclerView.ViewHolder vh;
        v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_item, parent, false);
        vh = new ChildHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        View childView = ((ChildHolder)holder).getView();
        TextView title = childView.findViewById(R.id.title);
        title.setText(mPostItems.get(position).getPost().getTitle());
        ImageView postImage = childView.findViewById(R.id.post_image);
        Picasso.with(mContext)
                .load(mPostItems.get(position).getPost().getUrl())
                .resize(imgSize, imgSize)
                .centerCrop()
                .into(postImage);
        TextView likesCount = childView.findViewById(R.id.likes_counter);
        likesCount.setText(mPostItems.get(position).getPost().getUpvoteCount()+"");
    }
    public class ChildHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ChildHolder(View v) {
            super(v);
            mView = v;
        }

        public View getView(){
            return mView;
        }
    }
    @Override
    public int getItemCount() {
        return mPostItems.size();
    }
}


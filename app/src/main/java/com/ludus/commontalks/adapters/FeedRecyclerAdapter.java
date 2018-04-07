package com.ludus.commontalks.adapters;

import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ludus.commontalks.R;
import com.ludus.commontalks.models.Post;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imhwan on 2017. 11. 22..
 */

public class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.FeedRecyclerViewHolder> {

    ArrayList<Post> mPostArray;

    public FeedRecyclerAdapter() {
        mPostArray = new ArrayList<>();
    }

    public Post getItem(int position) {
        return mPostArray.get(position);
    }

    public void addItem(Post item) {
        mPostArray.add(item);
        notifyDataSetChanged();
    }


    @Override
    public FeedRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_recycler_view,parent,false);
        return new FeedRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FeedRecyclerViewHolder holder, int position) {

        Post post = mPostArray.get(position);
        holder.feedContent.setText(post.getPostTxt());
        holder.feedNickname.setText(post.getUser().getUsername());
        if (post.getUser().getProfileUrl() != null) {
            Glide.with(holder.mFeedContentLayout)
                    .load(post.getUser().getProfileUrl())
                    .into(holder.feedProfilePhoto);
        }
    }

    @Override
    public int getItemCount() {
        return mPostArray.size();
    }

    public class FeedRecyclerViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.feedContent)
        TextView feedContent;

        @BindView(R.id.feedProfilePhoto)
        ImageView feedProfilePhoto;

        @BindView(R.id.feedUserNickname)
        TextView feedNickname;

        @BindView(R.id.itemLayout)
        FrameLayout itemLayout;

        @BindView(R.id.feedContentLayout)
        ConstraintLayout mFeedContentLayout;


        public FeedRecyclerViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }


    }

}

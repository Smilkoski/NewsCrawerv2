package com.example.newscrawerv2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newscrawerv2.R;
import com.example.newscrawerv2.Tweet;
import com.example.newscrawerv2.holders.CustomListTweetViewHolder;

import java.util.List;

public class CustomListTweetAdapter extends RecyclerView.Adapter {

    private List<Tweet> tweets;
    private OnTweetListener onTweetListener;

    public CustomListTweetAdapter(List<Tweet> tweets, OnTweetListener onTweetListener) {
        this.tweets = tweets;
        this.onTweetListener = onTweetListener;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tweet_list_layout, viewGroup, false);
        return new CustomListTweetViewHolder(view, onTweetListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((CustomListTweetViewHolder) viewHolder).setTweet(tweets.get(i));

    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public interface OnTweetListener {
        void onTweetClick(int possition);
    }
}

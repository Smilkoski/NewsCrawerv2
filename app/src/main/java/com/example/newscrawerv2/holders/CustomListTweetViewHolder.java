package com.example.newscrawerv2.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newscrawerv2.R;
import com.example.newscrawerv2.Tweet;
import com.example.newscrawerv2.adapters.CustomListTweetAdapter;

public class CustomListTweetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    CustomListTweetAdapter.OnTweetListener onTweetListener;
    private ImageView imageView;
    private TextView name;
    private TextView nickAndTime;
    private TextView content;
    private TextView retAndFav;

    public CustomListTweetViewHolder(@NonNull View itemView, CustomListTweetAdapter.OnTweetListener onTweetListener) {
        super(itemView);

        initViews();

        this.onTweetListener = onTweetListener;
        itemView.setOnClickListener(this);
    }

    void initViews() {
        this.imageView = itemView.findViewById(R.id.imageUser);
        this.name = itemView.findViewById(R.id.name);
        this.nickAndTime = itemView.findViewById(R.id.nickAndTime);
        this.content = itemView.findViewById(R.id.tweetContent);
        this.retAndFav = itemView.findViewById(R.id.retAndFav);
    }


    public void setTweet(Tweet t) {
        if (imageView == null) {
            System.out.println("imageView");
        }
        imageView.setImageBitmap(t.getBitmap());
        name.setText(t.getName());
        nickAndTime.setText(t.getNickname() + "  " + t.getVreme());
        content.setText(String.join(" ", t.getContent()));
        retAndFav.setText(t.getRetweets() + "      " + t.getFavorites());
    }

    @Override
    public void onClick(View v) {
        onTweetListener.onTweetClick(getAdapterPosition());
    }
}

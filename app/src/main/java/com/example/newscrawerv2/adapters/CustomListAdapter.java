package com.example.newscrawerv2.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newscrawerv2.Article;
import com.example.newscrawerv2.R;
import com.example.newscrawerv2.holders.CustomListViewHolder;

import java.util.List;

public class CustomListAdapter extends RecyclerView.Adapter {

    private List<Article> articles;
    private OnArticleListener onArticleListener;

    public CustomListAdapter(List<Article> dataset, OnArticleListener onArticleListener) {
        this.articles = dataset;
        this.onArticleListener = onArticleListener;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_layout, viewGroup, false);
        return new CustomListViewHolder(view, onArticleListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((CustomListViewHolder) viewHolder).setArticle(articles.get(i));

    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public interface OnArticleListener {
        void onArticleClick(int possition);
    }
}

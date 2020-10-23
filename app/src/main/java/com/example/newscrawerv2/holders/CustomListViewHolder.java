package com.example.newscrawerv2.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newscrawerv2.Article;
import com.example.newscrawerv2.R;
import com.example.newscrawerv2.adapters.CustomListAdapter;

import java.util.List;

public class CustomListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    CustomListAdapter.OnArticleListener onArticleListener;
    private ImageView imageView;
    private TextView title;
    private TextView date;
    private TextView content;

    public CustomListViewHolder(@NonNull View itemView, CustomListAdapter.OnArticleListener onArticleListener) {
        super(itemView);

        imageView = itemView.findViewById(R.id.slika);
        title = itemView.findViewById(R.id.title);
        date = itemView.findViewById(R.id.date);
        content = itemView.findViewById(R.id.content);
        this.onArticleListener = onArticleListener;

        itemView.setOnClickListener(this);
    }

    public void setArticle(Article a) {
        title.setText(a.getTitle());
        date.setText(a.getVremeNaObjava());

        List<String> data = a.getContent();
        String tmp = "";
        for (int i = 0; i < 3 && i < data.size(); i++) {
            tmp += data.get(i);
        }
        tmp = tmp.replace("-", "");
        if (tmp.length() > 250) {
            tmp = tmp.substring(0, 250) + "...";
        }
        content.setText(tmp);
        imageView.setImageBitmap(a.getBitmap());
    }

    @Override
    public void onClick(View v) {
        onArticleListener.onArticleClick(getAdapterPosition());
    }
}

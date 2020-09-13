package com.example.newscrawerv2.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newscrawerv2.Article;
import com.example.newscrawerv2.DetailsActivity;
import com.example.newscrawerv2.R;
import com.example.newscrawerv2.adapters.CustomListAdapter;
import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

public class SportFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static ArrayList<Article> articles;
    static CustomListAdapter adapter;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    private static Article scanDerbi(Document doc) {
        try {
            String title = doc.title();
            title = title.replace(" - Дерби", "");

            String vremeNaObjava = doc.getElementsByClass("mvp-post-date updated")
                    .select("time")
                    .attr("datetime");
            Date date = new SimpleDateFormat("yyyy-mm-dd").parse(vremeNaObjava);

            String imgSrc = doc
                    .getElementById("mvp-post-feat-img")
                    .select("meta").first()
                    .attr("content");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            ArrayList<String> list = (ArrayList<String>) doc.getElementById("mvp-content-main")
                    .children()
                    .stream()
                    .filter(e -> e.tag().normalName().equals("p"))
                    .map(Element::ownText)
                    .filter(s -> !s.equals(""))
                    .collect(Collectors.toList());

            return new Article(title, list, date, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (articleFactoryTask != null && !articleFactoryTask.getStatus().name().equals("FINISHED"))
            articleFactoryTask.cancel(true);
        if (scanArticleTask != null && !scanArticleTask.getStatus().name().equals("FINISHED"))
            scanArticleTask.cancel(true);
    }

    @Override
    public void onArticleClick(int possition) {
        Article a = articles.get(possition);
        Gson gson = new Gson();
        String inputString = gson.toJson(a);

        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra("string", inputString);

        startActivity(intent);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initRecycleView();

        if (savedInstanceState == null) {
            new ArticleFactoryTask().execute("https://derbi.mk/posledni-vesti/");
        }

        return inflater.inflate(R.layout.blanc_layout, container, false);
    }

    private void initRecycleView() {
        RecyclerView recyclerView = this.getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        articles = new ArrayList<>();
        adapter = new CustomListAdapter(articles, this);
        recyclerView.setAdapter(adapter);
    }

    private static class ArticleFactoryTask extends AsyncTask<String, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            try {
                return (ArrayList<String>) Jsoup.connect(strings[0]).get()
                        .getElementsByClass("mvp-blog-story-wrap left relative infinite-post")
                        .select("a")
                        .stream().map(e -> e.attr("href"))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            this.cancel(true);
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            strings.forEach(s -> new ScanArticleTask().execute(s));
        }
    }

    private static class ScanArticleTask extends AsyncTask<String, Void, Article> {

        @Override
        protected Article doInBackground(String... urls) {
            try {
                Document document = Jsoup.connect(urls[0]).get();
                return scanDerbi(document);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Article article) {
            insertElementIntoList(article);
        }
    }

}

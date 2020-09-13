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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class OhridNewsFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    private static Article scanOhridNews(Document doc) {
        try {
            String title = doc.title().replace(" - OhridNews", "");

            String vreme = doc.getElementsByClass("post-date-print")
                    .first()
                    .text();
            Date date = new SimpleDateFormat("dd.mm.yy hh:mm").parse(vreme);

            String imgSrc = doc.getElementsByClass("post-content")
                    .select("img")
                    .first()
                    .attr("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            ArrayList<String> list = (ArrayList<String>) doc.getElementsByClass("post-content")
                    .select("p,span,a,strong")
                    .stream().map(Element::ownText)
                    .filter(w -> !w.equals("") && !w.equals(" "))
                    .collect(Collectors.toList());

            return new Article(title, list, date, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (articleFactoryTask != null && !articleFactoryTask.getStatus().name().equals("FINISHED"))
            articleFactoryTask.cancel(true);
        if (scanArticleTask != null && !scanArticleTask.getStatus().name().equals("FINISHED"))
            scanArticleTask.cancel(true);
    }

    private void initRecycleView() {
        RecyclerView recyclerView = this.getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        articles = new ArrayList<>();
        adapter = new CustomListAdapter(articles, this);
        recyclerView.setAdapter(adapter);
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
            generateAllArticles();
        }

        return inflater.inflate(R.layout.blanc_layout, container, false);
    }

    public void generateAllArticles() {

        List<String> urls = Arrays.asList(
                "https://www.ohridnews.com/vesti/vesti-ohrid/",
                "https://www.ohridnews.com/vesti/vesti-ohrid/page/2/"
        );

        urls.forEach(u -> {
            articleFactoryTask = new ArticleFactoryTask();
            articleFactoryTask.execute(u);
        });
    }


    private static class ScanArticleTask extends AsyncTask<String, Void, Article> {

        @Override
        protected Article doInBackground(String... urls) {
            try {
                Document document = Jsoup.connect(urls[0]).get();
                return scanOhridNews(document);

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

    private static class ArticleFactoryTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... strings) {
            try {
                return Jsoup.connect(strings[0]).get()
                        .getElementsByClass("mob-post-inline")
                        .select("a")
                        .stream()
                        .map(w -> w.attr("href"))
                        .distinct()
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            strings.forEach(s -> {
                scanArticleTask = new ScanArticleTask();
                scanArticleTask.execute(s);
            });
        }
    }
}

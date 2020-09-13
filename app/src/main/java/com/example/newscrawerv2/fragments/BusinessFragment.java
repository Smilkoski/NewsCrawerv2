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
import java.util.List;
import java.util.stream.Collectors;

public class BusinessFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    private static Article scanKapital(Document doc) {
        Article article = new Article();
        try {
            String title = doc.title();
            title = title.replace(" | КАПИТАЛ", "");
            article.setTitle(title);
            String vremeNaObjava = doc.getElementsByTag("time")
                    .first().ownText();

            vremeNaObjava = vremeNaObjava.split(" ")[0] + " 08 2020, " + vremeNaObjava.split(" ")[3];
            Date date = new SimpleDateFormat("dd mm yyyy, hh:mm").parse(vremeNaObjava);
            article.setVremeNaObjava(date);

            String imgSrc = doc
                    .getElementsByClass("entry-content")
                    .select("img")
                    .first()
                    .attr("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            article.setBitmap(bitmap);

            doc.getElementsByClass("entry-content")
                    .first()
                    .select("p, h6, strong")
                    .stream()
                    .map(Element::ownText)
                    .filter(s -> !s.equals(""))
                    .filter(s -> !s.equals(" "))
                    .forEach(article::addContent);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return article;
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

    private void initRecycleView() {
        RecyclerView recyclerView = this.getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        articles = new ArrayList<>();
        adapter = new CustomListAdapter(articles, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initRecycleView();

        if (savedInstanceState == null) {
            articleFactoryTask = new ArticleFactoryTask();
            articleFactoryTask.execute("https://kapital.mk/kategorija/balkan-biznis-i-politika/");
        }

        return inflater.inflate(R.layout.blanc_layout, container, false);
    }

    private static class ArticleFactoryTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onCancelled() {
            this.cancel(true);
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            try {
                return Jsoup.connect(strings[0]).get()
                            .getElementsByClass("rest inner padding-bottom-zero")
                            .select("div.single-post-thumb > a")
                            .stream()
                            .map(a -> a.attr("href"))
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


    private static class ScanArticleTask extends AsyncTask<String, Void, Article> {

        @Override
        protected Article doInBackground(String... urls) {
            try {
                Document document = Jsoup.connect(urls[0]).get();
                return scanKapital(document);

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

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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class TopNewsFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    private static Article scanFokus(Document doc) {

        try {
            String title = doc.title();
            title = title.substring(0, title.length() - 8);
            String vremeNaObjava = doc
                    .getElementsByClass("entry-date published")
//                    .getElementsByClass("td-post-date td-post-date-no-dot")
//                    .select("time")
                    .first().text();

            SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
            Date date = formatter.parse(vremeNaObjava);

            String imgSrc = doc
                    .getElementsByClass("post-image")
//                    .getElementsByClass("td-post-featured-image")
                    .select("a")
                    .first().attr("href");
            if (imgSrc.equals("") || imgSrc.equals(" "))
                imgSrc = "https://i.imgflip.com/1rv9w5.jpg";
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            ArrayList<String> list = new ArrayList<>();
            doc
                    .getElementsByClass("inner-post-entry entry-content")
//                    .getElementsByClass("td-post-content")
                    .select("p,span,strong")
                    .stream()
                    .filter(e -> !e.ownText().contains("img") && !e.ownText().equals(""))
                    .forEach(e -> list.add(e.ownText()));

            return new Article(title, list, date, bitmap);
        } catch (ParseException | IOException e) {
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
                "https://fokus.mk/kategorija/aktuelno-2/"
//                "https://fokus.mk/kategorija/aktuelno-2/page/2/"
//                "https://fokus.mk/kategorija/aktuelno-2/page/3/",
//                "https://fokus.mk/kategorija/aktuelno-2/page/4/",
//                "https://fokus.mk/kategorija/aktuelno-2/page/5/"
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
                return scanFokus(document);

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
                List<String> list = Jsoup.connect(strings[0]).get()
//                        .getElementsByClass("penci-entry-title entry-title grid-title")
                        .getElementsByClass("grid-style grid-2-style")
                        .select(".thumbnail")
                        .select("a")
                        .stream().map(e -> e.attr("href"))
                        .distinct()
                        .collect(Collectors.toList());
                System.out.println(list.size());
                return list;
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

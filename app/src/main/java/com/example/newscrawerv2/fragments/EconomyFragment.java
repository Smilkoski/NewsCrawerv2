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
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EconomyFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    private static Article scanSP(Document doc) {

        try {
            String title = doc.title();
            title = title.replace(" - Слободен печат", "");
            String vremeNaObjava = doc
                    .getElementsByClass("entry-meta penci-entry-meta")
                    .select("time")
                    .first()
                    .attr("datetime")
                    .substring(0, 16).replace("T", " ");
            Date date = new SimpleDateFormat("yyyy-mm-dd HH:mm").parse(vremeNaObjava);

            String imgSrc = doc.getElementsByClass("penci-single-featured-img penci-disable-lazy post-image penci-standard-format")
                    .first().attr("style");
            imgSrc = imgSrc.substring(imgSrc.indexOf("(") + 1, imgSrc.lastIndexOf(")"));

            InputStream input = new URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            ArrayList<String> list = new ArrayList<>();

            doc.getElementsByClass("penci-entry-content entry-content")
                    .select("p,span,strong")
                    .stream()
                    .filter(e -> !e.ownText().contains("img") && !e.ownText().equals(""))
                    .map(Element::ownText)
                    .forEach(list::add);

            return new Article(title, list, date, bitmap);
        } catch (ParseException | IOException e) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initRecycleView();

        if (savedInstanceState == null) {
            generateAllArticles();
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

    @Override
    public void onArticleClick(int possition) {
        Article a = articles.get(possition);

        Gson gson = new Gson();
        String inputString = gson.toJson(a);

        Intent intent = new Intent(getContext(), DetailsActivity.class);
        intent.putExtra("string", inputString);

        startActivity(intent);
    }

    public void generateAllArticles() {

        List<String> urls = Arrays.asList(
                "https://www.slobodenpecat.mk/category/vesti/ekonomija/"
//                "https://www.slobodenpecat.mk/category/vesti/ekonomija/page/2/",
//                "https://www.slobodenpecat.mk/category/vesti/ekonomija/page/3/"
//                "https://www.slobodenpecat.mk/category/vesti/ekonomija/page/4/",
//                "https://www.slobodenpecat.mk/category/vesti/ekonomija/page/5/"
        );

        urls.forEach(u -> {
            articleFactoryTask = new ArticleFactoryTask();
            articleFactoryTask.execute(u);
        });
    }

    private static class ArticleFactoryTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... strings) {
            try {
                return Jsoup.connect(strings[0]).get()
                        .getElementsByClass("penci-link-post penci-image-holder penci-disable-lazy")
                        .stream().map(e -> e.attr("href"))
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
                return scanSP(document);

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

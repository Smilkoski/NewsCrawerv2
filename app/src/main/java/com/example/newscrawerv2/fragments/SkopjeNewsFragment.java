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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SkopjeNewsFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    private static Article scanSkopjeInfo(Document doc) {
        try {
            String title = doc.title().replace(" | skopjeinfo.mk", "");

            String vreme = doc.getElementsByClass("content__timestamp")
                    .first()
                    .text();
            vreme = vreme.split(" ")[0] + LocalDate.now().getMonth().getValue() + "." + vreme.split(" ")[2];
            Date date = new SimpleDateFormat("dd.mm.yyyy").parse(vreme);

            String imgSrc = doc
                    .getElementsByClass("field-item even")
                    .first()
                    .select("img")
                    .attr("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);

            ArrayList<String> list = new ArrayList<String>();

            doc.getElementsByClass("field-item even")
                    .subList(1, 2)
                    .forEach(w -> {
                        list.addAll(w.select("p")
                                .eachText()
                                .stream()
                                .filter(s -> !s.equals(""))
                                .collect(Collectors.toList()));
                    });
            Article a = new Article(title, list, date, bitmap);
            return a;
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

        generateAllArticles();


        return inflater.inflate(R.layout.blanc_layout, container, false);
    }

    public void generateAllArticles() {

        List<String> urls = Arrays.asList(
                "https://skopjeinfo.mk/skopje",
                "https://skopjeinfo.mk/skopje?page=1"
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
                return scanSkopjeInfo(document);

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
                        .getElementsByClass("list items--separator--border")
                        .select("a")
                        .stream()
                        .map(w -> "https://skopjeinfo.mk" + w.attr("href"))
                        .distinct()
                        .limit(15)
                        .filter(w -> !w.equals("https://skopjeinfo.mk/instagram-foto-na-denot/every-sunset-brings-promise-new-dawn"))
                        .filter(w -> !w.equals("https://skopjeinfo.mk/retro-video-od-skopje-vo-1998-godina"))
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

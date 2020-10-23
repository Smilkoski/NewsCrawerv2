package com.example.newscrawerv2.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newscrawerv2.Article;
import com.example.newscrawerv2.DetailsActivity;
import com.example.newscrawerv2.R;
import com.example.newscrawerv2.adapters.CustomListAdapter;
import com.google.gson.Gson;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
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

public class SerbiaNewsFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    static Translator serbianMacedonianTranslator = null;
    static boolean START_TRANSLATING = false;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    private static Article scanSrbijaDanas(Document doc) {
        Article article = new Article();
        try {
            while (!START_TRANSLATING) {
            }

            String title = doc.title().replace(" | Najnovije vesti - Srbija danas", "");

            serbianMacedonianTranslator.translate(title)
                    .addOnSuccessListener(article::setTitle);

            String vreme = doc.getElementsByClass("article__post-time").text().replace(". • ", " ");
            Date date = new SimpleDateFormat("dd.mm.yyyy hh:mm").parse(vreme);
            article.setVremeNaObjava(date);

            String imgSrc = doc.getElementsByClass("article__main-image")
                    .select("img").first().attr("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            article.setBitmap(bitmap);

            ArrayList<String> list = new ArrayList<>();
            list.add(doc.getElementsByClass("field-article-forspan")
                    .first().text());
            doc.getElementsByClass("article__body clearfix")
                    .select("p")
                    .forEach(w -> list.add(w.ownText()));


            list.forEach(w -> serbianMacedonianTranslator.translate(w)
                    .addOnSuccessListener(article::addContent));

            return article;
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

    void initAndDownloadTranslator() {
        // Create an Serbian-Macedonian translator:
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.CROATIAN)
                .setTargetLanguage(TranslateLanguage.MACEDONIAN)
                .build();
        serbianMacedonianTranslator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        serbianMacedonianTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        v -> START_TRANSLATING = true);

    }

    private void initRecycleView() {
        RecyclerView recyclerView = this.getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        articles = new ArrayList<>();
        adapter = new CustomListAdapter(articles, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initRecycleView();

        generateAllArticles();
        initAndDownloadTranslator();

        return inflater.inflate(R.layout.blanc_layout, container, false);
    }

    public void generateAllArticles() {

        List<String> urls = Arrays.asList(
                "https://www.srbijadanas.com/vesti/info"
        );

        urls.forEach(u -> {
            articleFactoryTask = new ArticleFactoryTask();
            articleFactoryTask.execute(u);
        });
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


    private static class ScanArticleTask extends AsyncTask<String, Void, Article> {

        @Override
        protected Article doInBackground(String... urls) {
            try {
                Document document = Jsoup.connect(urls[0]).get();
                return scanSrbijaDanas(document);

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
                        .getElementsByClass("o-media o-media--type-flag o-media-container__list-item")
                        .select("a")
                        .stream()
                        .map(w -> "https://www.srbijadanas.com" + w.attr("href"))
                        .limit(15)
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

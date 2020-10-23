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

public class AlbaniaNewsFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    static Translator albanianMacedonianTranslator = null;
    static boolean START_TRANSLATING = false;
    static ArticleFactoryTask articleFactoryTask;
    static ScanArticleTask scanArticleTask;

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    private static Article scanOpinion(Document doc) {
        Article article = new Article();
        try {
            if (!START_TRANSLATING) {
                initAndDownloadTranslator();
            }

            String title = doc.title().replace(" - Opinion.al", "");
//             Translation successful.
            albanianMacedonianTranslator.translate(title).addOnSuccessListener(article::setTitle);

            String vreme = doc.getElementsByClass("single_meta block_section")
                    .first()
                    .select("span")
                    .text();
            vreme = vreme.replace("Publikuar në : ", "");
            Date date = new SimpleDateFormat("hh:mm - dd/mm/yy |").parse(vreme);
            article.setVremeNaObjava(date);

            String imgSrc = doc
                    .getElementsByClass("single_article__thumb")
                    .select("img")
                    .attr("src");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            article.setBitmap(bitmap);

            ArrayList<String> list = (ArrayList<String>)
                    doc.getElementsByClass("content_right")
                            .first()
                            .select("p")
                            .eachText();

            list.forEach(w -> albanianMacedonianTranslator.translate(w)
                    .addOnSuccessListener(article::addContent));
            return article;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void initAndDownloadTranslator() {
        // Create an Albanian-Macedonian translator:
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ALBANIAN)
                .setTargetLanguage(TranslateLanguage.MACEDONIAN)
                .build();

        albanianMacedonianTranslator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        albanianMacedonianTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(v -> START_TRANSLATING = true);

        START_TRANSLATING = true;
    }

    private void initRecycleView() {
        RecyclerView recyclerView = this.getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        articles = new ArrayList<>();
        adapter = new CustomListAdapter(articles, this);
        recyclerView.setAdapter(adapter);
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

        generateAllArticles();
        initAndDownloadTranslator();
        return inflater.inflate(R.layout.blanc_layout, container, false);
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
                "https://opinion.al/"
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
                List<String> list = Jsoup.connect(strings[0]).get()
                        .getElementsByClass("block_section news_content hidden-mob")
                        .select("a")
                        .stream()
                        .map(w -> w.attr("href"))
                        .distinct()
                        .collect(Collectors.toList());
                Jsoup.connect(strings[0]).get()
                        .select("#main > div > section.news_section.politike_section.block_section > div > div > div:nth-child(3)")
                        .select("a")
                        .stream()
                        .map(w -> w.attr("href"))
                        .forEach(list::add);
                return list.subList(0, 15);
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
                    }
            );
        }
    }

    private static class ScanArticleTask extends AsyncTask<String, Void, Article> {

        @Override
        protected Article doInBackground(String... urls) {
            try {
                Document document = Jsoup.connect(urls[0]).get();
                return scanOpinion(document);
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

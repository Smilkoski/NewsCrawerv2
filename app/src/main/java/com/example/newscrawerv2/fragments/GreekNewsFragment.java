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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GreekNewsFragment extends Fragment implements CustomListAdapter.OnArticleListener {
    static List<Article> articles;
    static CustomListAdapter adapter;
    static Translator greekMacedonianTranslator = null;
    static boolean START_TRANSLATING = false;
    private static ArticleFactoryTask articleFactoryTask;
    private static ScanArticleTask scanArticleTask;

    public static void insertElementIntoList(Article article) {
        articles.add(article);
        adapter.notifyDataSetChanged();
    }

    static void initAndDownloadTranslator() {
        // Create an Greek-Macedonian translator:
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.GREEK)
                .setTargetLanguage(TranslateLanguage.MACEDONIAN)
                .build();
        greekMacedonianTranslator = Translation.getClient(options);

        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        greekMacedonianTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(v -> START_TRANSLATING = true);

        START_TRANSLATING = true;
    }

    private static Article scanTanea(Document doc) {
        Article article = new Article();
        try {
            if (!START_TRANSLATING) {
                initAndDownloadTranslator();
            }
            String title = doc.title().replace(" - ΤΑ ΝΕΑ", "");
            greekMacedonianTranslator.translate(title)
                    .addOnSuccessListener(article::setTitle);

            String vreme = doc.getElementsByClass("table-cell middle-align")
                    .select("time").attr("title");
            String[] parts = vreme.split(" ");
            vreme = parts[0] + "." + LocalDate.now().getMonth().getValue() + "." + parts[2] + " " + parts[3];
            Date date = new SimpleDateFormat("dd.mm.yyyy, hh:mm").parse(vreme);
            article.setVremeNaObjava(date);

            String imgSrc = doc
                    .getElementsByClass("post_image_l")
                    .first().attr("href");
            InputStream input = new java.net.URL(imgSrc).openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            article.setBitmap(bitmap);

            ArrayList<String> list = (ArrayList<String>)
                    doc.getElementsByClass("main-content pos-rel article-wrapper")
                            .select("p").eachText();

            list.forEach(w -> greekMacedonianTranslator.translate(w)
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initRecycleView();

        if (savedInstanceState == null) {
            generateAllArticles();
        }
        return inflater.inflate(R.layout.blanc_layout, container, false);
    }

    public void generateAllArticles() {

        List<String> urls = Arrays.asList(
                "https://www.tanea.gr/allnews/"
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
                        .getElementsByClass("daylist")
                        .select("a")
                        .stream()
                        .map(w -> w.attr("href"))
                        .distinct()
                        .limit(20)
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
                return scanTanea(document);

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

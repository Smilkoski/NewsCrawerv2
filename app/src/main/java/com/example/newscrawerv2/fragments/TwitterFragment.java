package com.example.newscrawerv2.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newscrawerv2.R;
import com.example.newscrawerv2.Tweet;
import com.example.newscrawerv2.adapters.CustomListTweetAdapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TwitterFragment extends Fragment implements CustomListTweetAdapter.OnTweetListener {
    private static List<Tweet> tweets;
    private static CustomListTweetAdapter adapter;
    private static TweetFactoryTask tweetFactoryTask;

    public static void insertElementIntoList(List<Tweet> tvitovi) {
        tweets.addAll(tvitovi);
        adapter.notifyDataSetChanged();
    }

    static Tweet scanTweet(Element e) {

        String name = e.getElementsByClass("name").first().ownText();
        String nickname = e.getElementsByClass("nick").first().ownText();
        String vreme = e.getElementsByClass("date").first().ownText();
        ArrayList<String> parts = (ArrayList<String>) e.getElementsByClass("content_middle").eachText();
        String imgSrc = e.select("a > span").attr("style");
        String retweets = e.select("div.content > div.content_bottom > span:nth-child(1)").text();
        String favorites = e.select("div.content > div.content_bottom > span:nth-child(2)").text();
        String link = e.getElementsByClass("tw_details").select("a").attr("href");
        imgSrc = imgSrc.substring(imgSrc.indexOf("'"), imgSrc.lastIndexOf("'"))
                .replace("' ", "");
        InputStream input = null;

        try {
            input = new java.net.URL(imgSrc).openStream();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(input);

        return new Tweet(name, nickname, parts, retweets, favorites, bitmap, vreme, link);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tweetFactoryTask != null && !tweetFactoryTask.getStatus().name().equals("FINISHED"))
            tweetFactoryTask.cancel(true);
    }

    private void initRecycleView() {
        RecyclerView recyclerView = this.getActivity().findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        tweets = new ArrayList<>();
        adapter = new CustomListTweetAdapter(tweets, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initRecycleView();

        if (savedInstanceState == null) {
            generateAllTweets();
        }

        return inflater.inflate(R.layout.blanc_layout, container, false);
    }

    public void generateAllTweets() {

        List<String> urls = Arrays.asList(
                "https://time.mk/twitter/2h/1",
                "https://time.mk/twitter/2h/2",
                "https://time.mk/twitter/2h/3",
                "https://time.mk/twitter/2h/4",
                "https://time.mk/twitter/2h/5",
                "https://time.mk/twitter/2h/6",
                "https://time.mk/twitter/2h/7",
                "https://time.mk/twitter/2h/8",
                "https://time.mk/twitter/2h/9"
        );

        urls.forEach(u -> {
            tweetFactoryTask = new TweetFactoryTask();
            tweetFactoryTask.execute(u);
        });
    }

    @Override
    public void onTweetClick(int possition) {
        String url = tweets.get(possition).getLink();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    private static class TweetFactoryTask extends AsyncTask<String, Void, List<Tweet>> {
        @Override
        protected List<Tweet> doInBackground(String... strings) {
            try {
                return Jsoup.connect(strings[0]).get()
                        .getElementsByClass("tweet")
                        .stream()
                        .map(TwitterFragment::scanTweet)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(List<Tweet> tweets) {
            insertElementIntoList(tweets);
        }
    }


}

package com.example.newscrawerv2;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class Tweet {
    private String name;
    private String nickname;
    private List<String> content;
    private String retweets;
    private String favorites;
    private Bitmap bitmap;
    private String vreme;
    private String link;

    public Tweet(String name, String nickname, List<String> content, String retweets, String favorites, Bitmap bitmap, String vreme, String link) {
        this.name = name;
        this.nickname = nickname;
        this.content = content;
        this.retweets = retweets;
        this.favorites = favorites;
        this.bitmap = bitmap;
        this.vreme = vreme;
        this.link = link;
    }

    public Tweet() {
        this.content = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public List<String> getContent() {
        return content;
    }

    public String getRetweets() {
        return retweets;
    }

    public String getFavorites() {
        return favorites;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getVreme() {

        return vreme;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", content=" + content +
                ", retweets=" + retweets +
                ", favorites=" + favorites +
                ", bitmap=" + bitmap +
                ", vreme='" + vreme + '\'' +
                '}';
    }


}

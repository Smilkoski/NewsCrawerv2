package com.example.newscrawerv2;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Article {
    private String title;
    private List<String> content;
    private Date vremeNaObjava;
    private Bitmap bitmap;

    public Article() {
        this.content = new ArrayList<>();
    }

    public Article(String title, ArrayList<String> content, Date vremeNaObjava, Bitmap bitmap) {
        this.title = title;
        this.content = content;
        this.vremeNaObjava = vremeNaObjava;
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getTitle() {
        if (title == null) {
            return "";
        }
        return title.replace("(видео)", "").replace("(ВИДЕО)", "")
                .replace("(Видео)", "");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getContent() {
        return content;
    }

    public void addContent(String content) {
        this.content.add(content + " ");
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getVremeNaObjava() {
        vremeNaObjava.setMonth(8);
        String []parts =  vremeNaObjava.toString().split(" ");

        return parts[2]+"-"+parts[1]+"-"+parts[5];
    }

    public void setVremeNaObjava(Date vremeNaObjava) {
        this.vremeNaObjava = vremeNaObjava;
    }
}

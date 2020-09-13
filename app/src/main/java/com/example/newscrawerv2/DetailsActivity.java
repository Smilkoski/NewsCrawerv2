package com.example.newscrawerv2;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_layout);

        ImageView imageView = findViewById(R.id.imageView);
        TextView title = findViewById(R.id.textView);
        TextView date = findViewById(R.id.textView2);
        TextView content = findViewById(R.id.textView3);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String s = getIntent().getStringExtra("string");

            Gson gson = new Gson();
            Type type = new TypeToken<Article>() {
            }.getType();
            Article article = gson.fromJson(s, type);

            assert article != null;
            imageView.setImageBitmap(article.getBitmap());
            title.setText(article.getTitle());
            date.setText("Објавено на: " + article.getVremeNaObjava());

            List<String> data = article.getContent();
            StringBuilder sb = new StringBuilder();

            for (String d : data) {
                sb.append("     ").append(d).append("\n");
            }

            content.setText(sb.toString());
        }
    }
}

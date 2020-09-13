package com.example.newscrawerv2;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.newscrawerv2.fragments.*;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Топ вести");

        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TopNewsFragment())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getTitle().toString()) {
            case "Топ вести":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TopNewsFragment())
                        .commit();
                toolbar.setTitle("Топ вести");
                break;
            case "Свет":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new WorldNewsFragment())
                        .commit();
                toolbar.setTitle("Свет");
                break;
            case "Економија":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EconomyFragment())
                        .commit();
                toolbar.setTitle("Економија");
                break;
            case "Бизнис":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BusinessFragment())
                        .commit();
                toolbar.setTitle("Бизнис");
                break;
            case "Спорт":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SportFragment())
                        .commit();
                toolbar.setTitle("Спорт");
                break;
            case "Технологија":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TechnologyFragment())
                        .commit();
                toolbar.setTitle("Технологија");
                break;

            case "Скопје":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SkopjeNewsFragment())
                        .commit();
                toolbar.setTitle("Скопје");
                break;
            case "Охрид":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new OhridNewsFragment())
                        .commit();
                toolbar.setTitle("Охрид");
                break;
            case "Битола":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BitolaNewsFragment())
                        .commit();
                toolbar.setTitle("Битола");
                break;
            case "Прилеп":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PrilepNewsFragment())
                        .commit();
                toolbar.setTitle("Прилеп");
                break;
            case "Гостивар":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new GostivarNewsFragment())
                        .commit();
                toolbar.setTitle("Гостивар");
                break;
            case "Струмица":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StrumicaFragment())
                        .commit();
                toolbar.setTitle("Струмица");
                break;

            case "Србија":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SerbiaNewsFragment())
                        .commit();
                toolbar.setTitle("Србија");
                break;
            case "Грција":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new GreekNewsFragment())
                        .commit();
                toolbar.setTitle("Грција");
                break;
            case "Албанија":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AlbaniaNewsFragment())
                        .commit();
                toolbar.setTitle("Албанија");
                break;
            case "Бугарија":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new BulgariaNewsFragment())
                        .commit();
                toolbar.setTitle("Бугарија");
                break;
            case "Топ Твитови":
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TwitterFragment())
                        .commit();
                toolbar.setTitle("Топ Твитови");
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

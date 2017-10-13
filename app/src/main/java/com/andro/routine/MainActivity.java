package com.andro.routine;

import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.andro.routine.news.NewsFragment;
import com.andro.routine.tasks.TasksFragment;
import com.andro.routine.weather.WeatherFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                FragmentTransaction fragmentTransaction;
                switch (item.getItemId()) {
                    case R.id.navigation_weather:
                        //revealBackground();
                        fragment = new WeatherFragment();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content, fragment).commit();
                        return true;
                    case R.id.navigation_news:
                        //revealBackground();
                        fragment = new NewsFragment();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content, fragment).commit();
                        return true;
                    case R.id.navigation_tasks:
                        //revealBackground();
                        fragment = new TasksFragment();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.content, fragment).commit();
                        return true;
                }
                return false;
            }
        });
        navigation.setSelectedItemId(R.id.navigation_weather);
    }

    public void revealBackground() {
        View myView = findViewById(R.id.revealFrame);
        int cx = myView.getWidth() / 2;
        int cy = myView.getHeight() / 2;
        float finalRadius = (float) Math.hypot(cx, cy);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Animator anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
            anim.start();
        }
        myView.setVisibility(View.VISIBLE);
    }
}

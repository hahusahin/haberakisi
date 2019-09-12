package com.haberinadresi.androidapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.haberinadresi.androidapp.R;
import com.haberinadresi.androidapp.adapters.SliderAdapter;

public class IntroActivity extends AppCompatActivity {

    private ViewPager slideViewPager;
    private LinearLayout dotLayout;
    private SliderAdapter sliderAdapter;
    private TextView[] dots;
    private Button nextButton, skipButton;
    private int currentPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Make changes to not show the intro slides again
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.custom_keys), MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(getResources().getString(R.string.is_intro_displayed), true).apply();

        slideViewPager = findViewById(R.id.intro_viewpager);
        dotLayout = findViewById(R.id.dot_layout);
        skipButton = findViewById(R.id.btn_skip);
        nextButton = findViewById(R.id.btn_next);

        sliderAdapter = new SliderAdapter(this);
        slideViewPager.setAdapter(sliderAdapter);

        addDotsIndicator(0);

        slideViewPager.addOnPageChangeListener(viewListener);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If at last page
                if(nextButton.getText().equals(getResources().getString(R.string.intro_finish))){
                    finish();
                }
                // If not at last page
                else {
                    slideViewPager.setCurrentItem(currentPage + 1);
                }
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void addDotsIndicator(int position){

        dots = new TextView[sliderAdapter.getCount()];
        dotLayout.removeAllViews();

        for(int i = 0; i < dots.length; i++){

            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(android.R.color.darker_gray));

            dotLayout.addView(dots[i]);
        }

        if(dots.length > 0){
            dots[position].setTextColor(getResources().getColor(android.R.color.white));
        }

    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            addDotsIndicator(i);
            currentPage = i;

            if(i == dots.length - 1){

                skipButton.setVisibility(View.GONE);
                nextButton.setText(getResources().getString(R.string.intro_finish));

            } else {

                skipButton.setVisibility(View.VISIBLE);
                nextButton.setText(getResources().getString(R.string.intro_next));
            }

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };


}

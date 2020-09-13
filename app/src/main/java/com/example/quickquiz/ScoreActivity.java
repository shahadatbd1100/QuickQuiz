package com.example.quickquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ScoreActivity extends AppCompatActivity {

    private TextView scored,total,status;
    private Button doneBtn;
    private LottieAnimationView pass,failed;
    private LinearLayout linearContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        loadAds();
        scored = findViewById(R.id.score_tv);
        total = findViewById(R.id.total_tv);
        doneBtn = findViewById(R.id.done_btn);
        pass = findViewById(R.id.pass_lottie);
        failed = findViewById(R.id.failed_lottie);
        linearContainer = findViewById(R.id.linearContainer);
        status = findViewById(R.id.textView2);


        int score_data = getIntent().getIntExtra("score",0);
        int total_data = getIntent().getIntExtra("total",0);

        scored.setText("Score : "+score_data);
        total.setText("OUT OF "+total_data);

        if (score_data > (total_data/2)){

            status.setText("PASSED");
            linearContainer.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            pass.setVisibility(View.VISIBLE);
            failed.setVisibility(View.GONE);
        }else {
            status.setText("FAILED!");
            linearContainer.setBackgroundColor(getResources().getColor(R.color.red));
            failed.setVisibility(View.VISIBLE);
            pass.setVisibility(View.GONE);
        }


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadAds() {

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
}
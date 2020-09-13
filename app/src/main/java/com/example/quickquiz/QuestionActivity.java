package com.example.quickquiz;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quickquiz.Model.QuestionModel;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    public static final String FILE_NAME = "QUIZZER";
    public static final String KEY_NAME = "QUESTIONS";

    private TextView question,numberIndicator;
    private FloatingActionButton bookmarkBtn;
    private LinearLayout optionContainer;
    private Button shareBtn,nextBtn;
    List<QuestionModel> list;


    private InterstitialAd mInterstitialAd;

    List<QuestionModel> bookmarkList;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    private  int matchedQuestionPosition;

    private int count = 0;
    private int position = 0;
    private int score = 0;
    private String category;
    private int setNo;
    private Dialog loadingDialog;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadAds();

        loadingDialog = new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingDialog.setCanceledOnTouchOutside(false);

        question = findViewById(R.id.question);
        numberIndicator = findViewById(R.id.number_indicator);
        bookmarkBtn = findViewById(R.id.book_mark_btn);
        optionContainer = findViewById(R.id.option_container);
        shareBtn = findViewById(R.id.share_btn);
        nextBtn = findViewById(R.id.next_btn);


        preferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();

        getBookmarks();

        category = getIntent().getStringExtra("category");
        setNo = getIntent().getIntExtra("setNo",1);

        list = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (modelMatch()){
                    bookmarkList.remove(matchedQuestionPosition);
                    bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.bookmark_border));
                }else {
                    bookmarkList.add(list.get(position));
                    bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.bookmarks_black));
                }
            }
        });

        loadingDialog.show();
        myRef.child("SETS").child(category).child("questions").orderByChild("setNo").equalTo(setNo)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            list.add(snapshot.getValue(QuestionModel.class));
                        }

                        if (list.size()>0){

                            for (int i = 0 ; i<4 ; i++){
                                optionContainer.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        checkAnswer((Button) v);
                                    }
                                });
                            }

                            playAnim(question,0,list.get(position).getQuestion());

                            nextBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    nextBtn.setEnabled(false);
                                    nextBtn.setAlpha(0.7f);
                                    enableOption(true);
                                    position++;
                                    if (position== list.size()){


                                        if (mInterstitialAd.isLoaded()){
                                            mInterstitialAd.show();
                                            return;
                                        }


                                        Intent scoreIntent = new Intent(QuestionActivity.this,ScoreActivity.class);
                                        scoreIntent.putExtra("score",score);
                                        scoreIntent.putExtra("total",list.size());
                                        startActivity(scoreIntent);
                                        finish();
                                        return;
                                    }
                                    count = 0;
                                    playAnim(question,0,list.get(position).getQuestion());
                                }
                            });


                            shareBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String body = list.get(position).getQuestion()+"\n"+
                                            list.get(position).getOptionA()+"\n"+
                                            list.get(position).getOptionB()+"\n"+
                                            list.get(position).getOptionC()+"\n"+
                                            list.get(position).getOptionD()
                                            ;
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("text/plain");
                                    intent.putExtra(Intent.EXTRA_SUBJECT,"Quizzer Challenge");
                                    intent.putExtra(Intent.EXTRA_TEXT,body);
                                    startActivity(Intent.createChooser(intent,"Share With"));
                                }
                            });

                        }else {
                            finish();
                            Toast.makeText(QuestionActivity.this, "No Questions Left", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QuestionActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                        finish();
                    }
                });



    }


    private void loadAds() {

        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));
        mInterstitialAd.loadAd(new  AdRequest.Builder().build());


        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

                Intent scoreIntent = new Intent(QuestionActivity.this,ScoreActivity.class);
                scoreIntent.putExtra("score",score);
                scoreIntent.putExtra("total",list.size());
                startActivity(scoreIntent);
                finish();
                return;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
     storeBookmarks();
    }

    private void playAnim(final View view, final int value, final String data){
        view.animate().alpha(value).scaleX(value).scaleY(value).setDuration(500).setStartDelay(100)
                .setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

                if (value == 0 && count<4){

                    String option = " ";

                    if (count == 0){
                        option = list.get(position).getOptionA();
                    }
                    else if (count==1){
                        option = list.get(position).getOptionB();
                    }
                    else if (count==2){
                        option = list.get(position).getOptionC();
                    }
                    else if (count==3){
                        option = list.get(position).getOptionD();
                    }

                    playAnim(optionContainer.getChildAt(count),0,option);
                    count++;
                }

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ///Data Change///




                ///Data Change///
                if (value == 0){
                    try {
                        ((TextView)view).setText(data);
                        numberIndicator.setText(position+1+"/"+list.size());

                        if (modelMatch()){
                            bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.bookmarks_black));
                        }else {
                            bookmarkBtn.setImageDrawable(getResources().getDrawable(R.drawable.bookmark_border));
                        }

                    }catch (Exception e){

                        ((Button)view).setText(data);
                    }
                    view.setTag(data);
                    playAnim(view,1,data);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private  void checkAnswer(Button selectedOption){
        enableOption(false);
        nextBtn.setEnabled(true);
        nextBtn.setAlpha(1f);
        if (selectedOption.getText().toString().equals(list.get(position).getCorrectANS())){
            //correct

            score++;

            selectedOption.setBackground(getResources().getDrawable(R.drawable.green_background));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            }else {
                selectedOption.setBackgroundColor(getResources().getColor(R.color.green));
            }
        }else {
            ///incorrect
            selectedOption.setBackground(getResources().getDrawable(R.drawable.red_background));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                selectedOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#ff0000")));
            }else {
                selectedOption.setBackgroundColor(getResources().getColor(R.color.red));
            }

            Button correctOption = (Button) optionContainer.findViewWithTag(list.get(position).getCorrectANS());

            correctOption.setBackground(getResources().getDrawable(R.drawable.green_background));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                correctOption.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
            }else {
                correctOption.setBackgroundColor(getResources().getColor(R.color.green));
            }
        }
    }

    private void enableOption(boolean enable){

        for (int i = 0 ; i<4 ; i++){
            optionContainer.getChildAt(i).setEnabled(enable);
            if (enable){

                optionContainer.getChildAt(i).setBackground(getResources().getDrawable(R.drawable.rounded_borders));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    optionContainer.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#989898")));
                }else {
                    optionContainer.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.normal));
                }
            }
        }
    }

    private void getBookmarks(){
        String json = preferences.getString(KEY_NAME,"");
        Type type = new TypeToken<List<QuestionModel>>(){}.getType();

        bookmarkList = gson.fromJson(json,type);

        if (bookmarkList == null){
            bookmarkList = new ArrayList<>();
        }

    }

    private boolean modelMatch(){
        boolean matched = false;
        int i = 0;
        for (QuestionModel model : bookmarkList){

            if (model.getQuestion().equals(list.get(position).getQuestion())
                    && model.getCorrectANS().equals(list.get(position).getCorrectANS())
                    && model.getSetNo() == list.get(position).getSetNo()){
                matched = true;
                matchedQuestionPosition = i;
            }
            i++;
        }
        return  matched;
    }

    private void storeBookmarks(){
        String json = gson.toJson(bookmarkList);
        editor.putString(KEY_NAME,json);
        editor.commit();
    }
}
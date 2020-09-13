package com.example.quickquiz.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.quickquiz.QuestionActivity;
import com.example.quickquiz.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class GridAdapter extends BaseAdapter {

    private int sets = 0;
    private String category;
    private InterstitialAd interstitialAd;

    public GridAdapter(int sets, String category, InterstitialAd interstitialAd) {
        this.sets = sets;
        this.category = category;
        this.interstitialAd = interstitialAd;
    }

    @Override
    public int getCount() {
        return sets;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final View view;

        if (convertView == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sets_item,parent,false);
        }else {
            view = convertView;
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                interstitialAd.setAdListener(new AdListener(){
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        interstitialAd.loadAd(new AdRequest.Builder().build());
                        Intent intent = new Intent(parent.getContext(), QuestionActivity.class);
                        intent.putExtra("category",category);
                        intent.putExtra("setNo",position+1);
                        parent.getContext().startActivity(intent);
                    }
                });

                if (interstitialAd.isLoaded()){
                    interstitialAd.show();
                    return;
                }

                Intent intent = new Intent(parent.getContext(), QuestionActivity.class);
                intent.putExtra("category",category);
                intent.putExtra("setNo",position+1);
                parent.getContext().startActivity(intent);


            }
        });

        ((TextView)view.findViewById(R.id.textView_item)).setText(String.valueOf(position+1));

        return view;
    }
}

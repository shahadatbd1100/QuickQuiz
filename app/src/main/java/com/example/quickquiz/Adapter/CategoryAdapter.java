package com.example.quickquiz.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.quickquiz.Model.CategoryModel;
import com.example.quickquiz.R;
import com.example.quickquiz.SetsActivity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    List<CategoryModel> categoryModels;
    Context context;

    public CategoryAdapter(List<CategoryModel> categoryModels, Context context) {
        this.categoryModels = categoryModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.category_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String image = categoryModels.get(position).getUrl();
        String title = categoryModels.get(position).getName();
        int sets = categoryModels.get(position).getSets();

        holder.setData(image,title,sets);

    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private TextView titleTV;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
             titleTV= itemView.findViewById(R.id.title_tv);
        }

        private void setData(String url, final String title, final int sets){


            Glide.with(context).load(url).into(imageView);
            titleTV.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), SetsActivity.class);
                    intent.putExtra("title",title);
                    intent.putExtra("sets",sets);
                    itemView.getContext().startActivity(intent);
                }
            });


        }

    }
}

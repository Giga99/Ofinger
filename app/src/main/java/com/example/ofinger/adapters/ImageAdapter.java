package com.example.ofinger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ofinger.R;
import com.example.ofinger.models.Image;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private List<Image> images;
    ItemClicked2 activity;

    public interface ItemClicked2{
        void onItemClicked2(int index);
    }

    public ImageAdapter(Context context, List<Image> list){
        images = list;
        activity = (ItemClicked2) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivPref;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPref = itemView.findViewById(R.id.ivPref);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked2(images.indexOf((Image) v.getTag()));
                }
            });
        }
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_image_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(images.get(position));

        Picasso.get().load(images.get(position).getInfo()).into(holder.ivPref);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}

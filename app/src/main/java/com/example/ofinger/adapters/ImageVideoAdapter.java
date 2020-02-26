package com.example.ofinger.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.ofinger.models.Image;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageVideoAdapter extends PagerAdapter {
    private List<Image> images;
    Context context;

    public ImageVideoAdapter(Context context, List<Image> list){
        images = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        VideoView vv = new VideoView(context);
        ImageView iv = new ImageView(context);
        Picasso.get().load(images.get(position).getInfo()).into(iv);
        container.addView(iv);
        return iv;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}

package aplikacija.apl.ofinger.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.squareup.picasso.Picasso;

import java.util.List;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.customDialogs.CustomDialogZoomImage;
import aplikacija.apl.ofinger.models.ImageVideo;

public class ImageVideoAdapter extends PagerAdapter {
    private List<ImageVideo> imageVideos;
    private Context context;
    private Activity activity;

    public ImageVideoAdapter(Activity context, List<ImageVideo> list){
        imageVideos = list;
        this.context = context;
        this.activity = context;
    }

    @Override
    public int getCount() {
        return imageVideos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup container, final int position) {
        final ImageVideo imageVideo = imageVideos.get(position);

        ImageView iv = new ImageView(context);
        Picasso.get().load(imageVideo.getInfo()).into(iv);
        container.addView(iv);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplicationClass.position = position;
                ApplicationClass.currentImageVideo = imageVideo;
                CustomDialogZoomImage customDialogZoomImage = new CustomDialogZoomImage(activity);
                customDialogZoomImage.show();
            }
        });

        return iv;
    }



    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ImageView) object);
    }
}

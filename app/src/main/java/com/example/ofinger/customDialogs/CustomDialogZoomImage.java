package com.example.ofinger.customDialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.squareup.picasso.Picasso;

public class CustomDialogZoomImage extends Dialog {
    public Activity c;
    ImageView image;
    ImageButton ivClose;

    private ScaleGestureDetector sgd;

    private static final float MIN_SCALE_FACTOR = 0.05f;
    private static final float MAX_SCALE_FACTOR = 10f;
    private float scale = 1.0f;

    public CustomDialogZoomImage(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_custom_dialog_zoom_image);

        image = findViewById(R.id.image);
        ivClose = findViewById(R.id.ivClose);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogZoomImage.this.dismiss();
            }
        });

        Picasso.get().load(ApplicationClass.currentImage.getInfo()).into(image);

        sgd = new ScaleGestureDetector(c, new ScaleGestureDetector.SimpleOnScaleGestureListener(){
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scale *= detector.getScaleFactor();
                scale = Math.max(MIN_SCALE_FACTOR, Math.min(scale, MAX_SCALE_FACTOR));
                image.setScaleX(scale);
                image.setScaleY(scale);
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        sgd.onTouchEvent(event);
        return true;
    }
}

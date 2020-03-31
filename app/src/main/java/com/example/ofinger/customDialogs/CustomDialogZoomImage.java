package com.example.ofinger.customDialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

public class CustomDialogZoomImage extends Dialog {
    public Activity c;
    private ImageView image;
    private ImageButton ivClose, deleteImage;

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

        image = findViewById(R.id.imageVideo);
        ivClose = findViewById(R.id.ivClose);
        deleteImage = findViewById(R.id.deleteImage);

        if(!ApplicationClass.currentUserCloth) deleteImage.setVisibility(View.GONE);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomDialogZoomImage.this.dismiss();
            }
        });

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(CustomDialogZoomImage.this.getContext());
                alert.setMessage("Da li ste sigurni da zelite da izbrisete ovu sliku?");

                alert.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!ApplicationClass.lastImage) {
                            if(ApplicationClass.currentActivityZoomImage.equals("clothInfo")) {
                                ApplicationClass.imageVideosClothInfo.remove(ApplicationClass.position);
                                ApplicationClass.adapterAddingClothImageVideo.notifyDataSetChanged();
                                FirebaseStorage.getInstance().getReferenceFromUrl(Uri.parse(ApplicationClass.currentImageVideo.getInfo()).toString()).delete();
                            } else if(ApplicationClass.currentActivityZoomImage.equals("addingCloth")){
                                ApplicationClass.imageVideosAddingCloth.remove(ApplicationClass.position);
                                FirebaseStorage.getInstance().getReferenceFromUrl(ApplicationClass.urls.get(ApplicationClass.position)).delete();
                                ApplicationClass.urls.remove(ApplicationClass.position);
                                ApplicationClass.adapterAddingClothImageVideo.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(CustomDialogZoomImage.this.getContext(), "Ne mozete da izbrisete poslednju sliku odela!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alert.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alert.show();
            }
        });

        Picasso.get().load(ApplicationClass.currentImageVideo.getInfo()).into(image);

        sgd = new ScaleGestureDetector(c, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                scale *= detector.getScaleFactor();
                scale = Math.max(MIN_SCALE_FACTOR, Math.min(scale, MAX_SCALE_FACTOR));
                image.setScaleX(scale);
                image.setScaleY(scale);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                image.setScaleX(1.0f);
                image.setScaleY(1.0f);
                scale = 1.0f;
            }
        });

        setCanceledOnTouchOutside(true);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        sgd.onTouchEvent(event);

        return true;
    }
}

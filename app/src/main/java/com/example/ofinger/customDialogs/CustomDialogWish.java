package com.example.ofinger.customDialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.models.Cloth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomDialogWish extends Dialog {
    public Activity c;

    RecyclerView wishList;
    RecyclerView.Adapter wishAdapter;
    RecyclerView.LayoutManager manager;
    List<Cloth> wishCloths;

    TextView headerWish;

    public CustomDialogWish(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_custom_dialog_wish);

        ApplicationClass.wish = true;

        wishList = findViewById(R.id.wishList);
        wishList.setHasFixedSize(true);
        manager = new LinearLayoutManager(c);
        wishList.setLayoutManager(manager);
        wishCloths = new ArrayList<>();

        headerWish = findViewById(R.id.headerWish);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wishCloths.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String clothID = snapshot.getKey();
                    for(Cloth cloth : ApplicationClass.mainCloths){
                        if(cloth.getObjectId().equals(clothID)) {
                            wishCloths.add(cloth);
                            break;
                        }
                    }
                }

                ApplicationClass.wishCloths = wishCloths;
                wishAdapter = new ClothAdapter(c, wishCloths);
                wishList.setAdapter(wishAdapter);

                if(wishCloths.size() == 0) headerWish.setText("Vasa korpa je prazna!");
                else headerWish.setText("Vasa korpa:");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        ApplicationClass.wish = false;
    }
}

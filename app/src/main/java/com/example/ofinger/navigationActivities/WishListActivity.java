package com.example.ofinger.navigationActivities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.models.Cloth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WishListActivity extends AppCompatActivity implements ClothAdapter.ItemClicked {
    RecyclerView wishList;
    RecyclerView.Adapter wishAdapter;
    RecyclerView.LayoutManager manager;
    List<Cloth> wishCloths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Vasa korpa:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        wishList = findViewById(R.id.wishList);
        wishList.setHasFixedSize(true);
        manager = new LinearLayoutManager(this);
        wishList.setLayoutManager(manager);
        wishCloths = new ArrayList<>();

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
                wishAdapter = new ClothAdapter(WishListActivity.this, wishCloths);
                wishList.setAdapter(wishAdapter);

                if(wishCloths.size() == 0) getSupportActionBar().setTitle("Vasa korpa je prazna!");
                else getSupportActionBar().setTitle("Vasa korpa:");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkTypingStatus (String typing){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
    }

    private void status(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        status("online");
        checkTypingStatus("noOne");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }

    @Override
    public void onItemClicked(int index) {
        int i = 0;
        for(Cloth cloth : ApplicationClass.mainCloths){
            if(cloth.getObjectId().equals(ApplicationClass.wishCloths.get(index).getObjectId())) break;
            i++;
        }

        Intent intent = new Intent(WishListActivity.this, ClothInfo.class);
        intent.putExtra("index", i);
        startActivity(intent);
    }
}

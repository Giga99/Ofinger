package aplikacija.apl.ofinger.navigationActivities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.adapters.ClothAdapter;
import aplikacija.apl.ofinger.info.ClothInfo;
import aplikacija.apl.ofinger.models.Cloth;

public class WishListActivity extends AppCompatActivity implements ClothAdapter.ItemClicked {
    RecyclerView wishList;
    RecyclerView.Adapter wishAdapter;
    RecyclerView.LayoutManager manager;
    List<Cloth> wishCloths;

    AdView mainAd;

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

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        mainAd = findViewById(R.id.mainAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainAd.loadAd(adRequest);

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
                wishAdapter = new ClothAdapter(WishListActivity.this, wishCloths, true);
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
        Animatoo.animateSlideRight(this);
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

package aplikacija.apl.ofinger.info;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.adapters.ImageVideoAdapter;
import aplikacija.apl.ofinger.adapters.ReviewAdapter;
import aplikacija.apl.ofinger.mainActivities.MainActivity;
import aplikacija.apl.ofinger.messaging.ChatActivity;
import aplikacija.apl.ofinger.models.ImageVideo;
import aplikacija.apl.ofinger.models.Review;
import aplikacija.apl.ofinger.models.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ClothInfo extends AppCompatActivity {
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    TextView tvClothName, tvOwnerName, tvClothDescription, tvPrice, reviewHeader;
    ImageView ivEdit, ivDelete, ivSold, ivNotSold;
    EditText etClothName, etClothDescription, etPrice;
    ImageView ivSubmit, ivReview;
    LinearLayout editField, userRating;
    RatingBar ratingBarUser, ratingBarOverall;
    EditText etReview;

    TextView header3;

    BottomNavigationView bottomNavigationView;

    CircleImageView ivProfileImage;
    TextView tvUsername;

    ViewPager viewpagerImages;
    TabLayout tabLayout;

    RecyclerView clothReviewsList;
    LinearLayoutManager linearLayoutManager;
    ReviewAdapter adapterReview;
    List<Review> reviews;

    boolean edit = false;
    int INDEX;
    float starsOverall;

    AdView mainAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_info);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUsername);

        ApplicationClass.currentActivityZoomImage = "clothInfo";

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        mainAd = findViewById(R.id.mainAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainAd.loadAd(adRequest);

        /**
         * Ispisivanje imena u zavisnosti da li je gost ili ne
         */
        if(ApplicationClass.currentUser.isAnonymous()){
            tvUsername.setText("Guest");
            ivProfileImage.setImageResource(R.drawable.profimage);
        } else {
            ApplicationClass.currentUserReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!isDestroyed()) {
                        User user = dataSnapshot.getValue(User.class);

                        tvUsername.setText(user.getUsername());
                        if (user.getImageURL().equals("default")) {
                            ivProfileImage.setImageResource(R.drawable.profimage);
                        } else {
                            Glide.with(ClothInfo.this).load(user.getImageURL()).into(ivProfileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        tvClothName = findViewById(R.id.tvClothName);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvClothDescription = findViewById(R.id.tvClothDescription);
        tvPrice = findViewById(R.id.tvPrice);

        ivEdit = findViewById(R.id.ivEdit);
        ivDelete = findViewById(R.id.ivDelete);
        ivSold = findViewById(R.id.ivSold);
        ivNotSold = findViewById(R.id.ivUncheckedSold);
        etClothName = findViewById(R.id.etClothName);
        etClothDescription = findViewById(R.id.etClothDescription);
        etPrice = findViewById(R.id.etPrice);

        viewpagerImages = findViewById(R.id.viewpagerImages);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewpagerImages, true);

        userRating = findViewById(R.id.userRating);
        ratingBarUser = findViewById(R.id.ratingBarUser);
        ratingBarOverall = findViewById(R.id.ratingBarOverall);
        reviewHeader = findViewById(R.id.reviewHeader);

        ivReview = findViewById(R.id.ivReview);
        etReview = findViewById(R.id.etReview);

        editField = findViewById(R.id.editField);

        ivSubmit = findViewById(R.id.ivSubmit);

        editField.setVisibility(View.GONE);

        INDEX = getIntent().getIntExtra("index", 0);

        clothInfo();

        tvClothName.setText(ApplicationClass.mainCloths.get(INDEX).getName());
        tvOwnerName.setText(ApplicationClass.mainCloths.get(INDEX).getOwnerUsername());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("sr_Latn_RS", "RS"));
        String price = format.format(ApplicationClass.mainCloths.get(INDEX).getPrice());
        tvPrice.setText(price);

        tvClothDescription.setText(ApplicationClass.mainCloths.get(INDEX).getDescription());

        etClothName.setText(ApplicationClass.mainCloths.get(INDEX).getName());
        etClothDescription.setText(ApplicationClass.mainCloths.get(INDEX).getDescription());
        etPrice.setText("" + ApplicationClass.mainCloths.get(INDEX).getPrice());

        reviews = new ArrayList<>();
        clothReviewsList = findViewById(R.id.clothReviewsList);
        linearLayoutManager = new LinearLayoutManager(this);
        clothReviewsList.setLayoutManager(linearLayoutManager);
        adapterReview = new ReviewAdapter(this, reviews, "cloth", INDEX, null);
        clothReviewsList.setAdapter(adapterReview);
        header3 = findViewById(R.id.header3);

        /**
         * Odlazak na profil vlasnika odela
         */
        tvOwnerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClothInfo.this, MainActivity.class);
                intent.putExtra("profileid", ApplicationClass.otherUser.getId());
                intent.putExtra("profile", "yes");
                startActivity(intent);
                ClothInfo.this.finish();
            }
        });

        /**
         * Editovanje odece
         */
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit = !edit;

                if (edit) {
                    editField.setVisibility(View.VISIBLE);
                } else {
                    editField.setVisibility(View.GONE);
                }
            }
        });

        /**
         * Brisanje trenutnog odela
         */
        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(ClothInfo.this);
                alert.setMessage("Da li ste sigurni da zelite da izbrisete ovu odecu?");

                alert.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress(true);
                        tvLoad.setText("Brisanje odece...");

                        /**
                         * Brisanje slika
                         */
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).child("urls");
                        databaseReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.getValue(String.class));
                                    storageReference.delete();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        /**
                         * Brisanje odela sa servera
                         */
                        FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).removeValue();

                        for(int j = 0; j < ApplicationClass.mainCloths.size(); j++){
                            /**
                             * Brisanje iz liste korisnickog odela
                             */
                            if(ApplicationClass.mainCloths.get(INDEX).getObjectId().equals(ApplicationClass.userCloths.get(j).getObjectId())){
                                ApplicationClass.userCloths.remove(j);
                                break;
                            }
                        }
                        /**
                         * Brisanje iz liste korisnicke odece
                         */
                        ApplicationClass.mainCloths.remove(INDEX);

                        /**
                         * Brisanje utisaka odela
                         */
                        FirebaseDatabase.getInstance().getReference("StarsCloth").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).exists())
                                    dataSnapshot.child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).getRef().removeValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        setResult(RESULT_OK);
                        ClothInfo.this.finish();
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

        /**
         * Prihvatanja da se promeni odelo
         */
        ivSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Mora sve da bude popunjeno kao i da opis ima najmanje 30 karaktera
                 */
                if(etClothName.getText().toString().isEmpty() || etPrice.getText().toString().isEmpty() || etClothDescription.getText().toString().isEmpty()){
                    Toast.makeText(ClothInfo.this, "Unesite sve podatke!", Toast.LENGTH_SHORT).show();
                } else if(etClothDescription.getText().toString().length() < 30){
                    Toast.makeText(ClothInfo.this, "Opis mora imati vise od 30 karaktera!", Toast.LENGTH_SHORT).show();
                } else {
                    ApplicationClass.mainCloths.get(INDEX).setName(etClothName.getText().toString().trim());
                    ApplicationClass.mainCloths.get(INDEX).setDescription(etClothDescription.getText().toString().trim());
                    ApplicationClass.mainCloths.get(INDEX).setPrice(Long.parseLong(etPrice.getText().toString().trim()));

                    showProgress(true);
                    tvLoad.setText("Menjanje podataka o odeci...");

                    final String name = etClothName.getText().toString().trim();
                    final String description = etClothDescription.getText().toString().trim();
                    final String price = etPrice.getText().toString().trim();

                    /**
                     * Izmene odela na serveru
                     */
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("name", name);
                    hashMap.put("description", description);
                    hashMap.put("price", price);
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.userCloths.get(INDEX).getObjectId());
                    reference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            editField.setVisibility(View.GONE);

                            tvClothName.setText(name);
                            tvPrice.setText(description);
                            tvClothDescription.setText(price);

                            edit = !edit;

                            showProgress(false);
                        }
                    });
                }
            }
        });

        /**
         * Premestanje u sekciju neprodatih stvari
         */
        ivSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(ClothInfo.this);
                dialog.setTitle("Jeste sigurni da zelite da prebacite odelo u sekciju neprodatih stvari?");

                dialog.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress(true);
                        tvLoad.setText("Prebacivanje odece...");
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sold", false);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId());
                        databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(ClothInfo.this, MainActivity.class);
                                intent.putExtra("profile", "yes");
                                intent.putExtra("profileid", ApplicationClass.currentUser.getUid());
                                startActivity(intent);
                                ClothInfo.this.finish();
                            }
                        });
                    }
                });

                dialog.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialog.show();
            }
        });

        /**
         * Premestanje u sekciju prodatih stvari
         */
        ivNotSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(ClothInfo.this);
                dialog.setTitle("Jeste sigurni da zelite da prebacite odelo u sekciju prodatih stvari?");

                dialog.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress(true);
                        tvLoad.setText("Prebacivanje odece...");
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sold", true);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId());
                        databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(ClothInfo.this, MainActivity.class);
                                intent.putExtra("profile", "yes");
                                intent.putExtra("profileid", ApplicationClass.currentUser.getUid());
                                startActivity(intent);
                                ClothInfo.this.finish();
                            }
                        });
                    }
                });

                dialog.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                dialog.show();
            }
        });

        calculateUser();

        /**
         * Ocenjivanje zvezdicama
         */
        ratingBarUser.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingBarUser.setRating(rating);
            }
        });

        /**
         * Ocenjivanje
         */
        ivReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress(true);
                tvLoad.setText("Ocenjivanje odece...");
                Review review = new Review();
                review.setStars(ratingBarUser.getRating());
                review.setText(etReview.getText().toString());
                review.setUserId(ApplicationClass.currentUser.getUid());

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId());
                String id = databaseReference.push().getKey();

                review.setReviewId(id);

                databaseReference.child(id).setValue(review).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                showProgress(false);
                                Toast.makeText(ClothInfo.this, "Uspesno ocenjeno!", Toast.LENGTH_SHORT).show();
                                ratingBarUser.setRating(0);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showProgress(false);
                                Toast.makeText(ClothInfo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

        calculateOverall();

        isWish();
    }

    private void clothInfo() {
        /**
         * Utvrdjivanje da li je vlasnik odela trenutni korisnik ili neki drugi
         */
        ApplicationClass.allUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(ApplicationClass.mainCloths.get(INDEX).getOwnerID().equals(user.getId())){
                        ApplicationClass.otherUser = user;
                        if(!ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())){
                            ApplicationClass.currentUserCloth = false;
                            ivEdit.setVisibility(View.GONE);
                            ivDelete.setVisibility(View.GONE);
                            ivSold.setVisibility(View.GONE);
                            ivNotSold.setVisibility(View.GONE);
                            userRating.setVisibility(View.VISIBLE);
                            bottomNavigationView.setVisibility(View.VISIBLE);
                        } else {
                            ApplicationClass.currentUserCloth = true;
                            bottomNavigationView.setVisibility(View.GONE);
                            if(ApplicationClass.mainCloths.get(INDEX).isSold()) ivSold.setVisibility(View.VISIBLE);
                            else ivNotSold.setVisibility(View.VISIBLE);
                            userRating.setVisibility(View.GONE);
                            ivEdit.setVisibility(View.VISIBLE);
                            ivDelete.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ClothInfo.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Dodavanje svih slika koji su za izabrano odelo u listu
         */
        ApplicationClass.imageVideosClothInfo = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).child("urls");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ImageVideo imageVideo = new ImageVideo();
                    imageVideo.setInfo(snapshot.getValue().toString());
                    imageVideo.setType("image");
                    ApplicationClass.imageVideosClothInfo.add(imageVideo);
                }

                ApplicationClass.lastImage = ApplicationClass.imageVideosClothInfo.size() <= 1;

                ApplicationClass.adapterClothInfoImageVideo = new ImageVideoAdapter(ClothInfo.this, ApplicationClass.imageVideosClothInfo);
                viewpagerImages.setAdapter(ApplicationClass.adapterClothInfoImageVideo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Pravljenje liste utisaka
         */
        DatabaseReference databaseReferenceReviews = FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId());
        databaseReferenceReviews.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reviews.clear();
                boolean myReview = false;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Review review = snapshot.getValue(Review.class);
                    if(review.getUserId().equals(ApplicationClass.currentUser.getUid())) myReview = true;
                    reviews.add(review);
                }

                if(myReview){
                    etReview.setVisibility(View.GONE);
                    ivReview.setVisibility(View.GONE);
                    userRating.setVisibility(View.GONE);
                    reviewHeader.setText("Hvala na ocenjivanju korisnika!");
                } else {
                    etReview.setVisibility(View.VISIBLE);
                    ivReview.setVisibility(View.VISIBLE);
                    userRating.setVisibility(View.VISIBLE);
                    reviewHeader.setText("Ocenite ovog korisnika:");
                }

                adapterReview.notifyDataSetChanged();

                if(reviews.size() == 0){
                    header3.setVisibility(View.GONE);
                    clothReviewsList.setVisibility(View.GONE);
                } else {
                    header3.setVisibility(View.VISIBLE);
                    clothReviewsList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isWish() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).exists()){
                    Drawable img = ClothInfo.this.getResources().getDrawable( R.drawable.wishlist );
                    bottomNavigationView.getMenu().getItem(1).setIcon(img);
                    bottomNavigationView.getMenu().getItem(1).setTitle("wish");
                } else {
                    Drawable img = ClothInfo.this.getResources().getDrawable( R.drawable.notwishlist );
                    bottomNavigationView.getMenu().getItem(1).setIcon(img);
                    bottomNavigationView.getMenu().getItem(1).setTitle("notwish");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Provera da li je ocenjeno
     */
    private void calculateUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Review review = snapshot.getValue(Review.class);

                    if(review.getUserId().equals(ApplicationClass.currentUser.getUid())){
                        etReview.setVisibility(View.GONE);
                        ivReview.setVisibility(View.GONE);
                        ratingBarUser.setVisibility(View.GONE);
                        reviewHeader.setText("Hvala na ocenjivanju odece!");
                    } else {
                        etReview.setVisibility(View.VISIBLE);
                        ivReview.setVisibility(View.VISIBLE);
                        ratingBarUser.setVisibility(View.VISIBLE);
                        reviewHeader.setText("Ocenite ovo odelo:");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Racunanje prosecne ocene
     */
    private void calculateOverall(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(INDEX).getObjectId());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float sum = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Review review = snapshot.getValue(Review.class);
                    sum += review.getStars();
                }
                starsOverall = sum / (float) dataSnapshot.getChildrenCount();

                ratingBarOverall.setRating(starsOverall);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){

                /**
                 * Odlazak na profil vlasnika odela
                 */
                case R.id.nav_profile_owner:
                    Intent intent = new Intent(ClothInfo.this, MainActivity.class);
                    intent.putExtra("profileid", ApplicationClass.otherUser.getId());
                    intent.putExtra("profile", "yes");
                    startActivity(intent);
                    ClothInfo.this.finish();
                    break;

                /**
                 * Odlazak u poruke sa vlasnikom odela
                 */
                case R.id.nav_chat_owner:
                    Intent intent2 = new Intent(ClothInfo.this, ChatActivity.class);
                    intent2.putExtra("userId", ApplicationClass.otherUser.getId());
                    startActivity(intent2);
                    break;

                case R.id.nav_wish:
                    if (item.getTitle().toString().equals("wish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).removeValue();
                    } else if (item.getTitle().toString().equals("notwish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(ApplicationClass.mainCloths.get(INDEX).getObjectId()).setValue(true);
                    }
                    break;
            }
            return true;
        }
    };

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

        checkTypingStatus("noOne");
        status("online");
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
        tvLoad.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Povratak na prethodnu aktivnost
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Animatoo.animateSlideDown(this);
        return super.onSupportNavigateUp();
    }
}

package com.example.ofinger.mainActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.adapters.ReviewAdapter;
import com.example.ofinger.adapters.UserAdapter;
import com.example.ofinger.messaging.ChatActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Review;
import com.example.ofinger.models.User;
import com.example.ofinger.notifications.Data;
import com.example.ofinger.notifications.Sender;
import com.example.ofinger.notifications.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 100;
    String[] cameraPermissions;

    private String TOPIC_POST_NOTIFICATION;

    private CircleImageView ivProfileImageUser;
    private TextView tvEmail, tvUsername, header, header2, reviewHeader, tvBio, numOfCloth, numOfFollowers, numOfReviews, header3;
    private RatingBar userOverallRating, userRating;
    private ImageView ivMore, ivMessage, ivFollowing, ivUserPosts, ivSoldList;
    private LinearLayout listOfFollowers, layoutNumOfReviews;

    private RecyclerView list, listSold;
    private LinearLayoutManager manager, managerSold;
    private ClothAdapter adapter, adapterSold;
    private ConstraintLayout rateUser, linearLayout3;

    private RecyclerView userReviewsList;
    private LinearLayoutManager linearLayoutManager;
    private ReviewAdapter adapterReview;
    private List<Review> reviews;

    private List<User> followers;
    private LinearLayoutManager layoutManagerFollowers;
    private UserAdapter followersAdapter;

    private ArrayList<String> followersID;

    EditText etReview;
    ImageView ivReview;

    private String profileid;

    private DatabaseReference reference, reference2;
    private List<Cloth> userCloths, soldCloths;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;

    float starsOverall;

    private RequestQueue requestQueue;

    private boolean follow, soldList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        requestQueue = Volley.newRequestQueue(ProfileFragment.this.getActivity().getApplicationContext());

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ivProfileImageUser = view.findViewById(R.id.ivProfileImageUser);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvUsername = view.findViewById(R.id.tvUsername);
        ivFollowing = view.findViewById(R.id.ivFollowing);
        ivMessage = view.findViewById(R.id.ivMessage);
        header = view.findViewById(R.id.header);
        header2 = view.findViewById(R.id.header2);
        ivUserPosts = view.findViewById(R.id.ivUserPosts);
        ivSoldList = view.findViewById(R.id.ivSoldList);
        ivMore = view.findViewById(R.id.ivMore);

        userOverallRating = view.findViewById(R.id.userOverallRating);
        userRating = view.findViewById(R.id.userRating);
        rateUser = view.findViewById(R.id.rateUser);
        reviewHeader = view.findViewById(R.id.reviewHeader);
        ivReview = view.findViewById(R.id.ivReview);
        etReview = view.findViewById(R.id.etReview);
        tvBio = view.findViewById(R.id.tvBio);
        numOfCloth = view.findViewById(R.id.numOfCloth);
        numOfFollowers = view.findViewById(R.id.numOfFollowers);
        numOfReviews = view.findViewById(R.id.numOfReviews);
        listOfFollowers = view.findViewById(R.id.listOfFollowers);
        layoutNumOfReviews = view.findViewById(R.id.layoutNumOfReviews);
        linearLayout3 = view.findViewById(R.id.linearLayout3);

        list = view.findViewById(R.id.listProfile);
        list.setHasFixedSize(true);
        manager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        list.setLayoutManager(manager);

        listSold = view.findViewById(R.id.listProfileSold);
        listSold.setHasFixedSize(true);
        managerSold = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        managerSold.setReverseLayout(true);
        managerSold.setStackFromEnd(true);
        listSold.setLayoutManager(managerSold);

        followersID = new ArrayList<>();

        storageReference = FirebaseStorage.getInstance().getReference("profileImages");
        reference = FirebaseDatabase.getInstance().getReference("Cloth");
        userCloths = new ArrayList<>();
        soldCloths = new ArrayList<>();

        if(!ProfileFragment.this.getActivity().isDestroyed()) {
            adapter = new ClothAdapter(getContext(), userCloths);
            adapterSold = new ClothAdapter(getContext(), soldCloths);
            list.setAdapter(adapter);
            listSold.setAdapter(adapterSold);
        }

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        checkUser();

        reviews = new ArrayList<>();
        userReviewsList = view.findViewById(R.id.userReviewsList);
        linearLayoutManager = new LinearLayoutManager(ProfileFragment.this.getContext());
        userReviewsList.setLayoutManager(linearLayoutManager);
        adapterReview = new ReviewAdapter(ProfileFragment.this.getActivity(), reviews, "user", -1, profileid);
        userReviewsList.setAdapter(adapterReview);
        header3 = view.findViewById(R.id.header3);

        listOfFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(ProfileFragment.this.getContext()).inflate(R.layout.dialog_list_of_followers, null);

                ImageView closeList = view.findViewById(R.id.closeList);
                followers = new ArrayList<>();
                final RecyclerView listFollowers = view.findViewById(R.id.listFollowers);
                layoutManagerFollowers = new LinearLayoutManager(ProfileFragment.this.getContext());
                listFollowers.setLayoutManager(layoutManagerFollowers);

                /**
                 * Pravljenje liste pratioca
                 */
                FirebaseDatabase.getInstance().getReference().child("Users")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                followers.clear();
                                for(DataSnapshot snapshot1 : dataSnapshot.getChildren()){
                                    User user1 = snapshot1.getValue(User.class);
                                    for(String id : followersID){
                                        if(id.equals(user1.getId())){
                                            followers.add(user1);
                                            break;
                                        }
                                    }
                                }

                                followersAdapter = new UserAdapter(ProfileFragment.this.getContext(), followers);
                                listFollowers.setAdapter(followersAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileFragment.this.getContext());
                builder.setView(view);

                final AlertDialog dialog = builder.create();
                dialog.show();

                closeList.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        /**
         * Zapratiti ili otpratiti korisnika
         */
        ivFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ivFollowing.getTag().toString().equals("Zaprati")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following").child(ApplicationClass.otherUser.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.otherUser.getId()).child("followers").child(ApplicationClass.currentUser.getUid()).setValue(true);
                    ivFollowing.setTag("Pratite");
                    ivFollowing.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.followingbutton));

                    TOPIC_POST_NOTIFICATION = "POST_" + ApplicationClass.otherUser.getId();
                    subscribePostNotification();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            if(follow) sendNotification(ApplicationClass.otherUser.getId(), user.getUsername());

                            String timestamp = String.valueOf(System.currentTimeMillis());

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("pId", "none");
                            hashMap.put("timestamp", timestamp);
                            hashMap.put("sUid", user.getId());
                            hashMap.put("pUid", profileid);
                            hashMap.put("notification", user.getUsername() + " vas je zapratio");
                            hashMap.put("sName", user.getUsername());
                            hashMap.put("sImage", user.getImageURL());
                            hashMap.put("type", "follow");

                            String idNotification = FirebaseDatabase
                                    .getInstance().getReference("Notifications").push().getKey();

                            FirebaseDatabase.getInstance().getReference("Notifications").child(idNotification)
                                    .setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                        Toast.makeText(ProfileFragment.this.getActivity(), "Uspesno poslata notifikacija!", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(ProfileFragment.this.getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else if(ivFollowing.getTag().toString().equals("Pratite")) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ProfileFragment.this.getContext());
                    dialog.setTitle("Jeste sigurni da zelite da otpratite korinsika?");

                    dialog.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following").child(ApplicationClass.otherUser.getId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.otherUser.getId()).child("followers").child(ApplicationClass.currentUser.getUid()).removeValue();
                            ivFollowing.setTag("Zaprati");
                            ivFollowing.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.followbutton));
                            unsubscribePostNotification();
                        }
                    });

                    dialog.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    dialog.show();
                }
            }
        });

        /**
         * Odlazak na poruke sa korisnikom ili guest frag
         */
        ivMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ApplicationClass.currentUser.isAnonymous()){
                    Intent intent = new Intent(ProfileFragment.this.getContext(), MainActivity.class);
                    intent.putExtra("profile", "guest");
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(ProfileFragment.this.getContext(), ChatActivity.class);
                    intent.putExtra("userId", ApplicationClass.otherUser.getId());
                    startActivity(intent);
                }
            }
        });

        /**
         * Prikaz neprodatog odela
         */
        ivUserPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listSold.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                if(ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())) header2.setText("Vasa Odeca:");
                else header2.setText("Odeca Korisnika:");
                ApplicationClass.sold = false;
                soldList = false;
                ivUserPosts.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.clothlistchecked));
                ivSoldList.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.soldclothlistunchecked));
            }
        });

        /**
         * Prikaz prodatog odela
         */
        ivSoldList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.setVisibility(View.GONE);
                listSold.setVisibility(View.VISIBLE);
                if(ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())) header2.setText("Vasa Prodata Odeca:");
                else header2.setText("Prodata Odeca Korisnika:");
                ApplicationClass.sold = true;
                soldList = true;
                ivUserPosts.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.clothlistunchecked));
                ivSoldList.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.soldclothlistchecked));
            }
        });

        /**
         * Promena profilne slike
         */
        ivProfileImageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkCameraPermission()){
                    requestCameraPermission();
                } else {
                    CropImage.activity().setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.RECTANGLE).start(ProfileFragment.this.getActivity(), ProfileFragment.this);
                }
            }
        });

        calculateUser();

        /**
         * Ocenjivanje zvezdicama
         */
        userRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                userRating.setRating(rating);
            }
        });

        ivReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Review review = new Review();
                review.setStars(userRating.getRating());
                review.setText(etReview.getText().toString());
                review.setUserId(ApplicationClass.currentUser.getUid());
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StarsUsers").child(profileid);
                String id = databaseReference.push().getKey();
                review.setReviewId(id);

                databaseReference.child(id).setValue(review)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ProfileFragment.this.getContext(), "Uspesno ocenjeno!", Toast.LENGTH_SHORT).show();
                                userRating.setRating(0);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        calculateOverall();

        /**
         * Blokiranje ili mute ili report
         */
        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(ProfileFragment.this.getContext(), v);
                popupMenu.setOnMenuItemClickListener(menuItemClickListener);
                popupMenu.inflate(R.menu.menu_profile_more);
                popupMenu.show();

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Block").child(ApplicationClass.currentUser.getUid());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(profileid).exists()) popupMenu.getMenu().getItem(0).setTitle("Odblokiraj");
                        else popupMenu.getMenu().getItem(0).setTitle("Blokiraj");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Mute").child(ApplicationClass.currentUser.getUid());
                databaseReference2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(profileid).exists()) popupMenu.getMenu().getItem(2).setTitle("Primaj poruke od korisnika");
                        else popupMenu.getMenu().getItem(2).setTitle("Ne primaj poruke od korisnika");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        return view;
    }

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(ProfileFragment.this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(ProfileFragment.this.getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return result1 && result2;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(ProfileFragment.this.getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.block:
                    if(item.getTitle().equals("Blokiraj")){
                        blockUser();
                    } else {
                        unBlockUser();
                    }
                    break;
                case R.id.report:
                    Toast.makeText(ProfileFragment.this.getContext(), "Report!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.mute:
                    if(item.getTitle().equals("Ne primaj poruke od korisnika")){
                        muteUser();
                    } else {
                        unMuteUser();
                    }
                    break;
            }

            return true;
        }
    };

    private void unMuteUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Mute").child(ApplicationClass.currentUser.getUid());
        databaseReference.child(profileid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileFragment.this.getContext(), "Primacete poruke od korisnika!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void muteUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Mute").child(ApplicationClass.currentUser.getUid());
        databaseReference.child(profileid).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileFragment.this.getContext(), "Necete primati poruke od korisnika!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unBlockUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Block").child(ApplicationClass.currentUser.getUid());
        databaseReference.child(profileid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileFragment.this.getContext(), "Korisnik uspesno odblokiran!", Toast.LENGTH_SHORT).show();
                checkUser();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Block").child(ApplicationClass.currentUser.getUid());
        databaseReference.child(profileid).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ProfileFragment.this.getContext(), "Korisnik uspesno blokiran!", Toast.LENGTH_SHORT).show();
                FirebaseDatabase.getInstance().getReference("Follow").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userCloths.clear();
                        soldCloths.clear();

                        if(dataSnapshot.child(ApplicationClass.currentUser.getUid()).child("following").child(profileid).exists())
                            dataSnapshot.child(ApplicationClass.currentUser.getUid()).child("following").child(profileid).getRef().removeValue();

                        if(dataSnapshot.child(ApplicationClass.currentUser.getUid()).child("followers").child(profileid).exists())
                            dataSnapshot.child(ApplicationClass.currentUser.getUid()).child("followers").child(profileid).getRef().removeValue();

                        if(dataSnapshot.child(profileid).child("following").child(ApplicationClass.currentUser.getUid()).exists())
                            dataSnapshot.child(profileid).child("following").child(ApplicationClass.currentUser.getUid()).getRef().removeValue();

                        if(dataSnapshot.child(profileid).child("followers").child(ApplicationClass.currentUser.getUid()).exists())
                            dataSnapshot.child(profileid).child("followers").child(ApplicationClass.currentUser.getUid()).getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUser() {
        FirebaseDatabase.getInstance().getReference("Users").child(profileid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isAdded()) {
                    final User user = dataSnapshot.getValue(User.class);

                    ApplicationClass.otherUser = user;
                    tvUsername.setText(user.getUsername());
                    tvEmail.setText(user.getEmail());
                    tvBio.setText(user.getBio());
                    if (user.getImageURL().equals("default")) {
                        ivProfileImageUser.setImageResource(R.drawable.profimage);
                    } else {
                        Picasso.get().load(user.getImageURL()).into(ivProfileImageUser);
                    }

                    if (!ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())) {
                        header.setText("Profil Korisnika");
                        header2.setText("Odeca Korisnika:");
                        tvEmail.setVisibility(View.GONE);
                        ivMessage.setVisibility(View.VISIBLE);
                        ivFollowing.setVisibility(View.VISIBLE);
                    } else {
                        ivMore.setVisibility(View.GONE);
                        rateUser.setVisibility(View.GONE);
                    }

                    follow = (boolean) dataSnapshot.child("notifications").child("follow").getValue();

                    FirebaseDatabase.getInstance().getReference("Block").child(ApplicationClass.currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.child(profileid).exists()) {
                                if (!ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())) {
                                    header2.setText("Odeca Korisnika:");
                                    ivMessage.setVisibility(View.VISIBLE);
                                    ivFollowing.setVisibility(View.VISIBLE);
                                    rateUser.setVisibility(View.VISIBLE);
                                }

                                if (soldList) {
                                    if (ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid()))
                                        header2.setText("Vasa Prodata Odeca:");
                                    else header2.setText("Prodata Odeca Korisnika:");
                                    listSold.setVisibility(View.VISIBLE);
                                    list.setVisibility(View.GONE);
                                    ivUserPosts.setBackground(ProfileFragment.this.getContext().getDrawable(R.drawable.clothlistunchecked));
                                    ivSoldList.setBackground(ProfileFragment.this.getContext().getDrawable(R.drawable.soldclothlistchecked));
                                } else {
                                    if (ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid()))
                                        header2.setText("Vasa Odeca:");
                                    else header2.setText("Odeca Korisnika:");
                                    listSold.setVisibility(View.GONE);
                                    list.setVisibility(View.VISIBLE);
                                    ivUserPosts.setBackground(ProfileFragment.this.getContext().getDrawable(R.drawable.clothlistchecked));
                                    ivSoldList.setBackground(ProfileFragment.this.getContext().getDrawable(R.drawable.soldclothlistunchecked));
                                }

                                /**
                                 * Formiranje liste korisnickog odela i prodatog odela
                                 */
                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        userCloths.clear();
                                        soldCloths.clear();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Cloth cloth = snapshot.getValue(Cloth.class);
                                            if (cloth.getOwnerID().equals(ApplicationClass.otherUser.getId())) {
                                                if (cloth.isSold()) soldCloths.add(cloth);
                                                else userCloths.add(cloth);
                                            }
                                        }

                                        if (ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid()))
                                            ApplicationClass.userCloths = userCloths;
                                        ApplicationClass.profileCloth = userCloths;
                                        ApplicationClass.soldCloths = soldCloths;
                                        numOfCloth.setText("" + userCloths.size());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(ProfileFragment.this.getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                reference2 = FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following");
                                reference2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child(ApplicationClass.otherUser.getId()).exists()) {
                                            ivFollowing.setTag("Pratite");
                                            ivFollowing.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.followingbutton));
                                        } else {
                                            ivFollowing.setTag("Zaprati");
                                            ivFollowing.setBackground(ProfileFragment.this.getContext().getResources().getDrawable(R.drawable.followbutton));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                /**
                                 * Broj pratioca
                                 */
                                FirebaseDatabase.getInstance().getReference().child("Follow")
                                        .child(ApplicationClass.otherUser.getId()).child("followers").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        numOfFollowers.setText("" + dataSnapshot.getChildrenCount());
                                        for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                                            String id = snapshot1.getKey();
                                            followersID.add(id);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                /**
                                 * Pravljenje liste utisaka
                                 */
                                DatabaseReference databaseReferenceReviews = FirebaseDatabase.getInstance().getReference("StarsUsers").child(profileid);
                                databaseReferenceReviews.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        reviews.clear();
                                        boolean myReview = false;
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            Review review = snapshot.getValue(Review.class);
                                            if (review.getUserId().equals(ApplicationClass.currentUser.getUid()))
                                                myReview = true;
                                            reviews.add(review);
                                        }

                                        numOfReviews.setText("" + dataSnapshot.getChildrenCount());

                                        if (myReview) {
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
                                        calculateUser();

                                        if (reviews.size() == 0) {
                                            header3.setVisibility(View.GONE);
                                            userReviewsList.setVisibility(View.GONE);
                                            layoutNumOfReviews.setVisibility(View.GONE);
                                        } else {
                                            header3.setVisibility(View.VISIBLE);
                                            userReviewsList.setVisibility(View.VISIBLE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            } else {
                                header2.setText("Korisnik je blokiran!");
                                ivMessage.setVisibility(View.GONE);
                                ivFollowing.setVisibility(View.GONE);
                                rateUser.setVisibility(View.GONE);
                                ivSoldList.setVisibility(View.GONE);
                                ivUserPosts.setVisibility(View.GONE);
                                list.setVisibility(View.GONE);
                                listSold.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Slanje notifikacije za pracenje
     * @param receiver
     * @param username
     */
    private void sendNotification(final String receiver, final String username) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(ApplicationClass.currentUser.getUid(), username + " vas je zapratio!", "Novo pracenje", receiver, R.drawable.ic_launcher_background, "follow");

                    Sender sender = new Sender(data, token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send",
                                senderJsonObj, new com.android.volley.Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAtNK1qRw:APA91bHJLg399DpOkV6U_EU2OPkC3Uu1L3NM9Lbn4C79ogYXvPQYjNoP6twQ5kVjF9WcsESShq-kFKFpcL-HoMnuvi_7iww6095qHb2NEm3NtOjMrb_n5He8fm-Z3rujPOQuMfibrpvI");

                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        Toast.makeText(ProfileFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
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
    private void calculateUser(){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StarsUsers").child(profileid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Review review = snapshot.getValue(Review.class);

                    if(review.getUserId().equals(ApplicationClass.currentUser.getUid())){
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("StarsUsers").child(profileid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float sum = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Review review = snapshot.getValue(Review.class);
                    sum += review.getStars();
                }
                starsOverall = sum / (float) dataSnapshot.getChildrenCount();

                userOverallRating.setRating(starsOverall);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = ProfileFragment.this.getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(ProfileFragment.this.getContext());
        pd.setMessage("Cuvanje slike na serveru...");
        pd.show();

        if(mImageUri != null){
            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + ApplicationClass.currentUser.getUid() + "." + getFileExtension(mImageUri));

            uploadTask = filereference.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()) throw task.getException();
                    return filereference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();

                        DatabaseReference reference3 = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageURL", "" + myUrl);

                        reference3.updateChildren(hashMap);
                        pd.dismiss();
                    } else {
                        Toast.makeText(ProfileFragment.this.getContext(), "Neuspesno cuvanje slike!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileFragment.this.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ProfileFragment.this.getContext(), "Slika nije izabrana!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * unsubscribe od notifikacija za odecu
     */
    private void unsubscribePostNotification() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Necete dobijati notifikacije o novoj odeci!";
                        if(!task.isSuccessful()){
                            msg = task.getException().getMessage();
                        }

                        Toast.makeText(ProfileFragment.this.getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * subscribe za notifikacije za odecu
     */
    private void subscribePostNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Dobijacete notifikacije o novoj odeci!";
                        if(!task.isSuccessful()){
                            msg = task.getException().getMessage();
                        }

                        Toast.makeText(ProfileFragment.this.getActivity(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();

            uploadImage();
        } else {
            Toast.makeText(ProfileFragment.this.getContext(), "Neuspesno cuvanje slike!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_REQUEST_CODE){
            if(grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                /**
                 * Dozvoljen je pristup i onda se uzima slika
                 */
                if (cameraAccepted && storageAccepted) {
                    CropImage.activity().setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.RECTANGLE).start(ProfileFragment.this.getActivity(), ProfileFragment.this);
                } else {
                    /**
                     * Nije dozvoljen pristup i onda se objasnjava zasto je potrebna
                     */
                    Toast.makeText(ProfileFragment.this.getContext(), "Dozvole za kameru i galeriju su potrebne!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ProfileFragment.this.getContext(), "Molimo vas dozvolite pristup, kako bi mogli da ubacite sliku!", Toast.LENGTH_SHORT).show();
            }
        }
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
    public void onResume() {
        super.onResume();

        status("online");
        checkTypingStatus("noOne");
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }
}

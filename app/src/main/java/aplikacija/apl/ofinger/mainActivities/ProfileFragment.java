package aplikacija.apl.ofinger.mainActivities;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.adapters.ClothSearchAdapter;
import aplikacija.apl.ofinger.adapters.ReviewAdapter;
import aplikacija.apl.ofinger.adapters.UserAdapter;
import aplikacija.apl.ofinger.messaging.ChatActivity;
import aplikacija.apl.ofinger.models.Cloth;
import aplikacija.apl.ofinger.models.Review;
import aplikacija.apl.ofinger.models.User;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private static final int CAMERA_REQUEST_CODE = 100;
    private String[] cameraPermissions;

    private CircleImageView ivProfileImageUser;
    private TextView tvEmail, tvUsername, header, header2, reviewHeader, tvBio, numOfCloth, numOfFollowers, numOfReviews, header3;
    private RatingBar userOverallRating, userRating;
    private ImageView ivMore, ivMessage, ivFollowing;
    private LinearLayout layoutNumOfReviews;

    private ImageView showNotSold, showSold;

    private RecyclerView list, listSold;
    private ClothSearchAdapter adapter, adapterSold;
    private ConstraintLayout rateUser;

    private RecyclerView userReviewsList;
    private ReviewAdapter adapterReview;
    private List<Review> reviews;

    private List<User> followers;
    private LinearLayoutManager layoutManagerFollowers;
    private UserAdapter followersAdapter;

    private ArrayList<String> followersID;

    private EditText etReview;
    private ImageView ivReview;

    private String profileid;

    private DatabaseReference reference, reference2;
    private List<Cloth> userCloths, soldCloths;

    private Uri mImageUri;
    private StorageReference storageReference;

    private float starsOverall;

    private RequestQueue requestQueue;

    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        requestQueue = Volley.newRequestQueue(ProfileFragment.this.getActivity().getApplicationContext());

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        progressDialog = new ProgressDialog(ProfileFragment.this.getActivity());

        ivProfileImageUser = view.findViewById(R.id.ivProfileImageUser);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvUsername = view.findViewById(R.id.tvUsername);
        ivFollowing = view.findViewById(R.id.ivFollowing);
        ivMessage = view.findViewById(R.id.ivMessage);
        header = view.findViewById(R.id.header);
        header2 = view.findViewById(R.id.header2);
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
        LinearLayout listOfFollowers = view.findViewById(R.id.listOfFollowers);
        layoutNumOfReviews = view.findViewById(R.id.layoutNumOfReviews);

        showNotSold = view.findViewById(R.id.showNotSold);
        showSold = view.findViewById(R.id.showSold);

        if(!ProfileFragment.this.getActivity().isDestroyed()) {
            list = view.findViewById(R.id.listProfile);
            list.setHasFixedSize(true);
            LinearLayoutManager manager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.HORIZONTAL, false);
            list.setLayoutManager(manager);

            listSold = view.findViewById(R.id.listProfileSold);
            listSold.setHasFixedSize(true);
            LinearLayoutManager managerSold = new GridLayoutManager(getContext(), 3, LinearLayoutManager.HORIZONTAL, false);
            listSold.setLayoutManager(managerSold);
            followersID = new ArrayList<>();

            storageReference = FirebaseStorage.getInstance().getReference("profileImages");
            reference = FirebaseDatabase.getInstance().getReference("Cloth");
            userCloths = new ArrayList<>();
            soldCloths = new ArrayList<>();

            adapter = new ClothSearchAdapter(getContext(), userCloths);
            adapterSold = new ClothSearchAdapter(getContext(), soldCloths);
            list.setAdapter(adapter);
            listSold.setAdapter(adapterSold);
        }

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        reviews = new ArrayList<>();
        userReviewsList = view.findViewById(R.id.userReviewsList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ProfileFragment.this.getContext());
        userReviewsList.setLayoutManager(linearLayoutManager);
        adapterReview = new ReviewAdapter(ProfileFragment.this.getActivity(), reviews, "user", -1, profileid);
        userReviewsList.setAdapter(adapterReview);
        header3 = view.findViewById(R.id.header3);

        checkUser();

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
                    ivFollowing.setImageResource(R.drawable.followingbutton);
                } else if(ivFollowing.getTag().toString().equals("Pratite")) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ProfileFragment.this.getContext());
                    dialog.setTitle("Jeste sigurni da zelite da otpratite korinsika?");

                    dialog.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following").child(ApplicationClass.otherUser.getId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.otherUser.getId()).child("followers").child(ApplicationClass.currentUser.getUid()).removeValue();
                            ivFollowing.setTag("Zaprati");
                            ivFollowing.setImageResource(R.drawable.followbutton);
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
        showNotSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())) header2.setText("Vasa Odeca:");
                else header2.setText("Odeca Korisnika:");
                ApplicationClass.sold = false;
                listSold.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                showNotSold.setImageResource(R.drawable.clothlistchecked);
                showSold.setImageResource(R.drawable.soldlistunchecked);
            }
        });

        /**
         * Prikaz prodatog odela
         */
        showSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())) header2.setText("Vasa Prodata Odeca:");
                else header2.setText("Prodata Odeca Korisnika:");
                ApplicationClass.sold = true;
                list.setVisibility(View.GONE);
                listSold.setVisibility(View.VISIBLE);
                showNotSold.setImageResource(R.drawable.clothlistunchecked);
                showSold.setImageResource(R.drawable.soldlistchecked);
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
                    reportUser();
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

    private void reportUser() {
        View view = LayoutInflater.from(ProfileFragment.this.getActivity()).inflate(R.layout.dialog_report, null);
        final MaterialEditText etText = view.findViewById(R.id.etText);
        ImageView btnSubmit = view.findViewById(R.id.btnSubmit);

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileFragment.this.getActivity());
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Slanje prijave...");
                progressDialog.show();

                final String text = etText.getText().toString();

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"ofingerdeveloperteam@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "Report");
                i.putExtra(Intent.EXTRA_TEXT   , text + "\n\n\n" + profileid);
                try {
                    startActivity(Intent.createChooser(i, "Slanje mejla:"));
                    progressDialog.dismiss();
                    dialog.dismiss();
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ProfileFragment.this.getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    dialog.dismiss();
                }
            }
        });
    }

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
                        ivMore.setVisibility(View.VISIBLE);
                        rateUser.setVisibility(View.VISIBLE);
                    } else {
                        header.setText("Vas Profil");
                        header2.setText("Vasa Odeca:");
                        tvEmail.setVisibility(View.VISIBLE);
                        ivMore.setVisibility(View.GONE);
                        rateUser.setVisibility(View.GONE);
                    }

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
                                        Collections.reverse(userCloths);
                                        Collections.reverse(soldCloths);
                                        ApplicationClass.profileCloth = userCloths;
                                        ApplicationClass.soldCloths = soldCloths;
                                        numOfCloth.setText("" + userCloths.size());
                                        adapter.notifyDataSetChanged();
                                        adapterSold.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        //Toast.makeText(ProfileFragment.this.getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                reference2 = FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following");
                                reference2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child(ApplicationClass.otherUser.getId()).exists()) {
                                            ivFollowing.setTag("Pratite");
                                            ivFollowing.setImageResource(R.drawable.followingbutton);
                                        } else {
                                            ivFollowing.setTag("Zaprati");
                                            ivFollowing.setImageResource(R.drawable.followbutton);
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

                                        if (myReview && !ApplicationClass.currentUser.getUid().equals(ApplicationClass.otherUser.getId())) {
                                            etReview.setVisibility(View.GONE);
                                            ivReview.setVisibility(View.GONE);
                                            userRating.setVisibility(View.GONE);
                                            reviewHeader.setText("Hvala na ocenjivanju korisnika!");
                                        } else if (!ApplicationClass.currentUser.getUid().equals(ApplicationClass.otherUser.getId())) {
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
                                            layoutNumOfReviews.setVisibility(View.VISIBLE);
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
                                showNotSold.setVisibility(View.GONE);
                                showSold.setVisibility(View.GONE);
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

            StorageTask uploadTask = filereference.putFile(mImageUri);
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

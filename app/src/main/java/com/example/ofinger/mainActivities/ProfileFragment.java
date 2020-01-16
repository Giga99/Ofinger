package com.example.ofinger.mainActivities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.messaging.ChatActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private CircleImageView ivProfileImageUser;
    private TextView tvEmail, tvUsername, header, header2;
    private Button btnMessage, btnFollowing, btnSettings;
    private ImageButton ivUserPosts, ivSoldList;

    private RecyclerView list, listSold;
    private LinearLayoutManager manager, managerSold;
    private ClothAdapter adapter, adapterSold;

    private String profileid;

    private DatabaseReference reference, reference2;
    private List<Cloth> userCloths, soldCloths;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ivProfileImageUser = view.findViewById(R.id.ivProfileImageUser);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvUsername = view.findViewById(R.id.tvUsername);
        btnFollowing = view.findViewById(R.id.btnFollowing);
        btnMessage = view.findViewById(R.id.btnMessage);
        btnSettings = view.findViewById(R.id.btnSettings);
        header = view.findViewById(R.id.header);
        header2 = view.findViewById(R.id.header2);
        ivUserPosts = view.findViewById(R.id.ivUserPosts);
        ivSoldList = view.findViewById(R.id.ivSoldList);

        list = view.findViewById(R.id.listProfile);
        list.setHasFixedSize(true);
        manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        list.setLayoutManager(manager);

        listSold = view.findViewById(R.id.listProfileSold);
        listSold.setHasFixedSize(true);
        managerSold = new LinearLayoutManager(getContext());
        listSold.setLayoutManager(managerSold);

        storageReference = FirebaseStorage.getInstance().getReference("profileImages");
        reference = FirebaseDatabase.getInstance().getReference("Cloth");
        userCloths = new ArrayList<>();
        soldCloths = new ArrayList<>();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid", "none");

        ApplicationClass.allUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(isAdded()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);

                        if (user.getId().equals(profileid)) {
                            ApplicationClass.otherUser = user;
                            tvUsername.setText("Korisnicko ime: " + user.getUsername());
                            tvEmail.setText("Imejl: " + user.getEmail());
                            if (user.getImageURL().equals("default")) {
                                ivProfileImageUser.setImageResource(R.mipmap.ic_launcher);
                            } else {
                                Glide.with(getContext()).load(user.getImageURL()).into(ivProfileImageUser);
                            }

                            if (!ApplicationClass.otherUser.getId().equals(ApplicationClass.currentUser.getUid())) {
                                header.setText("Profil Korisnika");
                                header2.setText("Odeca Korisnika:");
                                tvEmail.setVisibility(View.GONE);
                                btnSettings.setVisibility(View.GONE);
                                btnMessage.setVisibility(View.VISIBLE);
                                btnFollowing.setVisibility(View.VISIBLE);
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
                                    adapter = new ClothAdapter(getContext(), userCloths);
                                    adapterSold = new ClothAdapter(getContext(), soldCloths);
                                    list.setAdapter(adapter);
                                    listSold.setAdapter(adapterSold);
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
                                        btnFollowing.setText("Pratite");
                                    } else {
                                        btnFollowing.setText("Zapratite");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Odlazak na opcije
         */
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO podesavanja
                Toast.makeText(ProfileFragment.this.getContext(), "Podesavanja", Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * Zapratiti ili otpratiti korisnika
         */
        btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnFollowing.getText().toString().equals("Zapratite")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following").child(ApplicationClass.otherUser.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.otherUser.getId()).child("followers").child(ApplicationClass.currentUser.getUid()).setValue(true);
                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ProfileFragment.this.getContext());
                    dialog.setTitle("Jeste sigurni da zelite da otpratite korinsika?");

                    dialog.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following").child(ApplicationClass.otherUser.getId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.otherUser.getId()).child("followers").child(ApplicationClass.currentUser.getUid()).removeValue();
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
        btnMessage.setOnClickListener(new View.OnClickListener() {
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
            }
        });

        /**
         * Promena profilne slike
         */
        ivProfileImageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1, 1)
                        .setCropShape(CropImageView.CropShape.RECTANGLE).start(ProfileFragment.this.getActivity(), ProfileFragment.this);
            }
        });

        return view;
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
            final StorageReference filereference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

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

    private void status(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
    }

    @Override
    public void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    public void onPause() {
        super.onPause();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            status("offline");
        }
    }
}

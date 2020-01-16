package com.example.ofinger.adding;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.CustomDialogs.CustomDialogWish;
import com.example.ofinger.CustomDialogs.CustomDialogZoomImage;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.adapters.ImageAdapter;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.mainActivities.GuestFragment;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Image;
import com.example.ofinger.models.User;
import com.example.ofinger.startActivities.StartActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddingCloth extends AppCompatActivity implements ImageAdapter.ItemClicked2, ClothAdapter.ItemClicked {
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;

    EditText etName, etPrice, etDescription;

    List<Image> images;
    RecyclerView list2;
    ImageAdapter adapter;
    GridLayoutManager layoutManager;

    Button btnAddImage, btnFinish;

    CircleImageView ivProfileImage, navHeaderProfileImage;
    TextView tvUsername;
    ImageView ivBack;

    String username;

    DatabaseReference reference2, reference3;
    List<Image> allImages;
    List<Cloth> cloths, allCloth;

    Uri imageUri;
    String myUrl = "";
    List<String> urls;
    StorageTask uploadTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_cloth);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.wishList:
                        CustomDialogWish customDialogWish = new CustomDialogWish(AddingCloth.this);
                        customDialogWish.show();
                        break;
                    case R.id.logout:
                        if (ApplicationClass.currentUser.isAnonymous()) {
                            ApplicationClass.currentUserReference.removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    ApplicationClass.currentUser.delete();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(AddingCloth.this, StartActivity.class));
                                    AddingCloth.this.finish();
                                }
                            });
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(AddingCloth.this, StartActivity.class));
                            AddingCloth.this.finish();
                        }
                        break;

                    case R.id.settings:
                        if(ApplicationClass.currentUser.isAnonymous()){
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GuestFragment()).commit();
                        } else {
                            //TODO podesavanja
                            Toast.makeText(AddingCloth.this, "Settings!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });

        ivProfileImage = findViewById(R.id.ivProfileImage);
        View hView = navView.getHeaderView(0);
        navHeaderProfileImage = hView.findViewById(R.id.navHeaderProfileImage);
        tvUsername = findViewById(R.id.tvUsername);
        ivBack = findViewById(R.id.ivBack);

        storageReference = FirebaseStorage.getInstance().getReference("images");
        urls = new ArrayList<>();

        reference2 = FirebaseDatabase.getInstance().getReference("Cloth");
        reference3 = FirebaseDatabase.getInstance().getReference("Cloth");
        allImages = new ArrayList<>();
        cloths = new ArrayList<>();
        allCloth = new ArrayList<>();

        /**
         * Ispisivanje imena
         */
        ApplicationClass.currentUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!isDestroyed()) {
                    User user = dataSnapshot.getValue(User.class);
                    username = user.getUsername();
                    tvUsername.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        ivProfileImage.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(AddingCloth.this).load(user.getImageURL()).into(ivProfileImage);
                        Glide.with(AddingCloth.this).load(user.getImageURL()).into(navHeaderProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        images = new ArrayList<>();
        list2 = findViewById(R.id.list2);
        list2.setHasFixedSize(true);
        adapter = new ImageAdapter(this, images);
        list2.setAdapter(adapter);
        layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false);
        list2.setLayoutManager(layoutManager);

        btnAddImage = findViewById(R.id.btnAddImage);
        btnFinish = findViewById(R.id.btnFinish);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);

        /**
         * Povratak na prethodnu aktivnost
         */
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        /**
         * Dodavanje slike
         */
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setAspectRatio(1, 1).start(AddingCloth.this);
            }
        });

        /**
         * Dodavanje odece i podesavanje glavne liste odela, korisnicke liste odela i slika
         */
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Mora sve da bude popunjeno kao i da opis ima najmanje 30 karaktera
                 */
                if(etName.getText().toString().isEmpty() || etPrice.getText().toString().isEmpty() || etDescription.getText().toString().isEmpty()){
                    Toast.makeText(AddingCloth.this, "Unesite sve podatke!", Toast.LENGTH_SHORT).show();
                } else if(etDescription.getText().toString().length() < 30){
                    Toast.makeText(AddingCloth.this, "Opis mora imati vise od 30 karaktera!", Toast.LENGTH_SHORT).show();
                } else {
                    showProgress(true);
                    tvLoad.setText("Molim vas sacekajte...");

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cloth");

                    /**
                     * Pravljenje novog Cloth objekta
                     */
                    final Cloth cloth = new Cloth();
                    cloth.setName(etName.getText().toString().trim());
                    long price = Long.parseLong(etPrice.getText().toString().trim());
                    cloth.setPrice(price);
                    cloth.setDescription(etDescription.getText().toString().trim());
                    cloth.setOwnerID(ApplicationClass.currentUser.getUid());
                    cloth.setOwnerUsername(username);
                    cloth.setSearchName(etName.getText().toString().trim().toLowerCase());
                    cloth.setSold(false);
                    cloth.setUrls(urls);
                    String id = reference.push().getKey();
                    cloth.setObjectId(id);

                    reference.child(id).setValue(cloth).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(AddingCloth.this, "Odeca je uspesno dodata!", Toast.LENGTH_SHORT).show();

                            /**
                             * Formiranje liste korisnickog odela
                             */
                            reference2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    cloths.clear();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Cloth cloth = snapshot.getValue(Cloth.class);
                                        assert cloth != null;
                                        if (cloth.getOwnerID().equals(ApplicationClass.currentUser.getUid()) && !cloth.isSold()) {
                                            cloths.add(cloth);
                                        }
                                    }

                                    ApplicationClass.userCloths = cloths;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(AddingCloth.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            /**
                             * Pravljenje liste svih odela
                             */
                            reference3.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    allCloth.clear();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Cloth cloth = snapshot.getValue(Cloth.class);
                                        allCloth.add(cloth);
                                    }

                                    ApplicationClass.mainCloths = allCloth;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(AddingCloth.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                            /**
                             * Povratak na Main
                             */
                            startActivity(new Intent(AddingCloth.this, MainActivity.class));
                            AddingCloth.this.finish();
                        }
                    });
                }
            }
        });
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Snimanje slike...");
        progressDialog.show();

        if(imageUri != null){
            final StorageReference filereferance = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = filereferance.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()) throw task.getException();
                    return filereferance.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        urls.add(myUrl);

                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(AddingCloth.this, "Neuspesno!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddingCloth.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(AddingCloth.this, "Slika nije izabrana", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
           CropImage.ActivityResult result = CropImage.getActivityResult(data);
           imageUri = result.getUri();

           Image image = new Image();
           image.setInfo("" + imageUri);
           images.add(image);
           adapter.notifyDataSetChanged();

           uploadImage();
       }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            status("offline");
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

    @Override
    public void onItemClicked2(int index) {
        ApplicationClass.currentImage = images.get(index);
        CustomDialogZoomImage customDialogZoomImage = new CustomDialogZoomImage(AddingCloth.this);
        customDialogZoomImage.show();
    }

    @Override
    public void onItemClicked(int index) {
        int i = 0;
        for (Cloth cloth : ApplicationClass.mainCloths) {
            if (ApplicationClass.wish) {
                if (cloth.getObjectId().equals(ApplicationClass.wishCloths.get(index).getObjectId())) break;
            }
            i++;
        }
        Intent intent = new Intent(AddingCloth.this, ClothInfo.class);
        intent.putExtra("index", i);
        startActivityForResult(intent, 1);
    }
}

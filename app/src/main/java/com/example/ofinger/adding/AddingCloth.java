package com.example.ofinger.adding;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.adapters.ImageVideoAdapter;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Image;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddingCloth extends AppCompatActivity implements ClothAdapter.ItemClicked {
    private static final int REQUEST_CODE_ADD = 1;
    private static final int REQUEST_CODE_TAKE = 2;

    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    EditText etName, etPrice, etDescription;

    List<Image> images;
    ViewPager viewpagerImages;
    ImageVideoAdapter adapter;

    Button btnAddImage, btnFinish;

    CircleImageView ivProfileImage;
    TextView tvUsername;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUsername);

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
                        ivProfileImage.setImageResource(R.drawable.profimage);
                    } else {
                        Glide.with(AddingCloth.this).load(user.getImageURL()).into(ivProfileImage);
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
        viewpagerImages = findViewById(R.id.viewpagerImages);
        adapter = new ImageVideoAdapter(this, images);
        viewpagerImages.setAdapter(adapter);

        btnAddImage = findViewById(R.id.btnAddImage);
        btnFinish = findViewById(R.id.btnFinish);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);

        /**
         * Dodavanje slike
         */
        btnAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(AddingCloth.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(AddingCloth.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddingCloth.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD);
                    ActivityCompat.requestPermissions(AddingCloth.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_TAKE);
                } else if(ContextCompat.checkSelfPermission(AddingCloth.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddingCloth.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD);
                } else if (ContextCompat.checkSelfPermission(AddingCloth.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddingCloth.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_TAKE);
                } else {
                    CropImage.activity().setAspectRatio(1, 1).start(AddingCloth.this);
                }
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
                } else if(etDescription.getText().toString().length() < 30) {
                    Toast.makeText(AddingCloth.this, "Opis mora imati vise od 30 karaktera!", Toast.LENGTH_SHORT).show();
                } else if(images.size() == 0){
                    Toast.makeText(AddingCloth.this, "Morate dodati barem jednu sliku!", Toast.LENGTH_SHORT).show();
                } else {
                    showProgress(true);
                    tvLoad.setText("Molim vas sacekajte...");

                    String timestamp = String.valueOf(System.currentTimeMillis());

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
                    cloth.setTimestamp(timestamp);
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
            final StorageReference filereferance = storageReference.child(System.currentTimeMillis() + ApplicationClass.currentUser.getUid() + "." + getFileExtension(imageUri));

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

        /**
         * Obrada uzete slike i postavljanje na server
         */
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            /**
             * Dozvoljen je pristup i onda se uzima slika
             */
            if((CropImage.hasPermissionInManifest(AddingCloth.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            && (CropImage.hasPermissionInManifest(AddingCloth.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))){
                CropImage.activity().setAspectRatio(1, 1).start(AddingCloth.this);
            } else if(!CropImage.hasPermissionInManifest(AddingCloth.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                /**
                 * Nije dozvoljen pristup i onda se ponovo trazi dozvola sa objasnjenjem zasto je potrebna
                 */
                if(ActivityCompat.shouldShowRequestPermissionRationale(AddingCloth.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Ova dozvola je potrebna kako bi dodali sliku u aplikaciju. Molim vas dozvolite!").setTitle("Zahtev za vaznu dozvolu!");

                    builder.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(AddingCloth.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD);
                        }
                    });

                    builder.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(AddingCloth.this, "Ne moze se dodati!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();
                } else {
                    Toast.makeText(AddingCloth.this, "Nikad vas vise necemo pitati!", Toast.LENGTH_SHORT).show();
                }
            } else if(!CropImage.hasPermissionInManifest(AddingCloth.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                /**
                 * Nije dozvoljen pristup i onda se ponovo trazi dozvola sa objasnjenjem zasto je potrebna
                 */
                if(ActivityCompat.shouldShowRequestPermissionRationale(AddingCloth.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Ova dozvola je potrebna kako bi dodali sliku u aplikaciju. Molim vas dozvolite!").setTitle("Zahtev za vaznu dozvolu!");

                    builder.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(AddingCloth.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ADD);
                        }
                    });

                    builder.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(AddingCloth.this, "Ne moze se dodati!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();
                } else {
                    Toast.makeText(AddingCloth.this, "Nikad vas vise necemo pitati!", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}

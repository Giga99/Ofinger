package aplikacija.apl.ofinger.settings;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.models.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    String[] cameraPermissions;

    CircleImageView ivProfileImage;
    EditText etNewUsername, etNewBio;
    ImageView btnSave;

    private Uri mImageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;

    ProgressDialog progressDialog;

    AdView mainAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Promena informacija o vasem profilu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ivProfileImage = findViewById(R.id.ivProfileImage);
        etNewBio = findViewById(R.id.etNewBio);
        etNewUsername = findViewById(R.id.etNewUsername);
        btnSave = findViewById(R.id.btnSave);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        mainAd = findViewById(R.id.mainAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainAd.loadAd(adRequest);

        storageReference = FirebaseStorage.getInstance().getReference("profileImages");

        ApplicationClass.currentUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                etNewUsername.setText(user.getUsername());
                etNewBio.setText(user.getBio());
                if (user.getImageURL().equals("default")) {
                    ivProfileImage.setImageResource(R.drawable.profimage);
                } else {
                    if(!isDestroyed()) Glide.with(EditProfileActivity.this).load(user.getImageURL()).into(ivProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkCameraPermission()){
                    requestCameraPermission();
                } else {
                    CropImage.activity().setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.RECTANGLE).start(EditProfileActivity.this);
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etNewUsername.getText().toString().isEmpty()) {
                    etNewUsername.setError("Morate ovo popuniti");
                    etNewUsername.setFocusable(true);
                } else {
                    progressDialog.setMessage("Cuvanje podataka...");
                    progressDialog.show();

                    String username = etNewUsername.getText().toString().trim();
                    String bio;
                    if(etNewBio.getText().toString().isEmpty()) bio = "";
                    else bio = etNewBio.getText().toString().trim();

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("username", username);
                    hashMap.put("bio", bio);

                    ApplicationClass.currentUserReference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProfileActivity.this, "Uspesno sacuvani podaci!", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return result1 && result2;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = EditProfileActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(EditProfileActivity.this);
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
                        Toast.makeText(EditProfileActivity.this, "Neuspesno cuvanje slike!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "Slika nije izabrana!", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(EditProfileActivity.this, "Neuspesno cuvanje slike!", Toast.LENGTH_SHORT).show();
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
                            .setCropShape(CropImageView.CropShape.RECTANGLE).start(EditProfileActivity.this);
                } else {
                    /**
                     * Nije dozvoljen pristup i onda se objasnjava zasto je potrebna
                     */
                    Toast.makeText(EditProfileActivity.this, "Dozvole za kameru i galeriju su potrebne!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EditProfileActivity.this, "Molimo vas dozvolite pristup, kako bi mogli da ubacite sliku!", Toast.LENGTH_SHORT).show();
            }
        }
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
    protected void onDestroy() {
        super.onDestroy();

        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }
}

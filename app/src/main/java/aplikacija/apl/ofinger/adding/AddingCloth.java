package aplikacija.apl.ofinger.adding;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
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

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.adapters.ImageVideoAdapter;
import aplikacija.apl.ofinger.mainActivities.MainActivity;
import aplikacija.apl.ofinger.models.Cloth;
import aplikacija.apl.ofinger.models.ImageVideo;
import aplikacija.apl.ofinger.models.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddingCloth extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    String[] cameraPermissions;

    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    EditText etName, etPrice, etDescription;
    TextView tvCategory, tvSubCategory;
    ImageView ivCategory, ivSubCategory, ivAddImage, ivFinish;
    RelativeLayout expandCategory, expandSubCategory;

    ViewPager viewpagerImages;
    TabLayout tabLayout;

    CircleImageView ivProfileImage;
    TextView tvUsername;

    String username, nameImageVideo;

    DatabaseReference reference2, reference3;
    List<ImageVideo> allImageVideos;
    List<Cloth> userCloths, allCloth;

    String firstUrl;
    boolean first = false;
    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTaskImage;
    StorageReference storageReference;

    String image;

    PopupMenu popupMenuCategory, popupMenuSubcategory;
    String category = null, subcategory = null;

    AdView mainAd;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adding_cloth);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        ApplicationClass.currentUserCloth = true;

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvUsername = findViewById(R.id.tvUsername);

        storageReference = FirebaseStorage.getInstance().getReference("images");
        ApplicationClass.urls = new ArrayList<>();

        reference2 = FirebaseDatabase.getInstance().getReference("Cloth");
        reference3 = FirebaseDatabase.getInstance().getReference("Cloth");
        allImageVideos = new ArrayList<>();
        userCloths = new ArrayList<>();
        allCloth = new ArrayList<>();

        ApplicationClass.currentActivityZoomImage = "addingCloth";

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        mainAd = findViewById(R.id.mainAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainAd.loadAd(adRequest);

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

        ApplicationClass.imageVideosAddingCloth = new ArrayList<>();
        viewpagerImages = findViewById(R.id.viewpagerImages);
        ApplicationClass.adapterAddingClothImageVideo = new ImageVideoAdapter(this, ApplicationClass.imageVideosAddingCloth);
        viewpagerImages.setAdapter(ApplicationClass.adapterAddingClothImageVideo);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewpagerImages, true);

        ivAddImage = findViewById(R.id.ivAddImage);
        ivFinish = findViewById(R.id.ivFinish);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);

        tvCategory = findViewById(R.id.tvCategory);
        tvSubCategory = findViewById(R.id.tvSubCategory);
        ivCategory = findViewById(R.id.ivCategory);
        ivSubCategory = findViewById(R.id.ivSubCategory);
        expandCategory = findViewById(R.id.expandCategory);
        expandSubCategory = findViewById(R.id.expandSubCategory);

        /**
         * Dodavanje slike
         */
        ivAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkCameraPermission()){
                    requestCameraPermission();
                } else {
                    CropImage.activity().setAspectRatio(1, 1).start(AddingCloth.this);
                }
            }
        });

        /**
         * Dodavanje odece i podesavanje glavne liste odela, korisnicke liste odela i slika
         */
        ivFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Mora sve da bude popunjeno kao i da opis ima najmanje 30 karaktera
                 */
                if(etName.getText().toString().isEmpty() || etPrice.getText().toString().isEmpty() || etDescription.getText().toString().isEmpty()){
                    Toast.makeText(AddingCloth.this, "Unesite sve podatke!", Toast.LENGTH_SHORT).show();
                } else if(etDescription.getText().toString().length() < 30) {
                    Toast.makeText(AddingCloth.this, "Opis mora imati vise od 30 karaktera!", Toast.LENGTH_SHORT).show();
                } else if(ApplicationClass.imageVideosAddingCloth.size() == 0){
                    Toast.makeText(AddingCloth.this, "Morate dodati barem jednu sliku!", Toast.LENGTH_SHORT).show();
                } else if(category == null){
                    Toast.makeText(AddingCloth.this, "Morate izabrati kategoriju!", Toast.LENGTH_SHORT).show();
                } else if(subcategory == null){
                    Toast.makeText(AddingCloth.this, "Morate izabrati potkategoriju!", Toast.LENGTH_SHORT).show();
                } else {
                    showProgress(true);
                    tvLoad.setText("Molim vas sacekajte...");

                    final String timestamp = String.valueOf(System.currentTimeMillis());

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
                    cloth.setUrls(ApplicationClass.urls);
                    cloth.setTimestamp(timestamp);
                    cloth.setClothProfile(firstUrl);
                    cloth.setCategory(category);
                    cloth.setSubcategory(subcategory);
                    final String id = reference.push().getKey();
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
                                    userCloths.clear();
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        Cloth cloth = snapshot.getValue(Cloth.class);
                                        assert cloth != null;
                                        if (cloth.getOwnerID().equals(ApplicationClass.currentUser.getUid()) && !cloth.isSold()) {
                                            userCloths.add(cloth);
                                        }
                                    }

                                    ApplicationClass.userCloths = userCloths;
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

        expandCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivCategory.setImageResource(R.drawable.expandless);

                popupMenuCategory = new PopupMenu(AddingCloth.this, v);
                popupMenuCategory.setOnMenuItemClickListener(menuItemClickListenerCategory);
                popupMenuCategory.inflate(R.menu.menu_category);
                popupMenuCategory.show();

                popupMenuCategory.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        ivCategory.setImageResource(R.drawable.expandmore);
                    }
                });
            }
        });

        expandSubCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(category != null) {
                    ivSubCategory.setImageResource(R.drawable.expandless);

                    popupMenuSubcategory = new PopupMenu(AddingCloth.this, v);
                    if (category.equals("manCloth")) {
                        popupMenuSubcategory.setOnMenuItemClickListener(menuItemClickListenerManCloth);
                        popupMenuSubcategory.inflate(R.menu.menu_subcategory_man_cloth);
                    } else if (category.equals("womanCloth")) {
                        popupMenuSubcategory.setOnMenuItemClickListener(menuItemClickListenerWomanCloth);
                        popupMenuSubcategory.inflate(R.menu.menu_subcategory_woman_cloth);
                    } else if (category.equals("manShoes")) {
                        popupMenuSubcategory.setOnMenuItemClickListener(menuItemClickListenerManShoes);
                        popupMenuSubcategory.inflate(R.menu.menu_subcategory_man_shoes);
                    } else if (category.equals("womanShoes")) {
                        popupMenuSubcategory.setOnMenuItemClickListener(menuItemClickListenerWomanShoes);
                        popupMenuSubcategory.inflate(R.menu.menu_subcategory_woman_shoes);
                    } else if (category.equals("accessories")) {
                        popupMenuSubcategory.setOnMenuItemClickListener(menuItemClickListenerAccessories);
                        popupMenuSubcategory.inflate(R.menu.menu_subcategory_accessories);
                    }
                    popupMenuSubcategory.show();

                    popupMenuSubcategory.setOnDismissListener(new PopupMenu.OnDismissListener() {
                        @Override
                        public void onDismiss(PopupMenu menu) {
                            ivSubCategory.setImageResource(R.drawable.expandmore);
                        }
                    });
                } else {
                    Toast.makeText(AddingCloth.this, "Izaberite prvo kategoriju!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private PopupMenu.OnMenuItemClickListener menuItemClickListenerCategory = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.manCloth:
                    category = "manCloth";
                    break;
                case R.id.womanCloth:
                    category = "womanCloth";
                    break;
                case R.id.manShoes:
                    category = "manShoes";
                    break;
                case R.id.womanShoes:
                    category = "womanShoes";
                    break;
                case R.id.accessories:
                    category = "accessories";
                    break;
            }

            tvCategory.setText(item.getTitle());
            tvSubCategory.setText("Potkategorija");
            popupMenuCategory.dismiss();
            ivCategory.setImageResource(R.drawable.expandmore);
            return true;
        }
    };

    private PopupMenu.OnMenuItemClickListener menuItemClickListenerManCloth = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.subcategoryTshirt:
                    subcategory = "manTshirt";
                    break;
                case R.id.subcategoryTracksuit:
                    subcategory = "manTracksuit";
                    break;
                case R.id.subcategoryJeans:
                    subcategory = "manJeans";
                    break;
                case R.id.subcategoryPants:
                    subcategory = "manPants";
                    break;
                case R.id.subcategoryHoodies:
                    subcategory = "manHoodies";
                    break;
                case R.id.subcategoryShorts:
                    subcategory = "manShorts";
                    break;
                case R.id.subcategoryJackets:
                    subcategory = "manJackets";
                    break;
                case R.id.subcategoryShirting:
                    subcategory = "manShirting";
                    break;
                case R.id.subcategorySweaters:
                    subcategory = "manSweaters";
                    break;
                case R.id.subcategoryCoat:
                    subcategory = "manCoat";
                    break;
                case R.id.subcategorySports:
                    subcategory = "manSports";
                    break;
                case R.id.subcategorySuit:
                    subcategory = "manSuit";
                    break;
                case R.id.subcategoryVest:
                    subcategory = "manVest";
                    break;
                case R.id.subcategoryUnderwear:
                    subcategory = "manUnderwear";
                    break;
                case R.id.subcategoryTrunks:
                    subcategory = "manTrunks";
                    break;
                case R.id.subcategoryPajamas:
                    subcategory = "manPajamas";
                    break;
                case R.id.subcategoryOther:
                    subcategory = "manOther";
                    break;
            }

            tvSubCategory.setText(item.getTitle());
            popupMenuSubcategory.dismiss();
            ivSubCategory.setImageResource(R.drawable.expandmore);
            return true;
        }
    };

    private PopupMenu.OnMenuItemClickListener menuItemClickListenerWomanCloth = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.subcategoryTshirt:
                    subcategory = "womanTshirt";
                    break;
                case R.id.subcategoryDresses:
                    subcategory = "womanDresses";
                    break;
                case R.id.subcategorySkirts:
                    subcategory = "womanSkirts";
                    break;
                case R.id.subcategoryOveralls:
                    subcategory = "womanOveralls";
                    break;
                case R.id.subcategoryTracksuit:
                    subcategory = "womanTracksuit";
                    break;
                case R.id.subcategoryJeans:
                    subcategory = "womanJeans";
                    break;
                case R.id.subcategoryPants:
                    subcategory = "womanPants";
                    break;
                case R.id.subcategoryBlouses:
                    subcategory = "womanBlouses";
                    break;
                case R.id.subcategoryTunics:
                    subcategory = "womanTunics";
                    break;
                case R.id.subcategoryWedding:
                    subcategory = "womanWedding";
                    break;
                case R.id.subcategoryTights:
                    subcategory = "womanTights";
                    break;
                case R.id.subcategoryHoodies:
                    subcategory = "womanHoodies";
                    break;
                case R.id.subcategoryShorts:
                    subcategory = "womanShorts";
                    break;
                case R.id.subcategoryJackets:
                    subcategory = "womanJackets";
                    break;
                case R.id.subcategoryShirting:
                    subcategory = "womanShirting";
                    break;
                case R.id.subcategorySweaters:
                    subcategory = "womanSweaters";
                    break;
                case R.id.subcategoryCoat:
                    subcategory = "womanCoat";
                    break;
                case R.id.subcategorySports:
                    subcategory = "womanSports";
                    break;
                case R.id.subcategorySuit:
                    subcategory = "womanSuit";
                    break;
                case R.id.subcategoryVest:
                    subcategory = "womanVest";
                    break;
                case R.id.subcategoryUnderwear:
                    subcategory = "womanUnderwear";
                    break;
                case R.id.subcategoryTrunks:
                    subcategory = "womanTrunks";
                    break;
                case R.id.subcategoryPajamas:
                    subcategory = "womanPajamas";
                    break;
                case R.id.subcategoryOther:
                    subcategory = "womanOther";
                    break;
            }

            tvSubCategory.setText(item.getTitle());
            popupMenuSubcategory.dismiss();
            ivSubCategory.setImageResource(R.drawable.expandmore);
            return true;
        }
    };

    private PopupMenu.OnMenuItemClickListener menuItemClickListenerManShoes = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.subcategorySneakers:
                    subcategory = "manSneakers";
                    break;
                case R.id.subcategoryShoes:
                    subcategory = "manShoes";
                    break;
                case R.id.subcategoryBoots:
                    subcategory = "manBoots";
                    break;
                case R.id.subcategorySlippers:
                    subcategory = "manSlippers";
                    break;
                case R.id.subcategorySandals:
                    subcategory = "manSandals";
                    break;
                case R.id.subcategoryOther:
                    subcategory = "manOther";
                    break;
            }

            tvSubCategory.setText(item.getTitle());
            popupMenuSubcategory.dismiss();
            ivSubCategory.setImageResource(R.drawable.expandmore);
            return true;
        }
    };

    private PopupMenu.OnMenuItemClickListener menuItemClickListenerWomanShoes = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.subcategorySneakers:
                    subcategory = "womanSneakers";
                    break;
                case R.id.subcategoryShoes:
                    subcategory = "womanShoes";
                    break;
                case R.id.subcategoryBoots:
                    subcategory = "womanBoots";
                    break;
                case R.id.subcategorySlippers:
                    subcategory = "womanSlippers";
                    break;
                case R.id.subcategorySandals:
                    subcategory = "womanSandals";
                    break;
                case R.id.subcategoryBallet:
                    subcategory = "womanBallet";
                    break;
                case R.id.subcategoryOther:
                    subcategory = "womanOther";
                    break;
            }

            tvSubCategory.setText(item.getTitle());
            popupMenuSubcategory.dismiss();
            ivSubCategory.setImageResource(R.drawable.expandmore);
            return true;
        }
    };

    private PopupMenu.OnMenuItemClickListener menuItemClickListenerAccessories = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.subcategoryBracelets:
                    subcategory = "Bracelets";
                    break;
                case R.id.subcategoryWatch:
                    subcategory = "Watch";
                    break;
                case R.id.subcategoryNecklaces:
                    subcategory = "Necklaces";
                    break;
                case R.id.subcategoryRings:
                    subcategory = "Rings";
                    break;
                case R.id.subcategoryEarrings:
                    subcategory = "Earrings";
                    break;
                case R.id.subcategoryGlasses:
                    subcategory = "Glasses";
                    break;
                case R.id.subcategoryOther:
                    subcategory = "Other";
                    break;
            }

            tvSubCategory.setText(item.getTitle());
            popupMenuSubcategory.dismiss();
            ivSubCategory.setImageResource(R.drawable.expandmore);
            return true;
        }
    };

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return result1 && result2;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    public String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Snimanje slike...");
        progressDialog.show();

        if(imageUri != null){
            final StorageReference filereferance = storageReference.child(nameImageVideo + "." + getFileExtension(imageUri));

            uploadTaskImage = filereferance.putFile(imageUri);
            uploadTaskImage.continueWithTask(new Continuation() {
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

                        if(!first){
                            firstUrl = myUrl;
                            first = true;
                        }

                        ApplicationClass.urls.add(myUrl);

                        progressDialog.dismiss();
                    } else {
                        Toast.makeText(AddingCloth.this, "Neuspesno!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddingCloth.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(AddingCloth.this, "Slika nije izabrana", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        nameImageVideo = System.currentTimeMillis() + ApplicationClass.currentUser.getUid();

        /**
         * Obrada uzete slike i postavljanje na server
         */
       if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
           CropImage.ActivityResult result = CropImage.getActivityResult(data);
           imageUri = result.getUri();

           ImageVideo imageVideo = new ImageVideo();
           imageVideo.setInfo("" + imageUri);
           imageVideo.setType("image");
           imageVideo.setName(nameImageVideo);
           ApplicationClass.imageVideosAddingCloth.add(imageVideo);
           ApplicationClass.adapterAddingClothImageVideo.notifyDataSetChanged();
           ApplicationClass.lastImage = ApplicationClass.urls.size() <= 1;

           uploadImage();
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
                    CropImage.activity().setAspectRatio(1, 1).start(AddingCloth.this);
                } else {
                    /**
                     * Nije dozvoljen pristup i onda se objasnjava zasto je potrebna
                     */
                    Toast.makeText(AddingCloth.this, "Dozvole za kameru i galeriju su potrebne!", Toast.LENGTH_SHORT).show();
                }
            } else if(grantResults.length == 2){
                CropImage.activity().setAspectRatio(1, 1).start(AddingCloth.this);
            } else {
                Toast.makeText(AddingCloth.this, "Molimo vas dozvolite pristup, kako bi mogli da ubacite sliku!", Toast.LENGTH_SHORT).show();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Animatoo.animateSlideRight(this);
        return super.onSupportNavigateUp();
    }
}

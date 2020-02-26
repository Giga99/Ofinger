package com.example.ofinger.startActivities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Image;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    Button btnLogin, btnRegister;
    TextView tvGuest;
    SignInButton btnGoogleSignIn;

    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    DatabaseReference reference, reference2;
    List<Cloth> cloths;
    List<Image> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        auth = FirebaseAuth.getInstance();

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvGuest = findViewById(R.id.tvGuest);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        reference = FirebaseDatabase.getInstance().getReference("Cloth");
        reference2 = FirebaseDatabase.getInstance().getReference("Images");
        cloths = new ArrayList<>();
        images = new ArrayList<>();

        /**
         * Logovanje uz pomoc Google
         */
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Configure Google Sign In
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                mGoogleSignInClient = GoogleSignIn.getClient(StartActivity.this, gso);

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        /**
         * Strana za logovanje
         */
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });

        /**
         * Strana za registrovanje
         */
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });

        /**
         * Nastavak bez naloga
         */
        tvGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(true);
                        tvLoad.setText("Nastavak bez naloga...");

                        ApplicationClass.currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        /**
                         * Pravljenje liste odece
                         */
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                cloths.clear();
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    Cloth cloth = snapshot.getValue(Cloth.class);
                                    cloths.add(cloth);
                                }

                                ApplicationClass.mainCloths = cloths;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(StartActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", ApplicationClass.currentUser.getUid());
                        hashMap.put("username", "Gost");
                        hashMap.put("email", "Gost");
                        hashMap.put("bio", "");
                        hashMap.put("imageURL", "default");
                        hashMap.put("status", "offline");
                        hashMap.put("typingTo", "noOne");

                        reference2.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    ApplicationClass.currentUserReference = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());
                                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                                    StartActivity.this.finish();
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Zapamceno logovanje na uredjaju, pa se onda automatski uloguje
     */
    @Override
    protected void onStart() {
        super.onStart();

        showProgress(true);
        tvLoad.setText("Provera logovanja");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(firebaseUser != null && firebaseUser.isEmailVerified()){
            ApplicationClass.currentUser = firebaseUser;
            ApplicationClass.currentUserReference = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());

            /**
             * Pravljenje liste slika
             */
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    cloths.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Cloth cloth = snapshot.getValue(Cloth.class);
                        cloths.add(cloth);
                    }

                    ApplicationClass.mainCloths = cloths;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(StartActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            showProgress(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            firebaseUser = auth.getCurrentUser();
                            ApplicationClass.currentUser = auth.getCurrentUser();
                            ApplicationClass.currentUserReference = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());

                            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", firebaseUser.getUid());
                            String username = "" + firebaseUser.getEmail().replaceAll("@gmail.com", "");
                            hashMap.put("username", username);
                            hashMap.put("email", firebaseUser.getEmail());
                            hashMap.put("bio", "");
                            hashMap.put("imageURL", "" + firebaseUser.getPhotoUrl());
                            hashMap.put("status", "offline");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("searchName", username.toLowerCase());

                            reference2.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent intent = new Intent(StartActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });

                            /**
                             * Pravljenje liste odece
                             */
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    cloths.clear();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        Cloth cloth = snapshot.getValue(Cloth.class);
                                        cloths.add(cloth);
                                    }

                                    ApplicationClass.mainCloths = cloths;
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(StartActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(StartActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
}

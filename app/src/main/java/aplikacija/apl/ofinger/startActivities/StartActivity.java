package aplikacija.apl.ofinger.startActivities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import aplikacija.apl.ofinger.mainActivities.MainActivity;
import aplikacija.apl.ofinger.models.Cloth;
import aplikacija.apl.ofinger.models.ImageVideo;

public class StartActivity extends AppCompatActivity {
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    ImageView btnLogin;
    TextView tvGuest, tvRegister;

    FirebaseUser firebaseUser;
    FirebaseAuth auth;
    DatabaseReference reference, reference2;
    List<Cloth> cloths;
    List<ImageVideo> imageVideos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        auth = FirebaseAuth.getInstance();

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        tvGuest = findViewById(R.id.tvGuest);

        reference = FirebaseDatabase.getInstance().getReference("Cloth");
        reference2 = FirebaseDatabase.getInstance().getReference("Images");
        cloths = new ArrayList<>();
        imageVideos = new ArrayList<>();

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
        tvRegister.setOnClickListener(new View.OnClickListener() {
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

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("id", ApplicationClass.currentUser.getUid());
                        hashMap.put("username", "Gost");
                        hashMap.put("email", "Gost");
                        hashMap.put("bio", "");
                        hashMap.put("imageURL", "default");
                        hashMap.put("status", "offline");
                        hashMap.put("typingTo", "noOne");
                        hashMap.put("firstLogin", true);

                        HashMap<String, Object> hashMap2 = new HashMap<>();
                        hashMap2.put("message", false);
                        hashMap2.put("follow", false);
                        hashMap2.put("wish", false);

                        hashMap.put("notifications", hashMap2);

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

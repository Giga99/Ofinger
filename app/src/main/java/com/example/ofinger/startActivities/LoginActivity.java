package com.example.ofinger.startActivities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.ImageVideo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    EditText etMail, etPassword;
    ImageView ivLogin, ivBack;
    TextView tvReset, tvReg;

    FirebaseAuth auth;
    DatabaseReference reference, reference2;
    List<Cloth> cloths;
    List<ImageVideo> imageVideos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        etMail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        ivLogin = findViewById(R.id.ivLogin);
        tvReset = findViewById(R.id.tvReset);
        tvReg = findViewById(R.id.tvReg);
        ivBack = findViewById(R.id.ivBack);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Cloth");
        reference2 = FirebaseDatabase.getInstance().getReference("Images");
        cloths = new ArrayList<>();
        imageVideos = new ArrayList<>();

        /**
         * Resetovanje sifre
         */
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        /**
         * Odlazak na Register
         */
        tvReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        /**
         * Logovanje
         */
        ivLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Moraju svi podaci da se unesu
                 */
                if(etMail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Molim vas, unesite sve podatke!", Toast.LENGTH_SHORT).show();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(etMail.getText().toString().trim()).matches()){
                    etMail.setError("Mejl nije dobar!");
                    etMail.setFocusable(true);
                } else {
                    String email = etMail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();

                    showProgress(true);
                    tvLoad.setText("Ucitavanje...");

                    /**
                     * Pristupanje serveru i trazenje korisnika
                     */
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                if(auth.getCurrentUser().isEmailVerified() || !auth.getCurrentUser().isEmailVerified()) {
                                    ApplicationClass.currentUser = auth.getCurrentUser();
                                    ApplicationClass.currentUserReference = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());

                                    /**
                                     * Pravljenje liste odece
                                     */
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            cloths.clear();
                                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                Cloth cloth = snapshot.getValue(Cloth.class);
                                                cloths.add(cloth);
                                            }

                                            ApplicationClass.mainCloths = cloths;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Molim vas verifikujte mejl!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                showProgress(false);
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

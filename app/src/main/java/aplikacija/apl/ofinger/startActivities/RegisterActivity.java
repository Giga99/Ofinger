package aplikacija.apl.ofinger.startActivities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import aplikacija.apl.ofinger.R;

public class RegisterActivity extends AppCompatActivity {
    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    EditText etUsername, etEmail, etPassword, etReEnterPassword, etBio;
    ImageView ivRegister, backIcon;
    TextView terms;
    CheckBox checkBox;

    FirebaseAuth auth;
    DatabaseReference reference;

    Boolean checked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        tvLoad = findViewById(R.id.tvLoad);

        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etReEnterPassword = findViewById(R.id.etReEnterPassword);
        ivRegister = findViewById(R.id.ivRegister);
        backIcon = findViewById(R.id.backIcon);
        checkBox = findViewById(R.id.checkBox);
        terms = findViewById(R.id.terms);

        etBio = findViewById(R.id.etBio);

        auth = FirebaseAuth.getInstance();

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checked) {
                    checkBox.setChecked(false);
                    checked = false;
                } else {
                    checkBox.setChecked(true);
                    checked = true;
                }
            }
        });

        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ofinger.flycricket.io/terms.html"));
                startActivity(browserIntent);
            }
        });

        ivRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Sva polja moraju biti popunjena
                 */
                if(etUsername.getText().toString().isEmpty() || etEmail.getText().toString().isEmpty() || etPassword.getText().toString().isEmpty() || etReEnterPassword.getText().toString().isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Molim vas, unesite sve podatke!", Toast.LENGTH_SHORT).show();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()){
                    etEmail.setError("Mejl nije dobar!");
                    etEmail.setFocusable(true);
                } else if(etPassword.length() < 6) {
                    etPassword.setError("Sifra mora biti duza od 6 karaktera!");
                    etPassword.setFocusable(true);
                } else if(!checkBox.isChecked()){
                    checkBox.setError("Morate da prihvatite uslove koriscenja!");
                    checkBox.setFocusable(true);
                } else {
                    /**
                     * Sifre moraju da se podudaraju
                     */
                    if(etPassword.getText().toString().trim().equals(etReEnterPassword.getText().toString().trim())){
                        final String username = etUsername.getText().toString().trim();
                        final String email = etEmail.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();
                        final String bio;
                        if(etBio.getText().toString().isEmpty()) bio = "";
                        else bio = etBio.getText().toString().trim();

                        showProgress(true);
                        tvLoad.setText("Registrovanje...");

                        /**
                         * Registrovanje novog korisnika na serveru
                         */
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    final FirebaseUser firebaseUser = auth.getCurrentUser();
                                    assert firebaseUser != null;

                                    firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RegisterActivity.this, "Uspesno poslat mejl!", Toast.LENGTH_SHORT).show();
                                            String userId = firebaseUser.getUid();

                                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("id", userId);
                                            hashMap.put("username", username);
                                            hashMap.put("email", email);
                                            hashMap.put("bio", bio);
                                            hashMap.put("imageURL", "default");
                                            hashMap.put("status", "offline");
                                            hashMap.put("typingTo", "noOne");
                                            hashMap.put("searchName", username.toLowerCase());
                                            hashMap.put("firstLogin", true);

                                            reference.setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    showProgress(false);
                                                    Intent intent = new Intent(RegisterActivity.this, EmailVerificationActivity.class);
                                                    intent.putExtra("id", "register");
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    showProgress(false);
                                                    Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showProgress(false);
                                            Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else{
                                    showProgress(false);
                                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        etPassword.setError("Sifre nisu iste!");
                        etReEnterPassword.setError("Sifre nisu iste!");
                        etReEnterPassword.setFocusable(true);
                        etPassword.setFocusable(true);
                    }
                }
            }
        });

        backIcon.setOnClickListener(new View.OnClickListener() {
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

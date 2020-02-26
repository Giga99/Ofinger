package com.example.ofinger.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.startActivities.EmailVerificationActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class EditEmailActivity extends AppCompatActivity {
    MaterialEditText newEmail, pass;
    MaterialButton confirmNewEmail;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Promena mejla");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        newEmail = findViewById(R.id.newEmail);
        pass = findViewById(R.id.pass);
        confirmNewEmail = findViewById(R.id.confirmNewEmail);

        confirmNewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Promena mejla...");
                progressDialog.show();

                final String newemail = newEmail.getText().toString();
                final String password = pass.getText().toString();

                /**
                 * Ne sme da bude prazno polje
                 */
                if(TextUtils.isEmpty(newemail)){
                    progressDialog.dismiss();
                    newEmail.setError("Morate popuniti ovo polje!");
                    newEmail.setFocusable(true);
                } else if(TextUtils.isEmpty(password)){
                    progressDialog.dismiss();
                    pass.setError("Morate popuniti ovo polje!");
                    pass.setFocusable(true);
                } else {
                    final FirebaseUser user = ApplicationClass.currentUser;

                    /**
                     * Provera da li je dobra sifra
                     */
                    AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);
                    user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            user.updateEmail(newemail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();

                                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("email", user.getEmail());

                                            databaseReference.updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(EditEmailActivity.this, "Uspesno promenjen mejl!", Toast.LENGTH_SHORT).show();
                                                    FirebaseAuth.getInstance().signOut();
                                                    Intent intent = new Intent(EditEmailActivity.this, EmailVerificationActivity.class);
                                                    intent.putExtra("id", "update");
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(EditEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(EditEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditEmailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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

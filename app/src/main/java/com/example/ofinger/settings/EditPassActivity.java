package com.example.ofinger.settings;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class EditPassActivity extends AppCompatActivity {
    EditText oldPass, newPass, newPassCheck;
    Button confirmNewPass;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pass);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Promena sifre");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog = new ProgressDialog(this);

        oldPass = findViewById(R.id.oldPass);
        newPass = findViewById(R.id.newPass);
        newPassCheck = findViewById(R.id.newPassCheck);
        confirmNewPass = findViewById(R.id.confirmNewPass);

        confirmNewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Promena sifre...");
                progressDialog.show();

                String oldPassword = oldPass.getText().toString();
                final String newPassword = newPass.getText().toString();
                final String newPasswordCheck = newPassCheck.getText().toString();

                /**
                 * Ne sme da bude prazno polje
                 */
                if(TextUtils.isEmpty(oldPassword)){
                    progressDialog.dismiss();
                    oldPass.setError("Morate popuniti ovo polje!");
                    oldPass.setFocusable(true);
                } else if(TextUtils.isEmpty(newPassword)){
                    progressDialog.dismiss();
                    newPass.setError("Morate popuniti ovo polje!");
                    newPass.setFocusable(true);
                } else if(TextUtils.isEmpty(newPasswordCheck)){
                    progressDialog.dismiss();
                    newPassCheck.setError("Morate popuniti ovo polje!");
                    newPassCheck.setFocusable(true);
                } else if(newPassword.length() < 6){
                    progressDialog.dismiss();
                    newPass.setError("Sifra mora da ima vise od 6 karaktera!");
                    newPass.setFocusable(true);
                } else if(!newPassword.equals(newPasswordCheck)){
                    progressDialog.dismiss();
                    newPass.setError("Sifre nisu iste!");
                    newPass.setFocusable(true);
                    newPassCheck.setError("Sifre nisu iste!");
                    newPassCheck.setFocusable(true);
                } else {
                    final FirebaseUser user = ApplicationClass.currentUser;

                    /**
                     * Provera da li je dobra sifra stara
                     */
                    AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
                    user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            /**
                             * Promena sifre
                             */
                            user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditPassActivity.this, "Uspesno promenjena sifra!", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(EditPassActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditPassActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

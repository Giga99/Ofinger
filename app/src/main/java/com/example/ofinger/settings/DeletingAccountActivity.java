package com.example.ofinger.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.models.Message;
import com.example.ofinger.models.Review;
import com.example.ofinger.startActivities.StartActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class DeletingAccountActivity extends AppCompatActivity {
    MaterialEditText etEmail, etPassword;
    Button btnDelete;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleting_account); //TODO test

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Brisanje naloga");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnDelete = findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(DeletingAccountActivity.this);
                alert.setMessage("Da li ste sigurni da zelite da izbrisete ovaj nalog?");

                alert.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog = new ProgressDialog(DeletingAccountActivity.this);
                        progressDialog.setMessage("Brisanje naloga...");
                        progressDialog.show();

                        String email = etEmail.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();

                        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                        ApplicationClass.currentUser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                deletingUser();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(DeletingAccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
                    }
                });

                alert.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                alert.show();
            }
        });
    }

    private void deletingUser() {
        for(int i = 0; i < ApplicationClass.mainCloths.size(); i++){
            if(ApplicationClass.mainCloths.get(i).getOwnerID().equals(ApplicationClass.currentUser.getUid())) deleteCloth(i);
        }
        deleteFromDatabase();

        ApplicationClass.currentUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DeletingAccountActivity.this, "Uspesno izbrisan nalog!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DeletingAccountActivity.this, StartActivity.class));
                DeletingAccountActivity.this.finish();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DeletingAccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void deleteFromDatabase() {
        /**
         * Brisanje utisaka korisnika o odeci
         */
        for(int i = 0; i < ApplicationClass.mainCloths.size(); i++){
            FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(i).getObjectId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                Review review = snapshot.getValue(Review.class);
                                if(review.getUserId().equals(ApplicationClass.currentUser.getUid())) dataSnapshot.child(review.getReviewId()).getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        /**
         * Brisanje utisaka korisnika
         */
        FirebaseDatabase.getInstance().getReference("StarsUsers").child(ApplicationClass.currentUser.getUid()).removeValue();

        /**
         * Brisanje korpe
         */
        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).removeValue();

        /**
         * Brisanje tokena
         */
        FirebaseDatabase.getInstance().getReference("Tokens").child(ApplicationClass.currentUser.getUid()).removeValue();

        /**
         * Brisanje poruka
         */
        FirebaseDatabase.getInstance().getReference("Chatlist").child(ApplicationClass.currentUser.getUid()).removeValue();
        FirebaseDatabase.getInstance().getReference("Chatlist").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child(ApplicationClass.currentUser.getUid()).exists()) snapshot.child(ApplicationClass.currentUser.getUid()).getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);
                    if(message.getSender().equals(ApplicationClass.currentUser.getUid()) || message.getReceiver().equals(ApplicationClass.currentUser.getUid()))
                        snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Brisanje pracenja
         */
        FirebaseDatabase.getInstance().getReference("Follow").child(ApplicationClass.currentUser.getUid()).removeValue();
        FirebaseDatabase.getInstance().getReference("Follow").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child("following").child(ApplicationClass.currentUser.getUid()).exists())
                        snapshot.child("following").child(ApplicationClass.currentUser.getUid()).getRef().removeValue();

                    if(snapshot.child("followers").child(ApplicationClass.currentUser.getUid()).exists())
                        snapshot.child("followers").child(ApplicationClass.currentUser.getUid()).getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Brisanje korisnika iz baze podataka
         */
        FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid()).removeValue();
    }

    private void deleteCloth(final int index){
        /**
         * Brisanje slika
         */
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.mainCloths.get(index).getObjectId()).child("urls");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(snapshot.getValue(String.class));
                    storageReference.delete();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Brisanje odela sa servera
         */
        FirebaseDatabase.getInstance().getReference("Cloth").child(ApplicationClass.mainCloths.get(index).getObjectId()).removeValue();

        for(int j = 0; j < ApplicationClass.mainCloths.size(); j++){
            /**
             * Brisanje iz liste korisnickog odela
             */
            if(ApplicationClass.mainCloths.get(index).getObjectId().equals(ApplicationClass.userCloths.get(j).getObjectId())){
                ApplicationClass.userCloths.remove(j);
                break;
            }
        }
        /**
         * Brisanje iz liste korisnicke odece
         */
        ApplicationClass.mainCloths.remove(index);

        /**
         * Brisanje utisaka odela
         */
        FirebaseDatabase.getInstance().getReference("StarsCloth").child(ApplicationClass.mainCloths.get(index).getObjectId()).removeValue();

        /**
         * Brisanje odela iz korpi drugih korisnika
         */
        FirebaseDatabase.getInstance().getReference("Wishes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child(ApplicationClass.mainCloths.get(index).getObjectId()).exists())
                        snapshot.child(ApplicationClass.mainCloths.get(index).getObjectId()).getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
    protected void onPostResume() {
        super.onPostResume();

        status("online");
        checkTypingStatus("noOne");
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
}

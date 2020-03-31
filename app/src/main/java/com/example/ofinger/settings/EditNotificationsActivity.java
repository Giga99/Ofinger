package com.example.ofinger.settings;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditNotificationsActivity extends AppCompatActivity {
    SwitchCompat notificationsMessage, notificationsFollow, notificationsWish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Promena stizanja notifikacija");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        notificationsFollow = findViewById(R.id.notificationsFollow);
        notificationsMessage = findViewById(R.id.notificationsMessage);
        notificationsWish = findViewById(R.id.notificationsWish);

        FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid()).child("notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("message").getValue().equals(true))
                            notificationsMessage.setChecked(true);
                        else notificationsMessage.setChecked(false);

                        if (dataSnapshot.child("follow").getValue().equals(true))
                            notificationsFollow.setChecked(true);
                        else notificationsFollow.setChecked(false);

                        if (dataSnapshot.child("wish").getValue().equals(true))
                            notificationsWish.setChecked(true);
                        else notificationsWish.setChecked(false);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        notificationsMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid())
                        .child("notifications").child("message").setValue(isChecked);
                if(isChecked) Toast.makeText(EditNotificationsActivity.this, "Dobijacete notifikacije o novim porukama!", Toast.LENGTH_SHORT).show();
                else Toast.makeText(EditNotificationsActivity.this, "Necete dobijati notifikacije o novim porukama!", Toast.LENGTH_SHORT).show();
            }
        });

        notificationsFollow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid())
                        .child("notifications").child("follow").setValue(isChecked);
                if(isChecked) Toast.makeText(EditNotificationsActivity.this, "Dobijacete notifikacije o novim pracenjima!", Toast.LENGTH_SHORT).show();
                else Toast.makeText(EditNotificationsActivity.this, "Necete dobijati notifikacije o novim pracenjima!", Toast.LENGTH_SHORT).show();
            }
        });

        notificationsWish.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid())
                        .child("notifications").child("wish").setValue(isChecked);
                if(isChecked) Toast.makeText(EditNotificationsActivity.this, "Dobijacete notifikacije o novim dodavanjima odece u korpu!", Toast.LENGTH_SHORT).show();
                else Toast.makeText(EditNotificationsActivity.this, "Necete dobijati notifikacije o novim dodavanjima odece u korpu!", Toast.LENGTH_SHORT).show();
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

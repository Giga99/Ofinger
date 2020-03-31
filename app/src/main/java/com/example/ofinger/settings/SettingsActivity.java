package com.example.ofinger.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {
    LinearLayout editProfInfo, editPassword, about, editEmail, editNotifications, help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Podesavanja");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editProfInfo = findViewById(R.id.editProfInfo);
        editPassword = findViewById(R.id.editPassword);
        editEmail = findViewById(R.id.editEmail);
        editNotifications = findViewById(R.id.editNotifications);
        about = findViewById(R.id.about);
        help = findViewById(R.id.help);

        editProfInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });

        editEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) startActivity(new Intent(SettingsActivity.this, EditEmailActivity.class));
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });

        editPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) startActivity(new Intent(SettingsActivity.this, EditPassActivity.class));
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });

        editNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) startActivity(new Intent(SettingsActivity.this, EditNotificationsActivity.class));
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

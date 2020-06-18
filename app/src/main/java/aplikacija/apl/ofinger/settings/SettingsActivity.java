package aplikacija.apl.ofinger.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;

public class SettingsActivity extends AppCompatActivity {
    LinearLayout editProfInfo, editPassword, about, editEmail, help;
    AdView mainAd;

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
        about = findViewById(R.id.about);
        help = findViewById(R.id.help);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        mainAd = findViewById(R.id.mainAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainAd.loadAd(adRequest);

        editProfInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) {
                    startActivity(new Intent(SettingsActivity.this, EditProfileActivity.class));
                    Animatoo.animateSlideLeft(SettingsActivity.this);
                }
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });

        editEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) {
                    startActivity(new Intent(SettingsActivity.this, EditEmailActivity.class));
                    Animatoo.animateSlideLeft(SettingsActivity.this);
                }
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });

        editPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ApplicationClass.currentUser.isAnonymous()) {
                    startActivity(new Intent(SettingsActivity.this, EditPassActivity.class));
                    Animatoo.animateSlideLeft(SettingsActivity.this);
                }
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
                if(!ApplicationClass.currentUser.isAnonymous()) {
                    Animatoo.animateSlideLeft(SettingsActivity.this);startActivity(new Intent(SettingsActivity.this, HelpActivity.class));
                }
                else Toast.makeText(SettingsActivity.this, "Napravite profil prvo!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        Animatoo.animateSlideRight(this);
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

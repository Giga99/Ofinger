package com.example.ofinger.mainActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.adding.AddingCloth;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Message;
import com.example.ofinger.models.User;
import com.example.ofinger.navigationActivities.NotificationActivity;
import com.example.ofinger.navigationActivities.WishListActivity;
import com.example.ofinger.notifications.Token;
import com.example.ofinger.settings.SettingsActivity;
import com.example.ofinger.startActivities.StartActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements ClothAdapter.ItemClicked {
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment = null;

    private CircleImageView ivProfileImage, navHeaderProfileImage;
    private TextView tvUsername;

    DatabaseReference reference;
    List<Cloth> userCloths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.wishList:
                        startActivity(new Intent(MainActivity.this, WishListActivity.class));
                        break;
                    case R.id.notificationsList:
                        startActivity(new Intent(MainActivity.this, NotificationActivity.class));
                        break;
                    case R.id.logout:
                        if (ApplicationClass.currentUser.isAnonymous()) {
                            ApplicationClass.currentUserReference.removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    ApplicationClass.currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            startActivity(new Intent(MainActivity.this, StartActivity.class));
                                            MainActivity.this.finish();
                                        }
                                    });
                                }
                            });
                        } else {
                            String timestamp = String.valueOf(System.currentTimeMillis());
                            status(timestamp);
                            checkTypingStatus("noOne");
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(MainActivity.this, StartActivity.class));
                            MainActivity.this.finish();
                        }
                        break;

                    case R.id.settings:
                        if(ApplicationClass.currentUser.isAnonymous()){
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GuestFragment()).commit();
                        } else {
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        }
                        break;
                }
                return true;
            }
        });

        ivProfileImage = findViewById(R.id.ivProfileImage);
        View hView = navView.getHeaderView(0);
        navHeaderProfileImage = hView.findViewById(R.id.navHeaderProfileImage);
        tvUsername = findViewById(R.id.tvUsername);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        /**
         * Brojanje neprocitanih poruka
         */
        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Messages");
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int num = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);

                    if(message.getReceiver().equals(ApplicationClass.currentUser.getUid()) && !message.isIsseen()) num++;
                }

                if(num > 0) {
                    BadgeDrawable messageBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_chat);
                    messageBadge.setBadgeTextColor(getResources().getColor(R.color.white));
                    messageBadge.setNumber(num);
                } else { bottomNavigationView.removeBadge(R.id.nav_chat); }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Cloth");
        userCloths = new ArrayList<>();

        /**
         * Ispisivanje imena u zavisnosti da li je gost ili ne
         */
        if(ApplicationClass.currentUser.isAnonymous()){
            tvUsername.setText("Gost");
            ivProfileImage.setImageResource(R.drawable.profimage);
        } else {
            ApplicationClass.currentUserReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    tvUsername.setText(user.getUsername());
                    if (user.getImageURL().equals("default")) {
                        ivProfileImage.setImageResource(R.drawable.profimage);
                    } else {
                        if(!isDestroyed()) {
                            Glide.with(MainActivity.this).load(user.getImageURL()).into(ivProfileImage);
                            Glide.with(MainActivity.this).load(user.getImageURL()).into(navHeaderProfileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        /**
         * Referenca na sve korisnike
         */
        ApplicationClass.allUsers = FirebaseDatabase.getInstance().getReference("Users");

        /**
         * Pravljenje liste korisnicke odece
         */
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Cloth cloth = snapshot.getValue(Cloth.class);
                    if(cloth.getOwnerID().equals(ApplicationClass.currentUser.getUid()) && !cloth.isSold()){
                        userCloths.add(cloth);
                    }
                }

                ApplicationClass.userCloths = userCloths;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        /**
         * Izbor profile ili home fragmenta u zavisnosti od zahteva
         */
        String prof = getIntent().getStringExtra("profile");

        if(prof != null){
            if(prof.equals("yes")){
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", getIntent().getStringExtra("profileid"));
                editor.apply();

                selectedFragment = new ProfileFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            } else if (prof.equals("guest")){
                selectedFragment = new GuestFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
        } else {
            selectedFragment = new HomeFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        /**
         * Apdejtovanje Tokena
         */
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    public void updateToken(String t){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(t);
        databaseReference.child(ApplicationClass.currentUser.getUid()).setValue(token);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                /**
                 * Odlazak na home ili following stranicu
                 */
                case R.id.nav_home:
                    if(ApplicationClass.currentUser.isAnonymous()){
                        selectedFragment = new GuestFragment();
                        break;
                    } else {
                        selectedFragment = new HomeFragment();
                        break;
                    }

                /**
                 * Odlazak na search stranicu
                 */
                case R.id.nav_search:
                    selectedFragment = new SearchFragment();
                    break;

                /**
                 * Odlazak na listu poruka
                 */
                case R.id.nav_chat:
                    selectedFragment = new ChatFragment();
                    break;

                /**
                 * Odlazak na stranu za dodavanje odece ako je u pitanju korsnik inace gost mora da se registruje
                 */
                case R.id.nav_add:
                    if(ApplicationClass.currentUser.isAnonymous()){
                        selectedFragment = new GuestFragment();
                        break;
                    } else {
                        startActivity(new Intent(MainActivity.this, AddingCloth.class));
                        break;
                    }

                /**
                 * Odlazak na profil
                 */
                case R.id.nav_profile:
                    if(ApplicationClass.currentUser.isAnonymous()){
                        selectedFragment = new GuestFragment();
                        break;
                    } else {
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileid", ApplicationClass.currentUser.getUid());
                        editor.apply();
                        selectedFragment = new ProfileFragment();
                        break;
                    }
            }

            if(selectedFragment != null){
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true;
        }
    };

    /**
     * Kada se klikne na odredjeno odelo odlazi se na stranu gde se nalazi informacije, zavisi da li je to korisnicka odeca ili tudja
     */
    @Override
    public void onItemClicked(int index) {
        int i = 0;
        if(selectedFragment.getClass().equals(SearchFragment.class)){
            for(Cloth cloth : ApplicationClass.mainCloths){
                if(cloth.getObjectId().equals(ApplicationClass.searchCloth.get(index).getObjectId())) break;
                i++;
            }
        } else if(selectedFragment.getClass().equals(HomeFragment.class)){
            for(Cloth cloth : ApplicationClass.mainCloths){
                if(cloth.getObjectId().equals(ApplicationClass.followingCloth.get(index).getObjectId())) break;
                i++;
            }
        } else if(selectedFragment.getClass().equals(ProfileFragment.class)){
            for(Cloth cloth : ApplicationClass.mainCloths){
                if(!ApplicationClass.sold && cloth.getObjectId().equals(ApplicationClass.profileCloth.get(index).getObjectId())) break;
                else if (cloth.getObjectId().equals(ApplicationClass.soldCloths.get(index).getObjectId())) break;
                i++;
            }
        }
        Intent intent = new Intent(MainActivity.this, ClothInfo.class);
        intent.putExtra("index", i);
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(toggle.onOptionsItemSelected(item)) return true;
        return super.onOptionsItemSelected(item);
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

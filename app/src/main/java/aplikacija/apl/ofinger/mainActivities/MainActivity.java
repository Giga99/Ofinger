package aplikacija.apl.ofinger.mainActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
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

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.adapters.ClothAdapter;
import aplikacija.apl.ofinger.adding.AddingCloth;
import aplikacija.apl.ofinger.info.ClothInfo;
import aplikacija.apl.ofinger.models.Cloth;
import aplikacija.apl.ofinger.models.Message;
import aplikacija.apl.ofinger.models.User;
import aplikacija.apl.ofinger.navigationActivities.WishListActivity;
import aplikacija.apl.ofinger.notifications.Token;
import aplikacija.apl.ofinger.settings.SettingsActivity;
import aplikacija.apl.ofinger.startActivities.StartActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements ClothAdapter.ItemClicked {
    private ActionBarDrawerToggle toggle;
    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment = null;

    private CircleImageView ivProfileImage;
    private CircleImageView navHeaderProfileImage;
    private TextView tvUsername;

    private static List<Cloth> cloths;
    private static List<Cloth> userCloths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        NavigationView navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.wishList:
                        startActivity(new Intent(MainActivity.this, WishListActivity.class));
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
                            selectedFragment = new GuestFragment();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                        } else {
                            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        }
                        break;
                    default:
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

        mainClothList();
        userClothList();
        initialize();

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

    private static void userClothList() {
        userCloths = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Cloth").addValueEventListener(new ValueEventListener() {
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

            }
        });
    }

    private static void initialize() {
        ApplicationClass.allUsers = FirebaseDatabase.getInstance().getReference("Users");
        ApplicationClass.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        ApplicationClass.currentUserReference = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());
    }

    /**
     * Pravljenje liste odece
     */
    private static void mainClothList() {
        cloths = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference("Cloth").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cloths.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Cloth cloth = snapshot.getValue(Cloth.class);
                    cloths.add(cloth);
                }

                ApplicationClass.mainCloths = cloths;
            }

            /**
             * Dodacu kasnije
             * @param databaseError
             */
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                        if(selectedFragment.getClass().equals(HomeFragment.class) || selectedFragment.getClass().equals(SearchFragment.class))
                            Animatoo.animateSlideLeft(MainActivity.this);
                        else if(selectedFragment.getClass().equals(ChatFragment.class) || selectedFragment.getClass().equals(ProfileFragment.class))
                            Animatoo.animateSlideRight(MainActivity.this);
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
                default:
                    break;
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
                if(!ApplicationClass.sold && cloth.getObjectId().equals(ApplicationClass.profileCloth.get(index).getObjectId())
                        || (ApplicationClass.sold && cloth.getObjectId().equals(ApplicationClass.soldCloths.get(index).getObjectId()))) break;
                i++;
            }
        }
        Intent intent = new Intent(MainActivity.this, ClothInfo.class);
        intent.putExtra("index", i);
        startActivity(intent);
        Animatoo.animateSlideUp(MainActivity.this);
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

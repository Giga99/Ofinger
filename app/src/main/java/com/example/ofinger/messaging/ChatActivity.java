package com.example.ofinger.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.customDialogs.CustomDialogWish;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.adapters.MessageAdapter;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.mainActivities.GuestFragment;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Message;
import com.example.ofinger.models.User;
import com.example.ofinger.notifications.APIService;
import com.example.ofinger.notifications.Client;
import com.example.ofinger.notifications.Data;
import com.example.ofinger.notifications.Response;
import com.example.ofinger.notifications.Sender;
import com.example.ofinger.notifications.Token;
import com.example.ofinger.startActivities.StartActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity implements ClothAdapter.ItemClicked {
    ImageView ivBack;
    CircleImageView ivProfileImageToolbar, navHeaderProfileImage;
    TextView tvUsernameToolbar;

    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ActionBarDrawerToggle toggle;

    MessageAdapter messageAdapter;
    List<Message> messages;
    RecyclerView messagesList;

    MaterialEditText etSend;
    ImageButton btnSend;

    Intent intent;

    DatabaseReference reference;

    ValueEventListener seenListener;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

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
                        CustomDialogWish customDialogWish = new CustomDialogWish(ChatActivity.this);
                        customDialogWish.show();
                        break;
                    case R.id.logout:
                        if (ApplicationClass.currentUser.isAnonymous()) {
                            ApplicationClass.currentUserReference.removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    ApplicationClass.currentUser.delete();
                                    FirebaseAuth.getInstance().signOut();
                                    startActivity(new Intent(ChatActivity.this, StartActivity.class));
                                    ChatActivity.this.finish();
                                }
                            });
                        } else {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(ChatActivity.this, StartActivity.class));
                            ChatActivity.this.finish();
                        }
                        break;

                    case R.id.settings:
                        if(ApplicationClass.currentUser.isAnonymous()){
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new GuestFragment()).commit();
                        } else {
                            //TODO podesavanja
                            Toast.makeText(ChatActivity.this, "Settings!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });

        ivBack = findViewById(R.id.ivBack);
        ivProfileImageToolbar = findViewById(R.id.ivProfileImageToolbar);
        tvUsernameToolbar = findViewById(R.id.tvUsernameToolbar);

        View hView = navView.getHeaderView(0);
        navHeaderProfileImage = hView.findViewById(R.id.navHeaderProfileImage);

        messagesList = findViewById(R.id.messagesList);
        messagesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(linearLayoutManager);

        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        etSend = findViewById(R.id.etSend);
        btnSend = findViewById(R.id.btnSend);

        intent = getIntent();
        final String userid = intent.getStringExtra("userId");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = etSend.getText().toString();
                if(!msg.equals("")){
                    sendMessage(ApplicationClass.currentUser.getUid(), userid, msg);
                } else {
                    Toast.makeText(ChatActivity.this, "Ne mozete da posaljete praznu poruku!", Toast.LENGTH_SHORT).show();
                }

                etSend.setText("");
            }
        });

        ApplicationClass.currentUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!isDestroyed()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (!user.getImageURL().equals("default")) {
                        Glide.with(ChatActivity.this).load(user.getImageURL()).into(navHeaderProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                tvUsernameToolbar.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    ivProfileImageToolbar.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(ivProfileImageToolbar);
                }

                ApplicationClass.otherUser = user;
                readMessages(ApplicationClass.currentUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        ivProfileImageToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                intent.putExtra("profileid", ApplicationClass.otherUser.getId());
                intent.putExtra("profile", "yes");
                startActivity(intent);
                ChatActivity.this.finish();
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        seenMessage(userid);
    }

    /**
     * Pregled poruke
     * @param userid
     */
    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Messages");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message chat = snapshot.getValue(Message.class);
                    if(chat.getReceiver().equals(ApplicationClass.currentUser.getUid()) && chat.getSender().equals(userid)){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Slanje poruke
     * @param sender
     * @param receiver
     * @param message
     */
    private void sendMessage(final String sender, final String receiver, final String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("text", message);
        hashMap.put("isseen", false);

        reference.child("Messages").push().setValue(hashMap);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(sender).child(receiver);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(receiver).child(sender);
        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(sender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(sender);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if(notify){
                    sendNotification(receiver, user.getUsername(), message);
                }

                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Slanje notifikacije
     * @param receiver
     * @param username
     * @param message
     */
    private void sendNotification(final String receiver, final String username, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(ApplicationClass.currentUser.getUid(), username + ":" + message, "Nova poruka", receiver, R.drawable.ic_launcher_background);

                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                            Toast.makeText(ChatActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Ucitavanje svih poruka
     * @param myId
     * @param userId
     * @param imageURL
     */
    private void readMessages(final String myId, final String userId, final String imageURL){
        messages = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);
                    if(message.getReceiver().equals(myId) && message.getSender().equals(userId) || message.getReceiver().equals(userId) && message.getSender().equals(myId)){
                        messages.add(message);
                    }

                    messageAdapter = new MessageAdapter( ChatActivity.this , messages, imageURL);
                    messagesList.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    private void status(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        ApplicationClass.currentUserReference.updateChildren(hashMap);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        if(!ApplicationClass.currentUser.isAnonymous()) {
            status("offline");
        }
    }

    @Override
    public void onItemClicked(int index) {
        int i = 0;
        for (Cloth cloth : ApplicationClass.mainCloths) {
            if (ApplicationClass.wish) {
                if (cloth.getObjectId().equals(ApplicationClass.wishCloths.get(index).getObjectId())) break;
            }
            i++;
        }
        Intent intent = new Intent(ChatActivity.this, ClothInfo.class);
        intent.putExtra("index", i);
        startActivityForResult(intent, 1);
    }
}

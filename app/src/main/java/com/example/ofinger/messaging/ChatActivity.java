package com.example.ofinger.messaging;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.adapters.ClothAdapter;
import com.example.ofinger.adapters.MessageAdapter;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.Image;
import com.example.ofinger.models.Message;
import com.example.ofinger.models.User;
import com.example.ofinger.notifications.Data;
import com.example.ofinger.notifications.Sender;
import com.example.ofinger.notifications.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements ClothAdapter.ItemClicked {
    private static final int REQUEST_CODE_ADD = 1;
    private static final int REQUEST_CODE_TAKE = 2;

    CircleImageView ivProfileImageToolbar;
    TextView tvUsernameToolbar, tvStatusToolbar;

    MessageAdapter messageAdapter;
    List<Message> messages;
    RecyclerView messagesList;

    MaterialEditText etSend;
    ImageButton btnSend, ibMore;

    Intent intent;
    String userid;

    DatabaseReference reference;

    ValueEventListener seenListener;

    private RequestQueue requestQueue;
    private boolean notify = false;

    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference("messageImages");

        ivProfileImageToolbar = findViewById(R.id.ivProfileImageToolbar);
        tvUsernameToolbar = findViewById(R.id.tvUsernameToolbar);
        tvStatusToolbar = findViewById(R.id.tvStatusToolbar);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        messagesList = findViewById(R.id.messagesList);
        messagesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        messagesList.setLayoutManager(linearLayoutManager);

        etSend = findViewById(R.id.etSend);
        btnSend = findViewById(R.id.btnSend);
        ibMore = findViewById(R.id.ibMore);

        intent = getIntent();
        userid = intent.getStringExtra("userId");

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

        ibMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ChatActivity.this, v);
                popupMenu.setOnMenuItemClickListener(menuItemClickListener);
                popupMenu.inflate(R.menu.menu_message_more);
                popupMenu.show();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                tvUsernameToolbar.setText(user.getUsername());

                String status = user.getStatus();
                String typing = "" + dataSnapshot.child("typingTo").getValue();

                if(typing.equals(ApplicationClass.currentUser.getUid())){
                    tvStatusToolbar.setText("kuca poruku...");
                }else {
                    if(status.equals("online")) tvStatusToolbar.setText(user.getStatus());
                    else {
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(status));
                        String datetime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                        tvStatusToolbar.setText("Poslednji put vidjen/na: " + datetime);
                    }
                }

                if(user.getImageURL().equals("default")){
                    ivProfileImageToolbar.setImageResource(R.drawable.profimage);
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

        seenMessage(userid);

        /**
         * Provera promene teksta
         */
        etSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length() == 0) checkTypingStatus("noOne");
                else checkTypingStatus(userid);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.messageImage:
                    if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD);
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_TAKE);
                    } else if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD);
                    } else if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_TAKE);
                    } else {
                        notify = true;
                        CropImage.activity().setAspectRatio(1, 1).start(ChatActivity.this);
                    }
                    break;
                case R.id.messageVideo:
                    Toast.makeText(ChatActivity.this, "Video!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.messageTemplate:
                    Toast.makeText(ChatActivity.this, "Sablon!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.messageVoice:
                    Toast.makeText(ChatActivity.this, "Govorna!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.messageLocation:
                    Toast.makeText(ChatActivity.this, "Lokacija!", Toast.LENGTH_SHORT).show();
                    break;
            }

            return true;
        }
    };

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void sendImage(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Snimanje slike...");
        progressDialog.show();

        if(imageUri != null){
            final StorageReference filereferance = storageReference.child(System.currentTimeMillis() + ApplicationClass.currentUser.getUid() + "." + getFileExtension(imageUri));

            uploadTask = filereferance.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()) throw task.getException();
                    return filereferance.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        String timestamp = String.valueOf(System.currentTimeMillis());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", ApplicationClass.currentUser.getUid());
                        hashMap.put("receiver", userid);
                        hashMap.put("text", myUrl);
                        hashMap.put("type", "image");
                        hashMap.put("timestamp", timestamp);
                        hashMap.put("isseen", false);

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
                        databaseReference.push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(ApplicationClass.currentUser.getUid()).child(userid);
                                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.exists()){
                                            chatRef.child("id").setValue(userid);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(ApplicationClass.currentUser.getUid()).child(userid);
                                chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(!dataSnapshot.exists()){
                                            chatRef2.child("id").setValue(ApplicationClass.currentUser.getUid());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                DatabaseReference databaseReference2 = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());
                                databaseReference2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);

                                        if(notify){
                                            sendNotification(userid, user.getUsername(), "Vam je poslao sliku!");
                                        }

                                        notify = false;
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        Toast.makeText(ChatActivity.this, "Neuspesno!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(ChatActivity.this, "Slika nije izabrana", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         * Obrada uzete slike i postavljanje na server
         */
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            Image image = new Image();
            image.setInfo("" + imageUri);

            sendImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            /**
             * Dozvoljen je pristup i onda se uzima slika
             */
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                CropImage.activity().setAspectRatio(1, 1).start(ChatActivity.this);
            } else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                /**
                 * Nije dozvoljen pristup i onda se ponovo trazi dozvola sa objasnjenjem zasto je potrebna
                 */
                if(ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Ova dozvola je potrebna kako bi dodali sliku u aplikaciju. Molim vas dozvolite!").setTitle("Zahtev za vaznu dozvolu!");

                    builder.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ADD);
                        }
                    });

                    builder.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(ChatActivity.this, "Ne moze se dodati!", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.show();
                } else {
                    Toast.makeText(ChatActivity.this, "Nikad vas vise necemo pitati!", Toast.LENGTH_SHORT).show();
                }
            }
        }
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

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("text", message);
        hashMap.put("type", "text");
        hashMap.put("timestamp", timestamp);
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
     * Slanje notifikacije za poruku
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
                    Data data = new Data(ApplicationClass.currentUser.getUid(), username + ": " + message, "Nova poruka", receiver, R.drawable.ic_launcher_background, "message");

                    Sender sender = new Sender(data, token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send",
                                senderJsonObj, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("JSON_RESPONSE", "onResponse: " + response.toString());
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("JSON_RESPONSE", "onResponse: " + error.toString());
                            }
                        }){
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=AAAAtNK1qRw:APA91bHJLg399DpOkV6U_EU2OPkC3Uu1L3NM9Lbn4C79ogYXvPQYjNoP6twQ5kVjF9WcsESShq-kFKFpcL-HoMnuvi_7iww6095qHb2NEm3NtOjMrb_n5He8fm-Z3rujPOQuMfibrpvI");

                                return headers;
                            }
                        };

                        requestQueue.add(jsonObjectRequest);

                    } catch (JSONException e) {
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reference.removeEventListener(seenListener);
        if(!ApplicationClass.currentUser.isAnonymous()) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            status(timestamp);
            checkTypingStatus("noOne");
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}

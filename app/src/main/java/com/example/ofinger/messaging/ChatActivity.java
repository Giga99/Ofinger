package com.example.ofinger.messaging;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.ofinger.adapters.MessageAdapter;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.ImageVideo;
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
import com.google.android.material.textview.MaterialTextView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int VIDEO_REQUEST_CODE = 200;
    private static final int AUDIO_REQUEST_CODE = 300;
    String[] cameraPermissions, audioPermissions;

    CircleImageView ivProfileImageToolbar;
    TextView tvUsernameToolbar, tvStatusToolbar;

    MessageAdapter messageAdapter;
    List<Message> messages;
    RecyclerView messagesList;

    EditText etSend;
    ImageButton btnSend, audioRecord;

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

    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cameraPermissions = new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
        audioRecord = findViewById(R.id.audioRecord);

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

        audioRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Govorna!", Toast.LENGTH_SHORT).show(); //TODO
            }
        });
    }

    private PopupMenu.OnMenuItemClickListener menuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()){
                case R.id.messageImage:
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    } else {
                        notify = true;
                        CropImage.activity().setAspectRatio(1, 1).start(ChatActivity.this);
                    }
                    break;
                case R.id.messageVideo:
                    Toast.makeText(ChatActivity.this, "Video!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.messageTemplate:
                    sendTemplate();
                    break;
                case R.id.messageLocation:
                    Toast.makeText(ChatActivity.this, "Lokacija!", Toast.LENGTH_SHORT).show();
                    break;
            }

            return true;
        }
    };

    private boolean checkCameraPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        return result1 && result2;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private boolean checkAudioPermission(){
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        return result1 && result2;
    }

    private void requestAudioPermission(){
        ActivityCompat.requestPermissions(this, audioPermissions, AUDIO_REQUEST_CODE);
    }

    private void stopRecording() {
        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //mediaRecorder.setOutputFile(outputPath);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mediaRecorder.start();
    }

    private void sendTemplate() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_send_message_template, null);
        final MaterialTextView tvName = view.findViewById(R.id.tvName);
        final MaterialEditText etName = view.findViewById(R.id.etName);
        final MaterialTextView tvNumber = view.findViewById(R.id.tvNumber);
        final MaterialEditText etNumber = view.findViewById(R.id.etNumber);
        final MaterialTextView tvCity = view.findViewById(R.id.tvCity);
        final MaterialEditText etCity = view.findViewById(R.id.etCity);
        final MaterialTextView tvPostNumber = view.findViewById(R.id.tvPostNumber);
        final MaterialEditText etPostNumber = view.findViewById(R.id.etPostNumber);
        final MaterialTextView tvAddress = view.findViewById(R.id.tvAddress);
        final MaterialEditText etAddress = view.findViewById(R.id.etAddress);
        ImageView btnSendTemplate = view.findViewById(R.id.btnSendTemplate);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        btnSendTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etName.getText().toString().isEmpty()){
                    etName.setError("Molimo vas unesite ime!");
                    etName.setFocusable(true);
                } else if(etNumber.getText().toString().isEmpty()){
                    etNumber.setError("Molimo vas unesite broj telefona!");
                    etNumber.setFocusable(true);
                } else if(etCity.getText().toString().isEmpty()){
                    etCity.setError("Molimo vas unesite grad i drzavu!");
                    etCity.setFocusable(true);
                } else if(etPostNumber.getText().toString().isEmpty()){
                    etPostNumber.setError("Molimo vas unesite postanski broj!");
                    etPostNumber.setFocusable(true);
                } else if(etAddress.getText().toString().isEmpty()){
                    etAddress.setError("Molimo vas unesite ulicu i broj!");
                    etAddress.setFocusable(true);
                } else {
                    String message = tvName.getText().toString() + "\n     " + etName.getText().toString() + "\n" +
                                    tvNumber.getText().toString() + "\n     " + etNumber.getText().toString() + "\n" +
                                    tvCity.getText().toString() + "\n     " + etCity.getText().toString() + "\n" +
                                    tvPostNumber.getText().toString() + "\n     " + etPostNumber.getText().toString() + "\n" +
                                    tvAddress.getText().toString() + "\n     " + etAddress.getText().toString();

                    sendMessage(ApplicationClass.currentUser.getUid(), userid, message);
                    dialog.dismiss();
                }
            }
        });
    }

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
                                        final User user = dataSnapshot.getValue(User.class);

                                        FirebaseDatabase.getInstance().getReference("Mute").child(ApplicationClass.currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(!dataSnapshot.child(userid).exists()) {
                                                    FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid())
                                                            .child("notifications").addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.child("message").getValue().equals(true)){
                                                                if(notify){
                                                                    sendNotification(userid, user.getUsername(), "Vam je poslao sliku!");
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

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

            ImageVideo imageVideo = new ImageVideo();
            imageVideo.setInfo("" + imageUri);
            imageVideo.setType("image");

            sendImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_REQUEST_CODE){
            if(grantResults.length > 0) {
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                /**
                 * Dozvoljen je pristup i onda se uzima slika
                 */
                if (cameraAccepted && storageAccepted) {
                    CropImage.activity().setAspectRatio(1, 1).start(ChatActivity.this);
                } else {
                    /**
                     * Nije dozvoljen pristup i onda se objasnjava zasto je potrebna
                     */
                    Toast.makeText(ChatActivity.this, "Dozvole za kameru i galeriju su potrebne!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ChatActivity.this, "Molimo vas dozvolite pristup, kako bi mogli da ubacite sliku!", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == AUDIO_REQUEST_CODE){
            if(grantResults.length > 0) {
                boolean audioAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                /**
                 * Dozvoljen je pristup i onda se uzima slika
                 */
                if (audioAccepted && storageAccepted) {
                    //startRecording();
                } else {
                    /**
                     * Nije dozvoljen pristup i onda se objasnjava zasto je potrebna
                     */
                    Toast.makeText(ChatActivity.this, "Dozvole za mikrofon i galeriju su potrebne!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ChatActivity.this, "Molimo vas dozvolite pristup, kako bi mogli da snimite glasovnu poruku!", Toast.LENGTH_SHORT).show();
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

        String id = reference.child("Messages").push().getKey();
        hashMap.put("objectId", id);

        reference.child("Messages").child(id).setValue(hashMap);

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

        FirebaseDatabase.getInstance().getReference("Users").child(sender)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);

                        FirebaseDatabase.getInstance().getReference("Mute").child(ApplicationClass.currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.child(receiver).exists()) {
                                    FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid())
                                            .child("notifications").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.child("message").getValue().equals(true)) {
                                                if (notify) {
                                                    sendNotification(receiver, user.getUsername(), message);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

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
                    Data data = new Data( "" + ApplicationClass.currentUser.getUid(), username + ": " + message, "Nova poruka", "" + receiver, R.drawable.ic_launcher_background, "message");

                    Sender sender = new Sender(data, token.getToken());

                    try {
                        JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send",
                                senderJsonObj, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}

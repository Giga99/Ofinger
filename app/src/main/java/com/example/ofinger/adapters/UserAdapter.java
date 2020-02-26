package com.example.ofinger.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
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
import com.example.ofinger.mainActivities.ProfileFragment;
import com.example.ofinger.models.User;
import com.example.ofinger.notifications.Data;
import com.example.ofinger.notifications.Sender;
import com.example.ofinger.notifications.Token;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context context;
    private List<User> users;
    private RequestQueue requestQueue;

    public UserAdapter(Context context, List<User> list){
        this.context = context;
        users = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user_search, parent, false);

        requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = users.get(position);

        holder.tvUsername.setText(user.getUsername());
        if (user.getImageURL().equals("default")) {
            holder.ivProfileImage.setImageResource(R.drawable.profimage);
        } else {
            Glide.with(context).load(user.getImageURL()).into(holder.ivProfileImage);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(ApplicationClass.currentUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(user.getId()).exists()){
                    holder.btnFollowing.setText("Pratite");
                } else holder.btnFollowing.setText("Zapratite");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = context.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", user.getId());
                editor.apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });

        holder.btnFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.btnFollowing.getText().toString().equals("Zapratite")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("followers").child(ApplicationClass.currentUser.getUid()).setValue(true);

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid());
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user2 = dataSnapshot.getValue(User.class);

                            sendNotification(user.getId(), user2.getUsername());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Jeste sigurni da zelite da otpratite korinsika?");

                    dialog.setPositiveButton("DA", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(ApplicationClass.currentUser.getUid()).child("following").child(user.getId()).removeValue();
                            FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("followers").child(ApplicationClass.currentUser.getUid()).removeValue();
                        }
                    });

                    dialog.setNegativeButton("NE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    dialog.show();
                }
            }
        });
    }

    /**
     * Slanje notifikacije za pracenje
     * @param receiver
     * @param username
     */
    private void sendNotification(final String receiver, final String username) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(ApplicationClass.currentUser.getUid(), username + " vas je zapratio!", "Novo pracenje", receiver, R.drawable.ic_launcher_background, "follow");

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
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView ivProfileImage;
        public TextView tvUsername;
        public Button btnFollowing;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnFollowing = itemView.findViewById(R.id.btnFollowing);
        }
    }
}

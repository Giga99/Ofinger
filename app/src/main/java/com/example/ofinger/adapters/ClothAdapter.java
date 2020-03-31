package com.example.ofinger.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.models.Cloth;
import com.example.ofinger.models.User;
import com.example.ofinger.notifications.Data;
import com.example.ofinger.notifications.Sender;
import com.example.ofinger.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ClothAdapter extends RecyclerView.Adapter<ClothAdapter.ViewHolder> {
    private List<Cloth> cloths;
    Context context;
    ItemClicked activity;
    private RequestQueue requestQueue;

    public interface ItemClicked{
        void onItemClicked(int index);
    }

    public ClothAdapter(Context context, List<Cloth> list) {
        cloths = list;
        this.context = context;
        activity = (ItemClicked) context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTimestamp, tvOwnerName, tvPrice, tvClothName;
        ImageView ivCloth, ivShare;
        Button ivWish, ivInfo;
        CircleImageView ivUserProfImage;
        MaterialTextView numOfWishes, numOfReviews;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvOwnerName = itemView.findViewById(R.id.tvOwnerName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            ivCloth = itemView.findViewById(R.id.ivCloth);
            ivWish = itemView.findViewById(R.id.ivWish);
            ivInfo = itemView.findViewById(R.id.ivInfo);
            ivUserProfImage = itemView.findViewById(R.id.ivUserProfImage);
            tvClothName = itemView.findViewById(R.id.tvClothName);
            numOfWishes = itemView.findViewById(R.id.numOfWishes);
            numOfReviews = itemView.findViewById(R.id.numOfReviews);
            ivShare = itemView.findViewById(R.id.ivShare);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onItemClicked(cloths.indexOf((Cloth) v.getTag()));
                }
            });

            itemView.setFocusable(true);
        }
    }

    @NonNull
    @Override
    public ClothAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);

        requestQueue = Volley.newRequestQueue(context.getApplicationContext());

        return new ClothAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ClothAdapter.ViewHolder holder, final int position) {
        holder.itemView.setTag(cloths.get(position));

        holder.itemView.requestFocus();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(cloths.get(position).getOwnerID());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                holder.tvOwnerName.setText(user.getUsername());
                if(!user.getImageURL().equals("default")) Picasso.get().load(user.getImageURL()).into(holder.ivUserProfImage);
                    //Glide.with(context).load(user.getImageURL()).into(holder.ivUserProfImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * Izbor prve slike koja je za dato odelo
         */
        Picasso.get().load(cloths.get(position).getClothProfile()).into(holder.ivCloth);

        final String name = cloths.get(position).getName();
        holder.tvClothName.setText(name);

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("sr_Latn_RS", "RS"));
        final String price = format.format(cloths.get(position).getPrice());
        holder.tvPrice.setText(price);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(cloths.get(position).getTimestamp()));
        String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
        holder.tvTimestamp.setText(time);

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("StarsCloth").child(cloths.get(position).getObjectId());
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.numOfReviews.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Wishes");
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int num = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(snapshot.child(cloths.get(position).getObjectId()).exists()) num++;
                }

                holder.numOfWishes.setText("" + num);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.ivInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 0;
                while(true){
                    if(ApplicationClass.mainCloths.get(i).getObjectId().equals(cloths.get(position).getObjectId())) break;
                    else i++;
                }

                Intent intent = new Intent(context, ClothInfo.class);
                intent.putExtra("index", i);
                context.startActivity(intent);
            }
        });

        if(cloths.get(position).getOwnerID().equals(ApplicationClass.currentUser.getUid())){
            holder.ivWish.setVisibility(View.GONE);
        } else {
            isWish(cloths.get(position).getObjectId(), holder.ivWish);
            holder.ivWish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.ivWish.getTag().equals("notwish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(cloths.get(position).getObjectId()).setValue(true);

                        final String timestamp = String.valueOf(System.currentTimeMillis());

                        FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid())
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final User user = dataSnapshot.getValue(User.class);

                                        FirebaseDatabase.getInstance().getReference("Users").child(ApplicationClass.currentUser.getUid()).child("notifications")
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.child("wish").getValue().equals(true))
                                                            sendNotification(cloths.get(position).getOwnerID(), user.getUsername(), cloths.get(position).getObjectId());
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                        HashMap<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("pId", cloths.get(position).getObjectId());
                                        hashMap.put("timestamp", timestamp);
                                        hashMap.put("sUid", user.getId());
                                        hashMap.put("pUid", cloths.get(position).getOwnerID());
                                        hashMap.put("notification", user.getUsername() + " je dodao vase odelo u korpu");
                                        hashMap.put("sName", user.getUsername());
                                        hashMap.put("sImage", user.getImageURL());
                                        hashMap.put("type", "post");

                                        String idNotification = FirebaseDatabase
                                                .getInstance().getReference("Notifications").push().getKey();

                                        FirebaseDatabase.getInstance().getReference("Notifications").child(idNotification)
                                                .setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) Toast.makeText(context, "Uspesno poslata notifikacija!", Toast.LENGTH_SHORT).show();
                                                else Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    } else if (holder.ivWish.getTag().equals("wish")) {
                        FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid()).child(cloths.get(position).getObjectId()).removeValue();
                    }
                }
            });
        }

        holder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCloth(name, price, position);
            }
        });
    }

    private void shareCloth(String name, String price, int position) {
        String shareBody = name + "\n" + price;
        Uri uri = Uri.parse(cloths.get(position).getClothProfile());

        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri); //TODO testiranje
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/*");
        context.startActivity(Intent.createChooser(sIntent, "Podeli preko"));
    }

    /**
     * Slanje notifikacije za pracenje
     * @param receiver
     * @param username
     */
    private void sendNotification(final String receiver, final String username, final String postId) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(postId, username + " je dodao vasu odecu u korpu!!", "Novo odelo u korpi", receiver, R.drawable.ic_launcher_background, "wish");

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
        return cloths.size();
    }

    private void isWish(final String clothid, final Button button){
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Wishes").child(ApplicationClass.currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(clothid).exists()){
                    Drawable img = context.getResources().getDrawable( R.drawable.wishlist );
                    button.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    button.setTag("wish");
                } else {
                    Drawable img = context.getResources().getDrawable( R.drawable.notwishlist );
                    button.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                    button.setTag("notwish");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

package aplikacija.apl.ofinger.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.mainActivities.MainActivity;
import aplikacija.apl.ofinger.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Message> messages;
    private String imageURL;

    public MessageAdapter(Context mContext, List<Message> mChat, String imageURL){
        this.mContext = mContext;
        this.messages = mChat;
        this.imageURL = imageURL;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView showMessage, tvTime;
        ImageView profileImageMessage, messageIv;
        TextView txtSeen;
        RelativeLayout messageLayout;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.showMessage);
            profileImageMessage = itemView.findViewById(R.id.profileImageMessage);
            txtSeen = itemView.findViewById(R.id.txtSeen);
            tvTime = itemView.findViewById(R.id.tvTime);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            messageIv = itemView.findViewById(R.id.messageIv);
        }
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, final int position) {
        Message chat = messages.get(position);

        String time = chat.getTimestamp();
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(time));
        String datetime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        if(chat.getType().equals("text")){
            holder.showMessage.setText(chat.getText());
            holder.messageIv.setVisibility(View.GONE);
        } else {
            holder.showMessage.setVisibility(View.GONE);
            Picasso.get().load(chat.getText()).into(holder.messageIv);
        }

        holder.tvTime.setText(datetime);

        if(imageURL.equals("default")){
            holder.profileImageMessage.setImageResource(R.drawable.profimage);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profileImageMessage);
        }

        holder.profileImageMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("profileid", ApplicationClass.otherUser.getId());
                intent.putExtra("profile", "yes");
                mContext.startActivity(intent);
            }
        });

        if(position == messages.size() - 1){
            if(chat.isIsseen()){
                holder.txtSeen.setText("Seen");
            } else {
                holder.txtSeen.setText("Delivered");
            }
        } else {
            holder.txtSeen.setVisibility(View.GONE);
        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Brisanje poruke");
                builder.setMessage("Da li ste sigurni da zelite da izbrisete poruku?");

                builder.setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });

                builder.setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });

        if(holder.showMessage.getText().toString().equals("Ova poruka je obrisana...")) {
            holder.showMessage.setBackgroundColor(Color.WHITE);
            holder.showMessage.setTextColor(Color.BLACK);
            holder.tvTime.setVisibility(View.GONE);
        }
    }

    private void deleteMessage(int position) {
        if(messages.get(position).getType().equals("image")){
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(messages.get(position).getText());
            storageReference.delete();
        }

        String msgTimeStamp = messages.get(position).getTimestamp();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");
        Query query = databaseReference.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if(ApplicationClass.currentUser.getUid().equals(snapshot.child("sender").getValue())){
                        //brisanje
                        //snapshot.getRef().removeValue();

                        //promena teksta
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("text", "Ova poruka je obrisana...");
                        hashMap.put("type", "text");
                        snapshot.getRef().updateChildren(hashMap);
                        Toast.makeText(mContext, "Poruka izbrisana!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, "Mozete samo vase poruke da obrisete!", Toast.LENGTH_SHORT).show();
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
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSender().equals(ApplicationClass.currentUser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}

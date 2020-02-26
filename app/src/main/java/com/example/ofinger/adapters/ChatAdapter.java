package com.example.ofinger.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.messaging.ChatActivity;
import com.example.ofinger.models.Message;
import com.example.ofinger.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    private String theLastMessage;

    public ChatAdapter(Context mContext, List<User> mUsers, boolean ischat){
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.ischat = ischat;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvUsername, numOfMess;
        public CircleImageView profileImage;
        private CircleImageView imgOn, imgOff, imgNotSeen;
        private TextView tvLastMsg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            profileImage = itemView.findViewById(R.id.profileImage);
            imgOff = itemView.findViewById(R.id.imgOff);
            imgOn = itemView.findViewById(R.id.imgOn);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
            numOfMess = itemView.findViewById(R.id.numOfMess);
            imgNotSeen = itemView.findViewById(R.id.imgNotSeen);
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chats_row, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatAdapter.ViewHolder holder, int position) {
        final User user = mUsers.get(position);

        holder.tvUsername.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.drawable.profimage);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }

        if(ischat){
            lastMessage(user.getId(), holder.tvLastMsg);
        } else {
            holder.tvLastMsg.setVisibility(View.GONE);
        }

        if(ischat){
            if(user.getStatus().equals("online")){
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            } else {
                holder.imgOff.setVisibility(View.VISIBLE);
                holder.imgOn.setVisibility(View.GONE);
            }
        } else {
            holder.imgOff.setVisibility(View.GONE);
            holder.imgOn.setVisibility(View.GONE);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int num = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message message = snapshot.getValue(Message.class);

                    if(message.getReceiver().equals(ApplicationClass.currentUser.getUid()) && !message.isIsseen()) num++;
                }

                if(num > 0) {
                    holder.numOfMess.setText("" + num);
                    holder.imgNotSeen.setVisibility(View.VISIBLE);
                } else {
                    holder.imgNotSeen.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message chat = snapshot.getValue(Message.class);
                    if(chat.getReceiver().equals(ApplicationClass.currentUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getSender().equals(ApplicationClass.currentUser.getUid()) && chat.getReceiver().equals(userid)){
                        if(chat.getType().equals("image")){
                            theLastMessage = "Poslata slika";
                        } else if(chat.getType().equals("text")) {
                            theLastMessage = chat.getText();
                        }
                    }
                }

                switch (theLastMessage){
                    case "default":
                        last_msg.setText("No messages");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

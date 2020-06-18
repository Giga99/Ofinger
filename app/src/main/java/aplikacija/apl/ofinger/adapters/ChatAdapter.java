package aplikacija.apl.ofinger.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import aplikacija.apl.ofinger.ApplicationClass;
import aplikacija.apl.ofinger.R;
import aplikacija.apl.ofinger.messaging.ChatActivity;
import aplikacija.apl.ofinger.models.Message;
import aplikacija.apl.ofinger.models.User;
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

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvUsername, numOfMess, tvLastMsg;
        private CircleImageView profileImage;
        private CircleImageView active;
        private ImageView ivBlocked;
        private CardView rowBackground;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsername);
            profileImage = itemView.findViewById(R.id.profileImage);
            active = itemView.findViewById(R.id.active);
            tvLastMsg = itemView.findViewById(R.id.tvLastMsg);
            numOfMess = itemView.findViewById(R.id.numOfMess);
            ivBlocked = itemView.findViewById(R.id.ivBlocked);
            rowBackground = itemView.findViewById(R.id.rowBackground);
        }
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chats_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatAdapter.ViewHolder holder, int position) {
        final User user = mUsers.get(position);

        FirebaseDatabase.getInstance().getReference("Block").child(ApplicationClass.currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(user.getId()).exists()) {
                    user.setBlocked(true);
                    holder.ivBlocked.setVisibility(View.VISIBLE);
                    holder.numOfMess.setVisibility(View.GONE);
                } else {
                    user.setBlocked(false);

                    if(ischat){
                        lastMessage(user.getId(), holder.tvLastMsg);
                    } else {
                        holder.tvLastMsg.setVisibility(View.GONE);
                    }

                    if(ischat){
                        if(user.getStatus().equals("online")){
                            holder.active.setBackground(mContext.getDrawable(R.drawable.unactive));
                        } else {
                            holder.active.setBackground(mContext.getDrawable(R.drawable.active));
                        }
                    } else {
                        holder.active.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.tvUsername.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profileImage.setImageResource(R.drawable.profimage);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profileImage);
        }

        if(!user.isBlocked()) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int num = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Message message = snapshot.getValue(Message.class);

                        if (message.getReceiver().equals(ApplicationClass.currentUser.getUid()) && message.getSender().equals(user.getId()) && !message.isIsseen())
                            num++;
                    }

                    if (num > 0) {
                        holder.numOfMess.setVisibility(View.VISIBLE);
                        holder.numOfMess.setText("" + num);
                        holder.rowBackground.setBackground(mContext.getDrawable(R.drawable.unseenmessage));
                    } else {
                        holder.numOfMess.setVisibility(View.GONE);
                        holder.rowBackground.setBackground(mContext.getDrawable(R.drawable.seenmessage));
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
                    Animatoo.animateSlideUp(mContext);
                }
            });
        }
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
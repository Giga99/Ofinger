package com.example.ofinger.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.models.Notification;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Notification> notifications;

    public NotificationAdapter(Context context, ArrayList<Notification> list){
        this.context = context;
        this.notifications = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Notification notification = notifications.get(position);

        if(!notification.getsImage().equals("default")){
            Glide.with(context).load(notification.getsImage()).into(holder.userProfileImage);
        }

        holder.tvUsername.setText(notification.getsName());
        holder.tvNotification.setText(notification.getNotification());

        final Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(notification.getTimestamp()));
        String time = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.tvTime.setText(time);

        holder.userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("profile", "yes");
                intent.putExtra("profileid", notification.getsUid());
                context.startActivity(intent);
            }
        });

        holder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notification.getType().equals("follow")) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("profile", "yes");
                    intent.putExtra("profileid", notification.getsUid());
                    context.startActivity(intent);
                } else if(notification.getType().equals("post")){
                    int i = 0;
                    for(; i < ApplicationClass.mainCloths.size(); i++){
                        if(ApplicationClass.mainCloths.get(i).getObjectId().equals(notification.getpId())) break;
                    }

                    Intent intent = new Intent(context, ClothInfo.class);
                    intent.putExtra("index", i);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView userProfileImage;
        MaterialTextView tvUsername, tvTime, tvNotification;
        LinearLayout row;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            tvNotification = itemView.findViewById(R.id.tvNotification);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            row = itemView.findViewById(R.id.row);
        }
    }
}

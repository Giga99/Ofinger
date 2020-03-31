package com.example.ofinger.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.R;
import com.example.ofinger.info.ClothInfo;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.messaging.ChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


public class NotificationService extends FirebaseMessagingService {
    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String type = remoteMessage.getData().get("notificationType");

        if(type.equals("PostNotification")){
            final String pId = remoteMessage.getData().get("pId");
            String sender = remoteMessage.getData().get("sender");
            final String title = remoteMessage.getData().get("title");
            final String body = remoteMessage.getData().get("body");

            if(!sender.equals(ApplicationClass.currentUser.getUid())) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Follow").child(sender).child("followers");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(ApplicationClass.currentUser.getUid()).exists()) showPostNotification(pId, title, body);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        } else {
            String sent = remoteMessage.getData().get("sent");
            String user = remoteMessage.getData().get("user");

            if (ApplicationClass.currentUser != null && sent.equals(ApplicationClass.currentUser.getUid())) {
                if (!ApplicationClass.currentUser.getUid().equals(user)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendOAndAboveNotification(remoteMessage);
                    } else {
                        sendNormalNotification(remoteMessage);
                    }
                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String sender = remoteMessage.getData().get("sender");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("notificationType");

        int i = Integer.parseInt(sender.replaceAll("[\\D]", ""));

        Intent intent = null;
        if(type.equals("message")){
            intent = new Intent(this, ChatActivity.class);
        } else if(type.equals("follow")){
            intent = new Intent(this, MainActivity.class);
        } else if(type.equals("wish")){
            int k = 0;
            for(; k < ApplicationClass.mainCloths.size(); k++){
                if(ApplicationClass.mainCloths.get(k).getObjectId().equals(sender)) break;
            }

            intent = new Intent(this, ClothInfo.class);
            intent.putExtra("index", k);
        }
        intent.putExtra("userId", sender);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setSound(uri)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if(i > 0) j = i;
        notificationManager.notify(j, builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage) {
        String sender = remoteMessage.getData().get("sender");
        String icon = remoteMessage.getData().get("icon");
        final String title = remoteMessage.getData().get("title");
        final String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("notificationType");

        int i = Integer.parseInt(sender.replaceAll("[\\D]", ""));

        Intent intent = null;


        if (type.equals("message")) {
            intent = new Intent(this, ChatActivity.class);
        } else if (type.equals("follow")) {
            intent = new Intent(this, MainActivity.class);
        } else if (type.equals("wish")) {
            int k = 0;
            for(; k < ApplicationClass.mainCloths.size(); k++){
                if(ApplicationClass.mainCloths.get(k).getObjectId().equals(sender)) break;
            }

            intent = new Intent(this, ClothInfo.class);
            intent.putExtra("index", k);
        }
        intent.putExtra("userId", sender);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotification(title, body, pendingIntent, uri, icon);

        int j = 0;
        if (i > 0) j = i;
        notification1.getManager().notify(j, builder.build());
    }

    private void showPostNotification(String pId, String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = new Random().nextInt(3000);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager);
        }

        Intent intent = new Intent(this, ClothInfo.class);
        int i = 0;
        for(; i < ApplicationClass.mainCloths.size(); i++) {
            if(ApplicationClass.mainCloths.get(i).getObjectId().equals(pId)) break;
        }
        intent.putExtra("index", i);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "New Notification";
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if(notificationManager != null){
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    /*private RemoteViews getCustomDesign(String title, String message){
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.message, message);
        remoteViews.setImageViewResource(R.id.icon, R.drawable.ic_launcher_background);
        return remoteViews;
    }*/

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        String token = FirebaseInstanceId.getInstance().getToken();
        if(ApplicationClass.currentUser != null){
            updateToken(token);
        }
    }

    private void updateToken(String newToken){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(newToken);
        databaseReference.child(ApplicationClass.currentUser.getUid()).setValue(token);
    }
}

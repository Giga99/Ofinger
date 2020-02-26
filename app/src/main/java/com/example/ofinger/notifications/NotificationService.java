package com.example.ofinger.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.ofinger.ApplicationClass;
import com.example.ofinger.mainActivities.MainActivity;
import com.example.ofinger.messaging.ChatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String sent = remoteMessage.getData().get("sent");
        String user = remoteMessage.getData().get("user");

        if(ApplicationClass.currentUser != null && sent.equals(ApplicationClass.currentUser.getUid())){
            if(!ApplicationClass.currentUser.equals(user)){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendOAndAboveNotification(remoteMessage);
                } else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }

    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = null;
        if(type.equals("message")){
            intent = new Intent(this, ChatActivity.class);
        } else if(type.equals("follow")){
            intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra("userId", user);
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
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String type = remoteMessage.getData().get("type");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = null;
        if(type.equals("message")){
            intent = new Intent(this, ChatActivity.class);
        } else if(type.equals("follow")){
            intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra("userId", user);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotification(title, body, pendingIntent, uri, icon);

        int j = 0;
        if(i > 0) j = i;
        notification1.getManager().notify(j, builder.build());
    }

    /*private RemoteViews getCustomDesign(String title, String message){
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.title, title);
        remoteViews.setTextViewText(R.id.message, message);
        remoteViews.setImageViewResource(R.id.icon, R.drawable.ic_launcher_background);
        return remoteViews;
    }

    public void showNotification(String title, String message){
        Intent intent = new Intent(this, MainActivity.class);
        String channel_id = "ofinger_channel";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setSound(uri)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            builder = builder.setContent(getCustomDesign(title, message));
        } else {
            builder = builder.setContentTitle(title).setContentText(message).setSmallIcon(R.drawable.ic_launcher_background);
        }


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "ofinger", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(uri, null);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(0, builder.build());
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

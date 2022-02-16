package com.sushant.whatsapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    NotificationManager mNotificationManager;
    String receiverName, senderId, profilePic, email, message, msgType, title, icon, Gid, Type, isNotification, receiverId;
    NotificationCompat.Builder builder;
    String videoSoundPath;
    Ringtone videoRingtone;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        Log.d("FCM", "onMessageReceived: "+remoteMessage.getNotification().getBody());

// playing audio and vibration when user see request
//        String path = "android.resource://" + getPackageName() + "/" + R.raw.iphone;
//        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
//        r.play();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            r.setLooping(false);
//        }

//         vibration
//        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        long[] pattern = {100, 300, 300, 300};
//        v.vibrate(pattern, -1);
        Map<String, String> data = remoteMessage.getData();
        message = data.get("message");
        msgType = data.get("msgType");
        title = data.get("title");
        icon = data.get("icon");
        Type = data.get("Type");

        int resourceImage = getResources().getIdentifier(icon, "drawable", getPackageName());
        videoSoundPath = "android.resource://" + getPackageName() + "/" + R.raw.incoming_sound;

        switch (Type) {
            case "Chat": {
                senderId = data.get("UserId");
                profilePic = data.get("ProfilePic");
                email = data.get("userEmail");
                receiverName = data.get("UserName");
                builder = new NotificationCompat.Builder(this, "CHANNEL_ID1");
                Intent resultIntent = new Intent(this, ChatDetailsActivity.class);
                resultIntent.putExtra("UserId", senderId);
                resultIntent.putExtra("ProfilePic", profilePic);
                resultIntent.putExtra("userEmail", email);
                resultIntent.putExtra("UserName", receiverName);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentTitle(title);
                builder.setContentIntent(pendingIntent);
                break;
            }
            case "Group": {
                isNotification = data.get("Notification");
                Gid = data.get("GId");
                receiverName = data.get("UserName");
                profilePic = data.get("ProfilePic");
                builder = new NotificationCompat.Builder(this, "CHANNEL_ID2");
                Intent resultIntent = new Intent(this, GroupChatActivity.class);
                resultIntent.putExtra("GName", receiverName);
                resultIntent.putExtra("GId", Gid);
                resultIntent.putExtra("GPic", profilePic);
                resultIntent.putExtra("Notification", isNotification);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentTitle(title);
                builder.setContentIntent(pendingIntent);
                break;
            }
            case "videoCall": {
                if (msgType.equals("cancel")) {
                    NotificationManager mNotificationManager =
                            (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(101);
                    return;
                }
                senderId = data.get("UserId");
                profilePic = data.get("ProfilePic");
                receiverName = data.get("UserName");
                email = data.get("userEmail");
                receiverId = data.get("receiverId");
                Bitmap bitmap2 = getBitmapFromUrl(String.valueOf(profilePic));
                builder = new NotificationCompat.Builder(this, "CHANNEL_ID1");
                Intent receiveCallAction = new Intent(this, HeadsUpNotificationActionReceiver.class);
                receiveCallAction.putExtra("UserId", senderId);
                receiveCallAction.putExtra("ProfilePic", profilePic);
                receiveCallAction.putExtra("UserName", receiverName);
                receiveCallAction.putExtra("ACTION", "RECEIVE_CALL");
                receiveCallAction.putExtra("key", email);
                receiveCallAction.setAction("RECEIVE_CALL");

                Intent cancelCallAction = new Intent(this, HeadsUpNotificationActionReceiver.class);
                cancelCallAction.putExtra("ACTION", "CANCEL_CALL");
                cancelCallAction.putExtra("ProfilePic", profilePic);
                cancelCallAction.putExtra("UserName", receiverName);
                cancelCallAction.putExtra("UserId", senderId);
                cancelCallAction.setAction("CANCEL_CALL");

                PendingIntent receiveCallPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1200, receiveCallAction, PendingIntent.FLAG_ONE_SHOT);
                PendingIntent cancelCallPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1201, cancelCallAction, PendingIntent.FLAG_ONE_SHOT);

                Intent resultIntent = new Intent(this, InComingCall.class);
                resultIntent.putExtra("UserId", senderId);
                resultIntent.putExtra("ProfilePic", profilePic);
                resultIntent.putExtra("UserName", receiverName);
                resultIntent.putExtra("title", title);
                resultIntent.putExtra("icon", icon);
                resultIntent.putExtra("key", email);
                resultIntent.putExtra("receiverId", receiverId);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_ONE_SHOT);
                builder.setContentTitle(title);
                builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_CALL);
                builder.setAutoCancel(true);
                builder.setSmallIcon(resourceImage);
                builder.setOnlyAlertOnce(true);
                builder.setTimeoutAfter(60000);
                builder.setLights(0xff0000ff, 200, 200);
                builder.setDefaults(Notification.FLAG_SHOW_LIGHTS);
                builder.addAction(R.drawable.ic_call_green, getActionText(R.string.accept_call, R.color.colorPrimary), receiveCallPendingIntent)
                        .addAction(R.drawable.ic_cancel_sexy, getActionText(R.string.decline_call, R.color.red), cancelCallPendingIntent);
                builder.setContentText(message);
                builder.setLargeIcon(bitmap2);
                builder.setContentIntent(pendingIntent);
                builder.setWhen(0);
                builder.setShowWhen(true);
                builder.setFullScreenIntent(pendingIntent, true);

                NotificationManager mNotificationManager =
                        (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String channelId = "videoCallChannel";
                    NotificationChannel channel;
                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build();
                    channel = new NotificationChannel(
                            channelId,
                            "Channel human readable title",
                            NotificationManager.IMPORTANCE_HIGH);
                    channel.setSound(Uri.parse(videoSoundPath), audioAttributes);
                    mNotificationManager.createNotificationChannel(channel);
                    builder.setChannelId(channelId);
                }
                Notification note = builder.build();
                note.flags = Notification.FLAG_INSISTENT;//repeats ringtone until notification is not clicked
                mNotificationManager.notify(101, note);
                break;
            }
            default: {
                isNotification = data.get("Notification");
                profilePic = data.get("ProfilePic");
                builder = new NotificationCompat.Builder(this, "CHANNEL_ID3");
                Intent resultIntent = new Intent(this, FriendRequestActivity.class);
                resultIntent.putExtra("Notification", isNotification);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentTitle(title);
                builder.setContentIntent(pendingIntent);
                break;
            }
        }


        if (!Type.equals("videoCall")) {
            String path = "android.resource://" + getPackageName() + "/" + R.raw.iphone;
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(path));
            r.play();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                r.setLooping(false);
            }
            builder.setSound(Uri.parse(path), AudioManager.STREAM_NOTIFICATION);
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_MAX);
            builder.setSmallIcon(resourceImage);
            builder.setOnlyAlertOnce(true);
            builder.setLights(0xff0000ff, 200, 200);
            builder.setDefaults(Notification.FLAG_SHOW_LIGHTS);

            //        Uri img=remoteMessage.getNotification().getImageUrl();
            Bitmap bitmap2 = getBitmapFromUrl(String.valueOf(profilePic));
            if (Objects.requireNonNull(msgType).equals("photo")) {
                Bitmap bitmap1 = getBitmapFromUrlWithoutCircle(String.valueOf(message));
                builder.setContentText("Sent you a pic");
                builder.setLargeIcon(bitmap2).setStyle(
                        new NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap1)
                                .bigLargeIcon(null)
                );
            } else if (msgType.equals("text")) {
                builder.setContentText(message).setStyle(new NotificationCompat.BigTextStyle().bigText(message));
                builder.setLargeIcon(bitmap2);
            } else if (msgType.equals("audio")) {
                builder.setContentText("sent you an audio");
                builder.setLargeIcon(bitmap2);
            } else if (msgType.equals("video")) {
                builder.setContentText(message);
                builder.setLargeIcon(bitmap2);
            }

            mNotificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                    .setUsage(AudioAttributes.USAGE_ALARM)
//                    .build();
                String channelId = "Your_channel_id";
                NotificationChannel channel;
                channel = new NotificationChannel(
                        channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);//disable notification sound
//            channel.setSound(Uri.parse(path),audioAttributes);
                mNotificationManager.createNotificationChannel(channel);
                builder.setChannelId(channelId);


// notificationId is a unique int for each notification that you must define
                mNotificationManager.notify(100, builder.build());
            }

        }

    }


    public Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return getCircleBitmap(bitmap);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    public Bitmap getBitmapFromUrlWithoutCircle(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }


    public Bitmap getCircleBitmap(Bitmap bitmap) {
        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height) {
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height >> 1;
        } else {
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width) / 2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width >> 1;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;
    }


    private Spannable getActionText(@StringRes int stringRes, @ColorRes int colorRes) {
        Spannable spannable = new SpannableString(this.getText(stringRes));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // This will only work for cases where the Notification.Builder has a fullscreen intent set
            // Notification.Builder that does not have a full screen intent will take the color of the
            // app and the following leads to a no-op.
            spannable.setSpan(
                    new ForegroundColorSpan(this.getColor(colorRes)), 0, spannable.length(), 0);
        }
        return spannable;
    }
}



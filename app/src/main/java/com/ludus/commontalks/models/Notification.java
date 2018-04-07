package com.ludus.commontalks.models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.ludus.commontalks.R;

/**
 * Created by imhwan on 2017. 12. 7..
 */

public class Notification {

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;

    private int id;
    private CharSequence name;
    private String description;
    private String channelIds;

    public Notification(Context context) {
        this.mContext = context;
        this.id = 1;
        this.mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            this.channelIds = "channel1";
            this.name = mContext.getString(R.string.message_get_channel);
            this.description = mContext.getString(R.string.message_get_channel_desc);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = null;

            mChannel = new NotificationChannel(channelIds, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);

            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            mNotificationManager.createNotificationChannel(mChannel);
        }

        mNotifyBuilder = new NotificationCompat.Builder(mContext);
        mNotifyBuilder.setVibrate(new long[]{1000,1000});
        mNotifyBuilder.setPriority(100);
        mNotifyBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mNotifyBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
    }

    public Notification setSmallIcon(int resId) {
        mNotifyBuilder.setSmallIcon(resId);
        return this;

    }
    public Notification setTitle(String title){
        mNotifyBuilder.setContentTitle(title);
        mNotifyBuilder.setTicker(title);
        return this;
    }

    public Notification setText(String text){
        mNotifyBuilder.setContentText(text);
        return this;
    }

    public Notification setData(Intent intent) {
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(mContext);
        taskStackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent
                = taskStackBuilder.getPendingIntent(140, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyBuilder.setContentIntent(pendingIntent);
        return this;
    }

    public void notification() {
        try {


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                android.app.Notification notification = mNotifyBuilder.setChannelId(channelIds).build();
                notification.flags |= notification.flags | notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(id, notification);
            } else {
                android.app.Notification notification = mNotifyBuilder.build();
                notification.flags |= notification.flags | notification.FLAG_AUTO_CANCEL;
                mNotificationManager.notify(id, notification);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}



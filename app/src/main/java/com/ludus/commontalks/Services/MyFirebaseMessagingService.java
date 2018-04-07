package com.ludus.commontalks.Services;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ludus.commontalks.R;
import com.ludus.commontalks.models.Notification;
import com.ludus.commontalks.views.MainActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imhwan on 2018. 1. 23..
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private Notification mNotification;
    private DatabaseReference mNotiRef;
    private FirebaseAuth mFirebaseAuth;
    private ArrayList<Boolean> mNotiSettingBoolean;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        mFirebaseAuth = FirebaseAuth.getInstance();
        mNotiRef = FirebaseDatabase.getInstance().getReference("noti_setting")
                .child(mFirebaseAuth.getCurrentUser().getUid()).child("notiSetting");

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendNotification(remoteMessage);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /*
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(final RemoteMessage rm) {

        if (isAppRunning(getApplicationContext()) && isRunningInForeground(getApplicationContext())){
            return;
        }

        mNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<ArrayList<Boolean>> t = new GenericTypeIndicator<ArrayList<Boolean>>() {};
                    mNotiSettingBoolean = dataSnapshot.getValue(t);

                    if (mNotiSettingBoolean.get(0)) {
                        Intent chatIntent = new Intent(getApplicationContext(), MainActivity.class);
                        chatIntent.putExtra("chatId", rm.getData().get("chatId"));
                        chatIntent.putExtra("oppositeUserprofileUrl", rm.getData().get("oppositeUserUrl"));
                        chatIntent.putExtra("oppositeUserNickname", rm.getData().get("oppositeUserNickname"));
                        chatIntent.putExtra("oppositeUserUid", rm.getData().get("oppositeUserUid"));
                        chatIntent.putExtra("fromNotiFlag", true);


                        switch (rm.getData().get("chatType")) {
                            case "NEW_CHAT":
                                if (mNotiSettingBoolean.get(1)) {
                                    notifyNotification(chatIntent, rm);
                                }
                                break;

                            case "MSG":
                                if (mNotiSettingBoolean.get(2)) {
                                    notifyNotification(chatIntent, rm);
                                }
                                break;

                            case "COMPLIMENT":
                                if (mNotiSettingBoolean.get(3)) {
                                    chatIntent.putExtra("complient", true);
                                    notifyNotification(chatIntent, rm);
                                }
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void notifyNotification(Intent chatIntent, RemoteMessage rm){

        mNotification = new Notification(getApplicationContext());
        if (rm.getData().get("chatType").equals("NEW_CHAT")){
            mNotification.setData(chatIntent)
                    .setSmallIcon(R.drawable.ic_main_logo_big)
                    .setTitle(rm.getData().get("oppositeUserNickname"))
                    .setText("새로운 채팅이 도착했습니다.")
                    .notification();
        } else {
            mNotification.setData(chatIntent)
                    .setSmallIcon(R.drawable.ic_main_logo_big)
                    .setTitle(rm.getData().get("oppositeUserNickname"))
                    .setText(rm.getData().get("chatTxt"))
                    .notification();
        }
    }


    private boolean isAppRunning(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++){
            if(procInfos.get(i).processName.equals(context.getPackageName())){
                Log.i(TAG, "isAppRunning : true");
                return true;
            }
        }
        Log.i(TAG, "isAppRunning : false");
        return false;
    }

    private boolean isRunningInForeground(Context context) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List < ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equalsIgnoreCase(context.getPackageName())) {
            Log.i(TAG, "isRunningInForeground : true");
            return true;
        }
        Log.i(TAG, "isRunningInForeground : false");
        return false;
    }

}

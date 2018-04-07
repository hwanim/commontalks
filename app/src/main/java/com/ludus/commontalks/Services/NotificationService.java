package com.ludus.commontalks.Services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.R;
import com.ludus.commontalks.models.ChatRoom;
import com.ludus.commontalks.models.Notification;
import com.ludus.commontalks.views.ChatActivity;
import com.ludus.commontalks.views.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by imhwan on 2017. 12. 21..
 */

public class NotificationService extends Service {

    private DatabaseReference mChatRef;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mRatingRef;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mNotiRef;
    private Context mContext;
    private Notification mNotification;
    private ArrayList<Boolean> mNotificationBooleanArray;
    private String mUid;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        Log.i("NotiService", "service onBind");
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("NotiService", "onStartCommand");

        mFirebaseDb = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUid = mFirebaseUser.getUid();
        mChatRef = mFirebaseDb.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        mRatingRef = mFirebaseDb.getReference("users").child("ratings");
        mNotiRef = mFirebaseDb.getReference("noti_setting").child(mFirebaseUser.getUid());
        mContext = getApplicationContext();
        mNotification = new Notification(mContext);
        mNotificationBooleanArray = new ArrayList<>();
        addNotiSettingListeners();

        flags |= START_REDELIVER_INTENT;
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean isAppRunning(Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++){
            if(procInfos.get(i).processName.equals(context.getPackageName())){
                Log.i("NotiService", "isAppRunning : true");
                return isRunningInForeground(context);
            }
        }
        Log.i("NotiService", "isAppRunning : false");
        return false;
    }

    private boolean isRunningInForeground(Context context) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List < ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;
        if(componentInfo.getPackageName().equalsIgnoreCase(context.getPackageName())) {
            Log.i("NotiService", "isRunningInForeground : true");
            return true;
        }
        Log.i("NotiService", "isRunningInForeground : false");
        return false;
    }

    private void addNotiSettingListeners(){
        mNotiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.i("NotiService", "addNotiSettingListeners get datasnapshot");
                    GenericTypeIndicator<ArrayList<Boolean>> t = new GenericTypeIndicator<ArrayList<Boolean>>() {};
                    mNotificationBooleanArray = dataSnapshot.child("notiSetting").getValue(t);
                } else {
                    mNotificationBooleanArray = new ArrayList<>();
                    mNotificationBooleanArray.add(true);
                    mNotificationBooleanArray.add(true);
                    mNotificationBooleanArray.add(true);
                    mNotificationBooleanArray.add(true);
                    dataSnapshot.getRef().setValue(mNotificationBooleanArray);
                }
                addListeners();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.i("NotiService", "service onCreate");
    }


    @Override
    public void onDestroy() {
        Log.i("NotiService", "service onDestroy");
        removeListeners();
        super.onDestroy();
    }



    private void addListeners(){
        Log.i("NotiService", "addListeners");
        //세가지 리스너 애드
        //새채팅
        //새 메세지
        //나의 대화 칭찬
        addNewChatListener();
        addNewMsgChildEventListener();
        addNewComplimentListener();
    }

    private void addNewChatListener() {
        Log.i("NotiService", "newChatEventListener");
        mChatRef.addChildEventListener(newChatEventListener);
    }
    private void addNewMsgChildEventListener() {
        Log.i("NotiService", "newMsgChildEventListener");
        mChatRef.addChildEventListener(newMsgChildEventListener);
    }
    private void addNewComplimentListener() {
        Log.i("NotiService", "newComplimentListener");
        mRatingRef.addChildEventListener(newComplimentListener);
    }



    private void removeListeners(){
        Log.i("NotiService", "remove listeners");
        mChatRef.removeEventListener(newChatEventListener);
        mChatRef.removeEventListener(newMsgChildEventListener);
        mRatingRef.removeEventListener(newComplimentListener);
    }

    private ChildEventListener newChatEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.i("NotiService", "newChatEventListener onChildAdded");
            Log.i("NotiService", "mNotificationBooleanArray" + mNotificationBooleanArray.toString());
            if (!isAppRunning(mContext)) {
                //채팅방이 애드 되면 노티를 발송합니다.
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if (!chatRoom.getLastMessageUserId().equals(mUid)
                        && chatRoom.getChatRoomStatus().equals(ChatRoom.chatRoomStatusEnum.ACTIVE)
                        && mNotificationBooleanArray.get(0)
                        && mNotificationBooleanArray.get(1)) {
//                     노티피케이션 알림
                    Intent chatIntent = new Intent(mContext, MainActivity.class);
                    chatIntent.putExtra("chatId", chatRoom.getChatId());
                    chatIntent.putExtra("oppositeUserprofileUrl", chatRoom.getChatOppositeUser().getProfileUrl());
                    chatIntent.putExtra("oppositeUserNickname", chatRoom.getChatOppositeUser().getNickname());
                    chatIntent.putExtra("oppositeUserUid", chatRoom.getChatOppositeUser().getUid());
                    chatIntent.putExtra("fromNotiFlag", true);

                    mNotification.setData(chatIntent)
                            .setSmallIcon(R.drawable.ic_main_logo_big)
                            .setTitle(chatRoom.getChatOppositeUser().getNickname())
                            .setText("새로운 대화가 도착했습니다.")
                            .notification();
                }
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {}

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };

    private ChildEventListener newMsgChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i("NotiService", "newChatEventListener onChildChanged");
            Log.i("NotiService", "mNotificationBooleanArray" + mNotificationBooleanArray.toString());
            if (!isAppRunning(mContext)) {
                //업데이트 되면 노티 발송
                ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                if (!chatRoom.getLastMessageUserId().equals(mUid)
                        && chatRoom.getChatRoomStatus().equals(ChatRoom.chatRoomStatusEnum.ACTIVE)
                        && mNotificationBooleanArray.get(0)
                        && mNotificationBooleanArray.get(2)) {
//                   노티피케이션 알림
                    Intent chatIntent = new Intent(mContext, MainActivity.class);
                    chatIntent.putExtra("chatId", chatRoom.getChatId());
                    chatIntent.putExtra("oppositeUserNickname", chatRoom.getChatOppositeUser().getNickname());
                    chatIntent.putExtra("profileUrl", chatRoom.getChatOppositeUser().getProfileUrl());
                    chatIntent.putExtra("nickname", chatRoom.getChatOppositeUser().getNickname());
                    chatIntent.putExtra("oppositeUserUid", chatRoom.getChatOppositeUser().getUid());
                    chatIntent.putExtra("fromNotiFlag", true);

                    mNotification.setData(chatIntent)
                            .setSmallIcon(R.drawable.ic_main_logo_big)
                            .setTitle(chatRoom.getChatOppositeUser().getNickname())
                            .setText(chatRoom.getLastMessage())
                            .notification();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {}

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onCancelled(DatabaseError databaseError) {}
    };

    private ChildEventListener newComplimentListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.i("NotiService", "newComplimentListener onChildChanged");
            Log.i("NotiService", "mNotificationBooleanArray" + mNotificationBooleanArray.toString());
            if (!isAppRunning(mContext)) {
                if (mNotificationBooleanArray.get(0)
                        && mNotificationBooleanArray.get(3)) {
                    Intent chatIntent = new Intent(mContext, MainActivity.class);
                    mNotification.setData(chatIntent)
                            .setSmallIcon(R.drawable.ic_main_logo_big)
                            .setTitle("아주 칭찬해!")
                            .setText("누군가 대화를 끝내고 당신을 칭찬했습니다.")
                            .notification();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };



}

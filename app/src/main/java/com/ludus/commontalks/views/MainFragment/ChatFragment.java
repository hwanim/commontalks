package com.ludus.commontalks.views.MainFragment;


import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.CustomViews.RecyclerItemTouchHelper;
import com.ludus.commontalks.CustomViews.RecyclerViewItemClickListener;
import com.ludus.commontalks.CustomViews.TutorialDialog;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.ChatRoomStatusChangeEvent;
import com.ludus.commontalks.Services.PushEvent;
import com.ludus.commontalks.Services.UseItemPushEvent;
import com.ludus.commontalks.adapters.ChatRoomAdapter;
import com.ludus.commontalks.models.ChatRoom;
import com.ludus.commontalks.models.Currency;
import com.ludus.commontalks.models.Message;
import com.ludus.commontalks.models.Notification;
import com.ludus.commontalks.models.Token;
import com.ludus.commontalks.models.TutorialCheck;
import com.ludus.commontalks.models.User;
import com.ludus.commontalks.views.ChatActivity;
import com.ludus.commontalks.views.MainActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ludus.commontalks.models.ChatRoom.chatRoomStatusEnum.ACTIVE;
import static com.ludus.commontalks.models.ChatRoom.chatRoomStatusEnum.DISABLED;
import static com.ludus.commontalks.models.ChatRoom.chatRoomStatusEnum.NOTRATED;
import static com.ludus.commontalks.models.ChatRoom.chatRoomStatusEnum.RATING;

public class ChatFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private ChatRoomAdapter mChatRoomAdapter;

    private FirebaseUser mFirebaseUser;

    private DatabaseReference mUserChatRef;

    private DatabaseReference mChatRef;

    private DatabaseReference mUserRef;

    private FirebaseDatabase mFirebaseDatabase;

    private DatabaseReference mCurrencyRef;

    private String mChatId;

    private Notification mNotification;

    private Context mContext;

    private NotificationManager mNotificationManager;

    private User mCurrentUser;

    private HashMap<String, Boolean> mEndMsgAlreadySend;

    @BindView(R.id.chatFragmentRecyclerView)
    RecyclerView mReView;

    @BindView(R.id.noChatRoomLayout)
    ConstraintLayout noChatRoomLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View chatRoomView = inflater.inflate(R.layout.fragment_chat, container, false);

        ButterKnife.bind(this,chatRoomView);
        BusProvider.getInstance().register(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserChatRef = mFirebaseDatabase.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        mChatRef = mFirebaseDatabase.getReference("chat_messages");
        mUserRef = mFirebaseDatabase.getReference("users");
        mCurrencyRef = mFirebaseDatabase.getReference("currency").child(mFirebaseUser.getUid());
        mChatRoomAdapter = new ChatRoomAdapter();
        mEndMsgAlreadySend= new HashMap<>();
        mChatRoomAdapter.setFragment(this);
        mReView.setAdapter(mChatRoomAdapter);
        mReView.setLayoutManager(new LinearLayoutManager(getContext()));
        mContext = getContext();
        mCurrentUser = ((MainActivity)getActivity()).mUser;
        checkNoChatRoom();



        mReView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ChatRoom chatRoom = mChatRoomAdapter.getItem(position);

                if (chatRoom.getChatRoomStatus().equals(ChatRoom.chatRoomStatusEnum.ACTIVE)) {
                    mChatId = chatRoom.getChatId();
                    Intent intent = new Intent(getContext(),ChatActivity.class);
                    intent.putExtra("chatId", mChatId);
                    intent.putExtra("oppositeUserUid",chatRoom.getChatOppositeUser().getUid());
                    intent.putExtra("oppositeUserNickname",chatRoom.getChatOppositeUser().getNickname());
                    intent.putExtra("oppositeUserprofileUrl", chatRoom.getChatOppositeUser().getProfileUrl());
                    startActivity(intent);
                }
            }
        }));

        addItemSwipeListener();
        addChatListener();
        return chatRoomView;
    }

    private void checkNoChatRoom() {
        if (mChatRoomAdapter.getItemCount() == 0) {
            noChatRoomLayout.setVisibility(View.VISIBLE);
            mReView.setVisibility(View.GONE);
        } else {
            noChatRoomLayout.setVisibility(View.GONE);
            mReView.setVisibility(View.VISIBLE);
        }
    }

    private void addChatListener(){
        mUserChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot chatDataSnapshot, String s) {
                Log.i("CHATROOMADAPTER", "call chatFragment.drawUI, ADD");
                drawUI(chatDataSnapshot, DrawType.ADD);
            }

            @Override
            public void onChildChanged(DataSnapshot chatDataSnapshot, String s) {
                //변경된 방의 정보를 수신하여
                //해당 방의 안읽은 메세지 갯수를 가져와서 그걸 출력해주고,
                // 마지막 메세지 또한 수정되었으면 바꿔줍니다
                Log.i("CHATROOMADAPTER", "call chatFragment.drawUI, UPDATE");
                drawUI(chatDataSnapshot, DrawType.UPDATE);
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
        });
    }


    private void drawUI(DataSnapshot chatDataSnapshot, DrawType drawType) {
        final ChatRoom chatRoom = chatDataSnapshot.getValue(ChatRoom.class);
        if (drawType.equals(DrawType.ADD)){
            //활성화되지 않은 방은 추가해주지 않습니다.
            if (!chatRoom.getDisabled()){
                Log.i("CHATROOMADAPTER", "update chatItem" + chatRoom.getChatOppositeUser().getNickname());
                mChatRoomAdapter.addItem(chatRoom);
                mChatRoomAdapter.initMoveItem(chatRoom);
            }else {
                if (mChatRoomAdapter.hasItem(chatRoom)) {
                    mChatRoomAdapter.removeItem(chatRoom);
                }
            }
        } else if (drawType.equals(DrawType.UPDATE)){

            Log.i("CHATROOMADAPTER", "update chatItem" + chatRoom.getChatOppositeUser().getNickname());
            if (!chatRoom.getDisabled()){
                mChatRoomAdapter.updateItem(chatRoom);
                if (chatRoom.getChatRoomStatus().equals(ACTIVE)) {
                    mChatRoomAdapter.moveItem(chatRoom);
                }
            } else {
                if (mChatRoomAdapter.hasItem(chatRoom)) {
                    mChatRoomAdapter.removeItem(chatRoom);
                }
            }
        }
        checkNoChatRoom();
    }

    public void leaveChat(final ChatRoom item) {
        //alertdialog > 방 나가시겠습니까? > 예 클릭시 방나가는 로직 구현
        //방나가는 로직

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("대화방 나가기");
        alertDialog.setMessage("이 대화방에서 나가시겠습니까? 대화방은 즉시 삭제됩니다.");
        alertDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //1. 나간 유저에게서는 딜리트를 통해서 바로 삭제해줌
                //2. 반대쪽 유저에게는 평가를 NotRated로 바꿔서 평가를 받음.
                disabledChatRoom(item);
            }
        });
        alertDialog.show();
    }

    private void disabledChatRoom(final ChatRoom item) {
        mUserChatRef.child(item.getChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    final ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                    if (mChatRoomAdapter.hasItem(chatRoom)) {
                        mChatRoomAdapter.removeItem(item);
                    }
                    chatRoom.setDisabled(true);
                    chatRoom.setChatRoomStatus(ChatRoom.chatRoomStatusEnum.DISABLED);
                    dataSnapshot.getRef().setValue(chatRoom);
                    endMsgSend(EndType.QUIT,chatRoom.getChatId(),chatRoom.getChatOppositeUser().getUid());
                }
                checkNoChatRoom();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }



    public enum EndType{
        QUIT, TIMEOUT
    }


    public void endMsgSend(final EndType endType, final String chatId, final String oppositeUserUid) {
        final DatabaseReference msgRef;
        msgRef = FirebaseDatabase.getInstance().getReference("chat_messages").child(chatId);
        msgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean endMsgAlreadySend = false;
                    Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        Message msg = iterator.next().getValue(Message.class);
                        if (msg.getMessageType() == Message.messageTypeEnum.END) {
                            endMsgAlreadySend = true;
                        }
                    }
                    if (!endMsgAlreadySend) {
                        String msgId = msgRef.push().getKey();
                        Log.i("chattingEnd", chatId + ", endType : " + endType + ", msgId : " + msgId);

                        Message message = new Message();
                        message.setMessageId(msgId);
                        message.setMessageType(Message.messageTypeEnum.END);
                        message.setContinuoslySend(true);
                        message.setMessageDate(new Date().getTime());
                        message.setInitMessage(false);
                        final User user = ((MainActivity) getActivity()).mUser;
                        message.setMessageUser(user);
                        if (endType == EndType.QUIT) {
                            message.setMessageText("상대방이 나가 대화가 끝났습니다.");
                        } else {
                            message.setMessageText("타임아웃으로 대화가 끝났습니다.");
                        }
                        msgRef.child(msgId).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    ArrayList<String> chatUser = new ArrayList<>();
                                    chatUser.add(user.getUid());
                                    chatUser.add(oppositeUserUid);

                                    for (String uid : chatUser) {
                                        mUserRef
                                                .child(uid)
                                                .child("chats")
                                                .child(chatId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()){
                                                            if (dataSnapshot.getValue(ChatRoom.class)
                                                                    .getChatRoomStatus()
                                                                    .equals(ACTIVE)){
                                                                dataSnapshot
                                                                        .getRef()
                                                                        .child("chatRoomStatus")
                                                                        .setValue("NOTRATED");
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                    }
                                                });
                                    }
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void addItemSwipeListener(){
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mReView);
    }


    private enum DrawType{
        ADD, UPDATE
    }


    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, final int position) {
        //disabledchatRoom이 보일때만 스와이프를 할수 있도록 해줍니다.
        if (((ChatRoomAdapter.ChatRoomViewHolder) viewHolder).disabledChatRoomLayout.getVisibility()
                == mReView.getVisibility()){
            if (viewHolder instanceof ChatRoomAdapter.ChatRoomViewHolder) {
//                      상태에 따라서 스와이프 안하도록 해줘야함.
                final ChatRoom chatRoom = mChatRoomAdapter.getItem(position);
                if (direction == ItemTouchHelper.LEFT) {
                    Log.i("CHATROOMADAPTER", "RIGHT");
                    mUserRef.child(chatRoom.getChatOppositeUser().getUid()).child("ratings").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                GenericTypeIndicator<HashMap<String,Integer>> t = new GenericTypeIndicator<HashMap<String,Integer>>() {};
                                HashMap<String,Integer> data = dataSnapshot.getValue(t);
                                if (((String)((ChatRoomAdapter.ChatRoomViewHolder) viewHolder).disabledChatLikeBtn.getTag()).equals("1")) {
                                    Log.i("CHATROOMADAPTER", "caring + 1");
                                    data.put("caring", data.get("caring") + 1);
                                    Toast.makeText(getContext(), "Like를 보냈어요!",Toast.LENGTH_LONG).show();
                                    dataSnapshot.getRef().setValue(data);


                                    //토큰 업데이트 쳐줘야함. --칭찬임.
                                    User user = ((MainActivity)getActivity()).mUser;
                                    Token token = new Token();
                                    token.setUid(chatRoom.getChatOppositeUser().getUid());
                                    token.setChatId(chatRoom.getChatId());
                                    token.setOppositeUserUrl(user.getProfileUrl());
                                    token.setOppositeUserUid(user.getUid());
                                    token.setOppositeUserNickname(user.getNickname());
                                    token.setChatType(Token.ChatType.COMPLIMENT);
                                    token.setChatTxt("Like를 받았어요!");
                                    mFirebaseDatabase.getReference("tokens").child(token.getUid()).setValue(token);
                                }
                            }
                            disabledChatRoom(chatRoom);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }
        }
    }


    private long mExpectedDisappearTime;

    // chatActivity에서 아이템 사용시 푸쉬가 날아옴.
    @Subscribe
    public void useItemUpdateUI(final UseItemPushEvent mPushEvent) {
        Log.i("PushEvent", "push event subscribe" + mPushEvent.toString());
        if (mPushEvent != null) {
            Log.i("PushEvent", "pushevent come in :" + mPushEvent.toString());
            mExpectedDisappearTime = mPushEvent.getMItemUsingTime();
            final long itemUseTime = mPushEvent.getMItemUsingTime();
            long milliToHourConvert = 60 * 60 * 1000;
            switch (mPushEvent.getMItemType()){
                case ONEHOUR:
                    mExpectedDisappearTime = itemUseTime + milliToHourConvert;
                    break;

                case TWOHOUR:
                    mExpectedDisappearTime = itemUseTime + 2 * milliToHourConvert;
                    break;

                case FOURHOUR:
                    mExpectedDisappearTime = itemUseTime + 4 * milliToHourConvert;
                    break;

                case NIGHTSHIFT:
                    Date date = new Date();
                    date.setTime(((date.getTime() / milliToHourConvert) + 12 ) * milliToHourConvert);
                    mExpectedDisappearTime = date.getTime();
                    break;
            }


            //1. 나의 채팅방에 대해서 사라질 예정 시각과 아이템을 사용했다는 플래그를 true로 만들고
            //2. 상대방의 채팅방에 대해서도 똑같이 작업을 해준뒤에
            //3. 나의 화폐 기록에 사용한 기록을 남겨줌
            mUserChatRef.child(mPushEvent.getMChatId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                        if (chatRoom.getItemUsed()) {
                            //만약 아이템 사용된상태라면 추가로 시간을 추가해줍니다.
                            chatRoom.setDisappearTime(chatRoom.getDisappearTime() + mExpectedDisappearTime - itemUseTime);
                        } else {
                            chatRoom.setDisappearTime(mExpectedDisappearTime);
                            chatRoom.setItemUsed(true);
                        }
                        dataSnapshot.getRef().setValue(chatRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //상대방 유저의 채팅방도 업데이트 쳐줘야함
                                    mUserRef
                                            .child(chatRoom.getChatOppositeUser().getUid())
                                            .child("chats")
                                            .child(mPushEvent.getMChatId())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            ChatRoom oppositeUserChatRoomData = dataSnapshot.getValue(ChatRoom.class);
                                            if (oppositeUserChatRoomData.getItemUsed()) {
                                                oppositeUserChatRoomData
                                                        .setDisappearTime(oppositeUserChatRoomData.getDisappearTime() + mExpectedDisappearTime - itemUseTime);
                                            } else {
                                                oppositeUserChatRoomData.setDisappearTime(mExpectedDisappearTime);
                                                oppositeUserChatRoomData.setItemUsed(true);
                                            }
                                            //두개가 모두 성공하면 로그를 남겨줍니다
                                            dataSnapshot
                                                    .getRef()
                                                    .setValue(oppositeUserChatRoomData)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mCurrencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Currency currency = dataSnapshot.getValue(Currency.class);
                                                                if (currency.getWithdrawLog() == null) {
                                                                    currency.setWithdrawLog(new ArrayList<String>() {{
                                                                        add(mPushEvent.getMItemType()
                                                                                + " item used in chatRoom with "
                                                                                + chatRoom.getChatOppositeUser().getUid()
                                                                                + ", "
                                                                                + mPushEvent.getMCoincount());
                                                                    }});
                                                                } else {
                                                                    currency.getWithdrawLog().add(mPushEvent.getMItemType()
                                                                            + " item used in chatRoom with "
                                                                            + chatRoom.getChatOppositeUser().getUid()
                                                                            + ", "
                                                                            + mPushEvent.getMCoincount());
                                                                }
                                                                dataSnapshot
                                                                        .getRef()
                                                                        .setValue(currency);
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }
}

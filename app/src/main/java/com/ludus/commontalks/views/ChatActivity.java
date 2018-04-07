package com.ludus.commontalks.views;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.CustomViews.CustomDialog;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.ChatRoomStatusChangeEvent;
import com.ludus.commontalks.Services.PushEvent;
import com.ludus.commontalks.Services.UseItemPushEvent;
import com.ludus.commontalks.adapters.ChatMessageAdapter;
import com.ludus.commontalks.models.BlockedPost;
import com.ludus.commontalks.models.ChatRoom;
import com.ludus.commontalks.models.Message;
import com.ludus.commontalks.models.Token;
import com.ludus.commontalks.models.User;
import com.ludus.commontalks.views.MainFragment.ChatFragment;
import com.squareup.otto.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ludus.commontalks.models.Message.messageTypeEnum.END;
import static com.ludus.commontalks.models.Message.messageTypeEnum.INFOMATION;
import static com.ludus.commontalks.models.Message.messageTypeEnum.TXT;

public class ChatActivity extends BaseActivity {

    @BindView(R.id.chat_rec_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.senderBtn)
    ImageView mSenderBtn;

    @BindView(R.id.edtContent)
    EditText mEdtContent;

    @BindView(R.id.useItemBtn)
    ImageView mUseItemBtn;

    @BindView(R.id.chatActivityTitleView)
    TextView titleView;

    @BindView(R.id.chatActivityBackButton)
    ImageView chatActivityBackButton;

    private String mChatId;
    private FirebaseDatabase mFirebaseDb;
    private FirebaseUser mFirebaseUser;
    private ChatMessageAdapter mChatMessageAdapter;
    private DatabaseReference mUserChatRef;
    private DatabaseReference mMessageRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mUserCurrencyRef;
    private User mPostUser;
    private static User mCurrentUser;
    private static boolean setCurrentUser;
    private DatabaseReference mChatsRef;
    private DatabaseReference mTokenRef;
    private String mPostId;
    private DatabaseReference mBlockedPostRef;
    private CustomDialog mUseItemDialog;
    private CustomDialog mChooseDialog;
    private CustomDialog mMoveToStoreDialog;
    private Context mContext;
    private int CHAT_LIMIT_TIME = 600000; //10분
    private String oppositeUserProfileUrl;
    private String oppositeUserNickname;
    private Message message = new Message();
    private Message mInitMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        BusProvider.getInstance().register(this);
        ButterKnife.bind(this);

        mContext = this;
        mChatMessageAdapter = new ChatMessageAdapter();
        mRecyclerView.setAdapter(mChatMessageAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFirebaseDb = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mMessageRef = mFirebaseDb.getReference("chat_messages");
        mUserChatRef = mFirebaseDb.getReference("users").child(mFirebaseUser.getUid()).child("chats");
        mUserRef = mFirebaseDb.getReference("users");
        mBlockedPostRef = mFirebaseDb.getReference("blocked_posts").child(mFirebaseUser.getUid());
        mUserCurrencyRef = mFirebaseDb.getReference("currency").child(mFirebaseUser.getUid());
        mTokenRef = mFirebaseDb.getReference("tokens");

        //인텐트 넘어오는 경우는 세가지
        // 1. 포스트에서 넘어옴
        // 2. 챗목록에서 넘어옴
        // 3. 노티에서 넘어옴
        Bundle intentDataBundle = getIntent().getExtras();

        //피드에서 대화방으로 들어온 거라면 인텐트를 체크해서 피드에 글을 쓴 데이터를 가져옵니다
        //포스트에서 넘어왔으므로 첫 대화를 시작하는 것임.
        //또는 채팅방이나 노티에서 넘어온 경우에는 이미 대화방이 생성되어 있으므로, 대화방 데이터를 같이 가져오도록 함.
        if (intentDataBundle.containsKey("postId")) {
            mPostId = intentDataBundle.getString("postId");
            mPostUser = new User(
                    intentDataBundle.getString("uid"),
                    intentDataBundle.getString("email"),
                    intentDataBundle.getString("username"),
                    intentDataBundle.getString("profileUrl"),
                    intentDataBundle.getString("nickname")
            );
            titleView.setText(mPostUser.getNickname());
            oppositeUserProfileUrl = mPostUser.getProfileUrl();
            oppositeUserNickname = mPostUser.getNickname();

            //상대방 피드 글을 이용해서 첫 메세지를 임시적으로 세팅해줌.
            mInitMessage = new Message();
            mInitMessage.setContinuoslySend(false);
            mInitMessage.setMessageUser(mPostUser);
            mInitMessage.setInitMessage(true);
            mInitMessage.setMessageText(intentDataBundle.getString("postcontents"));
            mInitMessage.setMessageType(Message.messageTypeEnum.INIT);
            mInitMessage.setMessageDate(new Date().getTime());
            mChatMessageAdapter.addInitItem(mInitMessage);

        } else if (intentDataBundle.containsKey("chatId") || intentDataBundle.containsKey("fromNotiFlag") ) {
            mChatId = intentDataBundle.getString("chatId");
            mMessageRef = mMessageRef.child(mChatId);
            oppositeUserProfileUrl = intentDataBundle.getString("oppositeUserprofileUrl");
            oppositeUserNickname = intentDataBundle.getString("oppositeUserNickname");
            titleView.setText(oppositeUserNickname);
        }
        if (!setCurrentUser) {
            getCurrentUser();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mChatId != null) {
            mUserChatRef.child(mChatId).child("readMsg").setValue(true);
            removeMessageListener();
        }
    }

    @OnClick(R.id.chatActivityBackButton)
    public void onClickBackButton(){
        onBackPressed();
    }

    //데이터 끊키는 환경을 위해 매번 새로 로딩하는 것이 아니라 캐싱해서 채팅을 저장해 놓는 형태로 바꿔야함.
    //1. pause되면 대화 데이터를 앱에 저장한다
    //2. 다시 resume되면 저장된 데이터로 먼저 채팅방 데이터를 뿌려주고
    //3. 리스너가 연결되면 다시 리스너를 통해서 로딩해 줄 것
    @Override
    protected void onResume() {
        super.onResume();
        if (mChatId != null) {
            mUserChatRef.child(mChatId).child("readMsg").setValue(true);
            mMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long totalMessageCount = dataSnapshot.getChildrenCount();
                    mMessageEventListener.setTotalMessageCount(totalMessageCount);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            mChatMessageAdapter.clearItem();
            addMessageListener();
        }
    }

    private void getCurrentUser() {
        mCurrentUser = new User();
        mFirebaseDb.getReference("users").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentUser = dataSnapshot.getValue(User.class);
                    setCurrentUser = true;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @OnClick(R.id.senderBtn)
    public void onSenderBtnClicked() {

        //빈칸으로 눌렀는지 체크
        if (mEdtContent.getText().toString().isEmpty()) {
            return;
        }
        if (mChatId == null) {
            createChatRoom(mEdtContent.getText().toString());
        } else {
            sendMessage(TXT, false);
        }
    }


    private class MessageEventListener implements ChildEventListener {
        private long totalMessageCount;
        private long callCount = 1;

        public void setTotalMessageCount(long totalMessageCount) {
            this.totalMessageCount = totalMessageCount;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Message item = dataSnapshot.getValue(Message.class);
            if (item != null) {
                if (item.getMessageType() == Message.messageTypeEnum.TXT) {
                    mChatMessageAdapter.addItem(item);
                    if (!item.isReadMessage()){
                        item.setReadMessage(true);
                        dataSnapshot.getRef().setValue(item);
                    }
                } else if (item.getMessageType().equals(INFOMATION)) {
                    mChatMessageAdapter.addItem(item);
                } else if (item.getMessageType().equals(END)) {
                    mChatMessageAdapter.addItem(item);
                    preventChat();
                } else if (item.isInitMessage()) {
                    mChatMessageAdapter.clearItem();
                    mChatMessageAdapter.addItem(item);
                    mChatMessageAdapter.setmInitOppositeUserProfile(oppositeUserProfileUrl);
                    mChatMessageAdapter.setmInitOppositeUserNickName(oppositeUserNickname);
                }

                if (callCount >= totalMessageCount) {
                    //스크롤을 맨 마지막으로 내린다
                    mRecyclerView.scrollToPosition(mChatMessageAdapter.getItemCount() - 1);
                }
                callCount++;
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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

    }

    MessageEventListener mMessageEventListener = new MessageEventListener();

    private void addMessageListener(){
        mMessageRef.addChildEventListener(mMessageEventListener);
    }

    private void removeMessageListener() {
        mMessageRef.removeEventListener(mMessageEventListener);
    }



    private boolean isMessageSaved = false;

    private void createChatRoom(String firstMsg) {
        ChatRoom chatRoom = new ChatRoom();
        mChatId = mUserChatRef.push().getKey();
        mMessageRef = mMessageRef.child(mChatId);

        mChatMessageAdapter.clearItem();
        mMessageRef.push().setValue(mInitMessage);

        chatRoom.setChatId(mChatId);
        chatRoom.setLastMessage(mEdtContent.getText().toString());
        chatRoom.setLastMessageUserId(mFirebaseUser.getUid());
        chatRoom.setCreateDate(new Date().getTime());

        List<String> uidList = new ArrayList<>();
        uidList.add(getIntent().getStringExtra("uid"));
        uidList.add(mFirebaseUser.getUid());

        for (String userId: uidList) {
            if (userId.equals(mFirebaseUser.getUid())) {
                chatRoom.setChatOppositeUser(mPostUser);
            } else {
                chatRoom.setChatOppositeUser(mCurrentUser);
            }

            //방 생성에 성공시 메세지를 전송한 내역을 데이터베이스에 기록한다
            mUserRef.child(userId).child("chats").child(mChatId).setValue(chatRoom).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (!isMessageSaved) {
                            sendMessage(TXT, true);
                            addMessageListener();
                            isMessageSaved = true;

                            BlockedPost blockPost = new BlockedPost(mPostUser.getUid(), mPostId, BlockedPost.blockTypeEnum.alreadyRead);
                            mBlockedPostRef.child(mPostId).setValue(blockPost);
                        }
                    }
                }
            });
        }
    }

    //<readMsg UI> 처리하기.
    //1. 메세지를 상대방이 보내면 --> 상대방 대화 챗룸에 readMsg를 false로 해서 setValue함
    //2. chatActivity에서 클릭해서 들어가게 되면, 챗룸을 가져와서 readMsg를 체크해서 true로 바꿔줌.
    //3. chatRoom에서 readMSg 값에 따라서 UI 업데이트

    private void sendMessage(Message.messageTypeEnum messageType, final boolean chatStartFlag){
        final String messageId = mMessageRef.push().getKey();
        message.setMessageDate((new Date().getTime()));
        message.setMessageUser(mCurrentUser);
        message.setMessageId(messageId);
        message.setReadMessage(false);

        if (messageType.equals(TXT)) {
            message.setMessageType(Message.messageTypeEnum.TXT);
            message.setMessageText(mEdtContent.getText().toString());
            mEdtContent.setText("");
        } else if (messageType.equals(INFOMATION)) {
            Log.i("PushEvent", "message Infomation");
            message.setMessageType(INFOMATION);
            message.setMessageText(mChooseDialog.mItemInfo.get("itemName"));
        } else if (messageType.equals(END)) {
            message.setMessageUser(mCurrentUser);
            message.setMessageType(END);
            message.setMessageText("채팅이 끝났습니다.");
            message.setMessageDate(System.currentTimeMillis());
            message.setContinuoslySend(true);
        }

        final List<String> uidList = new ArrayList<>();
        if (getIntent().hasExtra("oppositeUserUid")) {
            uidList.add(getIntent().getStringExtra("oppositeUserUid"));
        } else {
            uidList.add(mPostUser.getUid());
        }
        uidList.add(mCurrentUser.getUid());

        // 1. {chat_room} > {chat_message} 추가
        // 2. 두 유저 모두에게 users > {uid} > {chats} > lastmessage date, lastmessage, disappear, warning 업데이트
        // 시스템인 경우 챗룸은 따로 업데이트 해주지 않음.
        mMessageRef.child(messageId).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // 메세지가 업데이트 되고 나면 채팅방 정보를 업데이트 해줌.
                if (task.isSuccessful() && message.getMessageType().equals(TXT)) {
                    for (final String uid : uidList) {
                        mChatsRef = mUserRef.child(uid).child("chats").child(mChatId);
                        mChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    //채팅방의 채팅 정보를 업데이트 해 줍니다.
                                    final ChatRoom chatRoom = dataSnapshot.getValue(ChatRoom.class);
                                    chatRoom.setLastMessageDate(message.getMessageDate());
                                    chatRoom.setLastMessage(message.getMessageText());
                                    if (mChatMessageAdapter.size() > 2 &&chatRoom.getLastMessageUserId().equals(mFirebaseUser.getUid())) {
                                        chatRoom.setContinuouslySend(true);
                                    } else {
                                        chatRoom.setContinuouslySend(false);
                                    }
                                    chatRoom.setLastMessageUserId(mCurrentUser.getUid());
                                    if (chatRoom.getChatOppositeUser().getUid().equals(mCurrentUser.getUid())) {
                                        chatRoom.setReadMsg(false);
                                    } else {
                                        chatRoom.setReadMsg(true);
                                    }
                                    //아이템을 사용했을 시에는 시간 리셋을 하지 않음.
                                    if (!chatRoom.getItemUsed() || (chatRoom.getDisappearTime() - System.currentTimeMillis() < CHAT_LIMIT_TIME)){
                                        //연속으로 보냈을 경우에는 시간초를 리셋하지 않음.
                                        if (!chatRoom.getContinuouslySend()) {
                                            chatRoom.setDisappearTime(message.getMessageDate() + CHAT_LIMIT_TIME);
                                        }
                                        chatRoom.setItemUsed(false);
                                    }
                                    dataSnapshot.getRef().setValue(chatRoom);

                                    //상대방에게 노티 발송하는 로직
                                    if (uid.equals(mCurrentUser.getUid())) {
                                        mTokenRef.child(chatRoom.getChatOppositeUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Token token = new Token();
                                                if (dataSnapshot.exists()) {
                                                    token = dataSnapshot.getValue(Token.class);
                                                }
                                                token.setChatId(chatRoom.getChatId());
                                                token.setChatTxt(message.getMessageText());

                                                //만약 챗을 생성하는 채팅이라면 챗을 생성하는 타입으로 노티피케이션을 발송합니다ㅣ
                                                if (chatStartFlag) {token.setChatType(Token.ChatType.NEW_CHAT);}
                                                else {token.setChatType(Token.ChatType.MSG);}
                                                token.setOppositeUserNickname(mCurrentUser.getNickname());
                                                token.setOppositeUserUid(mCurrentUser.getUid());
                                                token.setOppositeUserUrl(mCurrentUser.getProfileUrl());
                                                token.setUid(chatRoom.getChatOppositeUser().getUid());
                                                dataSnapshot.getRef().setValue(token);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

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
                }
            }
        });
    }



    //아이템 사용 로직
    @OnClick(R.id.useItemBtn)
    public void onClickUseItemBtn() {

        View.OnClickListener leftListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooseDialog.dismiss();
                mUseItemDialog.dismiss();

            }
        };
        View.OnClickListener rightListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChooseDialog.dismiss();
                mUseItemDialog.dismiss();

                //1. 재화가 있는지 확인하고
                //2. 재화가 없으면, 재화가 없다고 한 뒤에 상점으로 이동하는 다이얼로그 띄워주기
                //3. 재화가 있는 경우에는 사용하여 재화 갯수를 깍고
                //4. 채팅 시간을 늘리고(disappear 타임을 한시간 뒤로 돌린 뒤에)
                //5. 각종 UI 갱신 작업
                //  5-1.프로그레스바 없애주고,
                //  5-2.채팅방 UI 갱신(아이템 사용한 채팅방으로 상단 고정 및 유아이 체인지),
                //  5-3.채팅 창에서도 색깔 바꿔주고
                //  5-4.메세지 출력(아이템을 사용하셨습니다)

                mUserCurrencyRef.child("coinCount").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long coinCount = dataSnapshot.getValue(long.class);
                        final long needCoin = Long.parseLong(mChooseDialog.mItemInfo.get("price"));
                        if (coinCount < needCoin) {
                            //새로운 다이알로그 출력(상점으로 이동하기)
//                            mMoveToStoreDialog = new CustomDialog(mContext, moveToLeftListener, moveToRightListener, "moveToStore");
//                            mMoveToStoreDialog.show();
                            Toast.makeText(ChatActivity.this, "숨이 부족하여 타이머를 설정할 수 없습니다.",Toast.LENGTH_LONG).show();
                        } else {

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDD", Locale.KOREA);
                            Calendar c = Calendar.getInstance();
                            String strToday = sdf.format(c.getTime());
                            if (mChooseDialog.mItemInfo.get("itemName").equals("nightShift") && c.get(Calendar.HOUR_OF_DAY) > 12) {
                                mChooseDialog.dismiss();
                                mUseItemDialog.dismiss();
                                Toast.makeText(ChatActivity.this, "000상품은 밤 12시 이후에만 사용하실 수 있습니다", Toast.LENGTH_LONG).show();
                            }

                            coinCount -= needCoin;
                            dataSnapshot.getRef().setValue(coinCount).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        //(chatFragment)로 푸쉬 여기서 챗룸 업데이트
                                        BusProvider.getInstance().post(new UseItemPushEvent(
                                                mChatId,
                                                UseItemPushEvent.itemTypeEnum.valueOf(mChooseDialog.mItemInfo.get("itemName")),
                                                System.currentTimeMillis(),
                                                needCoin
                                                ));

                                        //동시에 챗 메세지는 여기서 업데이트.
                                        sendMessage(INFOMATION, false);
                                    }
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        mChooseDialog.dismiss();
                        mUseItemDialog.dismiss();
                    }
                });
            }
        };
        mChooseDialog = new CustomDialog(this,
                leftListener, rightListener, oppositeUserProfileUrl, oppositeUserNickname, "buyItem");
        mUseItemDialog = new CustomDialog(this,
                mChooseDialog,oppositeUserProfileUrl, oppositeUserNickname, "useItem");
        mUseItemDialog.show();
    }


    View.OnClickListener moveToLeftListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mChooseDialog.dismiss();
            mUseItemDialog.dismiss();
            mMoveToStoreDialog.dismiss();
        }
    };

    View.OnClickListener moveToRightListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mChooseDialog.dismiss();
            mUseItemDialog.dismiss();
            mMoveToStoreDialog.dismiss();
        }
    };


    //챗룸이 notrated로 바뀌면, 채팅 할 수 있는 기능을 막음.
    @Subscribe
    public void chatRoomStatusChangeEvent(ChatRoomStatusChangeEvent chatRoomStatusChangeEvent) {
        if (chatRoomStatusChangeEvent != null) {
            //만약에 notrated가 도착하면 챗룸의 상태를 바꿔줌.
            Log.i("BUSEVENT", "notrated subscribe!");
            if (chatRoomStatusChangeEvent.getChatRoomStatusEnum().equals(ChatRoom.chatRoomStatusEnum.NOTRATED)) {
                //보내기 버튼과 글 입력 버튼, 재화 사용버튼을을 막음
//                preventChat();
            }
        }
    }

    private void preventChat() {
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        };
        mSenderBtn.setOnClickListener(clickListener);
        mUseItemBtn.setOnClickListener(clickListener);
        mEdtContent.setEnabled(false);
    }


    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }
}

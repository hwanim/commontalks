package com.ludus.commontalks.adapters;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.R;
import com.ludus.commontalks.models.ChatRoom;
import com.ludus.commontalks.models.TutorialCheck;
import com.ludus.commontalks.views.MainFragment.ChatFragment;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imhwan on 2017. 11. 23..
 */

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder> {

    ArrayList<ChatRoom> mChatIdArray;

    private ArrayList<CountDownTimer> mCountDownTimers;

    SimpleDateFormat chatRoomTimeFormat = new SimpleDateFormat("hh:mm");

    private FirebaseUser mFirebaseUser;

    private ChatFragment mFragment;

    private DatabaseReference mChatRef;

    private CountDownTimer initTimer;


    public void addItem(final ChatRoom item) {
        Log.i("CHATROOMADAPTER", "add item" + item.getChatId());
        mCountDownTimers.add(initTimer);
        mChatIdArray.add(item);
        notifyDataSetChanged();
    }

    public void setFragment(ChatFragment fragment) {
        this.mFragment = fragment;
    }

    public void initMoveItem(ChatRoom item) {
        int position = getPosition(item.getChatId());
        for (int count= 1; count <= mChatIdArray.size(); count++) {
            //position = 0 이면, 즉 첫번째 아이템이면 건너뛴다.
            if (position - count < 0) {
                break;
            }
            if (mChatIdArray.get(position-count).getLastMessageDate() < mChatIdArray.get(position-count+1).getLastMessageDate()) {
                Collections.swap(mCountDownTimers, position-count, position-count+1);
                Collections.swap(mChatIdArray, position-count, position-count+1);
            } else {
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void moveItem(ChatRoom item) {
        int position = getPosition(item.getChatId());
        int count = 1;
        ChatRoom chatRoom = mChatIdArray.get(position);

        for (ChatRoom chatRooms : mChatIdArray) {
            if (position - count < 0 ){
                break;
            }
            Collections.swap(mCountDownTimers, position-count, position-count+1);
            Collections.swap(mChatIdArray, position-count, position-count+1);
            count++;
        }
        notifyDataSetChanged();
    }

    public boolean hasItem(ChatRoom item){
        for (ChatRoom chatRoom : mChatIdArray) {
            if (chatRoom.getChatId().equals(item.getChatId())){
                return true;
            }
        }
        return false;
    }


    public ChatRoom getItem(int position) {
        return mChatIdArray.get(position);
    }

    public ChatRoomAdapter() {
        mChatIdArray = new ArrayList<>();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCountDownTimers = new ArrayList<>();
    }

    public void removeItem(ChatRoom item) {
        int position = getPosition(item.getChatId());
        Log.i("CHATROOMADAPTER", "call chatRoomAdapter.removeItem : "  + item.getChatOppositeUser().getNickname());
        mChatIdArray.remove(position);
        mCountDownTimers.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItem(ChatRoom item) {
        Log.i("CHATROOMADAPTER", "call chatRoomAdapter.updateItem : " + item.getChatOppositeUser().getNickname());
        if (!item.getChatRoomStatus().equals(ChatRoom.chatRoomStatusEnum.DISABLED)) {
            int position = getPosition(item.getChatId());
            mChatIdArray.set(position, item);
            notifyItemChanged(position);
        }
    }

    public int getPosition(String chatId) {
        int position = 0;
        for (ChatRoom currItem : mChatIdArray) {
            if (currItem.getChatId().equals(chatId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    @Override
    public ChatRoomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.item_chat_room, parent, false);

        return new ChatRoomViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final ChatRoomViewHolder holder, final int position) {
        final ChatRoom chatRoom = mChatIdArray.get(position);
        Log.i("CHATROOMADAPTER", "call chatRoomAdapter.onBindViewHolder : "  + chatRoom.getChatOppositeUser().getNickname());

        if (chatRoom.getDisabled()){
            holder.activeChatRoomLayout.setVisibility(View.GONE);
        } else {
            CountDownTimer timer = mCountDownTimers.get(position);

            //clickListener에서 notrated chatRoom을 rating으로 바꿔서 업데이트 쳐주고, 업데이트 된 데이터로 로직을 타게 만들어줌.
            if (chatRoom.getChatRoomStatus().equals(ChatRoom.chatRoomStatusEnum.NOTRATED)){
                chatRoomNotRatedLogic(holder, chatRoom, position);
                if ((mCountDownTimers.get(getPosition(chatRoom.getChatId())) != null)){
                    mCountDownTimers.get(getPosition(chatRoom.getChatId())).cancel();
                }

            } else {
                holder.disabledFrameLayout.setVisibility(View.GONE);
                if (chatRoom.getReadMsg()) {
                    holder.chatRoomMessageComeNoti.setVisibility(View.GONE);
                } else {
                    holder.chatRoomMessageComeNoti.setVisibility(View.VISIBLE);
                }

                holder.chatRoomProgressBar.setVisibility(View.GONE);
                holder.recentChat.setText(chatRoom.getLastMessage());
                holder.activeChatRoomLayout.setVisibility(View.VISIBLE);

                long currentTime = System.currentTimeMillis();
                long millsInFuture;

                if (timer != null) {
                    Log.i("CHATROOMADAPTER", "timer is not null and have to reset timer");
                    timer.cancel();
                }

                //if millsInFuture가 0보다 작으면 타이머 새로 등록하지말고 그냥 끝난걸로 처리해주면 될듯?? -- 오류날 확률 있음.
                millsInFuture = chatRoom.getDisappearTime() - currentTime;
                Log.i("CHATROOMADAPTER", "currentTime : " + currentTime);
                Log.i("CHATROOMADAPTER", "getDisappearTime : " + chatRoom.getDisappearTime());

                CountDownTimer newTimer = new CountDownTimer(millsInFuture, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        //시계를 분단위 / 시단위로 표시해주는 거 수정해주고
                        // 프로그레스바도 시간이 되면 나타나야함.
                        Log.i("CHATROOMADAPTER", "set Text");
                        long hour = millisUntilFinished / (1000*60*60);
                        long minute = millisUntilFinished / (1000*60) - hour * 60;
                        long second = millisUntilFinished / (1000) - minute * 60;
                        if (hour  > 0) {
                            holder.chatRoomProgressBar.setVisibility(View.GONE);
                            holder.sendTime.setText(String.valueOf(hour) + "시간 " + String.valueOf(minute) + "분");
                        } else if (minute  > 0) {
                            holder.chatRoomProgressBar.setVisibility(View.GONE);
                            holder.sendTime.setText(String.valueOf(minute) + "분");
                        } else {
                            holder.sendTime.setText(String.valueOf(second));
                            holder.chatRoomProgressBar.setVisibility(View.VISIBLE);
                            holder.chatRoomProgressBar.setProgress((int)((second*100)/60));
                        }
                    }

                    @Override
                    public void onFinish() {
                        Log.i("CHATROOMADAPTER", "on Finish");
                        chatRoomNotRatedLogic(holder, chatRoom, position);
                        mFragment.endMsgSend(ChatFragment.EndType.TIMEOUT, chatRoom.getChatId(), chatRoom.getChatOppositeUser().getUid());
                    }
                };

                Log.i("CHATROOMADAPTER", "new Timer Start");
                newTimer.start();
                mCountDownTimers.set(position, newTimer);
                holder.oppsiteUserNickName.setText(chatRoom.getChatOppositeUser().getNickname());
                holder.itemConstLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mFragment != null) {
                            mFragment.leaveChat(chatRoom);
                        }
                        return true;
                    }
                });
                if (chatRoom.getChatOppositeUser().getProfileUrl() != null) {
                    Glide.with(holder.chatRoomProfilePhoto)
                            .load(chatRoom.getChatOppositeUser().getProfileUrl())
                            .into(holder.chatRoomProfilePhoto);
                    holder.chatRoomProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                    holder.chatRoomProfilePhoto.setClipToOutline(true);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void chatRoomNotRatedLogic(final ChatRoomViewHolder holder, ChatRoom chatRoom, int position) {
        //레이아웃 세팅을 해줍니다.
        Log.i("CHATROOMADAPTER", "chatRoomNotRatedLogic excuted.");
        holder.disabledChatLikeBtn.setTag("0");
        holder.disabledChatLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (holder.disabledChatLikeBtn.getTag().toString()) {
                    case "0":
                        holder.disabledChatLikeBtn.setImageResource(R.drawable.ic_like_actv);
                        holder.disabledChatLikeBtn.setTag("1");
                        break;
                    case "1":
                        holder.disabledChatLikeBtn.setImageResource(R.drawable.ic_like_blank);
                        holder.disabledChatLikeBtn.setTag("0");
                        break;
                }
            }
        });
        if (chatRoom.getChatOppositeUser().getProfileUrl() != null) {
            Glide.with(holder.disabledChatRoomLayout)
                    .load(chatRoom.getChatOppositeUser().getProfileUrl())
                    .into(holder.disabledChatRoomPhoto);

            holder.disabledChatRoomPhoto.setBackground(new ShapeDrawable(new OvalShape()));
            holder.disabledChatRoomPhoto.setClipToOutline(true);
        }
        holder.activeChatRoomLayout.setVisibility(View.GONE);
        holder.disabledChatOppositeUserNickname.setText(chatRoom.getChatOppositeUser().getNickname());
        holder.disabledFrameLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        return mChatIdArray.size();
    }

    public class ChatRoomViewHolder extends RecyclerView.ViewHolder {

        //Active ChatRoom Layout
        @BindView(R.id.chatRoomProfilePhoto)
        ImageView chatRoomProfilePhoto;

        @BindView(R.id.recentChat)
        TextView recentChat;

        @BindView(R.id.sendTime)
        TextView sendTime;

        @BindView(R.id.oppsiteUserNickName)
        TextView oppsiteUserNickName;

        @BindView(R.id.itemConstLayout)
        ConstraintLayout itemConstLayout;

        @BindView(R.id.activeChatRoomLayout)
        LinearLayout activeChatRoomLayout;

        @BindView(R.id.chatRoomProgressBar)
        ProgressBar chatRoomProgressBar;

        @BindView(R.id.chatRoomMessageComeNoti)
        TextView chatRoomMessageComeNoti;


        //disabled ChatRoom Layout
        @BindView(R.id.disabledFrameLayout)
        public FrameLayout disabledFrameLayout;

        @BindView(R.id.disabledChatRoomLayout)
        public LinearLayout disabledChatRoomLayout;

        @BindView(R.id.disabledChatRoomPhoto)
        ImageView disabledChatRoomPhoto;

        @BindView(R.id.disabledChatOppositeUserNickname)
        TextView disabledChatOppositeUserNickname;

        @BindView(R.id.ic_like)
        public ImageView disabledChatLikeBtn;


        public ChatRoomViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}

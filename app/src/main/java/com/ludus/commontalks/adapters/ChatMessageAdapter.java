package com.ludus.commontalks.adapters;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.Image;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.ludus.commontalks.R;
import com.ludus.commontalks.models.Message;
import com.ludus.commontalks.views.ChatActivity;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imhwan on 2017. 11. 23..
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    ArrayList<Message> mMessageArrayList;

    private String userId;

    private SimpleDateFormat messageDateFormat = new SimpleDateFormat("hh:mm");

    private String mInitOppositeUserProfile;
    private String mInitOppositeUserNickName;


    public ChatMessageAdapter() {
        mMessageArrayList = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void setmInitOppositeUserProfile(String mInitOppositeUserProfile) {
        this.mInitOppositeUserProfile = mInitOppositeUserProfile;
    }


    public void clearItem(){
        mMessageArrayList.clear();
    }

    public void addItem(Message item) {
        if (mMessageArrayList.size() > 0 && mMessageArrayList.get(mMessageArrayList.size()-1).getMessageUser().getUid().equals(item.getMessageUser().getUid())) {
            item.setContinuoslySend(true);
        } else {
            item.setContinuoslySend(false);
        }
        Log.i("CHATMESSAGEADAPTER", String.valueOf(item.isContinuoslySend()));
        mMessageArrayList.add(item);
        notifyDataSetChanged();
    }

    public void addInitItem(Message item) {
        if (item.isInitMessage()){
            mMessageArrayList.add(item);
            notifyDataSetChanged();
        }
    }

    public int size() {
        return mMessageArrayList.size();
    }


    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new MessageViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = mMessageArrayList.get(position);
        Log.i("CHATMESSAGEADAPTER", message.getMessageText());
        //userId가 없으면 다시한번 요청해 줍니다..
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if (message.isInitMessage()) {
            holder.chatRoomStartMsg.setVisibility(View.VISIBLE);


            if (message.getMessageUser().getUid().equals(userId)){
                //post한 유저와 내가 같다면 대화를 받은 것이므로, 말을 건 유저의 말과 내 말을 띄워줍니다.
                holder.initUserChatItem.setVisibility(View.VISIBLE);
                holder.initUserChatTxt.setText(message.getMessageText());
                holder.initUserChatTime.setText(messageDateFormat.format(message.getMessageDate()));
                holder.initOppositeUserNickName.setText(mInitOppositeUserNickName);
                holder.initChatMsg.setText("님과의 대화방입니다.\n답장을 보내 보세요.");
                if (mInitOppositeUserProfile!=null) {
                    Glide.with(holder.chatRoomStartMsg)
                            .load(mInitOppositeUserProfile)
                            .into(holder.chatRoomInitOppositeUserProfilePhoto);
                    holder.chatRoomInitOppositeUserProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                    holder.chatRoomInitOppositeUserProfilePhoto.setClipToOutline(true);
                }

            } else {
                //post한 유저와 내가 같지 않다면 내가 말을 건것이므로, 포스트 유저의 정보를 initMsg에 띄워줍니다.
                if (message.getMessageUser().getProfileUrl() != null) {
                    Glide.with(holder.chatRoomStartMsg)
                            .load(message.getMessageUser().getProfileUrl())
                            .into(holder.InitOppositeUserProfilePhoto);
                    holder.InitOppositeUserProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                    holder.InitOppositeUserProfilePhoto.setClipToOutline(true);

                    Glide.with(holder.chatRoomStartMsg)
                            .load(message.getMessageUser().getProfileUrl())
                            .into(holder.chatRoomInitOppositeUserProfilePhoto);
                    holder.chatRoomInitOppositeUserProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                    holder.chatRoomInitOppositeUserProfilePhoto.setClipToOutline(true);
                }
                holder.initOppositeUserNickName.setText(message.getMessageUser().getNickname());
                holder.InitOppositeUserChatTxt.setText(message.getMessageText());
                holder.InitOppositeUserChatTime.setText(messageDateFormat.format(message.getMessageDate()));
                holder.initOppositeUserChatItem.setVisibility(View.VISIBLE);
            }
        } else if (message.getMessageType().equals(Message.messageTypeEnum.INFOMATION)){
            holder.chatRoomStartMsg.setVisibility(View.GONE);
            holder.yourArea.setVisibility(View.GONE);
            holder.sendArea.setVisibility(View.GONE);
            String opposite = "상대방이 ";
            if (message.getMessageUser().getUid().equals(userId)){
                opposite = "";
            }
            switch (message.getMessageText()) {
                case("ONEHOUR"):
                    holder.chatMsgSystemTxt.setText(opposite + "타이머를 사용했습니다 : 1시간");
                    holder.chatMsgSystemTimer.setImageResource(R.drawable.ic_timer_2);
                    break;

                case("TWOHOUR"):
                    holder.chatMsgSystemTxt.setText(opposite + "타이머를 사용했습니다 : 2시간");
                    holder.chatMsgSystemTimer.setImageResource(R.drawable.ic_timer_3);
                    break;

                case("FOURHOUR"):
                    holder.chatMsgSystemTxt.setText(opposite + "타이머를 사용했습니다 : 4시간");
                    holder.chatMsgSystemTimer.setImageResource(R.drawable.ic_timer_4);
                    break;

                case("NIGHTSHIFT"):
                    holder.chatMsgSystemTxt.setText(opposite + "타이머를 사용했습니다 : NightShift");
                    holder.chatMsgSystemTimer.setImageResource(R.drawable.ic_timer_5);
                    break;
            }
            holder.chatMsgSystemTime.setText(messageDateFormat.format(new Date().getTime()));
            holder.systemChatFrameLayout.setVisibility(View.VISIBLE);
        } else if (message.getMessageType().equals(Message.messageTypeEnum.END)) {
            //end logic
            //채팅이 끝났습니다를 띄워준다.
            holder.chatRoomStartMsg.setVisibility(View.GONE);
            holder.yourArea.setVisibility(View.GONE);
            holder.sendArea.setVisibility(View.GONE);
            holder.systemChatFrameLayout.setVisibility(View.GONE);
            holder.exitFrameLayout.setVisibility(View.VISIBLE);
            holder.exitTime.setText(messageDateFormat.format(new Date().getTime()));
        } else {
            holder.chatRoomStartMsg.setVisibility(View.GONE);
            holder.systemChatFrameLayout.setVisibility(View.GONE);
            holder.chatRoomStartMsg.setVisibility(View.GONE);
            //내가 보낸 메세지인지 판별
            if (message.getMessageUser().getUid().equals(userId)) {
                holder.sendTxt.setText(message.getMessageText());
                holder.sendDate.setText(messageDateFormat.format(new Date(message.getMessageDate())));
                holder.yourArea.setVisibility(View.GONE);
                holder.sendArea.setVisibility(View.VISIBLE);
            } else {
                holder.rcvTextView.setText(message.getMessageText());
                holder.rcvDate.setText(messageDateFormat.format(new Date(message.getMessageDate())));
                if (message.isContinuoslySend()) {
                    holder.rcvProfileView.setVisibility(View.GONE);
                } else {
                    if (message.getMessageUser().getProfileUrl() != null) {
                        Log.i("CHATMESSAGEADAPTER", "show Opposite User profile : " + message.getMessageUser().getNickname());
                        Glide.with(holder.yourArea)
                                .load(message.getMessageUser()
                                        .getProfileUrl())
                                .into(holder.rcvProfileView);
                        holder.rcvProfileView.setBackground(new ShapeDrawable(new OvalShape()));
                        holder.rcvProfileView.setClipToOutline(true);
                        holder.rcvProfileView.setVisibility(View.VISIBLE);
                    }
                }
                holder.yourArea.setVisibility(View.VISIBLE);
                holder.sendArea.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMessageArrayList.size();
    }

    public void setmInitOppositeUserNickName(String mInitOppositeUserNickName) {
        this.mInitOppositeUserNickName = mInitOppositeUserNickName;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.oppositeUserChatItem)
        LinearLayout yourArea;

        @BindView(R.id.userChatItem)
        LinearLayout sendArea;

        @BindView(R.id.oppositeUserPrfilePhoto)
        ImageView rcvProfileView;

        @BindView(R.id.oppositeUserChatTxt)
        TextView rcvTextView;

        @BindView(R.id.oppositeUserChatTime)
        TextView rcvDate;

        @BindView(R.id.userChatTime)
        TextView sendDate;

        @BindView(R.id.userChatTxt)
        TextView sendTxt;

        //init MSG(공통)
        @BindView(R.id.chatRoomStartMsg)
        LinearLayout chatRoomStartMsg;

        @BindView(R.id.chatRoomInitOppositeUserProfilePhoto)
        ImageView chatRoomInitOppositeUserProfilePhoto;

        @BindView(R.id.initOppositeUserNickName)
        TextView initOppositeUserNickName;

        @BindView(R.id.initChatMsg)
        TextView initChatMsg;


        //init MSG(상대방이 포스트유저일때)
        @BindView(R.id.initOppositeUserChatItem)
        LinearLayout initOppositeUserChatItem;

        @BindView(R.id.InitOppositeUserChatTxt)
        TextView InitOppositeUserChatTxt;

        @BindView(R.id.InitOppositeUserChatTime)
        TextView InitOppositeUserChatTime;

        @BindView(R.id.InitOppositeUserProfilePhoto)
        ImageView InitOppositeUserProfilePhoto;


        //initMSG(내가 포스트 유저일때)
        @BindView(R.id.initUserChatItem)
        LinearLayout initUserChatItem;

        @BindView(R.id.initUserChatTxt)
        TextView initUserChatTxt;

        @BindView(R.id.initUserChatTime)
        TextView initUserChatTime;



        //system message
        @BindView(R.id.systemChatFrameLayout)
        FrameLayout systemChatFrameLayout;

        @BindView(R.id.chatMsgSystemTimer)
        ImageView chatMsgSystemTimer;

        @BindView(R.id.chatMsgSystemTxt)
        TextView chatMsgSystemTxt;

        @BindView(R.id.chatMsgSystemTime)
        TextView chatMsgSystemTime;


        //대화가 끝났을때
        @BindView(R.id.exitFrameLayout)
        FrameLayout exitFrameLayout;

        @BindView(R.id.exitTime)
        TextView exitTime;

        public MessageViewHolder(View v){
            super(v);
            ButterKnife.bind(this,v);
        }
    }
}

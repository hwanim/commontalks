package com.ludus.commontalks.CustomViews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.UseItemPushEvent;
import com.ludus.commontalks.models.Post;
import com.ludus.commontalks.views.ChatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.microedition.khronos.opengles.GL;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imhwan on 2017. 12. 11..
 */

public class CustomDialog extends Dialog {

    private Context mContext;
    private View.OnClickListener mLeftClickListener;
    private View.OnClickListener mRightClickListener;
    private Post mPost;
    private String dialogType;
    private CustomDialog dialog;
    public HashMap<String, String> mItemInfo;
    private String oppositeUserProfileUrl;
    private String oppositeUserNickname;

    //대화방 들어가기 다이알로그
    @BindView(R.id.popupChatLimit)
    TextView popupChatLimit;

    @BindView(R.id.postUserProfilePhoto)
    ImageView postUserProfilePhoto;

    @BindView(R.id.postUserNickname)
    TextView postUserNickname;

    @BindView(R.id.popupNoButton)
    TextView noButton;

    @BindView(R.id.popupYesButton)
    TextView yesButton;

    @BindView(R.id.popupLayout)
    LinearLayout popupLayout;

    @BindView(R.id.txt_content)
    TextView txt_content;

    //신고하기 dialog
    @BindView(R.id.reportUserPopupLayout)
    LinearLayout reportUserPopupLayout;

    @BindView(R.id.reportUserPopupProfilePhoto)
    ImageView reportUserPopupProfilePhoto;

    @BindView(R.id.reportUserPopupNickname)
    TextView reportUserPopupNickname;

    @BindView(R.id.reportUserNoBtn)
    TextView reportUserNoBtn;

    @BindView(R.id.reportUserYesBtn)
    TextView reportUserYesBtn;

    //아이템사용하기 다이알로그
    @BindView(R.id.useItemLayout)
    LinearLayout useItemLayout;

    @BindView(R.id.item1Layout)
    LinearLayout item1;

    @BindView(R.id.item2Layout)
    LinearLayout item2;

    @BindView(R.id.item3Layout)
    LinearLayout item3;

    @BindView(R.id.item4Layout)
    LinearLayout item4;

    @BindView(R.id.itemUserOpppositeUserPhoto)
    ImageView itemUserOpppositeUserPhoto;

    @BindView(R.id.itemUserOppositeUserNickname)
    TextView itemUserOppositeUserNickname;

    //buyitem 다이알로그
    @BindView(R.id.buyItemLayout)
    LinearLayout buyItemLayout;

    @BindView(R.id.buyNoBtn)
    TextView buyNoBtn;

    @BindView(R.id.buyYesBtn)
    TextView buyYesBtn;

    @BindView(R.id.buyItemTxt)
    TextView buyItemTxt;

    @BindView(R.id.buyItemOpppositeUserNickname)
    TextView buyItemOpppositeUserNickname;

    @BindView(R.id.buyItemTimer)
    ImageView buyItemTimer;

    @BindView(R.id.buyItemOpppositeUserPhoto)
    ImageView buyItemOpppositeUserPhoto;


    //beta dialog
    @BindView(R.id.betaDialogLayout)
    LinearLayout betaDialogLayout;

    @BindView(R.id.betaDialogBtn)
    TextView betaDialogBtn;

    @BindView(R.id.betaDialogContentsTxt)
    TextView betaDialogContentsTxt;


    //checkVersion Dialog
    @BindView(R.id.versionUpdateDialogLayout)
    LinearLayout versionUpdateDialogLayout;

    @BindView(R.id.versionUpdateYesBtn)
    TextView versionUpdateYesBtn;

    @BindView(R.id.versionUpdateNoBtn)
    TextView versionUpdateNoBtn;

    @BindView(R.id.versionUpdateTxt)
    TextView versionUpdateTxt;


    //채팅방제한 설명 다이얼로그
    @BindView(R.id.chatLimitDescribeLayout)
    LinearLayout chatLimitDescribeLayout;

    @BindView(R.id.chatLimitDescribeYesBtn)
    TextView chatLimitDescribeYesBtn;



    public CustomDialog(Context context, View.OnClickListener leftListener,
                        View.OnClickListener rightListener, Post post, String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mRightClickListener = rightListener;
        this.mLeftClickListener = leftListener;
        this.mPost = post;
        this.dialogType = dialogType;
    }

    public CustomDialog(Context context, View.OnClickListener rightListener, String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mRightClickListener = rightListener;
        this.dialogType = dialogType;
    }


    public CustomDialog(Context context, View.OnClickListener leftListener,
                        View.OnClickListener rightListener, String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mRightClickListener = rightListener;
        this.mLeftClickListener = leftListener;
        this.mContext = context;
        this.dialogType = dialogType;
    }

    public CustomDialog(Context context, CustomDialog dialog, String photoUrl, String nickname, String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.dialogType = dialogType;
        this.dialog = dialog;
        this.oppositeUserProfileUrl = photoUrl;
        this.oppositeUserNickname = nickname;


    }

    public CustomDialog(Context context, View.OnClickListener leftListener,
                        View.OnClickListener rightListener, String photoUrl, String nickname, String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mRightClickListener = rightListener;
        this.mLeftClickListener = leftListener;
        this.mContext = context;
        this.dialogType = dialogType;
        this.oppositeUserProfileUrl = photoUrl;
        this.oppositeUserNickname = nickname;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.activity_custom_dialog);
        ButterKnife.bind(this);

        //생성자 체크
        if (dialogType.equals("talkStart") ) {
            popupLayout.setVisibility(View.VISIBLE);

            postUserNickname.setText(mPost.getUser().getNickname());
            popupChatLimit.setText(String.valueOf(mPost.getChatLimitValue()));
            if (mPost.getUser().getProfileUrl() != null) {
                Glide.with(popupLayout)
                        .load(mPost.getUser().getProfileUrl())
                        .into(postUserProfilePhoto);
                postUserProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                postUserProfilePhoto.setClipToOutline(true);

            }

            txt_content.setText("채팅가능 금액으로 "
                    +String.valueOf(mPost.getChatLimitValue())
                    +"숨을 설정하셨습니다. \n "
                    + String.valueOf(mPost.getChatLimitValue())
                    +"숨으로 말을 걸 수 있어요. \n 대화를 거시겠습니까?");
        } else if (dialogType.equals("reportFeed")) {
            reportUserPopupLayout.setVisibility(View.VISIBLE);
            if (mPost.getUser().getProfileUrl() != null) {

                Glide.with(reportUserPopupLayout)
                        .load(mPost.getUser().getProfileUrl())
                        .into(reportUserPopupProfilePhoto);
                reportUserPopupProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
                reportUserPopupProfilePhoto.setClipToOutline(true);
            }
            reportUserPopupNickname.setText(mPost.getUser().getNickname());
        } else if (dialogType.equals("useItem")) {
            Log.i("CustomDialog", "use item 호출");

            itemUserOppositeUserNickname.setText(oppositeUserNickname);
            Glide
                    .with(useItemLayout)
                    .load(oppositeUserProfileUrl)
                    .into(itemUserOpppositeUserPhoto);
            itemUserOpppositeUserPhoto.setBackground(new ShapeDrawable(new OvalShape()));
            itemUserOpppositeUserPhoto.setClipToOutline(true);

            useItemLayout.setVisibility(View.VISIBLE);
            final HashMap<String, String> itemInfo = new HashMap<>();
            View.OnClickListener useItemClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()){
                        case R.id.item1Layout:
                            Log.i("CustomDialog", "item1Layout 호출");
                            itemInfo.put("itemName", "ONEHOUR");
                            itemInfo.put("price", "14");
                            break;
                        case R.id.item2Layout:
                            Log.i("CustomDialog", "item2Layout 호출");
                            itemInfo.put("itemName", "TWOHOUR");
                            itemInfo.put("price", "22");
                            break;
                        case R.id.item3Layout:
                            Log.i("CustomDialog", "item3Layout 호출");
                            itemInfo.put("itemName", "FOURHOUR");
                            itemInfo.put("price", "46");
                            break;
                        case R.id.item4Layout:
                            Log.i("CustomDialog", "item4Layout 호출");
                            itemInfo.put("itemName", "NIGHTSHIFT");
                            itemInfo.put("price", "60");
                            break;
                    }



                    dialog.mItemInfo = itemInfo;
                    dialog.show();

                }
            };
            item1.setOnClickListener(useItemClickListener);
            item2.setOnClickListener(useItemClickListener);
            item3.setOnClickListener(useItemClickListener);
            item4.setOnClickListener(useItemClickListener);

        } else if (dialogType.equals("buyItem")){
            Log.i("CustomDialog", "set buy item Dialog" + mItemInfo.toString());
            buyItemTxt.setText("님과의 대화에 타이머를 설정하시겠습니까? \n" +mItemInfo.get("price") + "숨을 사용합니다.");

            buyItemOpppositeUserNickname.setText(oppositeUserNickname);
            Glide
                    .with(buyItemLayout)
                    .load(oppositeUserProfileUrl)
                    .into(buyItemOpppositeUserPhoto);
            buyItemOpppositeUserPhoto.setBackground(new ShapeDrawable(new OvalShape()));
            buyItemOpppositeUserPhoto.setClipToOutline(true);

            switch (mItemInfo.get("itemName")){
                case "ONEHOUR":
                    buyItemTimer.setImageResource(R.drawable.ic_timer_2);
                    break;
                case "TWOHOUR":
                    buyItemTimer.setImageResource(R.drawable.ic_timer_3);
                    break;
                case "FOURHOUR":
                    buyItemTimer.setImageResource(R.drawable.ic_timer_4);
                    break;
                case "NIGHTSHIFT":
                    buyItemTimer.setImageResource(R.drawable.ic_timer_5);
                    break;
            }
            useItemLayout.setVisibility(View.GONE);
            buyItemLayout.setVisibility(View.VISIBLE);
        } else if (dialogType.equals("betaDialog")) {
            betaDialogLayout.setVisibility(View.VISIBLE);
            betaDialogContentsTxt.setText("안녕하세요! \n현재 '일상의 대화'는 베타서비스 중입니다. \n \n" +
                    "베타버전을 사용해주시는 여러분께 감사의 말씀을 드리며," +
                    " 베타 기간 동안 가입시 앱 내에서 사용 가능한 재화 '숨' 100개가 무료로 지급됩니다.\n\n" +
                    "개선 의견은 언제나 환영합니다!\n" +
                    "즐거운 대화되세요.");
        } else if (dialogType.equals("versionCheck")) {
            versionUpdateDialogLayout.setVisibility(View.VISIBLE);
            versionUpdateTxt.setText("안녕하세요!\n" +
                    "새 버전이 업데이트 됐어요.\n\n" +
                    "업데이트를 하셔야 로그인이 가능\n" +
                    "합니다.\n\n" +
                    "번거롭더라도 업데이트를 꼭 하셔서\n" +
                    "원활한 이용하시기 바랍니다!");
        } else if (dialogType.equals("chatLimitDescribe")) {
            chatLimitDescribeLayout.setVisibility(View.VISIBLE);
        }


        // 클릭 이벤트 셋팅
        if (mLeftClickListener != null && mRightClickListener != null) {
            switch (dialogType){
                case "talkStart":
                    noButton.setOnClickListener(mLeftClickListener);
                    yesButton.setOnClickListener(mRightClickListener);
                    break;

                case "reportFeed":
                    reportUserNoBtn.setOnClickListener(mLeftClickListener);
                    reportUserYesBtn.setOnClickListener(mRightClickListener);
                    break;

                case "buyItem":
                    buyNoBtn.setOnClickListener(mLeftClickListener);
                    buyYesBtn.setOnClickListener(mRightClickListener);

                case "versionCheck":
                    versionUpdateYesBtn.setOnClickListener(mRightClickListener);
                    versionUpdateNoBtn.setOnClickListener(mLeftClickListener);
            }
        } else if (mLeftClickListener == null && mRightClickListener != null) {
            switch (dialogType) {
                case "betaDialog":
                    betaDialogBtn.setOnClickListener(mRightClickListener);
                case "chatLimitDescribe":
                    chatLimitDescribeYesBtn.setOnClickListener(mRightClickListener);
            }
        }



    }
}

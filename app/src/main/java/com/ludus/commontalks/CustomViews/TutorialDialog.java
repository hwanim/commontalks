package com.ludus.commontalks.CustomViews;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.R;
import com.ludus.commontalks.models.ChatRoom;
import com.ludus.commontalks.models.TutorialCheck;
import com.ludus.commontalks.views.ChatActivity;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.roughike.bottombar.BottomBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;

/**
 * Created by imhwan on 2018. 1. 5..
 */

public class TutorialDialog extends Dialog {

    private Context mContext;
    private View.OnClickListener mClickListener, mBackBtnListener;
    private String mDialogType;
    private View.OnClickListener mSkipListener;
    private FirebaseDatabase mFirebaseDb;
    private FirebaseUser mFirebaseUser;
    private SwipePlaceHolderView mSwipeView;
    int count;

    @Getter
    private static boolean mLeftSwipe;
    @Getter
    private static boolean mRightSwipe;



    @BindView(R.id.tutorial2Layout)
    ConstraintLayout tutorial2Layout;

    @BindView(R.id.tutorial2LayoutIcon)
    ImageView tutorial2LayoutIcon;

    @BindView(R.id.tutorial2LayoutTitle)
    TextView tutorial2LayoutTitle;

    @BindView(R.id.tutorial2LayoutContent)
    TextView tutorial2LayoutContent;

    @BindView(R.id.tutorial2LayoutBackBtn)
    TextView tutorial2LayoutBackBtn;

    @BindView(R.id.tutorial2LayoutNextBtn)
    TextView tutorial2LayoutNextBtn;

    //swipeTuto
    @BindView(R.id.swipeTutorialLayout)
    ConstraintLayout swipeTutorialLayout;

    @BindView(R.id.swipeTutorialLayoutNextBtn)
    TextView swipeTutorialLayoutNextBtn;

    @BindView(R.id.swipeTutorialLayoutBackBtn)
    TextView swipeTutorialLayoutBackBtn;


    //firstSwipeTuto
    @BindView(R.id.firstSwipeLayout)
    ConstraintLayout firstSwipeLayout;

    @BindView(R.id.firstSwipeLayoutTitle)
    TextView firstSwipeLayoutTitle;

    @BindView(R.id.firstSwipeLayoutContent)
    TextView firstSwipeLayoutContent;

    @BindView(R.id.firstSwipeLayoutNextBtn)
    TextView firstSwipeLayoutNextBtn;

    @BindView(R.id.firstSwipeLayoutBackBtn)
    TextView firstSwipeLayoutBackBtn;

    //toolbar, bottombar, skipTuto, rootView
    @BindView(R.id.tutorialDialogRootViewLayout)
    ConstraintLayout tutorialDialogRootViewLayout;

    @BindView(R.id.tutorialToolbar)
    android.support.v7.widget.Toolbar tutorialToolbar;

    @BindView(R.id.tutorialToolbarTitleTextView)
    TextView tutorialToolbarTitleTextView;

    @BindView(R.id.tutorialBottomBar)
    BottomBar tutorialBottomBar;

    @BindView(R.id.skipTutorialBtn)
    TextView skipTutorialBtn;

    //ratingtuto

    @BindView(R.id.ratingTutoLayout)
    ConstraintLayout ratingTutoLayout;

    @BindView(R.id.ratingImageView)
    ImageView ratingImageView;

    @BindView(R.id.ratingTutoYesBtn)
    TextView ratingTutoYesBtn;

    //timertuto
    @BindView(R.id.ratingTutoNoBtn)
    TextView ratingTutoNoBtn;

    //chatTuto
    @BindView(R.id.chatTutoLayout)
    ConstraintLayout chatTutoLayout;


    public TutorialDialog(@NonNull Context context, String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mDialogType = dialogType;
    }


    public TutorialDialog(@NonNull Context context, View.OnClickListener skipListener, String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mDialogType = dialogType;
        this.mSkipListener = skipListener;
    }


    public TutorialDialog(@NonNull Context context, SwipePlaceHolderView swipeView , String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mDialogType = dialogType;
        this.mSwipeView = swipeView;
    }

    public TutorialDialog(@NonNull Context context, SwipePlaceHolderView swipeView, View.OnClickListener startTalkBtnListener , String dialogType) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.mContext = context;
        this.mDialogType = dialogType;
        this.mSwipeView = swipeView;
        this.mClickListener = startTalkBtnListener;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.tutorial_dialog);
        ButterKnife.bind(this);

        mFirebaseDb = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        skipTutorialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TutorialDialog.this.dismiss();
                setTutorialRef(TutorialType.FIRST);
            }
        });

        //UI 설정
        switch (mDialogType){
            case "seeNow":
                mClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog tutorialDialog = new TutorialDialog(mContext,mSkipListener, "swipe");
                        tutorialDialog.show();
                        TutorialDialog.this.dismiss();
                    }
                };

                tutorial2Layout.setBackgroundResource(R.drawable.ic_speech_bubble_1); //모양을 받아서 적용
                tutorial2LayoutIcon.setImageResource(R.drawable.ic_feed_icon_color);
                tutorial2LayoutTitle.setText(R.string.navDialogTitle1);
                tutorial2LayoutContent.setText(R.string.navDialog1);
                tutorial2LayoutNextBtn.setOnClickListener(mClickListener);
                tutorial2Layout.setVisibility(View.VISIBLE);
                tutorialBottomBar.selectTabWithId(R.id.tab_feed);
                tutorial2LayoutBackBtn.setVisibility(View.GONE);
                break;

            case "swipe":
                mClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog tutorialDialog = new TutorialDialog(mContext,mSkipListener, "writePost");
                        tutorialDialog.show();
                        TutorialDialog.this.dismiss();
                    }
                };

                mBackBtnListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog tutorialDialog = new TutorialDialog(mContext,mSkipListener, "seeNow");
                        tutorialDialog.show();
                        TutorialDialog.this.dismiss();
                    }
                };

                tutorial2Layout.setVisibility(View.GONE);
                swipeTutorialLayoutNextBtn.setOnClickListener(mClickListener);
                swipeTutorialLayoutBackBtn.setOnClickListener(mBackBtnListener);
                tutorialBottomBar.selectTabWithId(R.id.tab_feed);
                swipeTutorialLayout.setVisibility(View.VISIBLE);
                break;

            case "writePost":
                mClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog tutorialDialog = new TutorialDialog(mContext,mSkipListener, "chat");
                        tutorialDialog.show();
                        TutorialDialog.this.dismiss();
                    }
                };

                mBackBtnListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog tutorialDialog = new TutorialDialog(mContext,mSkipListener, "swipe");
                        tutorialDialog.show();
                        TutorialDialog.this.dismiss();
                    }
                };

                tutorial2Layout.setBackgroundResource(R.drawable.ic_speech_bubble_2); //모양을 받아서 적용
                tutorial2LayoutIcon.setImageResource(R.drawable.ic_write_post);
                tutorial2LayoutTitle.setText(R.string.navDialogTitle2);
                tutorial2LayoutTitle.setTextSize(20);
                tutorialToolbarTitleTextView.setText("글쓰기");
                tutorial2LayoutContent.setText(R.string.navDialog2);
                tutorial2LayoutNextBtn.setOnClickListener(mClickListener);
                tutorialBottomBar.selectTabWithId(R.id.tab_write);
                tutorial2LayoutBackBtn.setOnClickListener(mBackBtnListener);
                tutorial2Layout.setVisibility(View.VISIBLE);
                break;

            case "chat":
                mClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setTutorialRef(TutorialType.FIRST);
                        TutorialDialog.this.dismiss();
                    }
                };

                mBackBtnListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog tutorialDialog = new TutorialDialog(mContext,mSkipListener, "writePost");
                        tutorialDialog.show();
                        TutorialDialog.this.dismiss();
                    }
                };

                tutorial2Layout.setBackgroundResource(R.drawable.ic_speech_bubble_3); //모양을 받아서 적용
                tutorial2LayoutIcon.setImageResource(R.drawable.ic_chat_icon_color);
                tutorial2LayoutTitle.setText(R.string.navDialog3);
                tutorialToolbarTitleTextView.setText("채팅");
                tutorial2LayoutTitle.setTextSize(24);
                tutorial2LayoutContent.setText(R.string.navDialogTitle3);
                tutorial2LayoutNextBtn.setText("끝!");
                tutorialBottomBar.selectTabWithId(R.id.tab_chat);
                tutorial2LayoutNextBtn.setOnClickListener(mClickListener);
                tutorial2LayoutBackBtn.setOnClickListener(mBackBtnListener);
                tutorial2Layout.setVisibility(View.VISIBLE);
                break;

            case "firstLeftSwipe":
                setTutorialRef(TutorialType.LEFT);
                mClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog.this.dismiss();
                    }
                };
                mBackBtnListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSwipeView.undoLastSwipe();
                        TutorialDialog.this.dismiss();
                    }
                };
                skipTutorialBtn.setVisibility(View.GONE);
                tutorialToolbar.setVisibility(View.GONE);
                tutorialBottomBar.setVisibility(View.GONE);
                firstSwipeLayoutTitle.setText(R.string.firstSwipeLayoutTitleLeft);
                firstSwipeLayoutContent.setText(R.string.firstSwipeLayoutContentLeft);
                firstSwipeLayoutNextBtn.setText(R.string.swipe);
                firstSwipeLayoutNextBtn.setOnClickListener(mClickListener);
                firstSwipeLayoutBackBtn.setOnClickListener(mBackBtnListener);
                firstSwipeLayoutBackBtn.setVisibility(View.GONE);
                firstSwipeLayout.setVisibility(View.VISIBLE);
                break;

            case "firstRightSwipe":
                setTutorialRef(TutorialType.RIGHT);
                mBackBtnListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog.this.dismiss();
                    }
                };

                skipTutorialBtn.setVisibility(View.GONE);
                tutorialToolbar.setVisibility(View.GONE);
                tutorialBottomBar.setVisibility(View.GONE);
                firstSwipeLayoutTitle.setText(R.string.firstSwipeLayoutTitleRight);
                firstSwipeLayoutContent.setText(R.string.firstSwipeLayoutContentRight);
                firstSwipeLayoutNextBtn.setText(R.string.startTalk);
                firstSwipeLayoutNextBtn.setOnClickListener(mClickListener);
                firstSwipeLayoutBackBtn.setOnClickListener(mBackBtnListener);
                firstSwipeLayout.setVisibility(View.VISIBLE);
                break;



            case "chatTuto":
                setTutorialRef(TutorialType.CHAT);
                skipTutorialBtn.setVisibility(View.GONE);
                tutorialToolbar.setVisibility(View.GONE);
                tutorialBottomBar.setVisibility(View.GONE);
                chatTutoLayout.setVisibility(View.VISIBLE);
                tutorialDialogRootViewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog.this.dismiss();
                    }
                });
                break;

            case "ratingTuto":
                setTutorialRef(TutorialType.RATING);
                setMultipageClickListener(TutorialType.RATING);
                break;

            case "timerTuto":
                setTutorialRef(TutorialType.TIMER);
                setMultipageClickListener(TutorialType.TIMER);
                break;
        }
    }

    private void setMultipageClickListener(TutorialType tutorialType){
        count = 0;
        skipTutorialBtn.setVisibility(View.GONE);
        switch (tutorialType) {
            case RATING:
                ratingImageView.setImageResource(R.drawable.img_rating_guide1);
                ratingTutoLayout.setVisibility(View.VISIBLE);
                ratingTutoYesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        count++;
                        switch (count) {
                            case 1:
                                ratingImageView.setImageResource(R.drawable.img_rating_guide2);
                                break;
                            case 2:
                                ratingImageView.setImageResource(R.drawable.img_rating_guide3);
                                break;
                            case 3:
                                TutorialDialog.this.dismiss();
                                break;
                        }
                    }
                });
                break;

            case TIMER:
                ratingImageView.setImageResource(R.drawable.img_timer_guide2);
                ratingTutoLayout.setVisibility(View.VISIBLE);
                ratingTutoNoBtn.setVisibility(View.VISIBLE);
                ratingTutoYesBtn.setText(R.string.useTimeSpend);
                ratingTutoNoBtn.setText(R.string.cancel);

                ratingTutoYesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //아이템 다이럴로그를 팝업시켜줍니다.

                        //임시
                        TutorialDialog.this.dismiss();
                    }
                });

                ratingTutoNoBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TutorialDialog.this.dismiss();
                    }
                });
                break;
        }

    }


    private void setTutorialRef(final TutorialType tutorialType) {
        mFirebaseDb.getReference("tutorial_check").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (tutorialType.equals(TutorialType.FIRST)) {
                        TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                        if (!tutorialCheck.isSwipeTuto()) {
                            tutorialCheck.setSwipeTuto(true);
                            dataSnapshot.getRef().setValue(tutorialCheck);
                        }
                    } else if(tutorialType.equals(TutorialType.LEFT)) {
                        TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                        if (!tutorialCheck.isLeftSwipeTuto()) {
                            tutorialCheck.setLeftSwipeTuto(true);
                            dataSnapshot.getRef().setValue(tutorialCheck);
                            mLeftSwipe = true;
                        }
                    }else if(tutorialType.equals(TutorialType.RIGHT)) {
                        TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                        if (!tutorialCheck.isRightSwipeTuto()) {
                            tutorialCheck.setRightSwipeTuto(true);
                            dataSnapshot.getRef().setValue(tutorialCheck);
                            mRightSwipe = true;
                        }
                    } else if (tutorialType.equals(TutorialType.CHAT)) {
                        TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                        if (!tutorialCheck.isChatTuto()) {
                            tutorialCheck.setChatTuto(true);
                            dataSnapshot.getRef().setValue(tutorialCheck);
                        }
                    } else if (tutorialType.equals(TutorialType.RATING)) {
                        TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                        if (!tutorialCheck.isRatingTuto()) {
                            tutorialCheck.setRatingTuto(true);
                            dataSnapshot.getRef().setValue(tutorialCheck);
                        }
                    } else if (tutorialType.equals(TutorialType.TIMER)) {
                        TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                        if (!tutorialCheck.isTimerTuto()) {
                            tutorialCheck.setRatingTuto(true);
                            dataSnapshot.getRef().setValue(tutorialCheck);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private enum TutorialType {
        FIRST, LEFT, RIGHT, CHAT, RATING, TIMER
    }
}

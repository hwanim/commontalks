package com.ludus.commontalks.CustomViews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.PushEvent;
import com.ludus.commontalks.models.BlockedPost;
import com.ludus.commontalks.models.Post;
import com.ludus.commontalks.models.TutorialCheck;
import com.ludus.commontalks.models.User;
import com.ludus.commontalks.views.ChatActivity;
import com.ludus.commontalks.views.MainFragment.FeedFragment;
import com.mindorks.placeholderview.SwipeDirection;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.mindorks.placeholderview.annotations.infinite.LoadMore;
import com.mindorks.placeholderview.annotations.swipe.SwipeCancelState;
import com.mindorks.placeholderview.annotations.swipe.SwipeIn;
import com.mindorks.placeholderview.annotations.swipe.SwipeInState;
import com.mindorks.placeholderview.annotations.swipe.SwipeOut;
import com.mindorks.placeholderview.annotations.swipe.SwipeOutState;
import com.mindorks.placeholderview.annotations.swipe.SwipeTouch;
import com.mindorks.placeholderview.annotations.swipe.SwipingDirection;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;

import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;

import static android.view.View.GONE;
import static com.ludus.commontalks.models.BlockedPost.blockTypeEnum.reported;

/**
 * Created by imhwan on 2017. 12. 4..
 */

@Layout(R.layout.swipe_card_view)
public class FeedCard {

    @View(R.id.profileImageView)
    private ImageView postPhoto;

    @View(R.id.nameAgeTxt)
    private TextView nameAgeTxt;

    @View(R.id.locationNameTxt)
    private TextView locationNameTxt;

    @View(R.id.userNickname)
    private TextView userNickname;

    @View(R.id.userProfilePhoto)
    private ImageView userProfileView;

    @View(R.id.chatLimitValueDisplay)
    private TextView chatLimitValueDisplay;

    @View(R.id.reportFeedUser)
    private ImageView reportFeedUser;

    @View(R.id.cardViewRatingInfo)
    private TextView cardViewRatingInfo;

    @View(R.id.cardViewRatingImageView)
    private ImageView cardViewRatingImageView;

    @View(R.id.cardViewRatingLayout)
    private LinearLayout cardViewRatingLayout;

    private Post mPost;
    private Context mContext;
    private SwipePlaceHolderView mSwipeView;
    private Intent intent;
    private CustomDialog mStartTalkDialog;
    private CustomDialog mReportDialog;
    private DatabaseReference mPostRef;
    private DatabaseReference mCurrencyRef;
    private DatabaseReference mBlockPostRef;
    private FirebaseDatabase mFirebaseDb;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mTutorialRef;
    private Configuration mConfiguration;
    private boolean mLeftSwipe = true;
    private boolean mRightSwipe = true;
    private static String DOMAIN = "sandbox38def3c3373341ccacfe75fa4c858959.mailgun.org";
    private static String API_KEY = "key-aa5f6c3a16e50f7cd808976e819b4812";
    private TutorialDialog mDialog;


    public FeedCard(Context context, Post post,Boolean left, Boolean right, SwipePlaceHolderView swipeView) {
        mContext = context;
        mPost = post;
        mSwipeView = swipeView;
        mFirebaseDb = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mPostRef = mFirebaseDb.getReference("posts").child(mPost.getPostId()).child("reportCount");
        mBlockPostRef = mFirebaseDb.getReference("blocked_posts").child(mFirebaseUser.getUid());
        mLeftSwipe = left;
        mRightSwipe = right;

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Resolve
    private void onResolved(){
        Log.d("FEEDCARD", "postAdded" + mPost.getPostTxt());
        if (mPost.getUser().getProfileUrl() != null) {
            Glide.with(mContext).load(mPost.getUser().getProfileUrl()).into(userProfileView);
            userProfileView.setBackground(new ShapeDrawable(new OvalShape()));
            userProfileView.setClipToOutline(true);

        }

        if (mPost.getPhotoUrl() != null) {
            Glide.with(mContext)
                    .load(mPost.getPhotoUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_post_no_photo))
                            .into(postPhoto);
        }
        userNickname.setText(mPost.getUser().getNickname());
        nameAgeTxt.setText(mPost.getPostTxt());
        cardViewRatingInfo.setText(setRatingTextView(mPost.getUser().getRatings()));
        long time = System.currentTimeMillis() - mPost.getPostDate();
        long day = time / (1000 * 60 * 60 * 24);
        long hour = time / (1000*60*60);
        long minute = time / (1000*60);
        long second = time / (1000);
        Log.i("FEEDCARD", hour + " " + minute + " " + second);
        if (day >0) {
            locationNameTxt.setText(day + "일 전");
        } else if (hour  > 0) {
            locationNameTxt.setText(hour + "시간 전");
        } else if (minute  > 0) {
            locationNameTxt.setText(minute + "분 전");
        } else if (second > 30){
            locationNameTxt.setText(second+ "초 전");
        } else {
            locationNameTxt.setText("지금");
        }
        if (mPost.getChatLimitValue() != 0) {
            chatLimitValueDisplay.setText("(" + String.valueOf(mPost.getChatLimitValue()) + ")");
        }

        reportFeedUser.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Log.d("FEEDCARD", "Report Btn Clicked");
                mReportDialog = new CustomDialog(mContext, reportFeedUserNoBtn, reportFeedUserYesBtn, mPost, "reportFeed");
                mReportDialog.show();
            }
        });
    }

    public Post getmPost() {
        return mPost;
    }


    private String setRatingTextView(HashMap<String, Integer> hashMap) {
        Integer caring = hashMap.get("caring");

        if (caring > 0 ){
            cardViewRatingImageView.setVisibility(android.view.View.VISIBLE);
            return String.valueOf(caring);
        }

        return "칭찬 정보가 없어요.";
    }


    @SwipeOut
    private void onSwipedOut(){
        Log.i("FEEDCARD", "Child count : " + mSwipeView.getChildCount());
        Log.d("FEEDCARD", "onSwipeOut" + this.getmPost().getPostId());
        Log.d("FEEDCARD", "onSwipeOut" + mSwipeView.getAllResolvers().size());
        //뷰가 10개 남았을 때 추가 뷰를 로딩해줍니다
        if (!mLeftSwipe && !TutorialDialog.isMLeftSwipe()) {
            Log.d("FEEDCARD", "onSwipeOut" + TutorialDialog.isMLeftSwipe());
            TutorialDialog dialog = new TutorialDialog(mContext,mSwipeView,"firstLeftSwipe");
            dialog.show();
        }

        if (mSwipeView.getAllResolvers().size() == 10 ){
            loadAdditionalViews();
        } else if (mSwipeView.getAllResolvers().size() == 1) {
            BusProvider.getInstance().post(new PushEvent(false, true));
        }
    }


    @SwipeCancelState
    private void onSwipeCancelState(){
        Log.d("EVENT", "onSwipeCancelState");
    }

    @SwipeIn
    private void onSwipeIn(){
        Log.d("FEEDCARD", "onSwipedIn" + this.getmPost().getPostId());
        Log.d("FEEDCARD", "onSwipedIn" + mSwipeView.getAllResolvers().size());
        if (!mRightSwipe && !TutorialDialog.isMRightSwipe()) {
            android.view.View.OnClickListener startTalkBtnListener = new android.view.View.OnClickListener() {
                @Override
                public void onClick(android.view.View v) {
                    onClickStartTalk();
                    mDialog.dismiss();
                }
            };
            mDialog = new TutorialDialog(mContext,mSwipeView,startTalkBtnListener,"firstRightSwipe");
            mDialog.show();
        } else {
            onClickStartTalk();
        }


        //뷰가 10개 남았을 때 추가 뷰를 로딩해줍니다
        if (mSwipeView.getAllResolvers().size() == 10 ){
            loadAdditionalViews();
        }else if (mSwipeView.getAllResolvers().size() == 1) {
            BusProvider.getInstance().post(new PushEvent(false, true));
        }
    }

    @SwipeInState
    private void onSwipeInState(){
        Log.d("FEEDCARD", "onSwipeInState");
    }

    @SwipeOutState
    private void onSwipeOutState(){
        Log.d("FEEDCARD", "onSwipeOutState");
    }



    private void loadAdditionalViews() {
        Log.d("FEEDCARD", "time to load more views");
        BusProvider.getInstance().post(new PushEvent(true, false));

    }

    public void onClickStartTalk() {
        Log.d("FEEDCARD", "start Talk Button clicked");
        Log.i("CustomDialog", "Chat Limit : " + mPost.getChatLimitValue());
        int chatLimit = mPost.getChatLimitValue();
        if (chatLimit != 0) {
            mStartTalkDialog = new CustomDialog(mContext, startTalkNoBtnListener, startTalkYesBtnListener, mPost, "talkStart");
            mStartTalkDialog.show();
        } else {
            startNewTalk();
        }
    }

    private android.view.View.OnClickListener startTalkNoBtnListener = new android.view.View.OnClickListener() {
        @Override
        public void onClick(android.view.View v) {
            mStartTalkDialog.dismiss();
        }
    };

    private android.view.View.OnClickListener startTalkYesBtnListener = new android.view.View.OnClickListener() {
        @Override
        public void onClick(android.view.View v) {
            //코인 부족할경우 대화 실패함.
            FirebaseDatabase.getInstance()
                    .getReference("currency").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("coinCount")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long userCoin = dataSnapshot.getValue(long.class);
                            if (mPost.getChatLimitValue() > userCoin){
                                mStartTalkDialog.dismiss();
                                Toast.makeText(mContext, "숨이 부족하여 대화를 시작할 수 없습니다.", Toast.LENGTH_LONG).show();
                            } else {
                                dataSnapshot.getRef().setValue(userCoin - mPost.getChatLimitValue());
                                startNewTalk();
                                mStartTalkDialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    };


    private android.view.View.OnClickListener reportFeedUserNoBtn = new android.view.View.OnClickListener() {
        @Override
        public void onClick(android.view.View v) {
            mReportDialog.dismiss();
        }
    };

    private android.view.View.OnClickListener reportFeedUserYesBtn = new android.view.View.OnClickListener() {
        @Override
        public void onClick(android.view.View v) {
            //게시물 정보를 발송함

            mPostRef.setValue(mPost.getReportCount() + 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(mContext, "신고가 성공적으로 완료되었습니다.", Toast.LENGTH_LONG).show();
                        mSwipeView.doSwipe(false);
                        mReportDialog.dismiss();

                        //신고했기 때문에 신고한 게시자를 블록 포스트에 등록
                        BlockedPost blockedPost = new BlockedPost();
                        blockedPost.setBlockType(reported);
                        blockedPost.setPostId(mPost.getPostId());
                        blockedPost.setPostUserId(mPost.getUser().getUid());
                        mBlockPostRef.child(mPost.getPostId()).setValue(blockedPost);

                        //스레드를 통해서 메일 발송
                        Thread thread = new Thread() {
                            @Override
                            public void run() {
                                mConfiguration = new Configuration()
                                        .domain(DOMAIN)
                                        .apiKey(API_KEY)
                                        .from("ludus2018@gmail.com");

                                Mail.using(mConfiguration)
                                        .to("ludus2018@gmail.com")
                                        .subject("신고 게시물 보고")
                                        .text("UserId : " + mPost.getUser().getUid() + "\n" +
                                                "UserNickName : " + mPost.getUser().getNickname() + "\n" +
                                                "내용 : " + mPost.getPostTxt() +  "\n" +
                                                "사진 : " + mPost.getPhotoUrl())
                                        .build()
                                        .send();
                            }
                        };
                        thread.start();

                    }
                }
            });
        }
    };

    private void startNewTalk() {
        intent = new Intent(mContext, ChatActivity.class);
        User user = getmPost().getUser();
        intent.putExtra("uid", user.getUid());
        intent.putExtra("username", user.getUsername());
        intent.putExtra("email", user.getEmail());
        intent.putExtra("profileUrl", user.getProfileUrl());
        intent.putExtra("nickname", user.getNickname());
        intent.putExtra("postcontents", getmPost().getPostTxt());
        intent.putExtra("postId",getmPost().getPostId());
        mContext.startActivity(intent);
    }
}

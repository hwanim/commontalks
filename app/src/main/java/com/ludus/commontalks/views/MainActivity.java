package com.ludus.commontalks.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.CustomViews.CustomDialog;
import com.ludus.commontalks.CustomViews.TutorialDialog;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.CoinChangeEvent;
import com.ludus.commontalks.Services.MarketVersionChecker;
import com.ludus.commontalks.Services.NotiSettingPushEvent;
import com.ludus.commontalks.Services.NotificationService;
import com.ludus.commontalks.Services.PushEvent;
import com.ludus.commontalks.Services.UserDataChangeEvent;
import com.ludus.commontalks.models.NotificationSetting;
import com.ludus.commontalks.models.TutorialCheck;
import com.ludus.commontalks.models.User;
import com.ludus.commontalks.views.MainFragment.ChatFragment;
import com.ludus.commontalks.views.MainFragment.FeedFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private ChatFragment chatFragment;
    private FeedFragment feedFragment;

    private static int WRITE_POST_INTENT = 100;

    private Fragment mPrevFrag;

    private boolean mFromActivity = false;

    private String mPreviousTag;

    public User mUser;

    public long mCoinCount;

    private ArrayList<Boolean> mNotiSettingBoolean;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mNotiRef;
    private DatabaseReference mTokenRef;
    private DatabaseReference mCurrencyRef;
    private DatabaseReference mDatabaseRef;

    private FirebaseDatabase mFirebaseDb;

    private Intent mServiceStartIntent;

    private static Boolean mAlreadySeenChatTuto = true;
    private static Boolean mAlreadySeenSwipeTuto = false;

    private DatabaseReference mTutoRef;

    private Context mContext;

    private String store_version;

    private Handler mHandler = null;
    private CustomDialog mVersionCheckDialog;
    private static final int DIALOG_PRINT = 12;

    @BindView(R.id.bottomBar)
    BottomBar bottomBar;

    @BindView(R.id.my_toolbar)
    Toolbar mMainToolbar;

    @BindView(R.id.toolbarTitleTextView)
    TextView titleView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BusProvider.getInstance().register(this);

        //노티로부터 들어오면, 바로 메인을 거쳐서 챗 액티비티를 띄워준다.
        if (getIntent().getExtras() != null
                && getIntent().getBooleanExtra("fromNotiFlag", false)
                &&!getIntent().getExtras().containsKey("compliment")
                ) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
        }
        progressON();



        mContext = MainActivity.this;
        mFirebaseDb = FirebaseDatabase.getInstance();
        mDatabaseRef = mFirebaseDb.getReference("users");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mTutoRef = mFirebaseDb.getReference("tutorial_check").child(mFirebaseUser.getUid());
        mCurrencyRef = mFirebaseDb.getReference("currency").child(mFirebaseUser.getUid());
        mNotiRef = mFirebaseDb.getReference("noti_setting").child(mFirebaseUser.getUid());
        mTokenRef = mFirebaseDb.getReference("token_ids").child(mFirebaseUser.getUid());

        mHandler = new VersionCheckHandler();
        if (mUser == null) {
            mUser = new User();
            initUserSetting();
        }

        versionCheck();
        //튜토리얼 값을 받아오고
        //만약 튜토리얼 값이 없다면 값을 넣어주고 튜토리얼도 시작해준다.
        initTutorialSetting();


        //처음 시작할 때 boolean값을 받아와야함.
        mNotiSettingBoolean = new ArrayList<>();
        mNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    GenericTypeIndicator<ArrayList<Boolean>> t = new GenericTypeIndicator<ArrayList<Boolean>>() {};
                    mNotiSettingBoolean = dataSnapshot.child("notiSetting").getValue(t);
                } else {
                    mNotiSettingBoolean = new ArrayList<>();
                    mNotiSettingBoolean.add(true);
                    mNotiSettingBoolean.add(true);
                    mNotiSettingBoolean.add(true);
                    mNotiSettingBoolean.add(true);
                    dataSnapshot.getRef().setValue(mNotiSettingBoolean);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        ButterKnife.bind(this);

        mMainToolbar.setTitle("일상의 대화");
        mMainToolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        setSupportActionBar(mMainToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setFragment();
        checkToken();
    }

    @SuppressLint("HandlerLeak")
    class VersionCheckHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == DIALOG_PRINT) {
                mVersionCheckDialog.show();
            }
        }
    }

    private void versionCheck() {

        final View.OnClickListener leftListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVersionCheckDialog.dismiss();
            }
        };

        final View.OnClickListener rightListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + getPackageName()));
                startActivity(intent);
            }
        };
        mVersionCheckDialog = new CustomDialog(mContext, leftListener, rightListener, "versionCheck");

        Thread thread = new Thread() {
            @Override
            public void run() {
                store_version = MarketVersionChecker.getMarketVersion(getPackageName());

                try {
                    String device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

                    if (store_version == null) {
                        return;
                    }
                    if (store_version.compareTo(device_version) > 0) {
                        Log.i("VerIntCheck", "업데이트 필요");
                        Log.i("VerIntCheck", "StoreVersion : " + store_version + ", device_version : " + device_version);
                        Message msg = mHandler.obtainMessage();
                        msg.what = DIALOG_PRINT;
                        mHandler.sendMessage(msg);

                    } else {
                        Log.i("VerIntCheck", "업데이트 필요없음.");
                        Log.i("VerIntCheck", "StoreVersion : " + store_version + ", device_version : " + device_version);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void checkToken() {
        mTokenRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.getRef().setValue(FirebaseInstanceId.getInstance().getToken());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void initTutorialSetting() {
        final View.OnClickListener skipListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseDb.getReference("tutorial_check").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                            if (!tutorialCheck.isSwipeTuto()) {
                                tutorialCheck.setSwipeTuto(true);
                                dataSnapshot.getRef().setValue(tutorialCheck);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        if (!mAlreadySeenSwipeTuto) {
            mFirebaseDb.getReference("tutorial_check").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        //데이터가 있으면 데이터를 보고 아직 안했으면 튜토리얼을 진행하고
                        TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                        mAlreadySeenSwipeTuto = tutorialCheck.isSwipeTuto();
                        mAlreadySeenChatTuto = tutorialCheck.isRatingTuto();
                        if (!mAlreadySeenSwipeTuto) {
                            TutorialDialog tutorialDialog = new TutorialDialog(MainActivity.this,skipListener, "seeNow");
                            tutorialDialog.show();
                            mAlreadySeenSwipeTuto = true;
                        }
                    } else {
                        //datasnapshot이 아예 없으면 새로 업데이트 해주고,
                        //튜토리얼을 시작한다.
                        TutorialCheck tutorialCheck = new TutorialCheck();
                        tutorialCheck.setUid(mFirebaseUser.getUid());
                        dataSnapshot.getRef().setValue(tutorialCheck);
                        TutorialDialog tutorialDialog = new TutorialDialog(MainActivity.this,skipListener, "seeNow");
                        tutorialDialog.show();
                        mAlreadySeenSwipeTuto = true;
                        mAlreadySeenChatTuto = false;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }




    }

    @Subscribe
    public void notiSettingEvent(NotiSettingPushEvent notiSettingPushEvent) {
        if (notiSettingPushEvent != null) {
            mNotiSettingBoolean = new ArrayList<>();
            Log.i("NotiService", " notiSetting comes " + notiSettingPushEvent.toString());
            mNotiSettingBoolean.add(0,notiSettingPushEvent.getEntireSetting());
            mNotiSettingBoolean.add(1,notiSettingPushEvent.getSet1());
            mNotiSettingBoolean.add(2,notiSettingPushEvent.getSet2());
            mNotiSettingBoolean.add(3,notiSettingPushEvent.getSet3());
            mNotiRef.child("notiSetting").setValue(mNotiSettingBoolean);
        }
        Log.i("NotiService", " notiSetting complete " + mNotiSettingBoolean.toString());
    }


    private void checkHaveToLoadNewestPost() {
        //이벤트를 푸쉬해서 클릭했는데 만약에 피드에 포스트가 하나도 없으면, 새로 로딩해줍니다.
        PushEvent pushEvent = new PushEvent(false, false);
        pushEvent.setLoadWhenNoPostExists(true);
        BusProvider.getInstance().post(pushEvent);
    }

    private void initUserSetting(){
        mDatabaseRef.child(mFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mUser = dataSnapshot.getValue(User.class);
                    BusProvider.getInstance().post(new UserDataChangeEvent(mUser));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mCurrencyRef.child("coinCount").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                    mCoinCount = dataSnapshot.getValue(long.class);
                    BusProvider.getInstance().post(new CoinChangeEvent(mCoinCount));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
        });
    }

    @Override
    protected void onResume() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.show(mPrevFrag);
        transaction.commit();
        super.onResume();
    }

    private void setFragment() {
        feedFragment = new FeedFragment();
        chatFragment = new ChatFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.contentContainer, chatFragment, "chat");
        transaction.add(R.id.contentContainer, feedFragment, "feed");
        transaction.hide(chatFragment);
        mPrevFrag = feedFragment;
        titleView.setText("일상의 대화");
        transaction.commit();

        bottomBar.setDefaultTab(R.id.tab_feed);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                if (mFromActivity) {
                    mPrevFrag = getSupportFragmentManager().findFragmentByTag(mPreviousTag);
                    mFromActivity = false;
                    return;
                }
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if (tabId == R.id.tab_chat) {
                    transaction.show(chatFragment);
                    titleView.setText("채팅");
                    if (mPrevFrag != null ){
                        Log.i("FEEDCARD", "previous Fragment : " + mPrevFrag.getTag());
                        transaction.hide(mPrevFrag);
                    }
                    transaction.commit();
                    mPrevFrag = chatFragment;

                    //튜토리얼을 안봤다면 튜토리얼을 보여줍니다.
                    if (!mAlreadySeenChatTuto) {
                        TutorialDialog tutorialDialog = new TutorialDialog(mContext, "ratingTuto");
                        tutorialDialog.show();
                        mAlreadySeenChatTuto = true;
                    }


                } else if (tabId == R.id.tab_feed) {
                    transaction.show(feedFragment);
                    titleView.setText("일상의 대화");
                    checkHaveToLoadNewestPost();
                    if (mPrevFrag != null ){
                        Log.i("FEEDCARD", "previous Fragment : " + mPrevFrag.getTag());
                        transaction.hide(mPrevFrag);
                    }
                    transaction.commit();
                    mPrevFrag = feedFragment;
                } else if (tabId == R.id.tab_write) {
                    Log.i("FEEDCARD", "sending intent with mPrevFrag : " + mPrevFrag.getTag() );
                    Intent intent = new Intent(MainActivity.this, WritePostActivity.class);
                    intent.putExtra("previousFragment", mPrevFrag.getTag());
                    intent.putExtra("Uid", mFirebaseUser.getUid());
                    intent.putExtra("profilePhoto", mUser.getProfileUrl() );
                    intent.putExtra("nickname", mUser.getNickname());
                    intent.putExtra("rating", mUser.getRatings());
                    intent.putExtra("coincount", String.valueOf(mCoinCount));
                    startActivityForResult(intent, WRITE_POST_INTENT);
                    transaction.commit();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == WRITE_POST_INTENT) {
                mFromActivity = true;
                mPreviousTag = data.getExtras().getString("fragment");
                if (mPreviousTag.equals(chatFragment.getTag())) {
                    bottomBar.selectTabWithId(R.id.tab_chat);
                    titleView.setText("채팅");

                } else if (mPreviousTag.equals(feedFragment.getTag())) {
                    bottomBar.selectTabWithId(R.id.tab_feed);
                    titleView.setText("일상의 대화");
                    checkHaveToLoadNewestPost();
                }
            }
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @OnClick(R.id.settingActivityBtn)
    public void onClickSettingBtn() {
        Intent intent = new Intent(this, SettingActivity.class);
        intent.putExtra("Uid", mFirebaseUser.getUid());
        intent.putExtra("profilePhoto", mUser.getProfileUrl() );
        intent.putExtra("nickname", mUser.getNickname());
        intent.putExtra("rating", mUser.getRatings());
        intent.putExtra("coincount", String.valueOf(mCoinCount));
        startActivity(intent);
    }
}

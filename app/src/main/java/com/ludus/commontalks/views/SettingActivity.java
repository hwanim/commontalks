package com.ludus.commontalks.views;

import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.CoinChangeEvent;
import com.ludus.commontalks.Services.NotiSettingPushEvent;
import com.ludus.commontalks.Services.UserDataChangeEvent;
import com.ludus.commontalks.models.User;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.coinsLayout)
    LinearLayout coinsLayout;

    @BindView(R.id.settingProfilePhoto)
    ImageView settingProfilePhoto;

    @BindView(R.id.settingContentsLayout)
    LinearLayout settingContentsLayout;

    @BindView(R.id.settingNickName)
    TextView settingNickName;

    @BindView(R.id.settingRatingInfoLayout)
    LinearLayout settingRatingInfoLayout;
    @BindView(R.id.settingRatingInfo)
    TextView settingRatingInfo;

    @BindView(R.id.settingRatingLikeImage)
    ImageView settingRatingLikeImage;

    @BindView(R.id.haveCoins)
    TextView haveCoins;

    @BindView(R.id.notificationSwitch)
    Switch notificationSwitch;

    @BindView(R.id.newChatNotiSwitch)
    Switch newChatNotiSwitch;

    @BindView(R.id.newMsgNotiSwitch)
    Switch newMsgNotiSwitch;

    @BindView(R.id.newCompliNotiSwitch)
    Switch newCompliNotiSwitch;

    @BindView(R.id.notiDetailSettingLayout)
    LinearLayout notiDetailSettingLayout;

    @BindView(R.id.goToShopBtn)
    ImageView goToShopBtn;

    @BindView(R.id.suggestFeedbackBtn)
    ImageView suggestFeedbackBtn;

    private String mCoinCount;
    private DatabaseReference mNotiRef;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mDatabaseRef;
    private String mUid;
    private User mUser;
    private static Boolean notificationSwitchBoolean = false;
    private static Boolean newChatNotiSwitchBoolean = false;
    private static Boolean newMsgNotiSwitchBoolean = false;
    private static Boolean newCompliNotiSwitchBoolean = false;
    private static Boolean initLoadNotiSetting = true;
    private ArrayList<Boolean> mNotiSettingBoolean;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mFirebaseDb = FirebaseDatabase.getInstance();
        mUid = getIntent().getStringExtra("Uid");
        mDatabaseRef = mFirebaseDb.getReference("users").child(mUid);
        mNotiRef = mFirebaseDb.getReference("noti_setting").child(mUid).child("notiSetting");

        BusProvider.getInstance().register(this);
        Bundle bundle = getIntent().getExtras();
        mUser = new User();
        ButterKnife.bind(this);

        mUser.setNickname(getIntent().getStringExtra("nickname"));
        mUser.setProfileUrl(getIntent().getStringExtra("profilePhoto"));
        mUser.setUid(mUid);
        HashMap<String, Integer> hashMap = (HashMap<String, Integer>)getIntent().getSerializableExtra("rating");
        mUser.setRatings(hashMap);
        setRatingTextView(hashMap);

        if (initLoadNotiSetting){
            mNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        GenericTypeIndicator<ArrayList<Boolean>> t = new GenericTypeIndicator<ArrayList<Boolean>>() {};
                        mNotiSettingBoolean = dataSnapshot.getValue(t);
                        notificationSwitchBoolean = mNotiSettingBoolean.get(0);
                        newChatNotiSwitchBoolean = mNotiSettingBoolean.get(1);
                        newMsgNotiSwitchBoolean = mNotiSettingBoolean.get(2);
                        newCompliNotiSwitchBoolean = mNotiSettingBoolean.get(3);
                        setSwitches();

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            initLoadNotiSetting = false;
        }

        setSwitches();


        haveCoins.setText(bundle.getString("coincount"));
        mCoinCount = bundle.getString("coincount");

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    notiDetailSettingLayout.setVisibility(View.VISIBLE);
                } else {
                    notiDetailSettingLayout.setVisibility(View.GONE);
                }
            }
        });

        Glide.with(settingContentsLayout)
                .load(mUser.getProfileUrl())
                .into(settingProfilePhoto);
        settingProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
        settingProfilePhoto.setClipToOutline(true);
        settingNickName.setText(mUser.getNickname());
        haveCoins.setText(bundle.getString("coincount"));
        mCoinCount = bundle.getString("coincount");
    }

    private void setRatingTextView(HashMap<String, Integer> hashMap) {
        Integer caring = hashMap.get("caring");
        if (caring> 0) {
            settingRatingInfo.setText(String.valueOf(caring));
        } else {
            settingRatingLikeImage.setVisibility(View.GONE);
            settingRatingInfo.setText("아직 칭찬 정보가 없어요. 대화를 한 후 상대방을 칭찬해보세요!");
        }
    }



    @OnClick(R.id.coinsLayout)
    public void onClickCoinLayout() {
        Intent intent = new Intent(this,PaymentActivity.class);
        intent.putExtra("coincount", mCoinCount);
        startActivity(intent);
    }

    @OnClick(R.id.goToShopBtn)
    public void onClickGoToShop(){
        onClickCoinLayout();
    }





    @Subscribe
    public void changeCoinCount(CoinChangeEvent coinChangeEvent) {
        if (coinChangeEvent != null) {
            mCoinCount = String.valueOf(coinChangeEvent.getCoinCount());
            haveCoins.setText(mCoinCount);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe
    public void changeUserDataEvent(UserDataChangeEvent userDataChangeEvent) {
        if (userDataChangeEvent != null) {
            mUser = userDataChangeEvent.getMUser();
        }
        Log.i("BundleData", "subscribe");
        Glide.with(settingContentsLayout)
                .load(mUser.getProfileUrl())
                .into(settingProfilePhoto);
        settingProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
        settingProfilePhoto.setClipToOutline(true);

        settingNickName.setText(mUser.getNickname());
        setRatingTextView(mUser.getRatings());
    }

    private void checkSwitches(){
        if (notificationSwitch.isChecked()) {
            notificationSwitchBoolean = true;
        } else {
            notificationSwitchBoolean = false;
        }
        if (newMsgNotiSwitch.isChecked()) {
            newMsgNotiSwitchBoolean = true;
        }else {
            newMsgNotiSwitchBoolean = false;
        }
        if (newChatNotiSwitch.isChecked()) {
            newChatNotiSwitchBoolean = true;
        } else {
            newChatNotiSwitchBoolean = false;
        }
        if (newCompliNotiSwitch.isChecked()) {
            newCompliNotiSwitchBoolean = true;
        } else {
            newCompliNotiSwitchBoolean = false;
        }
    }

    private void setSwitches() {
        if (notificationSwitchBoolean) {
            notificationSwitch.setChecked(true);
            notiDetailSettingLayout.setVisibility(View.VISIBLE);
        }
        if (newMsgNotiSwitchBoolean) {
            newMsgNotiSwitch.setChecked(true);
        }
        if (newChatNotiSwitchBoolean) {
            newChatNotiSwitch.setChecked(true);
        }
        if (newCompliNotiSwitchBoolean) {
            newCompliNotiSwitch.setChecked(true);
        }

    }


    @OnClick(R.id.get_contracts)
    public void onClickGetContractsBtn(){
        startActivity(new Intent(SettingActivity.this, ContractsActivity.class));
    }



    @OnClick(R.id.suggestFeedbackBtn)
    public void onClickSuggestFeedbackBtn(){
        Intent intent = new Intent(this, SuggestFeedbackActivity.class);
        intent.putExtra("uid", mUser.getUid());
        intent.putExtra("nickname", mUser.getNickname());
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        checkSwitches();
        BusProvider.getInstance().post(new NotiSettingPushEvent(notificationSwitchBoolean,
                 newChatNotiSwitchBoolean,newMsgNotiSwitchBoolean, newCompliNotiSwitchBoolean));
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }
}

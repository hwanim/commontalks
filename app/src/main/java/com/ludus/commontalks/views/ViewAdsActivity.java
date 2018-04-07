package com.ludus.commontalks.views;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.R;

public class ViewAdsActivity extends BaseActivity implements RewardedVideoAdListener {


    private String APP_ID = "ca-app-pub-3937848554161704~3486062521";
    private String EXAM_APP_ID = "ca-app-pub-3940256099942544/5224354917";
    private String PROMOTION_SHOW_ADS_UNIT_5 = "ca-app-pub-3937848554161704/9859899186";
    private int AD_VIEW_SUCCEESS = 1;
    private int AD_VIEW_FAILED = 0;


    private RewardedVideoAd mRewardedVideoAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ads);
        MobileAds.initialize(this, APP_ID);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();
    }

    private void loadRewardedVideoAd() {
        Toast.makeText(this, "동영상을 로딩 중입니다.", Toast.LENGTH_SHORT).show();
        mRewardedVideoAd.loadAd(PROMOTION_SHOW_ADS_UNIT_5,
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Intent intent = new Intent();
        intent.putExtra("rewardResult", AD_VIEW_FAILED);
        setResult(RESULT_OK, intent);
        finish();
    }

        @Override
    public void onRewarded(RewardItem rewardItem) {
        //리워드를 지급해주면 됩니다.
        Intent intent = new Intent();
        intent.putExtra("rewardResult", AD_VIEW_SUCCEESS);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Toast.makeText(this, "광고 로딩을 실패했습니다. 인터넷 연결상태를 확인해주세요.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onResume() {
        mRewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }

}

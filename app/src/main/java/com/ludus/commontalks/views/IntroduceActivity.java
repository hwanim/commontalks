package com.ludus.commontalks.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.CustomViews.CustomDialog;
import com.ludus.commontalks.R;
import com.matthewtamlin.sliding_intro_screen_library.DotIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IntroduceActivity extends BaseActivity {

    @BindView(R.id.introduceViewPager)
    ViewPager mIntroViewPager;

    @BindView(R.id.main_indicator_ad)
    DotIndicator main_indicator_ad;

    @BindView(R.id.startAppBtn)
    ImageView startAppBtn;

    private CustomDialog betaDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduce);

        ButterKnife.bind(this);


        View.OnClickListener rightListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (betaDialog.isShowing()) {
                    betaDialog.dismiss();
                }
            }
        };
        betaDialog = new CustomDialog(this, rightListener, "betaDialog");
        betaDialog.show();

        main_indicator_ad.setSelectedDotColor( Color.parseColor( "#3e87ff" ) );
        main_indicator_ad.setUnselectedDotColor( Color.parseColor( "#dcdcdc" ) );


        mIntroViewPager.setAdapter(new pagerAdapter(getSupportFragmentManager()));
        main_indicator_ad.setNumberOfItems(5);

        mIntroViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                main_indicator_ad.setSelectedItem(mIntroViewPager.getCurrentItem(), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


    }

    @OnClick(R.id.startAppBtn)
    public void onClickStartBtn() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private class pagerAdapter extends FragmentStatePagerAdapter {

        public pagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return IntroduceFragment.create(position);
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (betaDialog.isShowing()) {
            betaDialog.dismiss();
        }
    }
}

package com.ludus.commontalks.Base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ludus.commontalks.R;

import java.util.HashMap;

/**
 * Created by imhwan on 2017. 12. 23..
 */

public class BaseApplication extends Application {


    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        super.attachBaseContext(base);
    }

    private static BaseApplication baseApplication;
    AppCompatDialog progressDialog;

    public static BaseApplication getInstance() {
        return baseApplication;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }

    public void progressON(Activity activity, String message) {
        Log.i("progress", "progressON");

        if (activity == null || activity.isFinishing()) {
            return;
        }


        if (progressDialog != null && progressDialog.isShowing()) {
            progressSET(message);
        } else {
            progressDialog = new AppCompatDialog(activity);

//            WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
//            lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
//            lpWindow.dimAmount = 0.8f;
//            getWindow().setAttributes(lpWindow);


            progressDialog.setCancelable(false);
            progressDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.progresss_loading);
            progressDialog.show();
        }

//
//        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
//        final AnimationDrawable frameAnimation = (AnimationDrawable) img_loading_frame.getBackground();
//        img_loading_frame.post(new Runnable() {
//            @Override
//            public void run() {
//                frameAnimation.start();
//            }
//        });
//
        TextView msgText = (TextView) progressDialog.findViewById(R.id.loading_text);
        if (!TextUtils.isEmpty(message)) {
            msgText.setText(message);
            msgText.setTextColor(Color.WHITE);
        }

        ImageView image = (ImageView)progressDialog.findViewById(R.id.loadingImageView);
        Glide.with(this)
                .asGif()
                .load(R.drawable.splash)
        .into(image);
    }

    public void progressSET(String message) {

        if (progressDialog == null || !progressDialog.isShowing()) {
            return;
        }

//
//        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
//        if (!TextUtils.isEmpty(message)) {
//            tv_progress_message.setText(message);
//        }

    }

    public void progressOFF() {
        Log.i("progress", "progressOFF");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

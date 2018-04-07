package com.ludus.commontalks.views;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.CustomViews.CustomDialog;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.MarketVersionChecker;
import com.ludus.commontalks.Services.NotificationService;
import com.ludus.commontalks.models.Currency;
import com.ludus.commontalks.models.NotificationSetting;
import com.ludus.commontalks.models.User;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class StartActivity extends BaseActivity {

    private FirebaseAuth mFirebaseAuth;

    private FirebaseUser mFirebaseUser;

    private FirebaseDatabase mFirebaseDb;

    private DatabaseReference mUserRef;

    private DatabaseReference mUserCurrencyRef;

    private DatabaseReference mNotiRef;

    private Intent mServiceStartIntent;

    private static Context mContext;

    //internet state check member variable
    public static final String WIFE_STATE = "WIFI";
    public static final String MOBILE_STATE = "MOBILE";
    public static final String NONE_STATE = "NONE";
    public static final String CONNECTION_CONFIRM_CLIENT_URL = "http://clients3.google.com/generate_204";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        mContext = this;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDb = FirebaseDatabase.getInstance();
        mUserRef = mFirebaseDb.getReference("users");

        Boolean internetCheck = isOnline();

        if (!internetCheck) {
            Log.i("VerIntCheck", "Make Toast.");
            Toast.makeText(this, "인터넷 연결상태가 좋지 않습니다.\n앱을 종료해주세요.", Toast.LENGTH_SHORT).show();
        }


        if (mFirebaseAuth.getCurrentUser() != null ) {
            mUserRef.child(mFirebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user.getUid() == null) {
                            startActivity(new Intent(StartActivity.this, IntroduceActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(StartActivity.this, MainActivity.class));
                            mUserCurrencyRef = mFirebaseDb.getReference("currency").child(mFirebaseAuth.getCurrentUser().getUid());
                            mFirebaseUser = mFirebaseAuth.getCurrentUser();
                            mUserRef.child(mFirebaseAuth.getCurrentUser().getUid()).child("lastLogin").setValue((new Date().getTime()));
                            mUserCurrencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        Currency newUserCurrency = new Currency();
                                        newUserCurrency.setCoinCount(100);
                                        newUserCurrency.setJamCount(0);
                                        final String registerLog = "Registered, 100";
                                        newUserCurrency.setDepositLog(new ArrayList<String>() {{
                                            add(registerLog);
                                        }});
                                        mUserCurrencyRef.setValue(newUserCurrency);
                                    }

                                    mNotiRef = mFirebaseDb.getReference("noti_setting").child(mFirebaseUser.getUid());
                                    mNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.i("NotiService", "get InitDataSnapshot");
                                            if (!dataSnapshot.exists()) {
                                                ArrayList<Boolean> newBooleanArray = new ArrayList<>();
                                                newBooleanArray.add(true);
                                                newBooleanArray.add(true);
                                                newBooleanArray.add(true);
                                                newBooleanArray.add(true);
                                                mNotiRef.setValue(new NotificationSetting(mFirebaseUser.getUid(), newBooleanArray));
                                            }
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    } else {
                        startActivity(new Intent(StartActivity.this, IntroduceActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        } else {
            startActivity(new Intent(StartActivity.this, IntroduceActivity.class));
            finish();
        }
    }



    public static String getWhatKindOfNetwork(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return WIFE_STATE;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return MOBILE_STATE;
            }
        }
        return NONE_STATE;
    }


    //network state check
    private static class CheckConnect extends Thread{
        private boolean success;
        private String host;

        public CheckConnect(String host){
            this.host = host;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)new URL(host).openConnection();
                conn.setRequestProperty("User-Agent","Android");
                conn.setConnectTimeout(1000);
                conn.connect();
                int responseCode = conn.getResponseCode();
                Log.i("VerIntCheck",String.valueOf(responseCode));
                if(responseCode == 204) success = true;
                else success = false;
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.i("VerIntCheck", "catch error");
                success = false;
            }
            if(conn != null){
                conn.disconnect();
            }
        }

        public boolean isSuccess(){
            return success;
        }
    }

    public static boolean isOnline() {
        CheckConnect cc = new CheckConnect(CONNECTION_CONFIRM_CLIENT_URL);
        cc.start();
        try{
            cc.join();
            return cc.isSuccess();
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
package com.ludus.commontalks.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
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
import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.CoinChangeEvent;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PaymentActivity extends BaseActivity implements PurchasesUpdatedListener {

    private int VIEW_ADS_REQUEST_CODE = 1;

    @BindView(R.id.showAds)
    ImageView showAdsBtn;

    @BindView(R.id.haveCoins)
    TextView haveCoins;

    @BindView(R.id.purchase1)
    LinearLayout purchase1;

    @BindView(R.id.purchase2)
    LinearLayout purchase2;

    @BindView(R.id.purchase3)
    LinearLayout purchase3;

    @BindView(R.id.purchase4)
    LinearLayout purchase4;

    @BindView(R.id.purchase5)
    LinearLayout purchase5;

    @BindView(R.id.purchase6)
    LinearLayout purchase6;


    private DatabaseReference mCoinRef;
    private FirebaseUser mFirebaseUser;
    private BillingClient mBillingClient;
    private BillingFlowParams flowParams;
    private String itemName;
    private String showItemName;
    private int responseCode;
    private ArrayList<String> mSkuList;
    private String mItemPrice;
    private Thread mThread;
    private HashMap<String, Integer> mItemInfo;
    private Context mContext;

    private static int SHOW_AD_REWARD_SOOM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Bundle bundle = getIntent().getExtras();
        ButterKnife.bind(this);
        BusProvider.getInstance().register(this);
        haveCoins.setText(bundle.getString("coincount"));
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCoinRef = FirebaseDatabase.getInstance().getReference("currency").child(mFirebaseUser.getUid());
        setClickListener();
        mContext = this;
        //setBillingAPI
        setBillingAPI();
        setItemInfo();
    }

    private void setItemInfo(){
        mItemInfo = new HashMap<>();
        mItemInfo.put("soom01", 17);
        mItemInfo.put("soom02", 35);
        mItemInfo.put("soom03", 53);
        mItemInfo.put("soom11", 187);
        mItemInfo.put("soom12", 408);
        mItemInfo.put("soom13", 637);
    }

    private void setBillingAPI() {
        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    Log.i("PURCHASELOG", "Billing Response OK");

                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.i("PURCHASELOG", "Billing Response Disconnected");
            }
        });

    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingResponse.OK
                && purchases != null) {
            //1. 데이터베이스에 구매한 아이템을 추가해주고,
            //2. 구매한 아이템을 소모한 것으로 보고 한번더 업데이트 쳐준다.
            for (Purchase purchase : purchases) {
                Toast.makeText(this, "in purchases.", Toast.LENGTH_SHORT).show();
                comsumePurchase(purchase.getPurchaseToken());
            }
        } else if (responseCode == BillingResponse.USER_CANCELED) {
            Toast.makeText(this, "취소되었습니다.", Toast.LENGTH_SHORT).show();
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            Toast.makeText(this, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            // Handle any other error codes.
        }
    }

    private void comsumePurchase(final String purchaseToken) {
        final ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@BillingResponse int responseCode, String outToken) {
                if (responseCode == BillingResponse.OK) {
                    // Handle the success of the consume operation.
                    // For example, increase the number of coins inside the user's basket.
                    addCoins(PurchaseType.PURCHASE, itemName);
                    Toast.makeText(PaymentActivity.this, "구매 완료!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        Runnable consumeRequest = new Runnable() {
            @Override
            public void run() {
                mBillingClient.consumeAsync(purchaseToken, listener);
            }
        };
        consumeRequest.run();
    }

    @OnClick(R.id.showAds)
    public void onClickShowAds() {
        startActivityForResult(new Intent(this, ViewAdsActivity.class), VIEW_ADS_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == VIEW_ADS_REQUEST_CODE) {

            switch (data.getExtras().get("rewardResult").toString()) {
                case "1":
                    addCoins(PurchaseType.SHOWAD, "ShowAd");
                    Toast.makeText(this, "3숨이 지급되었습니다.", Toast.LENGTH_SHORT).show();

                case "0":
            }
        }
    }

    private void addCoins(final PurchaseType purchaseType, String ItemName) {
        mCoinRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (purchaseType == PurchaseType.SHOWAD) {
                        long coin = dataSnapshot.child("coinCount").getValue(long.class);
                        coin += SHOW_AD_REWARD_SOOM;
                        dataSnapshot.getRef().child("coinCount").setValue(coin).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                                    ArrayList<String> depositLog = dataSnapshot.child("depositLog").getValue(t);
                                    depositLog.add("see ads, 5");
                                    dataSnapshot.child("depositLog").getRef().setValue(depositLog);
                                }
                            }
                        });
                    } else if (purchaseType == PurchaseType.PURCHASE) {
                        long coin = dataSnapshot.child("coinCount").getValue(long.class);
                        coin += mItemInfo.get(itemName);
                        dataSnapshot.getRef().child("coinCount").setValue(coin).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                                    ArrayList<String> depositLog = dataSnapshot.child("depositLog").getValue(t);
                                    depositLog.add("purchase item, " + mItemInfo.get(itemName));
                                    dataSnapshot.child("depositLog").getRef().setValue(depositLog);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Subscribe
    public void changeCoinCount(CoinChangeEvent coinChangeEvent) {
        if (coinChangeEvent != null) {
            haveCoins.setText(String.valueOf(coinChangeEvent.getCoinCount()));
        }
    }


    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }


    public void setClickListener(){
        View.OnClickListener purchaseClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                switch (v.getId()) {
                    case R.id.purchase1:
                        itemName = "soom01";
                        showItemName = "17숨";
                        break;

                    case R.id.purchase2:
                        itemName = "soom02";
                        showItemName = "35숨";
                        break;

                    case R.id.purchase3:
                        itemName = "soom03";
                        showItemName = "53숨";
                        break;

                    case R.id.purchase4:
                        itemName = "soom11";
                        showItemName = "187숨";
                        break;

                    case R.id.purchase5:
                        itemName = "soom12";
                        showItemName = "408숨";
                        break;

                    case R.id.purchase6:
                        itemName = "soom13";
                        showItemName = "637숨";
                        break;

                    default:
                        itemName = "error";
                        showItemName = "error";
                        break;
                }
                if(itemName.equals("error")) {
                    return;
                }

                AlertDialog.Builder dialog = new AlertDialog.Builder(PaymentActivity.this);
                dialog.setMessage(showItemName + "을 구매하시겠습니까?")
                        .setTitle("숨 구매하기")
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i("PURCHASELOG", itemName);
                                mThread = new Thread() {
                                    @Override
                                    public void run() {
                                        flowParams = BillingFlowParams.newBuilder()
                                                .setSku(itemName)//skuID가 들어가야함
                                                .setType(BillingClient.SkuType.INAPP)
                                                .build();
                                        responseCode = mBillingClient.launchBillingFlow(PaymentActivity.this, flowParams);
                                    }
                                };
                                mThread.start();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        };

        purchase1.setOnClickListener(purchaseClickListener);
        purchase2.setOnClickListener(purchaseClickListener);
        purchase3.setOnClickListener(purchaseClickListener);
        purchase4.setOnClickListener(purchaseClickListener);
        purchase5.setOnClickListener(purchaseClickListener);
        purchase6.setOnClickListener(purchaseClickListener);
    }


    private enum PurchaseType{
        PURCHASE, SHOWAD
    }
}
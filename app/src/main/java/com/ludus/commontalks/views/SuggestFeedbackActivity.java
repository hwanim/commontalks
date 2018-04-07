package com.ludus.commontalks.views;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.R;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.MailBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SuggestFeedbackActivity extends BaseActivity {

    @BindView(R.id.suggestFeedback)
    TextView suggestFeedback;

    @BindView(R.id.suggestFeedbackEt1)
    EditText suggestFeedbackEt1;

    @BindView(R.id.suggestSendBtn)
    ImageView suggestSendBtn;

    @BindView(R.id.suggestFeedbackConstraintLayout)
    ConstraintLayout suggestFeedbackConstraintLayout;

    private static String DOMAIN = "sandbox38def3c3373341ccacfe75fa4c858959.mailgun.org";
    private static String API_KEY = "key-aa5f6c3a16e50f7cd808976e819b4812";

    private String mUid;
    private String mNickname;
    private Configuration mConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_feedback);

        ButterKnife.bind(this);
        if (!getIntent().getExtras().isEmpty()) {
            Log.i("SuggestIntent", getIntent().getExtras().toString());
            mUid = getIntent().getStringExtra("uid");
            mNickname = getIntent().getStringExtra("nickname");
        }

        Log.i("SuggestIntent", mUid);
        Log.i("SuggestIntent", mNickname);
    }


    @OnClick(R.id.suggestSendBtn)
    public void onClikcSuggestSendBtn(){
        if (suggestFeedbackEt1.getText().toString().isEmpty()) {
            Toast.makeText(this,"내용을 입력해주세요.",Toast.LENGTH_LONG).show();
            return;
        } else {
            suggestSendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });
        }

        mConfiguration = new Configuration()
                .domain(DOMAIN)
                .apiKey(API_KEY)
                .from("ludus2018@gmail.com");

        Thread thread = new Thread() {
            @Override
            public void run() {
                Mail.using(mConfiguration)
                        .to("ludus2018@gmail.com")
                        .subject(mNickname + "님의 개선의견 전달")
                        .text("UserId : " + mUid + "\n" +
                                "내용 : " + suggestFeedbackEt1.getText().toString())
                        .build()
                        .send();
                Snackbar.make(suggestFeedbackConstraintLayout, "의견이 접수되었습니다. 감사합니다.", Snackbar.LENGTH_LONG).show();
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    finish();
                }
            }
        };
        thread.start();
    }
}

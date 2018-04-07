package com.ludus.commontalks.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContractsActivity extends BaseActivity {

    @BindView(R.id.webView)
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contracts);
        ButterKnife.bind(this);
        Toast.makeText(this, "로딩 중입니다.", Toast.LENGTH_LONG).show();
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://commontalkswebhook.herokuapp.com/");
    }
}

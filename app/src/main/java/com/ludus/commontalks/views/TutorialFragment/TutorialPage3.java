package com.ludus.commontalks.views.TutorialFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ludus.commontalks.R;
import com.ludus.commontalks.views.TutorialActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by imhwan on 2017. 11. 20..
 */

public class TutorialPage3 extends Fragment {

    @BindView(R.id.signInPageTitle)
    TextView mSignInPageTitle;

    @BindView(R.id.signInPageDescription)
    TextView mSignInPageDescription;

    @BindView(R.id.birthAndSexLayout)
    LinearLayout mBirthAndSexLayout;

    @BindView(R.id.nickNameSetLayout)
    LinearLayout mNickNameSetLayout;

    @BindView(R.id.nicknameEt)
    public EditText nicknameEt;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


//        ConstraintLayout mConstraintLayout = (ConstraintLayout)inflater.inflate(R.layout.tutorial_page, container,false);
        View tutorialView = inflater.inflate(R.layout.tutorial_page, container, false);

        ButterKnife.bind(this, tutorialView);
        mSignInPageTitle.setText("닉네임 정하기");
        mSignInPageDescription.setText("앱 내에서 사용할 이름을 정해주세요.");


        mNickNameSetLayout.setVisibility(View.VISIBLE);
        mBirthAndSexLayout.setVisibility(View.GONE);

        nicknameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("TextWatcher", "beforeTextChanged ");
                Log.i("TextWatcher", "s : " + s.toString());
                Log.i("TextWatcher", "start : " + start);
                Log.i("TextWatcher", "count : " + count);
                Log.i("TextWatcher", "after : " +after);
                if (s.length() >= 1) {
                    ((TutorialActivity)getActivity()).nextBtn.setImageResource(R.drawable.ic_confirm_actv);
                    ((TutorialActivity)getActivity()).nextBtn.setTag(R.drawable.ic_confirm_actv);

                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("TextWatcher", "onTextChanged ");
                Log.i("TextWatcher", "start : " + start);
                Log.i("TextWatcher", "before : " + before);
                Log.i("TextWatcher", "count : " +count);

            }

            @Override
            public void afterTextChanged(Editable s) {
//
                Log.i("TextWatcher", "afterTextChanged ");
                Log.i("TextWatcher", "Editable : " + s.toString());


                if (s.toString().length() == 0) {
                    ((TutorialActivity)getActivity()).nextBtn.setImageResource(R.drawable.ic_confirm_grey);
                }

            }
        });


        return tutorialView;

    }

}

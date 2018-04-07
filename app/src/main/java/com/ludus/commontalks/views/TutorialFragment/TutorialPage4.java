package com.ludus.commontalks.views.TutorialFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
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

public class TutorialPage4 extends Fragment {

    @BindView(R.id.signInPageTitle)
    TextView mSignInPageTitle;

    @BindView(R.id.birthAndSexLayout)
    LinearLayout mBirthAndSexLayout;

    @BindView(R.id.signInPageDescription)
    TextView mSignInPageDescription;

    @BindView(R.id.welcomeRegisterLayout)
    LinearLayout mWelcomeRegisterLayout;

    @BindView(R.id.nicknameDisplay)
    EditText mNicknameDisplay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View tutorialView = inflater.inflate(R.layout.tutorial_page, container, false);

        ButterKnife.bind(this, tutorialView);
        mSignInPageTitle.setText("회원가입 완료!");
        mSignInPageDescription.setText("환영합니다.");



        mNicknameDisplay.setText(((TutorialActivity)getActivity()).mUser.getNickname());
        mWelcomeRegisterLayout.setVisibility(View.VISIBLE);
        mBirthAndSexLayout.setVisibility(View.GONE);

        ((TutorialActivity)getActivity()).nextBtn.setImageResource(R.drawable.ic_confirm_getstart);
        ((TutorialActivity)getActivity()).nextBtn.setTag(R.drawable.ic_confirm_getstart);

        return tutorialView;

    }

}

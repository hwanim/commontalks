package com.ludus.commontalks.views;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ludus.commontalks.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroduceFragment extends Fragment {

    public IntroduceFragment(){
    }

    @BindView(R.id.introducePageImage)
    ImageView introducePageImage;

    private int mPageNumber;

    public static IntroduceFragment create(int pageNumber) {
        IntroduceFragment fragment = new IntroduceFragment();
        Bundle args = new Bundle();
        //데이터를 넣어 줌
        args.putInt("page", pageNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt("page");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FrameLayout layout = (FrameLayout)inflater.inflate(R.layout.fragment_introduce,container, false);
        ButterKnife.bind(this, layout);

        switch (mPageNumber){
            case 0:
                introducePageImage.setImageResource(R.drawable.intro_src_0);
                break;
            case 1:
                introducePageImage.setImageResource(R.drawable.intro_src_1);
                break;
            case 2:
                introducePageImage.setImageResource(R.drawable.intro_src_2);
                break;
            case 3:
                introducePageImage.setImageResource(R.drawable.intro_src_3);
                break;
            case 4:
                introducePageImage.setImageResource(R.drawable.intro_src_4);
                break;
        }


        return layout;
    }
}

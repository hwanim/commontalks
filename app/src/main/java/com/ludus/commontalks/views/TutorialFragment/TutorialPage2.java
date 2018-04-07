package com.ludus.commontalks.views.TutorialFragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ludus.commontalks.R;
import com.ludus.commontalks.views.TutorialActivity;
import com.ludus.commontalks.views.WritePostActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by imhwan on 2017. 11. 20..
 */

public class TutorialPage2 extends Fragment {


    @BindView(R.id.signInPageTitle)
    TextView mSignInPageTitle;

    @BindView(R.id.signInPageDescription)
    TextView mSignInPageDescription;

    @BindView(R.id.birthAndSexLayout)
    LinearLayout mBirthAndSexLayout;

    @BindView(R.id.profileInitSetLayout)
    FrameLayout mProfileInitSetLayout;

    @BindView(R.id.profilePreview)
    public ImageView profilePreview;


    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;

    private static int WRITE_POST_INTENT = 100;

    private String mCurrentPhotoPath;

    private Uri photoUri;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private static final int MULTIPLE_PERMISSIONS = 101;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View tutorialView = inflater.inflate(R.layout.tutorial_page, container, false);

        ButterKnife.bind(this, tutorialView);
        mSignInPageTitle.setText("프로필 사진 등록하기");
        mSignInPageDescription.setText("사진을 이용해서 자신을 표현해주세요. \n 어떤 사진이든 좋습니다.");

        mProfileInitSetLayout.setVisibility(View.VISIBLE);
        mBirthAndSexLayout.setVisibility(View.GONE);

        return tutorialView;


    }


    @OnClick(R.id.profileInitSetLayout)
    public void onClickAddPhoto() {
        ((TutorialActivity)getActivity()).profilePhotoAdd();
    }




}

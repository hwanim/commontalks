package com.ludus.commontalks.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ludus.commontalks.Base.BaseActivity;
import com.ludus.commontalks.R;
import com.ludus.commontalks.models.Currency;
import com.ludus.commontalks.models.NotificationSetting;
import com.ludus.commontalks.models.TutorialCheck;
import com.ludus.commontalks.models.User;
import com.ludus.commontalks.views.TutorialFragment.TutorialPage1;
import com.ludus.commontalks.views.TutorialFragment.TutorialPage2;
import com.ludus.commontalks.views.TutorialFragment.TutorialPage3;
import com.ludus.commontalks.views.TutorialFragment.TutorialPage4;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TutorialActivity extends BaseActivity {


    public User mUser;

    private int TUTORIAL_STATUS_COUNT = 1;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;

    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mUserRef;
    private StorageReference mStorageRef;
    private DatabaseReference mUserCurrencyRef;
    private DatabaseReference mNotiSettingRef;

    private static int WRITE_POST_INTENT = 100;

    private Uri photoUri;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private static final int MULTIPLE_PERMISSIONS = 101;

    private String mCurrentPhotoPath;

    private TutorialPage1 tutorialPage1;
    private TutorialPage2 tutorialPage2;
    private TutorialPage3 tutorialPage3;
    private TutorialPage4 tutorialPage4;


    @BindView(R.id.tutorialFragmentContainer)
    FrameLayout tutorialFragmentContainer;

    @BindView(R.id.nextBtn)
    public ImageView nextBtn;

    private FragmentTransaction transaction;

    private Uri mTemporaryUri;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        mFirebaseDb = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = mFirebaseDb.getReference("users").child(mFirebaseUser.getUid());
        mUserCurrencyRef = mFirebaseDb.getReference("currency").child(mFirebaseUser.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference("userProfiles").child(mFirebaseUser.getUid());
        mNotiSettingRef = mFirebaseDb.getReference("noti_setting").child(mFirebaseUser.getUid());


        ButterKnife.bind(this);
        mUser = new User();
        mUser.setUid(mFirebaseUser.getUid());
        mUser.setEmail(mFirebaseUser.getEmail());
        mUser.setUsername(mFirebaseUser.getDisplayName());
        setFragment();

        nextBtn.setTag(R.drawable.ic_confirm_grey);
        checkPermissions();
    }


    private void setFragment() {
        tutorialPage1 = new TutorialPage1();
        tutorialPage2 = new TutorialPage2();
        tutorialPage3 = new TutorialPage3();
        tutorialPage4 = new TutorialPage4();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.tutorialFragmentContainer, tutorialPage1, "Page1");
        transaction.commit();
    }

    private int getDrawableId(ImageView iv) {
        return (Integer) iv.getTag();
    }


    @OnClick(R.id.nextBtn)
    public void onClickNextBtn(){
        if (getDrawableId(nextBtn) == R.drawable.ic_confirm_grey){
            Log.i("tutorial", "grey button");
            return;
        }
        switch (TUTORIAL_STATUS_COUNT) {
            case (1):
                mUser.setBirth(tutorialPage1.sb.substring(0,6));
                mUser.setSex(Integer.parseInt(tutorialPage1.et7.getText().toString()));
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.tutorialFragmentContainer, tutorialPage2, "page2");
                transaction.commit();
                nextBtn.setImageResource(R.drawable.ic_confirm_grey);
                nextBtn.setTag(R.drawable.ic_confirm_grey);
                TUTORIAL_STATUS_COUNT++;
                break;
            case (2):
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.tutorialFragmentContainer, tutorialPage3, "page3");
                transaction.commit();
                nextBtn.setImageResource(R.drawable.ic_confirm_grey);
                nextBtn.setTag(R.drawable.ic_confirm_grey);
                TUTORIAL_STATUS_COUNT++;
                break;
            case (3):
                mUser.setNickname(tutorialPage3.nicknameEt.getText().toString());
                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.tutorialFragmentContainer, tutorialPage4, "page4");
                transaction.commit();
                nextBtn.setImageResource(R.drawable.ic_confirm_grey);
                nextBtn.setTag(R.drawable.ic_confirm_grey);
                TUTORIAL_STATUS_COUNT++;
                break;
            case (4):
                //유저 등록 로직 수행(시작하기 버튼)
                //1. 닉네임 설정
                //2. 프로필 사진 설정
                //3. 유저 첫 코인 지급
                //4. 메인 액티비티 시작
                Toast.makeText(this,"로딩중입니다.",Toast.LENGTH_LONG).show();
                registerUser();
        }
    }


    private void registerUser() {
        if (!photoUri.toString().isEmpty()) {
            mStorageRef.putFile(photoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    String profileUrl = task.getResult().getDownloadUrl().toString();
                    mUser.setProfileUrl(profileUrl);

                    mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mUserRef.setValue(mUser, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {

                                        //첫 노티 세팅 등록과정
                                        ArrayList<Boolean> notiList = new ArrayList<>();
                                        for (int i = 0; i < 4; i++) {
                                            notiList.add(true);
                                        }
                                        mNotiSettingRef.setValue(new NotificationSetting(mUser.getUid(), notiList));

                                        //첫 재화 등록 과정
                                        Currency newUserCurrency = new Currency();
                                        newUserCurrency.setCoinCount(100);
                                        newUserCurrency.setJamCount(0);
                                        final String registerLog = "Registered, 100";
                                        newUserCurrency.setDepositLog(new ArrayList<String>() {{add(registerLog);}});

                                        //튜토리얼 안한걸로 등록
                                        TutorialCheck tutorialCheck = new TutorialCheck();
                                        tutorialCheck.setUid(mUser.getUid());
                                        mFirebaseDb.getReference("tutorial_check").child(mUser.getUid()).setValue(tutorialCheck);


                                        mUserCurrencyRef.setValue(newUserCurrency, new DatabaseReference.CompletionListener(){
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                startActivity(new Intent(TutorialActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        });

                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
        }
    }

    public void profilePhotoAdd() {
        checkPermissions();
        new AlertDialog.Builder(this).setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진 촬영", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        takePhoto();

                    }
                })
                .setNegativeButton("앨범 선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAlbum();
                    }
                }).show();
    }

    //permissions Array에 있는 permission 리스트를 가져오고,
    //해당 퍼미션이 허용되지 않았으면 request permission한다.
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.i("POSTWRITE", "not granted permission add.");
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            Log.i("POSTWRITE", "permission request." + permissionList.toString());
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;

    }


    private void takePhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
            }
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        "com.ludus.commontalks.provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
            mTemporaryUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mTemporaryUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }



    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "cm_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/commontalks/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    Log.i("POSTWRITE", "grantResults : " + grantResults.length);
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            Log.i("POSTWRITE", "READ_EXTERNAL_STORAGE equal to request Permission : " + grantResults[i]);
                            Log.i("POSTWRITE", "Pemissions : " + permissions[i]);
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            Log.i("POSTWRITE", "WRITE_EXTERNAL_STORAGE equal to request Permission : " + grantResults[i]);
                            Log.i("POSTWRITE", "Pemissions : " + permissions[i]);
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            Log.i("POSTWRITE", "CAMERA equal to request Permission" + grantResults[i]);
                            Log.i("POSTWRITE", "Pemissions : " + permissions[i]);
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }


    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) {
                return;
            }
            photoUri = data.getData();
            cropImage();
        } else if (requestCode == PICK_FROM_CAMERA) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                photoUri = mTemporaryUri;
            }
            if (photoUri != null) {
                cropImage();
            } else {
                Toast.makeText(this, "이미지 처리 오류!", Toast.LENGTH_SHORT).show();
                return;
            }
            // 갤러리에 나타나게
            MediaScannerConnection.scanFile(this,
                    new String[]{photoUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        } else if (requestCode == CROP_FROM_CAMERA) {
            tutorialPage2.profilePreview.setImageURI(photoUri);
            tutorialPage2.profilePreview.setBackground(new ShapeDrawable(new OvalShape()));
            tutorialPage2.profilePreview.setClipToOutline(true);
            nextBtn.setImageResource(R.drawable.ic_confirm_actv);
            nextBtn.setTag(R.drawable.ic_confirm_actv);
        }
    }


    //Android N crop image
    public void cropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.grantUriPermission("com.android.camera", photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(photoUri, "image/*");

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            int size = list.size();
            if (size == 0) {
                Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                File croppedFileName = null;
                try {
                    croppedFileName = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File folder = new File(Environment.getExternalStorageDirectory() + "/commontalks/");
                File tempFile = new File(folder.toString(), croppedFileName.getName());

                photoUri = FileProvider.getUriForFile(this,
                        "com.ludus.commontalks.provider", tempFile);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                intent.putExtra("return-data", false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    grantUriPermission(res.activityInfo.packageName, photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);
            }

        } else {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(photoUri, "image/*");
            intent.putExtra("crop", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", false);
            String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
            photoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, CROP_FROM_CAMERA);
        }
    }
}

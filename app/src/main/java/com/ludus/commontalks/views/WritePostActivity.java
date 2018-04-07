package com.ludus.commontalks.views;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ExifInterface;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.formats.NativeAd;
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
import com.ludus.commontalks.CustomViews.CustomDialog;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.UserDataChangeEvent;
import com.ludus.commontalks.models.Post;
import com.ludus.commontalks.models.User;
import com.squareup.otto.Subscribe;

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class WritePostActivity extends BaseActivity {

    @BindView(R.id.postTxt)
    EditText mPostTxt;

    @BindView(R.id.postPreviewImageLayout)
    FrameLayout mAddPhoto;

    @BindView(R.id.postPreviewImage)
    ImageView mPostPreviewImage;

    @BindView(R.id.posting)
    ImageView mPosting;


    @BindView(R.id.plusCrossImage)
    ImageView plusCrossImage;

    @BindView(R.id.addYourPhotoTxt)
    TextView addYourPhotoTxt;

    @BindView(R.id.settingActivityBtn)
    ImageView settingActivityBtn;


    @BindView(R.id.userProfilePhoto)
    ImageView userProfilePhoto;

    @BindView(R.id.userNickname)
    TextView userNickname;

    @BindView(R.id.postProfileInfoLayout)
    LinearLayout postProfileInfoLayout;

    @BindView(R.id.notificationSwitch)
    Switch notificationSwitch;

    @BindView(R.id.chatLimitSeekbarLayout)
    LinearLayout chatLimitSeekbarLayout;

    @BindView(R.id.chatLimitSeekbar)
    SeekBar chatLimitSeekbar;

    @BindView(R.id.chatLimitShow)
    TextView chatLimitShow;

    @BindView(R.id.writePostRatingInfo)
    TextView writePostRatingInfo;

    @BindView(R.id.describeChatLimit)
    TextView describeChatLimit;

    @BindView(R.id.writePostRatingLikeImage)
    ImageView writePostRatingLikeImage;

    private DatabaseReference mFirebaseRef;

    private FirebaseUser mFirebaseUser;

    private StorageReference mStorageRef;

    private String mPostId;

    private String mPostPhotoUrl;

    private Uri mImgUri = null;

    private Uri mImageUri;

    private User mUser;

    private CustomDialog mDialog;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;


    private static int WRITE_POST_INTENT = 100;

    private Uri photoUri;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private static final int MULTIPLE_PERMISSIONS = 101;

    private String mCurrentPhotoPath;

    private String mPreviousFragmentTag;

    private Uri mTemporaryUri;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        BusProvider.getInstance().register(this);
        ButterKnife.bind(this);

        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mPostId = mFirebaseRef.child("posts").push().getKey();
        mStorageRef = FirebaseStorage.getInstance().getReference("posts").child(mPostId);

        mUser = new User();
        checkPermissions();

        Bundle bundle = getIntent().getExtras();
        Log.i("BundleData", "bundle : " + bundle.get("rating").toString() );
        if (bundle.containsKey("previousFragment")){
            Log.i("FEEDCARD", "get PreviousFrag in Activity : " + bundle.getString("previousFragment") );
            mPreviousFragmentTag = bundle.getString("previousFragment");
        }
        mUser.setProfileUrl(bundle.getString("profilePhoto"));
        mUser.setNickname(bundle.getString("nickname"));
        HashMap<String, Integer> hashMap = (HashMap<String, Integer>)getIntent().getSerializableExtra("rating");
        mUser.setRatings(hashMap);
        setRatingTextView(hashMap);

        Glide.with(postProfileInfoLayout)
                .load(mUser.getProfileUrl())
                .into(userProfilePhoto);
        userProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
        userProfilePhoto.setClipToOutline(true);

        userNickname.setText(mUser.getNickname());


        setChatLimitFunction();
        checkPermissions();
    }


    private void setChatLimitFunction() {

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    chatLimitSeekbarLayout.setVisibility(View.VISIBLE);
                } else {
                    chatLimitSeekbarLayout.setVisibility(View.GONE);

                }
            }
        });

        chatLimitSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                chatLimitShow.setText(String.valueOf(progress)+"숨");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Subscribe
    public void changeUserDataEvent(UserDataChangeEvent userDataChangeEvent) {
        if (userDataChangeEvent != null) {
            mUser = userDataChangeEvent.getMUser();
        }
        Log.i("BundleData", "subscribe");
        Glide.with(postProfileInfoLayout)
                .load(mUser.getProfileUrl())
                .into(userProfilePhoto);
        userProfilePhoto.setBackground(new ShapeDrawable(new OvalShape()));
        userProfilePhoto.setClipToOutline(true);

        userNickname.setText(mUser.getNickname());
        setRatingTextView(mUser.getRatings());

    }

    private void setRatingTextView(HashMap<String, Integer> hashMap) {
        Integer caring = hashMap.get("caring");
        if (caring > 0 ) {
            writePostRatingInfo.setText(String.valueOf(caring));
            writePostRatingLikeImage.setVisibility(View.VISIBLE);
        } else {
            writePostRatingInfo.setText("칭찬 정보가 없어요.");
        }
    }

    @OnClick(R.id.postPreviewImageLayout)
    public void onClickAddPhoto() {


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
                })
                .setNeutralButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }




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
        String imageFileName = "cmtalks_" + timeStamp + "_";
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
        onBackPressed();
    }


    @OnClick(R.id.posting)
    public void onClickPostingBtn() {
        writePost();
    }

    private void writePost() {
        if (mPostTxt.getText().toString().length() == 0) {
            Toast.makeText(this, "지금 내용을 작성해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //
        mPosting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Toast.makeText(this, "작성중입니다.", Toast.LENGTH_SHORT).show();
        mFirebaseRef.child("users").child(mFirebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                //users table 이외의 유저 정보에는 로그인 정보를 빼줌
                final Post post = new Post();
                post.setHashTags(extractHashtags(mPostTxt.getText().toString()));
                post.setPostTxt(mPostTxt.getText().toString());
//                post.setChatLimitValue(mSeekbar.getProgress());
                post.setPostId(mPostId);
                post.setPostDate((new Date().getTime()));
                post.setUser(user);
                if (notificationSwitch.isChecked()){
                    post.setChatLimitValue(Integer.parseInt(chatLimitShow.getText().toString()));
                }

                if (mStorageRef == null) {
                    mStorageRef = FirebaseStorage.getInstance().getReference("posts").child(mPostId);
                }
                if (photoUri != null ){
                    mStorageRef.putFile(photoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                post.setWhichPost(Post.postType.PHOTO);

                                mPostPhotoUrl = task.getResult().getDownloadUrl().toString();
                                post.setPhotoUrl(mPostPhotoUrl);
                                completeWritePost(post);
                            }
                        }
                    });
                } else {
                    post.setWhichPost(Post.postType.TXT);
                    post.setPhotoUrl(null);
                    completeWritePost(post);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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
            plusCrossImage.setVisibility(View.GONE);
            addYourPhotoTxt.setVisibility(View.GONE);
            mPostPreviewImage.setImageURI(photoUri);
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
                return;
            } else {
                Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 38);
                intent.putExtra("aspectY", 23);
                intent.putExtra("scale", true);
                File croppedFileName = null;
                try {
                    croppedFileName = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File folder = new File(Environment.getExternalStorageDirectory() + "/commontalks/");
                File tempFile = new File(folder.toString(), croppedFileName.getName());

                photoUri = FileProvider.getUriForFile(WritePostActivity.this,
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
        intent.putExtra("aspectX", 38);
        intent.putExtra("aspectY", 23);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        photoUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, CROP_FROM_CAMERA);
        }
    }

    private ArrayList<String> extractHashtags(String item) {
        ArrayList<String> hashTags = new ArrayList<>();
        Pattern p = Pattern.compile("\\#([0-9a-zA-Z가-힣]*)");
        Matcher m = p.matcher(item);
        while (m.find()) {
            hashTags.add(m.group());
        }
        return hashTags;
    }

    private void completeWritePost(Post post) {
        mFirebaseRef.child("posts").child(mPostId).setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(WritePostActivity.this,"글쓰기가 완료되었습니다", Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }
        });

    }

    @OnClick(R.id.describeChatLimit)
    public void onClikcDescribeChatLimitBtn(){


        View.OnClickListener rightListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        };

        mDialog =  new CustomDialog(this,rightListener, "chatLimitDescribe" );
        mDialog.show();

    }

    @Override
    public void onBackPressed() {
        Log.i("FEEDCARD", "sending Intent to MainActivity with intent : " + mPreviousFragmentTag );
        Intent intent = new Intent();
        intent.putExtra("fragment", mPreviousFragmentTag);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }
}

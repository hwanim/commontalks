package com.ludus.commontalks.views.MainFragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ludus.commontalks.CustomViews.FeedCard;
import com.ludus.commontalks.CustomViews.TutorialDialog;
import com.ludus.commontalks.R;
import com.ludus.commontalks.Services.BusProvider;
import com.ludus.commontalks.Services.PushEvent;
import com.ludus.commontalks.models.BlockedPost;
import com.ludus.commontalks.models.Post;
import com.ludus.commontalks.models.TutorialCheck;
import com.ludus.commontalks.views.MainActivity;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FeedFragment extends Fragment {

    private Context mContext;

    @BindView(R.id.swipeView)
    SwipePlaceHolderView mSwipeView;

    @BindView(R.id.noChatRoomLayout)
    ConstraintLayout noChatRoomLayout;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPostRef;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mBlockedPostRef;
    private DatabaseReference mTutorialRef;

    private int REPORT_COUNT_LIMIT = 5;
    private int mThreadCount = 0;

    private ArrayList<Post> mPostArray;
    private ArrayList<BlockedPost> mBlockedPost;
    private ArrayList<Post> mRecentPost;

    private String mLastLoadedPostId;
    private String mFirstLoadedPostId;

    private Boolean mLeftSwipe = true;
    private Boolean mRightSwipe = true;

    private int mCount = 0;
    private long mChildCount = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View feedView = inflater.from(getContext()).inflate(R.layout.fragment_feed, container, false);

        ButterKnife.bind(this, feedView);
        mContext = getContext();
        mPostArray = new ArrayList<>();
        BusProvider.getInstance().register(this);
        noChatRoomLayout.setVisibility(View.GONE);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mPostRef = mFirebaseDatabase.getReference("posts");
        mBlockedPostRef = mFirebaseDatabase.getReference("blocked_posts").child(mFirebaseUser.getUid());
        mBlockedPost = new ArrayList<>();
        mRecentPost = new ArrayList<>();

        addTalkedPostListener();
        ititSwipeViewSetup();

        return feedView;
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        removeTalkedPostListener();
        super.onDestroy();
    }


    @Subscribe
    public void loadPosts(PushEvent mPushEvent) {
        if (mPushEvent.isGetNewPosts()) {
            Log.d("FEEDCARD", "isGetNewPosts");
            //로딩 방법 못찾음 아직
//            AddViewThread thread = new AddViewThread();
//            thread.start();
        } else if (mPushEvent.isPostStackEnd()) {
            Log.d("FEEDCARD", "isPostStackEnd");
            noChatRoomLayout.setVisibility(View.VISIBLE);
        } else if (mPushEvent.isLoadWhenNoPostExists()) {
            if (noChatRoomLayout.getVisibility() == View.VISIBLE) {
            }
        }
    }

    private void ititSwipeViewSetup() {
        //setup swipView and load initial posts
        mSwipeView.getBuilder()
                .setDisplayViewCount(3)
                .setSwipeDecor(new SwipeDecor()
                        .setSwipeInMsgLayoutId(R.layout.swipe_in_msg_view)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_out_msg_view));
        mTutorialRef = mFirebaseDatabase.getReference("tutorial_check").child(mFirebaseUser.getUid());
        mTutorialRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    TutorialCheck tutorialCheck = dataSnapshot.getValue(TutorialCheck.class);
                    mLeftSwipe = tutorialCheck.isLeftSwipeTuto();
                    mRightSwipe = tutorialCheck.isRightSwipeTuto();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mChildCount = dataSnapshot.getChildrenCount();
                    if (mChildCount != 0) {
                        Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            Post post = iterator.next().getValue(Post.class);
                            checkPostAndGetPosts(post, mPostArray);
                            mCount++;
                        }
                        Collections.reverse(mPostArray);
                        for (Post addPost : mPostArray) {
                            mSwipeView.addView(new FeedCard(mContext, addPost,mLeftSwipe,mRightSwipe, mSwipeView));
                        }
                    } else {
                        noChatRoomLayout.setVisibility(View.VISIBLE);
                    }
                    ((MainActivity)getActivity()).progressOFF();
                } else {
                    noChatRoomLayout.setVisibility(View.VISIBLE);
                    ((MainActivity)getActivity()).progressOFF();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void checkPostAndGetPosts(Post post, ArrayList<Post> array) {
        boolean equals = false;
        boolean alreadySame = false;
        if (!mBlockedPost.isEmpty()) {
            for (BlockedPost blockPost: mBlockedPost) {
                switch (blockPost.getBlockType()){
                    case reported:
                        if (blockPost.getPostUserId().equals(post.getUser().getUid())){
                            equals = true;
                            alreadySame = true;
                        }
                        break;

                    case alreadyRead:
                        if (blockPost.getPostId().equals(post.getPostId())) {
                            equals = true;
                            alreadySame = true;
                        }
                        break;
                }
                if (alreadySame) {
                    break;
                }
            }
        }
        if (!equals && post.getReportCount() < REPORT_COUNT_LIMIT) {
            long loadingStandardTime = System.currentTimeMillis() - (1000*60*60*24);
            if (post.getPostDate() > loadingStandardTime) {
                if (!post.getUser().getUid().equals(mFirebaseUser.getUid())){
                    Log.d("FEEDCARD", "addView in firebase" + post.getPostId());
                    array.add(post);
                }
            }
        }

    }

    private void addTalkedPostListener() {
        Log.i("FEEDCARD","add talkedChildEventListener");
        mBlockedPostRef.addChildEventListener(talkedChildEventListener);
    }

    private void removeTalkedPostListener() {
        mBlockedPostRef.removeEventListener(talkedChildEventListener);
    }

    private ChildEventListener talkedChildEventListener = new ChildEventListener(){
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if(dataSnapshot.exists()) {
                Log.i("FEEDCARD", "blockedpost add");
                mBlockedPost.add(dataSnapshot.getValue(BlockedPost.class));
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
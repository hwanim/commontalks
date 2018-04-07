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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by imhwan on 2017. 11. 20..
 */

public class TutorialPage1 extends Fragment {

    @BindView(R.id.signInPageTitle)
    TextView mSignInPageTitle;

    @BindView(R.id.signInPageDescription)
    TextView mSignInPageDescription;

    @BindView(R.id.birthAndSexLayout)
    LinearLayout mBirthAndSexLayout;

    @BindView(R.id.et1)
    EditText et1;

    @BindView(R.id.et2)
    EditText et2;

    @BindView(R.id.et3)
    EditText et3;

    @BindView(R.id.et4)
    EditText et4;

    @BindView(R.id.et5)
    EditText et5;

    @BindView(R.id.et6)
    EditText et6;

    @BindView(R.id.et7)
    public EditText et7;

    public StringBuffer sb = new StringBuffer();

    private ArrayList<Boolean> birthInputCheck;

    private int count = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View tutorialView = inflater.inflate(R.layout.tutorial_page, container, false);
        ButterKnife.bind(this, tutorialView);

        birthInputCheck = new ArrayList<>();
        for (int count = 0; count < 7; count++) {
            birthInputCheck.add(false);
        }
        Log.i("TextWatCher", "birthInputCheck" + birthInputCheck.toString());

        addBtnsChangeListener();
        return tutorialView;
    }

    private void booleanCheck() {
        Log.i("TextWatCher", "booleanCheck");
        Boolean flag = true;
        for (Boolean booleanCheck: birthInputCheck){
            if (!booleanCheck){
                Log.i("TextWatCher", "boolean false");
                flag =  false;
            }
        }
        if (flag){
            Log.i("TextWatCher", "boolean true");
            sb.append(et1.getText());
            sb.append(et2.getText());
            sb.append(et3.getText());
            sb.append(et4.getText());
            sb.append(et5.getText());
            sb.append(et6.getText());
            ((TutorialActivity)getActivity()).nextBtn.setImageResource(R.drawable.ic_confirm_actv);
            ((TutorialActivity)getActivity()).nextBtn.setTag(R.drawable.ic_confirm_actv);
        }
    }


    private void addBtnsChangeListener(){
        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 1 && after ==0) {
                    closeNextBtn();
                } else if (count == 0 && after == 1) {
                    Log.i("TextWatCher", "birthCheck");
                    birthInputCheck.remove(0);
                    birthInputCheck.add(0, true);

                    booleanCheck();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    et2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 1 && after ==0) {
                    closeNextBtn();
                } else if (count == 0 && after == 1) {
                    Log.i("TextWatCher", "birthCheck");
                    birthInputCheck.remove(1);
                    birthInputCheck.add(1, true);
                    booleanCheck();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    et3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 1 && after ==0) {
                    closeNextBtn();
                } else if (count == 0 && after == 1) {
                    Log.i("TextWatCher", "birthCheck");
                    birthInputCheck.remove(2);
                    birthInputCheck.add(2, true);
                    booleanCheck();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    et4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 1 && after ==0) {
                    closeNextBtn();
                } else if (count == 0 && after == 1) {
                    Log.i("TextWatCher", "birthCheck");
                    birthInputCheck.remove(3);
                    birthInputCheck.add(3, true);
                    booleanCheck();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    et5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 1 && after ==0) {
                    closeNextBtn();
                } else if (count == 0 && after == 1) {
                    Log.i("TextWatCher", "birthCheck");
                    birthInputCheck.remove(4);
                    birthInputCheck.add(4, true);
                    booleanCheck();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    et6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (count == 1 && after ==0) {
                    closeNextBtn();
                } else if (count == 0 && after == 1) {
                    Log.i("TextWatCher", "birthCheck");
                    birthInputCheck.remove(5);
                    birthInputCheck.add(5, true);
                    booleanCheck();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    et7.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et7.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (count == 1 && after ==0) {
                    closeNextBtn();
                } else if (count == 0 && after == 1) {
                    Log.i("TextWatCher", "birthCheck");
                    birthInputCheck.remove(6);
                    birthInputCheck.add(6, true);
                    booleanCheck();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void closeNextBtn(){
        ((TutorialActivity)getActivity()).nextBtn.setImageResource(R.drawable.ic_confirm_grey);
        ((TutorialActivity)getActivity()).nextBtn.setTag(R.drawable.ic_confirm_grey);
    }

}

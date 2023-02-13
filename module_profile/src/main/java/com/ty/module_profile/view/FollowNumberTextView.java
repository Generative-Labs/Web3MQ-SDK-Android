package com.ty.module_profile.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ty.module_profile.R;

public class FollowNumberTextView extends ConstraintLayout {
    private TextView tv_followers_number,tv_following_number;
    public FollowNumberTextView(@NonNull Context context) {
        this(context,null);
    }

    public FollowNumberTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FollowNumberTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.view_follow_numbers,this);
        tv_followers_number = rootView.findViewById(R.id.tv_followers_number);
        tv_following_number = rootView.findViewById(R.id.tv_following_number);
    }

    public void setNumbers(int followers_number, int following_number){
        tv_followers_number.setText(followers_number+"");
        tv_following_number.setText(following_number+"");
    }
}

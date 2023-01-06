package com.ty.web3mq.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ty.web3mq.R;

public class InputPwdView extends ConstraintLayout {
    private EditText et_pwd;
    private EmptyWatcher emptyWatcher;
    private ImageView iv_show_hide_pwd;
    private boolean isOpenEye;
    public InputPwdView(@NonNull Context context) {
        this(context,null);
    }

    public InputPwdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public InputPwdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.view_input_pwd,this);
        et_pwd = rootView.findViewById(R.id.et_pwd);
        iv_show_hide_pwd = rootView.findViewById(R.id.iv_show_hide_pwd);
        et_pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(emptyWatcher!=null){
                    emptyWatcher.onEmptyChange(TextUtils.isEmpty(et_pwd.getText().toString()));
                }
            }
        });

        iv_show_hide_pwd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isOpenEye) {
                    isOpenEye = true;
                    iv_show_hide_pwd.setImageResource(R.mipmap.ic_pwd_visible);
                    //密码可见
                    et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    et_pwd.setSelection(et_pwd.getText().toString().length());
                }else{
                    isOpenEye = false;
                    iv_show_hide_pwd.setImageResource(R.mipmap.ic_pwd_invisible);
                    //密码不可见
                    et_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    et_pwd.setSelection(et_pwd.getText().toString().length());
                }
            }
        });

    }

    public void initHint(int id){
        et_pwd.setHint(id);
    }

    public void setEmptyWatcher(EmptyWatcher emptyWatcher){
        this.emptyWatcher = emptyWatcher;
    }

    public interface EmptyWatcher{
        void onEmptyChange(boolean empty);
    }

    public String getPwd(){
        return et_pwd.getText().toString();
    }
}

package com.ty.module_chat.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.common.activity.BaseActivity;
import com.ty.common.config.Constants;
import com.ty.module_chat.R;
import com.ty.module_chat.adapter.RoomMembersAdapter;
import com.ty.module_chat.fragment.InviteGroupFragment;
import com.ty.web3_mq.Web3MQGroup;
import com.ty.web3_mq.http.beans.GroupMembersBean;
import com.ty.web3_mq.interfaces.GetGroupMembersCallback;

public class RoomSettingsActivity extends BaseActivity {
    private ImageView iv_back;
    private RecyclerView list_members;
    private ConstraintLayout cl_group_avatar,cl_room_name;
    private String group_id;
    private RoomMembersAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_room_settings);
        group_id = getIntent().getStringExtra(Constants.INTENT_GROUP_ID);
        if(group_id!=null){
            initView();
            setListener();
            requestData();
        }else{
            Toast.makeText(RoomSettingsActivity.this,"no group id",Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        cl_group_avatar = findViewById(R.id.cl_group_avatar);
        cl_room_name = findViewById(R.id.cl_room_name);
        list_members = findViewById(R.id.list_members);
        list_members.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
    }

    private void setListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cl_group_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO change avatar
            }
        });
        cl_room_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO change name
            }
        });
    }

    private void requestData() {
        Web3MQGroup.getInstance().getGroupMembers(1, 500, group_id, new GetGroupMembersCallback() {
            @Override
            public void onSuccess(GroupMembersBean groupMembersBean) {
                if(groupMembersBean.result.size()>0){
                    adapter = new RoomMembersAdapter(groupMembersBean.result,RoomSettingsActivity.this);
                    adapter.setOnAddPeopleClickListener(new RoomMembersAdapter.OnAddPeopleClickListener() {
                        @Override
                        public void onAddPeopleClick() {
                            InviteGroupFragment.getInstance(group_id).show(getSupportFragmentManager(),"invite people");
                        }
                    });
                    list_members.setAdapter(adapter);
                }
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(RoomSettingsActivity.this,"get group members error: "+error,Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.ty.module_chat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.ty.common.activity.BaseActivity;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;
import com.ty.common.utils.CommonUtils;
import com.ty.common.view.Web3MQListView;
import com.ty.module_chat.R;
import com.ty.module_chat.adapter.MessagesAdapter;
import com.ty.module_chat.bean.MessageItem;
import com.ty.module_chat.fragment.InviteGroupFragment;
import com.ty.web3_mq.Web3MQChats;
import com.ty.web3_mq.Web3MQGroup;
import com.ty.web3_mq.Web3MQMessageManager;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.GroupMembersBean;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.http.beans.MessagesBean;
import com.ty.web3_mq.interfaces.GetGroupMembersCallback;
import com.ty.web3_mq.interfaces.GetMessageHistoryCallback;
import com.ty.web3_mq.interfaces.MessageCallback;
import com.ty.web3_mq.interfaces.UpdateMyChatCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;

import java.util.ArrayList;

@Route(path = RouterPath.CHAT_MESSAGE)
public class MessageActivity extends BaseActivity {
    private static final int PAGE_SIZE = 100;
    @Autowired
    String chat_id;
    @Autowired
    String chat_type;
    private ArrayList<MessageItem> messageList = new ArrayList<>();
    private MessagesAdapter adapter;
    private Web3MQListView list_message;
    private TextView tv_title_user_id;
    private ImageButton btn_send,btn_add_member,btn_more;
    private EditText et_message;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_message);
        initView();
        setListener();
        observeMessageReceive();
        requestData(1, PAGE_SIZE);
    }

    private void requestData(int page, int size) {
        Web3MQMessageManager.getInstance().getMessageHistory(page, size, chat_id, new GetMessageHistoryCallback() {
            @Override
            public void onSuccess(MessagesBean messagesBean) {
                messageList.clear();
                DefaultSPHelper.getInstance().saveMessage(chat_id,messagesBean);
                if(messagesBean.total>0){
                    list_message.hideEmptyView();
                    for(MessageBean msg: messagesBean.result){
                        MessageItem messageItem = new MessageItem();
                        messageItem.from = msg.from;
                        messageItem.content = new String(Base64.decode(msg.payload,Base64.DEFAULT));
                        messageItem.timestamp = msg.timestamp;
                        messageItem.isMine = DefaultSPHelper.getInstance().getUserID().equals(msg.from);
                        messageList.add(0,messageItem);
                    }
                    updateView();
                }else{
                    list_message.showEmptyView();
                    long timeStamp = System.currentTimeMillis();
                    Web3MQChats.getInstance().updateMyChat(timeStamp, chat_id, Constants.CHAT_TYPE_USER, new UpdateMyChatCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MessageActivity.this,"update success",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFail(String error) {
                            Toast.makeText(MessageActivity.this,"update error : "+error,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
//                list_message.setRefreshing(false);
            }

            @Override
            public void onFail(String error) {
//                list_message.setRefreshing(false);
            }
        });

//        if(chat_type.equals(Constants.CHAT_TYPE_GROUP)){
//            Web3MQGroup.getInstance().getGroupMembers(1,500,chat_id, new GetGroupMembersCallback() {
//                @Override
//                public void onSuccess(GroupMembersBean groupMembersBean) {
//                    if(groupMembersBean.total>0){
//
//                    }
//                }
//
//                @Override
//                public void onFail(String error) {
//
//                }
//            });
//        }
    }

    private void initView() {
        tv_title_user_id = findViewById(R.id.tv_title_user_id);
        tv_title_user_id.setText(chat_id);
        list_message = findViewById(R.id.list_message);
        btn_send = findViewById(R.id.btn_send);
        et_message = findViewById(R.id.et_message);
        btn_add_member = findViewById(R.id.btn_add_member);
        btn_more = findViewById(R.id.btn_more);
        switch (chat_type){
            case Constants.CHAT_TYPE_USER:
                btn_add_member.setVisibility(View.GONE);
                btn_more.setVisibility(View.GONE);
                break;
            case Constants.CHAT_TYPE_GROUP:
                btn_add_member.setVisibility(View.VISIBLE);
                btn_more.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setListener() {
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = et_message.getText().toString();
                if(TextUtils.isEmpty(message)){
                    return;
                }
                Web3MQMessageManager.getInstance().sendMessage(message,chat_id,true);
                MessageItem messageItem = new MessageItem();
                messageItem.from = Web3MQUser.getInstance().getMyUserId();
                messageItem.content = message;
                messageItem.timestamp = System.currentTimeMillis();
                messageItem.isMine = true;
                updateMessage(messageItem);
                CommonUtils.hideKeyboard(MessageActivity.this);
                et_message.setText("");
            }
        });
        btn_add_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InviteGroupFragment.getInstance(chat_id).show(getSupportFragmentManager(),"invite people");
            }
        });
        btn_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, RoomSettingsActivity.class);
                intent.putExtra(Constants.INTENT_GROUP_ID,chat_id);
                startActivity(intent);
            }
        });
    }

    private void observeMessageReceive() {
        switch (chat_type){
            case Constants.CHAT_TYPE_USER:
                Web3MQMessageManager.getInstance().addDMCallback(chat_id, new MessageCallback() {
                    @Override
                    public void onMessage(com.ty.web3_mq.websocket.bean.MessageBean message) {
                        MessageItem messageItem = new MessageItem();
                        messageItem.from = message.from;
                        messageItem.content = message.payload;
                        messageItem.timestamp = message.timestamp;
                        messageItem.isMine = false;
                        updateMessage(messageItem);
                    }
                });
                break;
            case Constants.CHAT_TYPE_GROUP:
                Web3MQMessageManager.getInstance().addGroupMessageCallback(chat_id, new MessageCallback() {
                    @Override
                    public void onMessage(com.ty.web3_mq.websocket.bean.MessageBean message) {
                        MessageItem messageItem = new MessageItem();
                        messageItem.from = message.from;
                        messageItem.content = message.payload;
                        messageItem.timestamp = message.timestamp;
                        messageItem.isMine = false;
                        updateMessage(messageItem);
                    }
                });
                break;
        }
    }

    private void updateMessage(MessageItem messageItem){
        messageList.add(messageItem);
        if(adapter==null){
            adapter = new MessagesAdapter(messageList,MessageActivity.this);
            list_message.setAdapter(adapter);
        }else {
            adapter.notifyItemInserted(messageList.size());
        }
        list_message.scrollTo(messageList.size()-1);
    }

    private void updateView() {
        if(adapter==null){
            adapter = new MessagesAdapter(messageList,MessageActivity.this);
            list_message.setAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }
        list_message.scrollTo(messageList.size()-1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        switch (chat_type){
            case Constants.CHAT_TYPE_USER:
                Web3MQMessageManager.getInstance().removeDMCallback(chat_id);
                break;
            case Constants.CHAT_TYPE_GROUP:
                Web3MQMessageManager.getInstance().removeGroupMessageCallback(chat_id);
                break;
        }
    }
}
package com.ty.web3mq.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.web3_mq.Web3MQGroup;
import com.ty.web3_mq.Web3MQMessageManager;
import com.ty.web3_mq.Web3MQUser;
import com.ty.web3_mq.http.beans.GroupBean;
import com.ty.web3_mq.http.beans.GroupMembersBean;
import com.ty.web3_mq.http.beans.MemberBean;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.http.beans.MessagesBean;
import com.ty.web3_mq.interfaces.GetGroupMembersCallback;
import com.ty.web3_mq.interfaces.GetMessageHistoryCallback;
import com.ty.web3_mq.interfaces.InvitationGroupCallback;
import com.ty.web3_mq.interfaces.MessageCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3mq.R;
import com.ty.web3mq.adapter.GroupMembersAdapter;
import com.ty.web3mq.adapter.MessagesAdapter;
import com.ty.web3mq.adapter.RecyclerViewScrollListener;
import com.ty.web3mq.bean.MessageItem;
import com.ty.web3mq.utils.Tools;

import java.util.ArrayList;

public class MessageDMFragment extends BaseFragment{
    private static MessageDMFragment instance;
    private RecyclerView recycler_view;
    private MessagesAdapter adapter;
    private static final String TAG = "MessageDMFragment";
    private String chatid;
    private String chat_type;
    private Button btn_send;
    private EditText et_message;
    private ImageView iv_group_member;
    private ArrayList<MessageItem> messageList = new ArrayList<>();
    private AlertDialog alertDialog;
    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    public static synchronized MessageDMFragment getInstance(String chatid, String chat_type) {
        if (instance == null) {
            instance = new MessageDMFragment();
        }
        Bundle bundle = new Bundle();
        bundle.putString("chatid",chatid);
        bundle.putString("chat_type",chat_type);
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_message,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData(1, PAGE_SIZE);
        initView();
        setListener();
        observeMessageReceive();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(chat_type.equals("user")){
            Web3MQMessageManager.getInstance().removeDMCallback(chatid);
        }else if(chat_type.equals("group")){
            Web3MQMessageManager.getInstance().removeGroupMessageCallback(chatid);
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
                Web3MQMessageManager.getInstance().sendMessage(message,chatid,true);
                MessageItem messageItem = new MessageItem();
                messageItem.from = Web3MQUser.getInstance().getMyUserId();
                messageItem.content = message;
                messageItem.timestamp = System.currentTimeMillis();
                updateMessage(messageItem);
                Tools.hideKeyboard(getActivity());
                et_message.setText("");
            }
        });
        iv_group_member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGroupMembers();
            }
        });
    }

    private void getGroupMembers() {
        showLoadingDialog();
        Web3MQGroup.getInstance().getGroupMembers(1, 20, chatid, new GetGroupMembersCallback() {
            @Override
            public void onSuccess(GroupMembersBean membersBean) {
                ArrayList<MemberBean>  members = membersBean.result;
                ArrayList<String> user_ids = new ArrayList<>();
                for(MemberBean memberBean: members){
                    user_ids.add(memberBean.userid);
                }
                hideLoadingDialog();
                showMembersDialog(user_ids);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"get group members error:"+error,Toast.LENGTH_SHORT).show();
                hideLoadingDialog();
            }
        });

    }

    private void showMembersDialog(ArrayList<String> user_ids){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_group_members,null);
        RecyclerView recyclerView = v.findViewById(R.id.recycler_view_members);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        GroupMembersAdapter membersAdapter = new GroupMembersAdapter(user_ids);
        recyclerView.setAdapter(membersAdapter);
        EditText et_userid = v.findViewById(R.id.et_userid);
        Button btn_invite = v.findViewById(R.id.btn_invite);
        Button btn_cancel = v.findViewById(R.id.btn_cancel);
        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_id = et_userid.getText().toString();
                inviteMember(user_id);
                alertDialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        builder.setView(v);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(CommonUtils.dp2px(getActivity(),335),CommonUtils.dp2px(getActivity(),500));
    }

    private void inviteMember(String user_id){
        Web3MQGroup.getInstance().invitation(chatid, new String[]{user_id}, new InvitationGroupCallback() {
            @Override
            public void onSuccess(GroupBean invitationGroupBean) {
                Toast.makeText(getActivity(),"invite success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"invite error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMessage(MessageItem messageItem){
        messageList.add(messageItem);
        adapter.notifyItemInserted(messageList.size());
        recycler_view.smoothScrollToPosition(messageList.size()-1);
    }

    private void observeMessageReceive() {
        if(chat_type.equals("user")){
            Web3MQMessageManager.getInstance().addDMCallback(chatid, new MessageCallback() {
                @Override
                public void onMessage(com.ty.web3_mq.websocket.bean.MessageBean message) {
                    MessageItem messageItem = new MessageItem();
                    messageItem.from = message.from;
                    messageItem.content = message.payload;
                    messageItem.timestamp = message.timestamp;
                    updateMessage(messageItem);
                }
            });
        }else if(chat_type.equals("group")){
            Web3MQMessageManager.getInstance().addGroupMessageCallback(chatid, new MessageCallback() {
                @Override
                public void onMessage(com.ty.web3_mq.websocket.bean.MessageBean message) {
                    MessageItem messageItem = new MessageItem();
                    messageItem.from = message.from;
                    messageItem.content = message.payload;
                    messageItem.timestamp = message.timestamp;
                    updateMessage(messageItem);
                }
            });
        }
    }

    private void initView() {
        recycler_view = rootView.findViewById(R.id.recycler_view_dm_chat);
        et_message = rootView.findViewById(R.id.et_message);
        btn_send = rootView.findViewById(R.id.btn_send);
        iv_group_member = rootView.findViewById(R.id.iv_group_member);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(manager);
        setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData(1,PAGE_SIZE);
            }
        });
        recycler_view.addOnScrollListener(new RecyclerViewScrollListener(PAGE_SIZE));
        if(chat_type.equals("group")){
            iv_group_member.setVisibility(View.VISIBLE);
        }else{
            iv_group_member.setVisibility(View.GONE);
        }
    }

    private void requestData(int page, int size) {
        Bundle bundle = getArguments();
        chatid = bundle.getString("chatid");
        chat_type = bundle.getString("chat_type");
        Web3MQMessageManager.getInstance().getMessageHistory(page, size, chatid, new GetMessageHistoryCallback() {
            @Override
            public void onSuccess(MessagesBean messagesBean) {
                messageList.clear();
                for(MessageBean msg: messagesBean.result){
                    MessageItem messageItem = new MessageItem();
                    messageItem.from = msg.from;
                    messageItem.content = new String(Base64.decode(msg.payload,Base64.DEFAULT));
                    messageItem.timestamp = msg.timestamp;
                    messageList.add(0,messageItem);
                }
                updateView();
                stopRefresh();
            }

            @Override
            public void onFail(String error) {
                stopRefresh();
            }
        });
    }


    private void updateView() {
        adapter = new MessagesAdapter(messageList);
        recycler_view.setAdapter(adapter);
        adapter.setOnItemClickListener(new MessagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //TODO
            }
        });
        recycler_view.scrollToPosition(messageList.size()-1);
    }
}

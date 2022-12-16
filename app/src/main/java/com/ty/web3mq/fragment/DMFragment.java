package com.ty.web3mq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.web3_mq.Web3MQChats;
import com.ty.web3_mq.Web3MQMessageManager;
import com.ty.web3_mq.http.beans.ChatBean;
import com.ty.web3_mq.http.beans.ChatsBean;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.http.beans.MessagesBean;
import com.ty.web3_mq.interfaces.GetChatsCallback;
import com.ty.web3_mq.interfaces.GetMessageHistoryCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.DMChatActivity;
import com.ty.web3mq.adapter.ChatsAdapter;
import com.ty.web3mq.bean.ChatItem;
import com.ty.web3mq.utils.Constants;

import java.util.ArrayList;

public class DMFragment extends BaseFragment{
    private static final String TAG = "DMFragment";
    private RecyclerView recycler_view_dm;
    private static DMFragment instance;
    private ArrayList<ChatItem> chats = new ArrayList<>();
    private ChatsAdapter chatsAdapter;
    public static synchronized DMFragment getInstance() {
        if (instance == null) {
            instance = new DMFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_dm,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        setListener();
    }

    private void requestData(){
        Web3MQChats.getInstance().getChats(1, 20, new GetChatsCallback() {
            @Override
            public void onSuccess(ChatsBean chatsBean) {
                chats.clear();
                for(ChatBean chatBean:chatsBean.result){
                    ChatItem chatItem = new ChatItem();
                    chatItem.chat_type = chatBean.chat_type;
                    chatItem.chatid = chatBean.topic;
                    chatItem.title = chatBean.chat_name;
                    chats.add(chatItem);
                }
                updateView();
                stopRefresh();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"request chats error:"+error,Toast.LENGTH_SHORT).show();
                stopRefresh();
            }
        });
    }


    private void initView(){
        recycler_view_dm = rootView.findViewById(R.id.recycler_view_dm);
        recycler_view_dm.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
    }

    private void updateView(){
        chatsAdapter = new ChatsAdapter(chats);
        recycler_view_dm.setAdapter(chatsAdapter);
        chatsAdapter.setOnItemClickListener(new ChatsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ChatItem chatItem = chats.get(position);
                Intent intent = new Intent(getActivity(), DMChatActivity.class);
                intent.putExtra(Constants.INTENT_CHAT_ID,chatItem.chatid);
                intent.putExtra(Constants.INTENT_CHAT_TYPE,chatItem.chat_type);
                getActivity().startActivity(intent);
            }
        });
    }

    private void setListener() {
        setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
    }

}
package com.ty.module_chat.fragment;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.common.fragment.BaseFragmentRefresh;
import com.ty.module_chat.R;
import com.ty.module_chat.adapter.ChatsAdapter;
import com.ty.module_chat.bean.ChatItem;
import com.ty.web3_mq.Web3MQChats;
import com.ty.web3_mq.Web3MQMessageManager;
import com.ty.web3_mq.http.beans.ChatBean;
import com.ty.web3_mq.http.beans.ChatsBean;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.http.beans.MessageStatus;
import com.ty.web3_mq.http.beans.MessagesBean;
import com.ty.web3_mq.interfaces.ChatsMessageCallback;
import com.ty.web3_mq.interfaces.GetChatsCallback;
import com.ty.web3_mq.interfaces.MessageCallback;
import com.ty.web3_mq.utils.DefaultSPHelper;

import java.util.ArrayList;

import web3mq.Message;


public class ChatsFragment extends BaseFragmentRefresh {
    private static final String TAG = "ChatsFragment";
    private static ChatsFragment instance;
    private ArrayList<ChatItem> chats = new ArrayList<>();
    private ChatsAdapter chatsAdapter;
    private RecyclerView recycler_view_chats;
    private ConstraintLayout cl_chats_empty;
    private static final int INIT_CHATS_SIZE = 20;
    public static synchronized ChatsFragment getInstance() {
        if (instance == null) {
            instance = new ChatsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_chats,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        setListener();
    }

    private void requestData(){
        Web3MQChats.getInstance().getChats(1, INIT_CHATS_SIZE, new GetChatsCallback() {
            @Override
            public void onSuccess(ChatsBean chatsBean) {
                chats.clear();
                if(chatsBean.result.size()==0){
                    cl_chats_empty.setVisibility(View.VISIBLE);
                }else{
                    cl_chats_empty.setVisibility(View.GONE);
                }

                for(ChatBean chatBean:chatsBean.result){
                    ChatItem chatItem = new ChatItem();
                    chatItem.chat_type = chatBean.chat_type;
                    chatItem.chatid = chatBean.topic;
                    chatItem.title = chatBean.chat_name;
                    MessagesBean bean = DefaultSPHelper.getInstance().getMessages(chatItem.chatid);
                    if(bean!=null&&bean.result.size()>0){
                        MessageBean lastMessageBean = bean.result.get(0);
                        chatItem.timestamp = lastMessageBean.timestamp;
                        chatItem.content = new String(Base64.decode(lastMessageBean.payload,Base64.DEFAULT));
                        int unreadCount = 0;
                        for(MessageBean msgBean:bean.result){
                            if(!msgBean.status.status.equals(MessageStatus.STATUS_READ)){
                                unreadCount+=1;
                            }
                        }
                        chatItem.unreadCount = unreadCount;
                    }

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
        recycler_view_chats = rootView.findViewById(R.id.recycler_view_chats);
        cl_chats_empty = rootView.findViewById(R.id.cl_chats_empty);
        recycler_view_chats.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
    }

    private void updateView(){
        chatsAdapter = new ChatsAdapter(chats);
        recycler_view_chats.setAdapter(chatsAdapter);
        chatsAdapter.setOnItemClickListener(new ChatsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                ChatItem chatItem = chats.get(position);
//                Intent intent = new Intent(getActivity(), DMChatActivity.class);
//                intent.putExtra(Constants.INTENT_CHAT_ID,chatItem.chatid);
//                intent.putExtra(Constants.INTENT_CHAT_TYPE,chatItem.chat_type);
//                getActivity().startActivity(intent);
                //TODO
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
        //TODO 监听websocket更新列表
        Web3MQMessageManager.getInstance().setChatsMessageCallback(new ChatsMessageCallback() {
            @Override
            public void onMessage(Message.Web3MQMessage message) {
                for(int i=0;i<chats.size();i++){
                    ChatItem chatItem = chats.get(i);
                    switch (chatItem.chat_type){
                        case ChatItem.CHAT_TYPE_USER:
                            if(message.getComeFrom().equals(chatItem.chatid)){
                                chatItem.unreadCount+=1;
                                chatItem.content = message.getPayload().toStringUtf8();
                                chatsAdapter.notifyItemChanged(i);
                            }
                            break;
                        case ChatItem.CHAT_TYPE_GROUP:
                            if(message.getContentTopic().equals(chatItem.chatid)){
                                chatItem.unreadCount+=1;
                                chatItem.content = message.getPayload().toStringUtf8();
                                chatsAdapter.notifyItemChanged(i);
                            }
                            break;
                    }
                }
            }
        });
    }


}

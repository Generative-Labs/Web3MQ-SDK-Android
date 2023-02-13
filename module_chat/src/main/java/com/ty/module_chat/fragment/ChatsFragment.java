package com.ty.module_chat.fragment;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;
import com.ty.common.fragment.BaseFragment;
import com.ty.common.view.Web3MQListView;
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

//import web3mq.Message;


public class ChatsFragment extends BaseFragment implements ChatsMessageCallback{
    private static final String TAG = "ChatsFragment";
    private static ChatsFragment instance;
    private ArrayList<ChatItem> chats = new ArrayList<>();
    private ChatsAdapter chatsAdapter;
    private Web3MQListView recycler_view_chats;
//    private ConstraintLayout cl_chats_empty;
    private static final int INIT_CHATS_SIZE = 100;
    private ToNewMessageListener toNewMessageListener;
    private ImageView iv_new_message;
    public static synchronized ChatsFragment getInstance() {
        if (instance == null) {
            instance = new ChatsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_chats);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        setListener();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.i(TAG,"hidden:"+hidden);
        super.onHiddenChanged(hidden);
        if(!hidden){
            requestData();
        }
    }

    public void setToNewMessageListener(ToNewMessageListener toNewMessageListener){
        this.toNewMessageListener = toNewMessageListener;
    }

    @Override
    public void onMessage(Message.Web3MQMessage message) {
        Log.i(TAG,"MessageType:"+message.getMessageType());
        Log.i(TAG,"MessageId:"+message.getMessageId());
        Log.i(TAG,"MessageType:"+message.getMessageType());
        Log.i(TAG,"ComeFrom:"+message.getComeFrom());
        Log.i(TAG,"Payload:"+message.getPayload().toStringUtf8());
        Log.i(TAG,"PayloadType:"+message.getPayloadType());
        Log.i(TAG,"ContentTopic:"+message.getContentTopic());
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

    public interface ToNewMessageListener {
        void toNewMessageModule();
    }

    private void requestData(){
        Web3MQChats.getInstance().getChats(1, INIT_CHATS_SIZE, new GetChatsCallback() {
            @Override
            public void onSuccess(ChatsBean chatsBean) {
                recycler_view_chats.setRefreshing(false);
                chats.clear();
                if(chatsBean.result.size()==0){
                    recycler_view_chats.showEmptyView();
                }else{
                    recycler_view_chats.hideEmptyView();
                }

                for(ChatBean chatBean:chatsBean.result){
                    ChatItem chatItem = new ChatItem();
                    chatItem.chat_type = chatBean.chat_type;
                    chatItem.chatid = chatBean.topic;
                    chatItem.title = chatBean.chat_name;
                    // get timestamp and content from local storage
                    MessagesBean bean = DefaultSPHelper.getInstance().getMessages(chatItem.chatid);
                    if(bean!=null&&bean.result.size()>0){
                        MessageBean lastMessageBean = bean.result.get(0);
                        chatItem.timestamp = lastMessageBean.timestamp;
                        chatItem.content = new String(Base64.decode(lastMessageBean.payload,Base64.DEFAULT));
                    }
                    chats.add(chatItem);
                }
                updateView();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"request chats error:"+error,Toast.LENGTH_SHORT).show();
                recycler_view_chats.setRefreshing(false);
            }
        });
    }


    private void initView(){
        recycler_view_chats = rootView.findViewById(R.id.recycler_view_chats);
        iv_new_message = rootView.findViewById(R.id.iv_new_message);
        recycler_view_chats.setEmptyIcon(R.mipmap.ic_chats_empty);
        recycler_view_chats.setEmptyMessage("Your message list is empty");

    }

    private void updateView(){
        chatsAdapter = new ChatsAdapter(chats);
        recycler_view_chats.setAdapter(chatsAdapter);
        chatsAdapter.setOnItemClickListener(new ChatsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ChatItem chatItem = chats.get(position);
                String chat_type = chatItem.chat_type;
                String chat_id = chatItem.chatid;
                if(chatItem.unreadCount!=0){
                    chatItem.unreadCount = 0;
                    chatsAdapter.notifyItemChanged(position);
                }

                ARouter.getInstance().build(RouterPath.CHAT_MESSAGE).withString(Constants.ROUTER_KEY_CHAT_TYPE,chat_type).withString(Constants.ROUTER_KEY_CHAT_ID,chat_id).navigation();
            }
        });
    }

    private void setListener() {
        recycler_view_chats.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });

        iv_new_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toNewMessageListener!=null){
                    toNewMessageListener.toNewMessageModule();
                }
            }
        });
        //TODO 监听websocket更新列表
        Web3MQMessageManager.getInstance().setChatsMessageCallback(this);
    }


}

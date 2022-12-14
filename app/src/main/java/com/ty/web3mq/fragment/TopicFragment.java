package com.ty.web3mq.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ty.web3_mq.Web3MQMessageManager;
import com.ty.web3_mq.Web3MQTopic;
import com.ty.web3_mq.http.beans.MessageBean;
import com.ty.web3_mq.http.beans.MessagesBean;
import com.ty.web3_mq.http.beans.TopicBean;
import com.ty.web3_mq.interfaces.CreateTopicCallback;
import com.ty.web3_mq.interfaces.GetMessageHistoryCallback;
import com.ty.web3_mq.interfaces.GetMyCreateTopicCallback;
import com.ty.web3_mq.interfaces.GetMySubscribeTopicCallback;
import com.ty.web3_mq.interfaces.PublishTopicMessageCallback;
import com.ty.web3_mq.interfaces.SubscribeCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3mq.R;
import com.ty.web3mq.adapter.ChatsAdapter;
import com.ty.web3mq.bean.ChatItem;

import java.util.ArrayList;

public class TopicFragment extends BaseFragment{
    private static final String TAG = "TopicFragment";
    private RecyclerView recycler_view;
    private ChatsAdapter chatsAdapter;
    private static TopicFragment instance;
    private ArrayList<ChatItem> chats = new ArrayList<>();
    private BottomSheetDialog bottomSheetDialog;
    private static final int ACTION_CREATE = 0;
    private static final int ACTION_SUBSCRIBE = 1;
    private int topic_action = ACTION_CREATE;
    private TextView tv_create_topic,tv_subscribe_topic;
    private AlertDialog publishDialog;
    public static synchronized TopicFragment getInstance() {
        if (instance == null) {
            instance = new TopicFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_topic,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        setListener();
    }

    private void requestData() {
        Web3MQTopic.getInstance().getMyCreateTopicList(1, 20, new GetMyCreateTopicCallback() {
            @Override
            public void onSuccess(ArrayList<TopicBean> topicList) {
                chats.clear();
                for(TopicBean topicBean:topicList){
                    ChatItem chatItem = new ChatItem();
                    chatItem.chat_type = "topic";
                    chatItem.chatid = topicBean.topicid;
                    chatItem.title = topicBean.topic_name;
                    chats.add(chatItem);
                }
                updateView();
                stopRefresh();
            }

            @Override
            public void onFail(String error) {
                stopRefresh();
                Toast.makeText(getActivity(),"get topic error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateView(){
        chatsAdapter = new ChatsAdapter(chats);
        recycler_view.setAdapter(chatsAdapter);
        chatsAdapter.setOnItemClickListener(new ChatsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                ChatItem chatItem = chats.get(position);
                showPublishMessageDialog(chatItem.chatid);
            }
        });
    }

    private void initView(){
        recycler_view = rootView.findViewById(R.id.recycler_view);
        tv_create_topic = rootView.findViewById(R.id.tv_create_topic);
        tv_subscribe_topic = rootView.findViewById(R.id.tv_subscribe_topic);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
    }

    private void setListener() {
        tv_create_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topic_action = ACTION_CREATE;
                showBottomDialog();
            }
        });
        tv_subscribe_topic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topic_action = ACTION_SUBSCRIBE;
                showBottomDialog();
            }
        });
        setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestData();
            }
        });
    }

    private void showBottomDialog() {
        bottomSheetDialog = new BottomSheetDialog(getActivity());
        View view = View.inflate(getActivity(), R.layout.bottom_dialog_create_topic, null);
        Button btn_topic_action = view.findViewById(R.id.btn_topic_action);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        EditText et_input_name = view.findViewById(R.id.et_input_name);
        if(topic_action == ACTION_CREATE){
            et_input_name.setHint("Topic Name");
            btn_topic_action.setText("Create Topic");
            btn_topic_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    createTopic(et_input_name.getText().toString());
                }
            });
        }else if(topic_action == ACTION_SUBSCRIBE){
            et_input_name.setHint("Topic ID");
            btn_topic_action.setText("Subscribe Topic");
            btn_topic_action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    subscribeTopic(et_input_name.getText().toString());
                }
            });
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet)
                .setBackgroundResource(android.R.color.transparent);
        bottomSheetDialog.show();
    }

    private void createTopic(String name){
        Web3MQTopic.getInstance().createTopic(name, new CreateTopicCallback() {
            @Override
            public void onSuccess(TopicBean topicBean) {
                Toast.makeText(getActivity(),"create topic success",Toast.LENGTH_SHORT).show();
                if(bottomSheetDialog !=null){
                    bottomSheetDialog.dismiss();
                }
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"create topic fail. error:"+error,Toast.LENGTH_SHORT).show();
                if(bottomSheetDialog !=null){
                    bottomSheetDialog.dismiss();
                }
            }
        });
    }

    private void subscribeTopic(String topicId){
        Web3MQTopic.getInstance().subscribeTopic(topicId, new SubscribeCallback() {
            @Override
            public void onSuccess() {

                Toast.makeText(getActivity(),"create topic success",Toast.LENGTH_SHORT).show();
                if(bottomSheetDialog !=null){
                    bottomSheetDialog.dismiss();
                }
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"create topic fail. error:"+error,Toast.LENGTH_SHORT).show();
                if(bottomSheetDialog !=null){
                    bottomSheetDialog.dismiss();
                }
            }
        });
    }

    public void showPublishMessageDialog(String topic_id){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_publish_topic,null);
        EditText et_topic_title = v.findViewById(R.id.et_topic_title);
        EditText et_topic_content = v.findViewById(R.id.et_topic_content);
        Button btn_publish = v.findViewById(R.id.btn_publish);
        btn_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = et_topic_title.getText().toString();
                String content = et_topic_content.getText().toString();
                publishTopic(topic_id, title, content);
            }
        });
        builder.setView(v);
        publishDialog = builder.create();
        publishDialog.show();
        publishDialog.getWindow().setLayout(CommonUtils.dp2px(getActivity(),300),CommonUtils.dp2px(getActivity(),500));
    }

    private void publishTopic(String topic_id, String title, String content){
        Web3MQTopic.getInstance().publishTopicMessage(topic_id, title, content, new PublishTopicMessageCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(),"publish success",Toast.LENGTH_SHORT).show();
                publishDialog.dismiss();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"publish error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
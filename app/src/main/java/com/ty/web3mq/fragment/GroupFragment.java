package com.ty.web3mq.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.ty.web3_mq.Web3MQGroup;
import com.ty.web3_mq.http.beans.GroupBean;
import com.ty.web3_mq.http.beans.GroupsBean;
import com.ty.web3_mq.interfaces.CreateGroupCallback;
import com.ty.web3_mq.interfaces.GetGroupListCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.adapter.ChatsAdapter;
import com.ty.web3mq.bean.ChatItem;

import java.util.ArrayList;

public class GroupFragment extends BaseFragment{
    private static final String TAG = "GroupFragment";
    private RecyclerView recycler_view;
    private static GroupFragment instance;
    private TextView tv_create_group;
    private BottomSheetDialog bottomSheetDialog;
    private ChatsAdapter adapter;
    private ArrayList<ChatItem> chats = new ArrayList<>();
    public static synchronized GroupFragment getInstance() {
        if (instance == null) {
            instance = new GroupFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_group,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
        setListener();
    }

    private void requestData(){
        Web3MQGroup.getInstance().getGroupList(1, 20, new GetGroupListCallback() {
            @Override
            public void onSuccess(GroupsBean groups) {
                chats.clear();
                for(GroupBean groupBean:groups.result){
                    ChatItem chatItem = new ChatItem();
                    chatItem.chat_type = "group";
                    chatItem.chatid = groupBean.groupid;
                    chatItem.title = groupBean.groupid;
                    chats.add(chatItem);
                }
                updateView();
                stopRefresh();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"get group list error:"+error,Toast.LENGTH_SHORT).show();
                stopRefresh();
            }
        });
    }


    private void initView(){
        recycler_view = rootView.findViewById(R.id.recycler_view);
        tv_create_group = rootView.findViewById(R.id.tv_create_group);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
    }

    private void updateView(){
        adapter = new ChatsAdapter(chats);
        recycler_view.setAdapter(adapter);
        adapter.setOnItemClickListener(new ChatsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
//                ChatItem chatItem = chats.get(position);
//                Intent intent = new Intent(getActivity(), DMChatActivity.class);
//                intent.putExtra(Constants.INTENT_CHAT_ID,chatItem.chatid);
//                getActivity().startActivity(intent);
                //TODO
            }
        });
    }

    private void setListener() {
        tv_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        View view = View.inflate(getActivity(), R.layout.bottom_dialog_create_group, null);
        Button btn_create_group = view.findViewById(R.id.btn_create_group);
        Button btn_cancel = view.findViewById(R.id.btn_cancel);
        EditText et_input_name = view.findViewById(R.id.et_input_name);
        btn_create_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGroup(et_input_name.getText().toString());
            }
        });
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

    private void createGroup(String name){
        Web3MQGroup.getInstance().createGroup(name, new CreateGroupCallback() {
            @Override
            public void onSuccess(GroupBean groupBean) {
                Toast.makeText(getActivity(),"create group success",Toast.LENGTH_SHORT).show();
                if(bottomSheetDialog!=null){
                    bottomSheetDialog.dismiss();
                }
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"create group fail. error:"+error,Toast.LENGTH_SHORT).show();
                if(bottomSheetDialog!=null){
                    bottomSheetDialog.dismiss();
                }
            }
        });
    }
}
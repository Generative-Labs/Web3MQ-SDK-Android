package com.ty.moudle_new_message;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ty.common.config.Constants;
import com.ty.web3_mq.utils.ConvertUtil;
import com.ty.common.view.Web3MQListView;
import com.ty.moudle_new_message.adapter.FriendListAdapter;
import com.ty.web3_mq.Web3MQFollower;
import com.ty.web3_mq.Web3MQGroup;
import com.ty.web3_mq.http.beans.FollowerBean;
import com.ty.web3_mq.http.beans.FollowersBean;
import com.ty.web3_mq.http.beans.GroupBean;
import com.ty.web3_mq.interfaces.CreateGroupCallback;
import com.ty.web3_mq.interfaces.GetMyFollowingCallback;
import com.ty.web3_mq.interfaces.InvitationGroupCallback;
import com.ty.web3_mq.interfaces.SendFriendRequestCallback;

import java.util.ArrayList;

public class NewMessageFragment extends BottomSheetDialogFragment {
    private static final String TAG = "NewMessageFragment";
    private static volatile NewMessageFragment instance;
    private BottomSheetBehavior mBehavior;
    private Web3MQListView list_friends;
    private ArrayList<FollowerBean> shownFollower = new ArrayList<>();
    private ArrayList<FollowerBean> allFollower = new ArrayList<>();
    private FriendListAdapter adapter;
    private Button btn_add_friends,btn_create_room;
    private BottomSheetDialog dialog;
    private EditText et_search;
    private ImageView iv_back;
    private ToMessageListener toMessageListener;
    private String userid;

    public static synchronized NewMessageFragment getInstance() {
        if (instance == null) {
            instance = new NewMessageFragment();
        }
        return instance;
    }

    private NewMessageFragment(){}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        if(getArguments()!=null){
            userid = getArguments().getString(Constants.ROUTER_KEY_USER_ID);
        }
        View view;
        if(userid==null){
            requestData();
            view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_new_message, null);
            initNewMessageView(view);
        }else{
            view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_add_friends, null);
            initAddFriendsView(view);
        }
        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        userid = null;
        super.onDismiss(dialog);
    }

    public void setToMessageListener(ToMessageListener toMessageListener){
        this.toMessageListener = toMessageListener;
    }

    public interface ToMessageListener {
        void toMessage(String chat_type,String chat_id);
    }

    private void initNewMessageView(View rootView){
        list_friends = rootView.findViewById(R.id.list_friends);
        btn_add_friends = rootView.findViewById(R.id.btn_add_friends);
        btn_create_room = rootView.findViewById(R.id.btn_create_room);
        et_search = rootView.findViewById(R.id.et_search);
        iv_back = rootView.findViewById(R.id.iv_back);
        list_friends.setEmptyMessage("No friends");
        btn_add_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_add_friends, null);
                initAddFriendsView(view);
                dialog.setContentView(view);
            }
        });
        btn_create_room.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_create_room_step1, null);
                initCreateRoomStep1(view);
                dialog.setContentView(view);
            }
        });
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(TextUtils.isEmpty(s.toString())){
                    filterEachFollower();
                    updateList(FriendListAdapter.STYLE_DEFAULT);
                }else {
                    filterFollowerByString(s.toString());
                    updateList(FriendListAdapter.STYLE_SEARCH);
                }

            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        requestData();
    }

    private void initCreateRoomStep1(View rootView) {
        RecyclerView list_create_room = rootView.findViewById(R.id.list_create_room);
        list_create_room.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        Button btn_next = rootView.findViewById(R.id.btn_next);
        ImageView iv_cancel = rootView.findViewById(R.id.iv_cancel);
        ImageView iv_back = rootView.findViewById(R.id.iv_back);
        FriendListAdapter friendListAdapter = new FriendListAdapter(allFollower,getActivity());
        friendListAdapter.setStyle(FriendListAdapter.STYLE_CREATE_ROOM);
        list_create_room.setAdapter(friendListAdapter);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(friendListAdapter.getCheckedFollower().size()>0){
                    View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_create_room_step2, null);
                    initCreateRoomStep2(view,friendListAdapter.getCheckedFollower());
                    dialog.setContentView(view);
                }else{
                    Toast.makeText(getActivity(),"please check someone",Toast.LENGTH_SHORT).show();
                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_new_message, null);
                initNewMessageView(view);
                dialog.setContentView(view);
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initCreateRoomStep2(View rootView,ArrayList<FollowerBean> checkedFollower){
        RecyclerView recyclerview_room_member = rootView.findViewById(R.id.recyclerview_room_member);
        recyclerview_room_member.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        Button btn_create = rootView.findViewById(R.id.btn_create);
        ImageView iv_cancel = rootView.findViewById(R.id.iv_cancel);
        ImageView iv_back = rootView.findViewById(R.id.iv_back);
        EditText et_room_name = rootView.findViewById(R.id.et_room_name);
        FriendListAdapter adapter = new FriendListAdapter(checkedFollower,getActivity());
        adapter.setStyle(FriendListAdapter.STYLE_DEFAULT);
        recyclerview_room_member.setAdapter(adapter);
        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO create
                String group_name = et_room_name.getText().toString();
                ArrayList<String> invite_user_id = new ArrayList<>();
                for(FollowerBean follower: checkedFollower){
                    invite_user_id.add(follower.userid);
                }
                createGroup(group_name, invite_user_id);
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_create_room_step1, null);
                initCreateRoomStep1(view);
                dialog.setContentView(view);
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void createGroup(String group_name, ArrayList<String> userIds){
        Web3MQGroup.getInstance().createGroup(group_name, new CreateGroupCallback() {
            @Override
            public void onSuccess(GroupBean groupBean) {
                inviteMember(groupBean.groupid,userIds);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"create group fail error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inviteMember(String group_id, ArrayList<String> userIds){
        String[] ids = new String[]{};
        Web3MQGroup.getInstance().invite(group_id, userIds.toArray(ids), new InvitationGroupCallback() {
            @Override
            public void onSuccess(GroupBean invitationGroupBean) {
                dismiss();
                if(toMessageListener!=null){
                    toMessageListener.toMessage(Constants.CHAT_TYPE_GROUP,invitationGroupBean.groupid);
                }
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"invite member fail error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateList(String style){
        if(shownFollower.size()>0){
            list_friends.hideEmptyView();
//            if(adapter==null){
                adapter = new FriendListAdapter(shownFollower,getActivity());
                adapter.setStyle(style);
                adapter.setOnItemClickListener(new FriendListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        FollowerBean bean = shownFollower.get(position);
                        dismiss();
                        if(toMessageListener!=null){
                            toMessageListener.toMessage(Constants.CHAT_TYPE_USER,bean.userid);
                        }
                    }
                });
                list_friends.setAdapter(adapter);
//            }else{
//                adapter.setStyle(style);
//                adapter.notifyDataSetChanged();
//            }
        }else{
            list_friends.showEmptyView();
        }
    }

    private void initAddFriendsView(View rootView){
        ImageView iv_back = rootView.findViewById(R.id.iv_back);
        ImageView iv_cancel = rootView.findViewById(R.id.iv_cancel);
        EditText et_user_id = rootView.findViewById(R.id.et_user_id);
        EditText et_invitation_note = rootView.findViewById(R.id.et_invitation_note);
        Button btn_add = rootView.findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_id = et_user_id.getText().toString();
                String invitation_note = et_invitation_note.getText().toString();
                long timeStamp = System.currentTimeMillis();
                sendFriendRequest(user_id, invitation_note, timeStamp);
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_new_message, null);
                initNewMessageView(view);
                dialog.setContentView(view);
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void sendFriendRequest(String user_id, String invitation_note,long timeStamp){
        Web3MQFollower.getInstance().sendFriendRequest(user_id, timeStamp, invitation_note, new SendFriendRequestCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(),"send friend request success",Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"send friend request Fail",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void requestData() {
        Web3MQFollower.getInstance().getFollowerAndFollowing(0, 500, new GetMyFollowingCallback() {
            @Override
            public void onSuccess(String response) {
                shownFollower.clear();
                FollowersBean followersBean = ConvertUtil.convertJsonToFollowersBean(response);
                allFollower = followersBean.user_list;
                filterEachFollower();
                updateList(FriendListAdapter.STYLE_DEFAULT);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"request data error: "+error,Toast.LENGTH_SHORT).show();
                list_friends.showEmptyView();
            }
        });
    }

    private void filterEachFollower(){
        shownFollower.clear();
        for(FollowerBean bean: allFollower){
            if(bean.follow_status.equals(Constants.FOLLOW_STATUS_EACH)){
                shownFollower.add(bean);
            }
        }
    }

    private void filterFollowerByString(String prefix){
        shownFollower.clear();
        for(FollowerBean bean: allFollower){
            if(bean.userid.startsWith(prefix)){
                shownFollower.add(bean);
            }
        }
    }


}

package com.ty.module_chat.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.ty.module_chat.R;
import com.ty.module_chat.adapter.InviteGroupAdapter;
import com.ty.web3_mq.Web3MQFollower;
import com.ty.web3_mq.Web3MQGroup;
import com.ty.web3_mq.http.beans.FollowerBean;
import com.ty.web3_mq.http.beans.FollowersBean;
import com.ty.web3_mq.http.beans.GroupBean;
import com.ty.web3_mq.interfaces.GetMyFollowingCallback;
import com.ty.web3_mq.interfaces.InvitationGroupCallback;

import java.util.ArrayList;

public class InviteGroupFragment extends BottomSheetDialogFragment {
    private static final String TAG = "InviteGroupFragment";
    private static volatile InviteGroupFragment instance;
    private BottomSheetBehavior mBehavior;
    private RecyclerView list_invite_group;
    private InviteGroupAdapter adapter;
    private BottomSheetDialog dialog;
    private ArrayList<FollowerBean> allFollower = new ArrayList<>();
    private ArrayList<FollowerBean> shownFollower = new ArrayList<>();
    private String group_id;

    public static synchronized InviteGroupFragment getInstance(String group_id) {
        if (instance == null) {
            instance = new InviteGroupFragment();
        }
        Bundle bundle = new Bundle();
        bundle.putString("group_id",group_id);
        instance.setArguments(bundle);
        return instance;
    }

    private InviteGroupFragment(){}

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        requestData();
        View view = View.inflate(getContext(), R.layout.dialog_bottom_sheet_invite_group, null);
        initView(view);
        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void requestData() {
        Web3MQFollower.getInstance().getFollowerAndFollowing(0, 500, new GetMyFollowingCallback() {
            @Override
            public void onSuccess(String response) {
                shownFollower.clear();
                FollowersBean followersBean = ConvertUtil.convertJsonToFollowersBean(response);
                allFollower = followersBean.user_list;
                filterEachFollower();
                adapter = new InviteGroupAdapter(shownFollower,getActivity());
                list_invite_group.setAdapter(adapter);
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"request data error: "+error,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initView(View rootView){
        list_invite_group = rootView.findViewById(R.id.list_invite_group);
        list_invite_group.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        Button btn_add = rootView.findViewById(R.id.btn_add);
        ImageView iv_back = rootView.findViewById(R.id.iv_back);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getCheckedFollower().size()>0){
                    ArrayList<String> invite_user_id = new ArrayList<>();
                    for(FollowerBean follower: adapter.getCheckedFollower()){
                        invite_user_id.add(follower.userid);
                    }
                    Bundle bundle = getArguments();
                    String group_id = bundle.getString("group_id");
                    inviteMember(group_id,invite_user_id);
                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
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

    private void inviteMember(String group_id, ArrayList<String> userIds){
        String[] ids = new String[]{};
        Web3MQGroup.getInstance().invite(group_id, userIds.toArray(ids), new InvitationGroupCallback() {
            @Override
            public void onSuccess(GroupBean invitationGroupBean) {
                dismiss();
            }

            @Override
            public void onFail(String error) {
                Toast.makeText(getActivity(),"invite member fail error:"+error,Toast.LENGTH_SHORT).show();
            }
        });
    }
}

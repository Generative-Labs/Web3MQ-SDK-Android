package com.ty.module_chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ty.module_chat.R;
import com.ty.module_chat.adapter.viewHolder.InviteGroupViewHolder;
import com.ty.web3_mq.http.beans.FollowerBean;
import com.ty.web3_mq.http.beans.MemberBean;

import java.util.ArrayList;

public class RoomMembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<MemberBean> memberBeans;
    private static final int VIEW_TYPE_ADD_PEOPLE = 0;
    private static final int VIEW_TYPE_MEMBER = 1;
    private OnAddPeopleClickListener onAddPeopleClickListener;
    private Context context;
    public RoomMembersAdapter(ArrayList<MemberBean> memberBeans,Context context){
        this.memberBeans = memberBeans;
        this.context = context;
    }

    public void setOnAddPeopleClickListener(OnAddPeopleClickListener onAddPeopleClickListener){
        this.onAddPeopleClickListener = onAddPeopleClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ADD_PEOPLE){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_member_add_people,parent,false);
            AddPeopleViewHolder viewHolder = new AddPeopleViewHolder(v);
            return viewHolder;
        }else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_member,parent,false);
            MembersViewHolder viewHolder = new MembersViewHolder(v);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int realPosition = 0;
        if(position>0){
            realPosition = position -1;
        }
        MemberBean memberBean = memberBeans.get(realPosition);
        if(holder instanceof AddPeopleViewHolder){
            AddPeopleViewHolder viewHolder = (AddPeopleViewHolder) holder;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onAddPeopleClickListener!=null){
                        onAddPeopleClickListener.onAddPeopleClick();
                    }
                }
            });
        }else if(holder instanceof MembersViewHolder){
            MembersViewHolder viewHolder = (MembersViewHolder) holder;
//            Glide.with(context).load()
//            viewHolder.iv_avatar
            viewHolder.tv_userid.setText(memberBean.userid);
        }
    }

    class AddPeopleViewHolder extends RecyclerView.ViewHolder{

        public AddPeopleViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class MembersViewHolder extends RecyclerView.ViewHolder{
        public ImageView iv_avatar;
        public TextView tv_userid;
        public MembersViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_avatar = itemView.findViewById(R.id.iv_avatar);
            tv_userid = itemView.findViewById(R.id.tv_userid);
        }
    }

    public interface OnAddPeopleClickListener{
        void onAddPeopleClick();
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return VIEW_TYPE_ADD_PEOPLE;
        }else{
            return VIEW_TYPE_MEMBER;
        }
    }


    @Override
    public int getItemCount() {
        return memberBeans.size()+1;
    }
}

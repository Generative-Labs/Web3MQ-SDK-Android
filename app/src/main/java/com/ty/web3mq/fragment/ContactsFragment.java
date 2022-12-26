package com.ty.web3mq.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ty.web3_mq.Web3MQContacts;
import com.ty.web3_mq.http.beans.ContactBean;
import com.ty.web3_mq.http.beans.ContactsBean;
import com.ty.web3_mq.interfaces.GetContactsCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.DMChatActivity;
import com.ty.web3mq.activity.NewFriendActivity;
import com.ty.web3mq.adapter.ContactsAdapter;
import com.ty.web3mq.adapter.RecyclerViewScrollListener;
import com.ty.web3mq.utils.Constants;

import java.util.ArrayList;

public class ContactsFragment extends BaseFragment {
    private static ContactsFragment instance;
    private RecyclerView recycler_view;
    private ContactsAdapter contactsAdapter;
    private static final String TAG = "ContactsFragment";
    private ArrayList<ContactBean> contacts = new ArrayList<>();
    private static final int PAGE_SIZE = 20;
    private int currentPage = 1;
    private RecyclerViewScrollListener scrollListener;
    public static synchronized ContactsFragment getInstance() {
        if (instance == null) {
            instance = new ContactsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_contacts,true);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData(1,PAGE_SIZE);
        initView();
    }

    private void requestData(int page, int size) {
        Web3MQContacts.getInstance().getContactList(page, size, new GetContactsCallback() {
            @Override
            public void onSuccess(ContactsBean contactsBean) {
                contacts.addAll(contactsBean.result);
                updateContactsView();
                currentPage = page;
                scrollListener.setMaxLoadCount(currentPage*PAGE_SIZE);
                stopRefresh();
            }

            @Override
            public void onFail(String error) {
                stopRefresh();
            }
        });
    }

    private void updateContactsView(){
//        if(contactsAdapter==null){
            contactsAdapter = new ContactsAdapter(getActivity(),contacts);
            recycler_view.setAdapter(contactsAdapter);
            contactsAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if(position == 0){
                        // new friend
                        Intent intent = new Intent(getActivity(), NewFriendActivity.class);
                        startActivity(intent);
                    }else{
                        ContactBean contact = contacts.get(position-1);
                        // contact item click
                        Intent intent = new Intent(getActivity(), DMChatActivity.class);
                        intent.putExtra(Constants.INTENT_CHAT_TYPE,"user");
                        intent.putExtra(Constants.INTENT_CHAT_ID,contact.userid);
                        startActivity(intent);
                    }
                }
            });
//        }else{
//            int lastCount = currentPage*PAGE_SIZE;
//            contactsAdapter.notifyItemRangeInserted(lastCount,contacts.size()-lastCount);
//        }

    }


    private void initView() {
        recycler_view = rootView.findViewById(R.id.recycler_view_contacts);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        scrollListener = new RecyclerViewScrollListener(PAGE_SIZE){
            @Override
            public void onScrollToBottom() {
                requestData(currentPage+1,PAGE_SIZE);
            }
        };
        recycler_view.addOnScrollListener(scrollListener);
        updateContactsView();
        setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                contacts.clear();
                requestData(1,PAGE_SIZE);
            }
        });
    }
}

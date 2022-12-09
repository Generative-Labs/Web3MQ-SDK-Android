package com.ty.web3mq.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ty.web3_mq.Web3MQContacts;
import com.ty.web3_mq.http.beans.ContactBean;
import com.ty.web3_mq.http.beans.ContactsBean;
import com.ty.web3_mq.interfaces.FriendRequestCallback;
import com.ty.web3_mq.interfaces.GetContactsCallback;
import com.ty.web3_mq.interfaces.SearchContactsCallback;
import com.ty.web3mq.R;
import com.ty.web3mq.activity.NewFriendActivity;
import com.ty.web3mq.adapter.ContactsAdapter;

import java.util.ArrayList;

public class ContactsFragment extends BaseFragment {
    private static ContactsFragment instance;
    private RecyclerView recycler_view;
    private ContactsAdapter contactsAdapter;
    private static final String TAG = "ContactsFragment";
    public static synchronized ContactsFragment getInstance() {
        if (instance == null) {
            instance = new ContactsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.fragment_contacts);
    }

    @Override
    protected void onBaseCreateView() {
        super.onBaseCreateView();
        requestData();
        initView();
    }

    private void requestData() {
        Web3MQContacts.getInstance().getContactList(1, 20, new GetContactsCallback() {
            @Override
            public void onSuccess(ContactsBean contactsBean) {
                ArrayList<ContactBean> contacts = contactsBean.result;
                updateContactsView(contacts);

                Log.i(TAG,"total: "+contactsBean.total);
            }

            @Override
            public void onFail(String error) {
                Log.e(TAG,"onFail:"+error);
            }
        });
    }

    private void updateContactsView(ArrayList<ContactBean> contactBeans){
        contactsAdapter = new ContactsAdapter(getActivity(),contactBeans);
        recycler_view.setAdapter(contactsAdapter);
        contactsAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position == 0){
                    // new friend
                    Intent intent = new Intent(getActivity(), NewFriendActivity.class);
                    startActivity(intent);
                }else{
                    // contact item click
                }
            }
        });
    }


    private void initView() {
        recycler_view = rootView.findViewById(R.id.recycler_view_contacts);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        updateContactsView(null);
    }
}

package com.ty.web3_mq.interfaces;

import com.ty.web3_mq.http.beans.TopicBean;

import java.util.ArrayList;

public interface PublishTopicMessageCallback {
    void onSuccess();
    void onFail(String error);
}

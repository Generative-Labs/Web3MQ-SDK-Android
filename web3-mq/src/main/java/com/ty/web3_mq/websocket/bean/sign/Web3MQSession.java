package com.ty.web3_mq.websocket.bean.sign;

import java.util.HashMap;

public class Web3MQSession {
    /// 自己的 topicId
    public String selfTopic;

    /// 对方的 topicId
    public String peerTopic;

    /// 自己方信息，包括 publicKey 和 App 相关信息
    public Participant selfParticipant;

    /// 对方信息，包括 publicKey 和 App 相关信息
    public Participant peerParticipant;

    /// 过期时间
    public long expiryDate;

    /// 功能集 Namespace 相关参考 https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-25.md
    public String[] namespaces;

    public HashMap<String,SignConversation> signConversationMap;
}

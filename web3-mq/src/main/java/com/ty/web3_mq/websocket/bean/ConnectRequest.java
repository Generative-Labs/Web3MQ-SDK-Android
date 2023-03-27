package com.ty.web3_mq.websocket.bean;

import java.util.ArrayList;
import java.util.List;

public class ConnectRequest {
    public String topic;
    public String id;
    public String jsonrpc;
    public String method;
    public String publicKey;
    public String name;
    public String description;
    public String url;
    public List<String> icons;
    public String redirect;
    public String expiry;
}
package com.ty.web3_mq.websocket;

public class WebsocketConfig {
    public static final String WS_PROTOCOL = "wss";
    public static final String WS_DEV_HOST_URL = "dev-ap-jp-1.web3mq.com";
    public static final String WS_TEST_NET_HOST_URL = "testnet-ap-jp-1.web3mq.com";
    public static final String WS_URL_DEV = WebsocketConfig.WS_PROTOCOL+"://"+ WebsocketConfig.WS_DEV_HOST_URL +"/messages";
    public static final String WS_URL_TEST_NET = WebsocketConfig.WS_PROTOCOL+"://"+ WebsocketConfig.WS_TEST_NET_HOST_URL +"/messages";

    // ping
    public static final byte PbTypePingCommand = (byte) 0b10000000;
    public static final byte PbTypePongCommand = (byte) 0b10000001;
    // connect to node
    public static final byte PbTypeConnectReqCommand = 0b00000010;
    public static final byte PbTypeConnectRespCommand = 0b00000011;

    // normally message
    public static final byte PbTypeMessage = 0b00010000;
    public static final byte PbTypeMessageStatusResp = 0b00010101;

    // notification
    public static final byte PbTypeNotificationListResp = 0b00010100;

    // connect to bridge
    public static final byte PbTypeWeb3MQBridgeConnectCommand = 100;
    public static final byte PbTypeWeb3MQBridgeConnectResp = 101;

    public static final int category = 10;


}

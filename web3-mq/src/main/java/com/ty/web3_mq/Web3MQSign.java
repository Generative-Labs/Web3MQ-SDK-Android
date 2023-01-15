package com.ty.web3_mq;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import com.goterl.lazysodium.LazySodiumAndroid;
import com.goterl.lazysodium.SodiumAndroid;
import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.Sign;
import com.goterl.lazysodium.utils.KeyPair;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.BridgeMessageCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.interfaces.OnSignRequestMessageCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.websocket.MessageManager;
import com.ty.web3_mq.websocket.WebsocketConfig;
import com.ty.web3_mq.websocket.bean.BridgeMessage;
import com.ty.web3_mq.websocket.bean.BridgeMessageContent;
import com.ty.web3_mq.websocket.bean.BridgeMessageProposer;
import com.ty.web3_mq.websocket.bean.BridgeMessageWalletInfo;

import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.whispersystems.curve25519.Curve25519;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import web3mq.Message;

public class Web3MQSign {
    private static final String TAG = "Web3MQSign";
    private volatile static Web3MQSign web3MQSign;
    private KeyPair ed25519KeyPair, x25519KeyPair;
    private byte[] ed25519PrvKey = new byte[32];
    private String my_topic_id,target_topic_id;
    private Gson gson;
    private OnSignRequestMessageCallback onSignRequestMessageCallback;
    private OnConnectResponseCallback onConnectResponseCallback;
    private OnSignResponseMessageCallback onSignResponseMessageCallback;
    public static Web3MQSign getInstance() {
        if (null == web3MQSign) {
            synchronized (Web3MQSign.class) {
                if (null == web3MQSign) {
                    web3MQSign = new Web3MQSign();

                }
            }
        }
        return web3MQSign;
    }

    private Web3MQSign(){
        gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    public void init(String dAppID, BridgeConnectCallback callback){
        generate25519KeyPair();
        if(Web3MQClient.getInstance().getNodeId()==null || !Web3MQClient.getInstance().getSocketClient().isOpen()){
            Log.e(TAG,"websocket not connect");
            return;
        }
        MessageManager.getInstance().setBridgeConnectCallback(callback);
        this.my_topic_id = dAppID + "@" + CryptoUtils.SHA1_ENCODE(ed25519KeyPair.getPublicKey().getAsBytes());
        Web3MQClient.getInstance().sendBridgeConnectCommand(dAppID, my_topic_id);

        MessageManager.getInstance().setBridgeMessageCallback(new BridgeMessageCallback() {
            @Override
            public void onBridgeMessage(String comeFrom, BridgeMessage bridgeMessage) {
                switch (bridgeMessage.content.action){
                    case BridgeMessage.ACTION_SIGN_REQUEST:
                        if(onSignRequestMessageCallback!=null){
                            onSignRequestMessageCallback.onSignRequestMessage(bridgeMessage.content.proposer, bridgeMessage.content.address, bridgeMessage.content.signRaw);
                        }
                        break;
                    case BridgeMessage.ACTION_CONNECT_RESPONSE:
                        target_topic_id = comeFrom;
                        if(onConnectResponseCallback!=null){
                            if(bridgeMessage.content.approve){
                                // connect approve
                                onConnectResponseCallback.onApprove(bridgeMessage.content.walletInfo);
                            }else{
                                //connect reject
                                onConnectResponseCallback.onReject();
                            }
                        }
                        break;
                    case BridgeMessage.ACTION_SIGN_RESPONSE:
                        if(onSignResponseMessageCallback!=null){
                            if(bridgeMessage.content.approve){
                                onSignResponseMessageCallback.onApprove(bridgeMessage.content.signature);
                            }else{
                                onSignResponseMessageCallback.onReject();
                            }
                        }
                        break;
                }
            }
        });
    }

    public void setTargetTopicID(String topicID){
        target_topic_id = topicID;
        Log.i(TAG,"setTargetTopicID:"+topicID);
    }

    public String generateConnectDeepLink(String icon_url, String website, String redirect){
        if(my_topic_id == null || ed25519KeyPair == null){
            Log.e(TAG,"Web3MQSign not init");
            return null;
        }
        String deepLink = "web3mq://?action=connect" +
                "&topicId="+ my_topic_id +
                "&ed25519Pubkey="+CryptoUtils.bytesToHexString(ed25519KeyPair.getPublicKey().getAsBytes())+
                "&bridge="+WebsocketConfig.WS_URL+
                "&iconUrl="+icon_url+
                "&website="+website+
                "&redirect="+redirect;
        Log.i(TAG,"generateDeepLink:"+deepLink);
        return deepLink;
    }

    public String generateSignDeepLink(){
        if(my_topic_id == null || ed25519KeyPair == null){
            Log.e(TAG,"Web3MQSign not init");
            return null;
        }
        String deepLink = "web3mq://?action=sign";
        Log.i(TAG,"generateDeepLink:"+deepLink);
        return deepLink;
    }

    public void sendSignRequest(BridgeMessageProposer proposer, String signRaw, String address, boolean needStore){
        BridgeMessage message = new BridgeMessage();
        message.content = new BridgeMessageContent();
        message.content.action = BridgeMessage.ACTION_SIGN_REQUEST;
        message.content.proposer = proposer;
        message.content.signRaw = signRaw;
        message.content.address = address;
        sendBridgeMessage(message, needStore);
    }

    /**
     * wallet reply connect request
     */
    public void sendConnectResponse(boolean approve, BridgeMessageWalletInfo walletInfo, boolean needStore){
        BridgeMessage message = new BridgeMessage();
        message.content = new BridgeMessageContent();
        message.content.action = BridgeMessage.ACTION_CONNECT_RESPONSE;
        message.content.approve = approve;
        message.content.walletInfo = walletInfo;
        sendBridgeMessage(message, needStore);
    }

    public void sendSignResponse(boolean approve, String signature, boolean needStore){
        BridgeMessage message = new BridgeMessage();
        message.content = new BridgeMessageContent();
        message.content.action = BridgeMessage.ACTION_SIGN_RESPONSE;
        message.content.approve = approve;
        if(signature!=null){
            message.content.signature = signature;
        }
        sendBridgeMessage(message, needStore);
    }


    public void setOnSignRequestMessageCallback(OnSignRequestMessageCallback onSignRequestMessageCallback){
        this.onSignRequestMessageCallback = onSignRequestMessageCallback;
    }

    public void setOnSignResponseMessageCallback(OnSignResponseMessageCallback onSignResponseMessageCallback){
        this.onSignResponseMessageCallback = onSignResponseMessageCallback;
    }

    public void setOnConnectResponseCallback(OnConnectResponseCallback callback){
        this.onConnectResponseCallback = callback;
    }

    public void sendBridgeMessage(BridgeMessage message, boolean needStore){
        if(Web3MQClient.getInstance().getNodeId()==null || !Web3MQClient.getInstance().getSocketClient().isOpen()){
            Log.e(TAG,"websocket not connect");
            return;
        }

        Log.i(TAG,"-----sendBridgeMessage-----");
        try {
            long timestamp = System.currentTimeMillis();
            String node_id = Web3MQClient.getInstance().getNodeId();
            String msg_str = gson.toJson(message);
            Log.i(TAG,"BridgeMessage: "+msg_str);
            String msg_id = GenerateMessageID(my_topic_id, target_topic_id, timestamp, msg_str.getBytes());
            String signContent = msg_id + my_topic_id + target_topic_id + node_id + timestamp;
            Log.i(TAG,"signContent:"+signContent);
            String sign = Ed25519.ed25519Sign(Ed25519.bytesToHexString(ed25519PrvKey),signContent.getBytes());
            Message.Web3MQMessage.Builder builder= Message.Web3MQMessage.newBuilder();
            builder.setNodeId(node_id);
            Log.i(TAG,"node_id:"+node_id);
            builder.setCipherSuite("NONE");
            builder.setPayloadType("application/json");
            builder.setFromSign(sign);
            Log.i(TAG,"sign:"+sign);
            builder.setTimestamp(timestamp);
            Log.i(TAG,"timestamp:"+timestamp);
            builder.setMessageId(msg_id);
            Log.i(TAG,"msg_id:"+msg_id);
            builder.setVersion(1);
            builder.setComeFrom(my_topic_id);
            Log.i(TAG,"comfrom:"+my_topic_id);
            builder.setContentTopic(target_topic_id);
            Log.i(TAG,"topic_id:"+ target_topic_id);
            builder.setNeedStore(needStore);
            Log.i(TAG,"needStore:"+needStore);
            builder.setPayload(ByteString.copyFrom(msg_str.getBytes()));
            Log.i(TAG,"payload:"+msg_str);
            String base64PubKey = Base64.encodeToString(ed25519KeyPair.getPublicKey().getAsBytes(),Base64.NO_WRAP);
            Log.i(TAG,"base64PubKey:"+base64PubKey);
            builder.setValidatePubKey(base64PubKey);

            builder.setMessageType("Web3MQ/bridge");
            byte[] sendMessageBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypeMessage, builder.build().toByteArray());
            Web3MQClient.getInstance().getSocketClient().send(sendMessageBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String GenerateMessageID(String user_id, String topic, long timestamp, byte[] payload){
        MessageDigest md = new SHA3.Digest224();
        md.update(user_id.getBytes());
        md.update(topic.getBytes());
        md.update((""+timestamp).getBytes());
        md.update(payload);
        byte[] messageDigest = md.digest();
        BigInteger no = new BigInteger(1, messageDigest);
        return no.toString(16);
    }

    /**
     *
     */
    private void generate25519KeyPair(){
        try {
            LazySodiumAndroid ls = new LazySodiumAndroid(new SodiumAndroid());
            Sign.Lazy sign = ls;
            ed25519KeyPair = sign.cryptoSignKeypair();
            ed25519PrvKey = Arrays.copyOfRange(ed25519KeyPair.getSecretKey().getAsBytes(),0,32);
            x25519KeyPair = sign.convertKeyPairEd25519ToCurve25519(ed25519KeyPair);
        } catch (SodiumException e) {
            e.printStackTrace();
        }
    }

    public void generate25519KeyPairTest(){
//        try {
//            LazySodiumAndroid ls = new LazySodiumAndroid(new SodiumAndroid());
//            Sign.Lazy sign = ls;
//            ed25519KeyPair = sign.cryptoSignKeypair();
//
//            x25519KeyPair = sign.convertKeyPairEd25519ToCurve25519(ed25519KeyPair);
//            Curve25519 cipher_x25519 = Curve25519.getInstance(Curve25519.BEST);
//            byte[] shareKey = cipher_x25519.calculateAgreement(x25519KeyPair.getPublicKey().getAsBytes(),x25519KeyPair.getSecretKey().getAsBytes());
//            Log.i(TAG,"shareKey length:"+shareKey.length);
//        } catch (SodiumException e) {
//            e.printStackTrace();
//        }
//        Boolean result = Ed25519.ed25519VerifySign(Ed25519.bytesToHexString(Base64.decode("R3HLn1qT+4PhzehTEzYhyCA550sPFmj5YYdm+W7JCII=",Base64.NO_WRAP)),
//                "6fd5b2bc2372bbf2198f037b3518b0501c483e80e449b4bb157fc7d4web3MQ_test_wallet:wallet@47953B5BCB93CC2EC2432BF8FAF71E50421F1BB0web3MQ_test_dapp:dapp@41910F665A5DCF798027E040C8584C7C1DDF504112D3KooWAxLmzfTLj2oS7X9DV1fC35E5qSMXiJP5xcM2NpvHMTwZ1673586359629",
//                "u/vCIT5FAtKa0EL/fGP87v7TF593b6LjpugysZUzJh9pE0SFCXEF8uU2VjN9vZli59PefyDclpSKuUgIFXzoCw==");
//        Log.i(TAG,"result:"+result);
    }

    private byte[] encryptionContent(byte[] publicKey, byte[] privateKey, byte[] content){
        Curve25519 cipher_x25519 = Curve25519.getInstance(Curve25519.BEST);
        byte[] shareKey = cipher_x25519.calculateAgreement(publicKey,privateKey);
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA384Digest());
        hkdf.init(new HKDFParameters(shareKey, "".getBytes(), "".getBytes()));
        byte[] prk = new byte[32];
        hkdf.generateBytes(prk, 0, 32);
        //AES-GCM
        byte[] iv = new byte[12];
        SecretKeySpec key = new SecretKeySpec(prk, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 创建 GCM 参数规范
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); // 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key, spec); // 初始化密码器
            byte[] ciphertext = cipher.doFinal(content);
            return ciphertext;
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decryptionContent(byte[] publicKey, byte[] privateKey, byte[] content){
        Curve25519 cipher_x25519 = Curve25519.getInstance(Curve25519.BEST);
        byte[] shareKey = cipher_x25519.calculateAgreement(publicKey,privateKey);
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA384Digest());
        hkdf.init(new HKDFParameters(shareKey, "".getBytes(), "".getBytes()));
        byte[] prk = new byte[32];
        hkdf.generateBytes(prk, 0, 32);
        //AES-GCM
        byte[] iv = new byte[12];
        SecretKeySpec key = new SecretKeySpec(prk, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 创建 GCM 参数规范
        try {
            Cipher decipher = Cipher.getInstance("AES/GCM/NoPadding"); // 创建密码器
            decipher.init(Cipher.DECRYPT_MODE, key, spec); // 初始化密码器
            byte[] ciphertext = decipher.doFinal(content);
            return ciphertext;
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
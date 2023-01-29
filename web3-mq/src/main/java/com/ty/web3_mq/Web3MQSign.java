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

import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
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
    private KeyPair ed25519KeyPair;
//    private KeyPair x25519KeyPair;
    private byte[] ed25519PrvKey = new byte[32];
    private String my_topic_id,target_topic_id;
    private Gson gson;
    private OnSignRequestMessageCallback onSignRequestMessageCallback;
    private OnConnectResponseCallback onConnectResponseCallback;
    private OnSignResponseMessageCallback onSignResponseMessageCallback;
    private String targetPubKey;
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
        this.my_topic_id = "bridge:"+CryptoUtils.SHA1_ENCODE((dAppID + "@" + Base64.encodeToString(ed25519KeyPair.getPublicKey().getAsBytes(),Base64.NO_WRAP)));
        Web3MQClient.getInstance().sendBridgeConnectCommand(dAppID, my_topic_id);

        MessageManager.getInstance().setBridgeMessageCallback(new BridgeMessageCallback() {
            @Override
            public void onBridgeMessage(String comeFrom, BridgeMessage bridgeMessage) {

                targetPubKey = bridgeMessage.publicKey;
                String json_content = new String(decryptionContent(Ed25519.hexStringToBytes(targetPubKey),ed25519PrvKey,Base64.decode(bridgeMessage.content,Base64.NO_WRAP)));
                BridgeMessageContent content = gson.fromJson(json_content,BridgeMessageContent.class);
                Log.i(TAG,"BridgeMessageContent:"+content);
                switch (content.action){
                    case BridgeMessage.ACTION_SIGN_REQUEST:
                        if(onSignRequestMessageCallback!=null){
                            onSignRequestMessageCallback.onSignRequestMessage(content.proposer, content.address, content.signRaw, content.requestId,content.userInfo);
                        }
                        break;
                    case BridgeMessage.ACTION_CONNECT_RESPONSE:
                        target_topic_id = comeFrom;
                        if(onConnectResponseCallback!=null){
                            if(content.approve){
                                // connect approve
                                onConnectResponseCallback.onApprove(content.walletInfo);
                            }else{
                                //connect reject
                                onConnectResponseCallback.onReject();
                            }
                        }
                        break;
                    case BridgeMessage.ACTION_SIGN_RESPONSE:
                        if(onSignResponseMessageCallback!=null){
                            if(content.approve){
                                onSignResponseMessageCallback.onApprove(content.signature);
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

    public void setTargetPubKey(String ed25519Pubkey){
        this.targetPubKey = ed25519Pubkey;
        Log.i(TAG,"updateTargetPubKey:"+targetPubKey);
    }

    public void sendSignRequest(BridgeMessageProposer proposer, String signRaw, String address,String requestId,String userInfo, boolean needStore){
        if(targetPubKey==null){
            Log.e(TAG,"targetPubKey is null");
            return;
        }
        BridgeMessage message = new BridgeMessage();
        BridgeMessageContent content = new BridgeMessageContent();
        content.action = BridgeMessage.ACTION_SIGN_REQUEST;
        content.proposer = proposer;
        content.signRaw = signRaw;
        content.address = address;
        content.requestId = requestId;
        content.userInfo = userInfo;
        String json_content = gson.toJson(content);
        message.content = Base64.encodeToString(encryptionContent(Ed25519.hexStringToBytes(targetPubKey),ed25519PrvKey,json_content.getBytes()),Base64.NO_WRAP);
        message.publicKey = ed25519KeyPair.getPublicKey().getAsHexString();
        sendBridgeMessage(message, needStore);
    }

    /**
     * wallet reply connect request
     */
    public void sendConnectResponse(boolean approve, BridgeMessageWalletInfo walletInfo, boolean needStore){
        BridgeMessage message = new BridgeMessage();
        BridgeMessageContent content = new BridgeMessageContent();
        content.action = BridgeMessage.ACTION_CONNECT_RESPONSE;
        content.approve = approve;
        content.walletInfo = walletInfo;
        String json_content = gson.toJson(content);
        Log.i(TAG,"targetPubKey:"+targetPubKey+"");
        message.content = Base64.encodeToString(encryptionContent(Ed25519.hexStringToBytes(targetPubKey),ed25519PrvKey,json_content.getBytes()),Base64.NO_WRAP);
        message.publicKey = ed25519KeyPair.getPublicKey().getAsHexString();
        Log.i(TAG,"send pubKy:"+message.publicKey);
        sendBridgeMessage(message, needStore);
    }

    public void sendSignResponse(boolean approve, String signature,String requestId,String userInfo, boolean needStore){
        Log.i(TAG,"sendSignResponse requestId:"+requestId);
        Log.i(TAG,"sendSignResponse userInfo:"+userInfo);
        Log.i(TAG,"sendSignResponse approve:"+approve);
        Log.i(TAG,"sendSignResponse signature:"+signature);
        BridgeMessage message = new BridgeMessage();
        BridgeMessageContent content = new BridgeMessageContent();
        content.action = BridgeMessage.ACTION_SIGN_RESPONSE;
        content.approve = approve;
        content.requestId = requestId;
        content.userInfo = userInfo;
        if(signature!=null){
            content.signature = signature;
        }
        String json_content = gson.toJson(content);
        message.content = Base64.encodeToString(encryptionContent(Ed25519.hexStringToBytes(targetPubKey),ed25519PrvKey,json_content.getBytes()),Base64.NO_WRAP);
        message.publicKey = ed25519KeyPair.getPublicKey().getAsHexString();
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
            Log.i(TAG,"NodeId:"+node_id);
            builder.setCipherSuite("NONE");
            Log.i(TAG,"CipherSuite:"+"NONE");
            builder.setPayloadType("application/json");
            Log.i(TAG,"PayloadType:"+"application/json");
            builder.setFromSign(sign);
            Log.i(TAG,"FromSign:"+sign);
            builder.setTimestamp(timestamp);
            Log.i(TAG,"timestamp:"+timestamp);
            builder.setMessageId(msg_id);
            Log.i(TAG,"MessageId:"+msg_id);
            builder.setVersion(1);
            Log.i(TAG,"Version:"+1);
            builder.setComeFrom(my_topic_id);
            Log.i(TAG,"ComeFrom:"+my_topic_id);
            builder.setContentTopic(target_topic_id);
            Log.i(TAG,"ContentTopic:"+ target_topic_id);
            builder.setNeedStore(needStore);
            Log.i(TAG,"NeedStore:"+needStore);
            builder.setPayload(ByteString.copyFrom(msg_str.getBytes()));
            Log.i(TAG,"Payload:"+msg_str);
            String base64PubKey = Base64.encodeToString(ed25519KeyPair.getPublicKey().getAsBytes(),Base64.NO_WRAP);
            builder.setValidatePubKey(base64PubKey);
            Log.i(TAG,"ValidatePubKey:"+base64PubKey);
            builder.setMessageType("Web3MQ/bridge");
            Log.i(TAG,"MessageType:"+"Web3MQ/bridge");
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
//            x25519KeyPair = sign.convertKeyPairEd25519ToCurve25519(ed25519KeyPair);

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

    /**
     *
     * @param content
     * @return
     */
    private byte[] encryptionContent(byte[] ed25519PublicKey, byte[] ed25519PrivateKey, byte[] content){
        Log.i(TAG,"---------encrypt--------");
        Log.i(TAG,"ed25519PublicKey: "+Ed25519.bytesToHexString(ed25519PublicKey));
        Log.i(TAG,"ed25519PrivateKey: "+Ed25519.bytesToHexString(ed25519PrivateKey));
        Log.i(TAG,"content: "+new String(content));
        LazySodiumAndroid lazySodium = new LazySodiumAndroid(new SodiumAndroid());
        Sign.Native aNative = lazySodium;
        byte[] x25519PublicKey = new byte[32];
        aNative.convertPublicKeyEd25519ToCurve25519(x25519PublicKey, ed25519PublicKey);
        byte[] x25519PrivateKey = new byte[32];
        aNative.convertSecretKeyEd25519ToCurve25519(x25519PrivateKey, ed25519PrivateKey);
        X25519Agreement x25519Agreement = new X25519Agreement();
        byte[] shareKey = new byte[x25519Agreement.getAgreementSize()];
        x25519Agreement.init(new X25519PrivateKeyParameters(x25519PrivateKey));
        x25519Agreement.calculateAgreement(new X25519PublicKeyParameters(x25519PublicKey),shareKey,0);
        Log.i(TAG,"shareKey:"+Ed25519.bytesToHexString(shareKey));

        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA384Digest());
        hkdf.init(new HKDFParameters(shareKey, "".getBytes(), "".getBytes()));
        byte[] prk = new byte[32];
        hkdf.generateBytes(prk, 0, 32);
        Log.i(TAG,"SHA384 prk:"+Ed25519.bytesToHexString(prk));
        //AES-GCM
        String prk_base64 = Base64.encodeToString(prk,Base64.NO_WRAP);
        String iv_str = prk_base64.substring(0,16);
        Log.i(TAG,"iv_str length:"+iv_str.length());
        byte[] iv = Base64.decode(iv_str,Base64.NO_WRAP);
        Log.i(TAG,"iv length:"+iv.length);
        SecretKeySpec key = new SecretKeySpec(prk, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 创建 GCM 参数规范
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding"); // 创建密码器
            cipher.init(Cipher.ENCRYPT_MODE, key, spec); // 初始化密码器
            byte[] ciphertext = cipher.doFinal(content);
            Log.i(TAG,"final content base64:"+Base64.encodeToString(ciphertext,Base64.NO_WRAP));
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

    private byte[] decryptionContent(byte[] ed25519PublicKey, byte[] ed25519PrivateKey, byte[] content){
        Log.i(TAG,"---------decrypt--------");
        Log.i(TAG,"ed25519PublicKey: "+Ed25519.bytesToHexString(ed25519PublicKey));
        Log.i(TAG,"ed25519PrivateKey: "+Ed25519.bytesToHexString(ed25519PrivateKey));
        LazySodiumAndroid lazySodium = new LazySodiumAndroid(new SodiumAndroid());
        Sign.Native aNative = lazySodium;
        byte[] x25519PublicKey = new byte[32];
        aNative.convertPublicKeyEd25519ToCurve25519(x25519PublicKey, ed25519PublicKey);
        byte[] x25519PrivateKey = new byte[32];
        aNative.convertSecretKeyEd25519ToCurve25519(x25519PrivateKey, ed25519PrivateKey);
        X25519Agreement x25519Agreement = new X25519Agreement();
        byte[] shareKey = new byte[x25519Agreement.getAgreementSize()];
        x25519Agreement.init(new X25519PrivateKeyParameters(x25519PrivateKey));
        x25519Agreement.calculateAgreement(new X25519PublicKeyParameters(x25519PublicKey),shareKey,0);
        Log.i(TAG,"shareKey:"+Ed25519.bytesToHexString(shareKey));

        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA384Digest());
        hkdf.init(new HKDFParameters(shareKey, "".getBytes(), "".getBytes()));
        byte[] prk = new byte[32];
        hkdf.generateBytes(prk, 0, 32);
        Log.i(TAG,"SHA384 prk:"+Ed25519.bytesToHexString(prk));
        //AES-GCM
        String prk_base64 = Base64.encodeToString(prk,Base64.NO_WRAP);
        String iv_str = prk_base64.substring(0,16);
        Log.i(TAG,"iv_str length:"+iv_str.length());
        byte[] iv = Base64.decode(iv_str,Base64.NO_WRAP);
        Log.i(TAG,"iv length:"+iv.length);
        SecretKeySpec key = new SecretKeySpec(prk, "AES");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 创建 GCM 参数规范
        try {
            Cipher decipher = Cipher.getInstance("AES/GCM/NoPadding"); // 创建密码器
            decipher.init(Cipher.DECRYPT_MODE, key, spec); // 初始化密码器
            byte[] ciphertext = decipher.doFinal(content);
            Log.i(TAG,"content source:"+new String(ciphertext));
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

    public void test(byte[] ed25519PublicKey, byte[] ed25519PrivateKey) {
        LazySodiumAndroid lazySodium = new LazySodiumAndroid(new SodiumAndroid());
        Sign.Native aNative = lazySodium;
        byte[] x25519PublicKey = new byte[32];
        aNative.convertPublicKeyEd25519ToCurve25519(x25519PublicKey, ed25519PublicKey);
        byte[] x25519PrivateKey = new byte[32];
        aNative.convertSecretKeyEd25519ToCurve25519(x25519PrivateKey, ed25519PrivateKey);
        X25519Agreement x25519Agreement = new X25519Agreement();
        byte[] shareKey = new byte[x25519Agreement.getAgreementSize()];
        x25519Agreement.init(new X25519PrivateKeyParameters(x25519PrivateKey));
        x25519Agreement.calculateAgreement(new X25519PublicKeyParameters(x25519PublicKey),shareKey,0);
//        Log.i(TAG,"x25519PublicKey:"+Ed25519.bytesToHexString(x25519PublicKey));
//        Log.i(TAG,"x25519PrivateKey:"+Ed25519.bytesToHexString(x25519PrivateKey));
        Log.i(TAG,"shareKey:"+Ed25519.bytesToHexString(shareKey));
    }

    public void test1(byte[] x25519PublicKey, byte[] x25519PrivateKey) {
        LazySodiumAndroid lazySodium = new LazySodiumAndroid(new SodiumAndroid());
        byte[] shareKey = new byte[32];
        lazySodium.cryptoBoxBeforeNm(shareKey, x25519PublicKey, x25519PrivateKey);

//        Sign.Native aNative = lazySodium;

//        Curve25519 cipher_x25519 = Curve25519.getInstance(Curve25519.BEST);
//        byte[] shareKey = cipher_x25519.calculateAgreement(x25519PublicKey, x25519PrivateKey);
        Log.i(TAG,"x25519PublicKey:"+Ed25519.bytesToHexString(x25519PublicKey));
        Log.i(TAG,"x25519PrivateKey:"+Ed25519.bytesToHexString(x25519PrivateKey));
        Log.i(TAG,"shareKey:"+Ed25519.bytesToHexString(shareKey));
    }

    public void test2(byte[] x25519PublicKey, byte[] x25519PrivateKey) {
//        byte[] prv_key= new byte[x25519PrivateKey.length+x25519PublicKey.length];
//        System.arraycopy(x25519PrivateKey, 0, prv_key, 0, x25519PrivateKey.length);
//        System.arraycopy(x25519PublicKey, 0, prv_key, x25519PrivateKey.length, x25519PublicKey.length);

        Curve25519 cipher_x25519 = Curve25519.getInstance(Curve25519.BEST);
        byte[] shareKey = cipher_x25519.calculateAgreement(x25519PublicKey, x25519PrivateKey);
        Log.i(TAG,"x25519PublicKey:"+Ed25519.bytesToHexString(x25519PublicKey));
        Log.i(TAG,"x25519PrivateKey:"+Ed25519.bytesToHexString(x25519PrivateKey));
        Log.i(TAG,"shareKey:"+Ed25519.bytesToHexString(shareKey));
    }

    public void test3(byte[] x25519PublicKey, byte[] x25519PrivateKey) {
        X25519Agreement x25519Agreement = new X25519Agreement();
        byte[] shareKey = new byte[x25519Agreement.getAgreementSize()];
        x25519Agreement.init(new X25519PrivateKeyParameters(x25519PrivateKey));
        x25519Agreement.calculateAgreement(new X25519PublicKeyParameters(x25519PublicKey),shareKey,0);
        Log.i(TAG,"x25519PublicKey:"+Ed25519.bytesToHexString(x25519PublicKey));
        Log.i(TAG,"x25519PrivateKey:"+Ed25519.bytesToHexString(x25519PrivateKey));
        Log.i(TAG,"shareKey:"+Ed25519.bytesToHexString(shareKey));
    }
}
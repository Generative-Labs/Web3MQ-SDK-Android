package com.ty.web3_mq.websocket.bean.sign;
import android.os.CountDownTimer;
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
import com.ty.web3_mq.Web3MQClient;
import com.ty.web3_mq.interfaces.BridgeConnectCallback;
import com.ty.web3_mq.interfaces.BridgeMessageCallback;
import com.ty.web3_mq.interfaces.SendBridgeMessageCallback;
import com.ty.web3_mq.interfaces.OnConnectResponseCallback;
import com.ty.web3_mq.interfaces.OnSignRequestMessageCallback;
import com.ty.web3_mq.interfaces.OnSignResponseMessageCallback;
import com.ty.web3_mq.utils.CommonUtils;
import com.ty.web3_mq.utils.ConvertUtil;
import com.ty.web3_mq.utils.CryptoUtils;
import com.ty.web3_mq.utils.DateUtils;
import com.ty.web3_mq.utils.DefaultSPHelper;
import com.ty.web3_mq.utils.Ed25519;
import com.ty.web3_mq.utils.RandomUtils;
import com.ty.web3_mq.websocket.MessageManager;
import com.ty.web3_mq.websocket.WebsocketConfig;
import com.ty.web3_mq.websocket.bean.AuthorizationResponseSuccessData;
import com.ty.web3_mq.websocket.bean.BridgeMessage;
import com.ty.web3_mq.websocket.bean.BridgeMessageContent;
import com.ty.web3_mq.websocket.bean.BridgeMessageMetadata;
import com.ty.web3_mq.websocket.bean.ConnectRequest;
import com.ty.web3_mq.websocket.bean.ConnectSuccessResponse;
import com.ty.web3_mq.websocket.bean.ErrorResponse;
import com.ty.web3_mq.websocket.bean.Namespaces;
import com.ty.web3_mq.websocket.bean.ResponseErrorData;
import com.ty.web3_mq.websocket.bean.SignRequest;
import com.ty.web3_mq.websocket.bean.SignSuccessResponse;

import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.jcajce.provider.digest.SHA3;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Gson gson;
    private OnSignRequestMessageCallback onSignRequestMessageCallback;
    private OnConnectResponseCallback onConnectResponseCallback;
    private OnSignResponseMessageCallback onSignResponseMessageCallback;
    private Sign.Native aNative;
    private Web3MQSession currentSession;
    private String dAppID;
    //session有效期
    private long session_valid_duration = 1000*60*60*24;
    //request有效期
    private long request_valid_duration = 1000*60*60*3;

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
        LazySodiumAndroid lazySodium = new LazySodiumAndroid(new SodiumAndroid());
        aNative = lazySodium;
        currentSession = new Web3MQSession();
        currentSession.selfParticipant = new Participant();
        currentSession.peerParticipant = new Participant();
    }

    public void init(String dAppID, BridgeConnectCallback callback){
        if(Web3MQClient.getInstance().getNodeId()==null || !Web3MQClient.getInstance().getSocketClient().isOpen()){
            Log.e(TAG,"websocket not connect");
            return;
        }
        this.dAppID = dAppID;

        generate25519KeyPair();
        MessageManager.getInstance().setBridgeConnectCallback(callback);
        currentSession.selfTopic = "bridge:"+CryptoUtils.SHA1_ENCODE((dAppID + "@" + Base64.encodeToString(CryptoUtils.hexStringToBytes(currentSession.selfParticipant.ed25519Pubkey),Base64.NO_WRAP)));
        Web3MQClient.getInstance().sendBridgeConnectCommand(dAppID, currentSession.selfTopic);

        MessageManager.getInstance().setBridgeMessageCallback(new BridgeMessageCallback() {
            @Override
            public void onBridgeMessage(String comeFrom,String publicKey, String content) {
                currentSession.peerParticipant.ed25519Pubkey = publicKey;
                String hex_prv = currentSession.selfParticipant.ed25519PrvKey;
                Log.i(TAG,"hex_prv:"+hex_prv);
                String json_content = new String(decryptionContent(Ed25519.hexStringToBytes(publicKey),CryptoUtils.hexStringToBytes(currentSession.selfParticipant.ed25519PrvKey),Base64.decode(content,Base64.NO_WRAP)));
                BridgeMessageContent bridgeMessageContent = ConvertUtil.convertJsonToBridgeMessageContent(json_content,gson);
                switch (bridgeMessageContent.type){
                    //dapp
                    case BridgeMessageContent.TYPE_CONNECT_SUCCESS_RESPONSE:
                        currentSession.peerTopic = comeFrom;
                        ConnectSuccessResponse connectSuccessResponse = (ConnectSuccessResponse) bridgeMessageContent.content;
                        DefaultSPHelper.getInstance().appendSession(currentSession);
                        // TODO connect request是否要加入到session request中？
                        if(onConnectResponseCallback!=null){
                            onConnectResponseCallback.onApprove(connectSuccessResponse.result.metadata,connectSuccessResponse.getETHAddress());
                        }
                        break;
                    //dapp
                    case BridgeMessageContent.TYPE_CONNECT_ERROR_RESPONSE:
                        if(onConnectResponseCallback!=null){
                            onConnectResponseCallback.onReject();
                        }
                        break;
                    //wallet
                    case BridgeMessageContent.TYPE_SIGN_REQUEST:
                        Web3MQSession session = DefaultSPHelper.getInstance().getSession(currentSession.peerTopic);
                        SignRequest signRequest = (SignRequest) bridgeMessageContent.content;
                        DefaultSPHelper.getInstance().appendSignRequest(session.peerTopic,signRequest.id,signRequest);
                        if(onSignRequestMessageCallback!=null){
                            onSignRequestMessageCallback.onSignRequestMessage(signRequest.id,currentSession.peerParticipant, signRequest.getAddress(), signRequest.getSignRaw());
                        }
                        break;
                    //dapp
                    case BridgeMessageContent.TYPE_SIGN_SUCCESS_RESPONSE:
                        SignSuccessResponse signSuccessResponse = (SignSuccessResponse) bridgeMessageContent.content;
                        DefaultSPHelper.getInstance().appendSignSuccessResponse(currentSession.peerTopic,signSuccessResponse.id,signSuccessResponse);
                        if(onSignResponseMessageCallback!=null){
                            onSignResponseMessageCallback.onApprove(signSuccessResponse.result);
                        }
                        break;
                    //dapp
                    case BridgeMessageContent.TYPE_SIGN_ERROR_RESPONSE:
                        ErrorResponse errorResponse = (ErrorResponse) bridgeMessageContent.content;
                        DefaultSPHelper.getInstance().appendSignErrorResponse(currentSession.peerTopic,errorResponse.id,errorResponse);
                        if(onSignResponseMessageCallback!=null){
                            onSignResponseMessageCallback.onReject();
                        }
                        break;
                }
            }
        });
    }

    public ArrayList<Web3MQSession> getSessionList(){
        return DefaultSPHelper.getInstance().getSessionList();
    }

    public void switchSession(String dAppID,Web3MQSession session){
        this.dAppID = dAppID;
        currentSession = session;
        Web3MQClient.getInstance().sendBridgeConnectCommand(dAppID, currentSession.selfTopic);
    }

    public void reconnect(){
        Web3MQClient.getInstance().sendBridgeConnectCommand(dAppID, currentSession.selfTopic);
    }

    public SignRequest checkPendingRequest(Web3MQSession web3MQSession){
        if(web3MQSession.signConversationMap == null){
            return null;
        }
        for(Map.Entry<String, SignConversation> entry : web3MQSession.signConversationMap.entrySet()) {
            SignConversation value = entry.getValue();
            if(value.request!=null && value.successResponse==null && value.errorResponse==null){
                return value.request;
            }
        }
        return null;
    }

    public Web3MQSession getLastSession(){
        ArrayList<Web3MQSession> sessionList = DefaultSPHelper.getInstance().getSessionList();
        if(sessionList == null || sessionList.size() == 0){
            return null;
        }
        return sessionList.get(0);
    }

    public Web3MQSession getCurrentSession(){
        return currentSession;
    }

    public boolean initialized(){
        return currentSession.selfTopic!=null && currentSession.selfParticipant.ed25519Pubkey!=null && currentSession.selfParticipant.ed25519PrvKey!=null;
    }

    public void setTargetTopicID(String topicID){
        currentSession.peerTopic = topicID;
        Log.i(TAG,"setTargetTopicID:"+topicID);
    }

    public String generateConnectDeepLink(String icon_url, String website, String redirect){
        if(currentSession.selfTopic == null || currentSession.selfParticipant == null){
            Log.e(TAG,"Web3MQSign not init");
            return null;
        }
        ConnectRequest request = new ConnectRequest();
        request.topic = URLEncoder.encode(currentSession.selfTopic);
        request.id = System.currentTimeMillis()+""+ RandomUtils.random4Number();
        request.jsonrpc = "2.0";
        request.name = "";
        request.description = "";
        if(icon_url!=null){
            List<String> icons = new ArrayList<>();
            icons.add(URLEncoder.encode(icon_url));
            request.icons = icons;
        }
        request.redirect = URLEncoder.encode(redirect);
        request.url = URLEncoder.encode(website);
        request.method = "provider_authorization";
        request.publicKey = URLEncoder.encode(currentSession.selfParticipant.ed25519Pubkey);
        request.expiry = DateUtils.getISOOffsetTime(System.currentTimeMillis()+request_valid_duration);
        return ConvertUtil.convertConnectRequestToDeepLink(request);
    }

    public String generateSignDeepLink(){
        if(currentSession.selfTopic == null || currentSession.selfParticipant == null){
            Log.e(TAG,"Web3MQSign not init");
            return null;
        }
        String deepLink = "web3mq://?action=sign";
        Log.i(TAG,"generateDeepLink:"+deepLink);
        return deepLink;
    }

    public void setTargetPubKey(String ed25519Pubkey){
        currentSession.peerParticipant.ed25519Pubkey = ed25519Pubkey;
        Log.i(TAG,"updateTargetPubKey:"+ed25519Pubkey);
    }

    //dapp TODO
    public void sendSignRequest(String signRaw, String address, boolean needStore, SendBridgeMessageCallback callback){
        if(currentSession.peerParticipant.ed25519Pubkey==null){
            Log.e(TAG,"targetPubKey is null");
            return;
        }
        BridgeMessage message = new BridgeMessage();
        SignRequest request = new SignRequest();
        request.id = System.currentTimeMillis()+ "" + RandomUtils.random4Number();
        request.jsonrpc = "2.0";
        request.method = "personal_sign";
        request.params = new ArrayList<>();
        request.params.add(signRaw);
        request.params.add(address);
        String json_content = gson.toJson(request);
        message.content = Base64.encodeToString(encryptionContent(Ed25519.hexStringToBytes(currentSession.peerParticipant.ed25519Pubkey)
                ,Ed25519.hexStringToBytes(currentSession.selfParticipant.ed25519PrvKey),json_content.getBytes()),Base64.NO_WRAP);
        message.publicKey = currentSession.selfParticipant.ed25519Pubkey;
        sendBridgeMessage(message, needStore, callback);
        DefaultSPHelper.getInstance().appendSignRequest(currentSession.peerTopic,request.id,request);
    }

    //wallet
    public void sendConnectResponse(String id,String address, boolean approve, BridgeMessageMetadata walletInfo, boolean needStore, SendBridgeMessageCallback callback){
        BridgeMessage message = new BridgeMessage();
        String json_content;
        if(approve){
            ConnectSuccessResponse connectSuccessResponse = new ConnectSuccessResponse();
            connectSuccessResponse.id = id;
            connectSuccessResponse.jsonrpc = "2.0";
            connectSuccessResponse.method = "provider_authorization";
            connectSuccessResponse.result = new AuthorizationResponseSuccessData();
            connectSuccessResponse.result.metadata = walletInfo;
            connectSuccessResponse.result.sessionNamespaces = new HashMap<>();
            Namespaces namespaces = new Namespaces();
            namespaces.chains = new ArrayList<>();
            namespaces.chains.add("eip155:1");
            namespaces.events = new ArrayList<>();
            namespaces.events.add("personal_sign");
            namespaces.methods = new ArrayList<>();
            namespaces.accounts = new ArrayList<>();
            namespaces.accounts.add("eip155:1:"+address);
            connectSuccessResponse.result.sessionNamespaces.put("eip155",namespaces);
            json_content = gson.toJson(connectSuccessResponse);
        }else{
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.id = id;
            errorResponse.jsonrpc = "2.0";
            errorResponse.method = "provider_authorization";
            errorResponse.error = new ResponseErrorData();
            errorResponse.error.code = 5001;
            errorResponse.error.message = "User disapproved requested methods";
            json_content = gson.toJson(errorResponse);
        }

        Log.i(TAG,"targetPubKey:"+currentSession.peerParticipant.ed25519Pubkey+"");
        message.content = Base64.encodeToString(encryptionContent(Ed25519.hexStringToBytes(currentSession.peerParticipant.ed25519Pubkey),
                Ed25519.hexStringToBytes(currentSession.selfParticipant.ed25519PrvKey),json_content.getBytes()),Base64.NO_WRAP);
        message.publicKey = currentSession.selfParticipant.ed25519Pubkey;
        Log.i(TAG,"send pubKy:"+message.publicKey);
        sendBridgeMessage(message, needStore,callback);
    }

    //wallet
    public void sendSignResponse(String id, boolean approve, String signature, boolean needStore, SendBridgeMessageCallback callback){
        Log.i(TAG,"sendSignResponse approve:"+approve);
        Log.i(TAG,"sendSignResponse signature:"+signature);
        BridgeMessage message = new BridgeMessage();
        String json_content;
        if(approve){
            SignSuccessResponse response = new SignSuccessResponse();
            response.id = id;
            response.jsonrpc = "2.0";
            response.method = "personal_sign";
            response.result = signature;
            json_content = gson.toJson(response);
            DefaultSPHelper.getInstance().appendSignSuccessResponse(currentSession.peerTopic,id,response);
        }else{
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.id = id;
            errorResponse.jsonrpc = "2.0";
            errorResponse.method = "personal_sign";
            ResponseErrorData errorData = new ResponseErrorData();
            errorData.code = 5001;
            errorData.message = "User disapproved requested methods";
            errorResponse.error = errorData;
            json_content = gson.toJson(errorResponse);
            DefaultSPHelper.getInstance().appendSignErrorResponse(currentSession.peerTopic,id,errorResponse);
        }
        message.content = Base64.encodeToString(encryptionContent(CryptoUtils.hexStringToBytes(currentSession.peerParticipant.ed25519Pubkey),
                CryptoUtils.hexStringToBytes(currentSession.selfParticipant.ed25519PrvKey),json_content.getBytes()),Base64.NO_WRAP);
        message.publicKey = currentSession.selfParticipant.ed25519Pubkey;
        sendBridgeMessage(message, needStore, callback);
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

    private void sendBridgeMessage(BridgeMessage message, boolean needStore, SendBridgeMessageCallback callback){
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
            String msg_id = GenerateMessageID(currentSession.selfTopic, currentSession.peerTopic, timestamp, msg_str.getBytes());
            Log.i(TAG,"msg_id:"+msg_id);
            String signContent = msg_id + currentSession.selfTopic + currentSession.peerTopic + node_id + timestamp;
            Log.i(TAG,"signContent:"+signContent);
            String sign = Ed25519.ed25519Sign(currentSession.selfParticipant.ed25519PrvKey,signContent.getBytes());
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
            builder.setComeFrom(currentSession.selfTopic);
            Log.i(TAG,"ComeFrom:"+currentSession.selfTopic);
            builder.setContentTopic(currentSession.peerTopic);
            Log.i(TAG,"ContentTopic:"+ currentSession.peerTopic);
            builder.setNeedStore(needStore);
            Log.i(TAG,"NeedStore:"+needStore);
            builder.setPayload(ByteString.copyFrom(msg_str.getBytes()));
            Log.i(TAG,"Payload:"+msg_str);
            String base64PubKey = Base64.encodeToString(CryptoUtils.hexStringToBytes(currentSession.selfParticipant.ed25519Pubkey),Base64.NO_WRAP);
            builder.setValidatePubKey(base64PubKey);
            Log.i(TAG,"ValidatePubKey:"+base64PubKey);
            builder.setMessageType("Web3MQ/bridge");
            Log.i(TAG,"MessageType:"+"Web3MQ/bridge");
            byte[] sendMessageBytes = CommonUtils.appendPrefix(WebsocketConfig.category, WebsocketConfig.PbTypeMessage, builder.build().toByteArray());
            Web3MQClient.getInstance().getSocketClient().send(sendMessageBytes);
            MessageManager.getInstance().addSendBridgeMessageCallback(msg_id,callback);
            new CountDownTimer(request_valid_duration,request_valid_duration){

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if(callback!=null){
                        callback.onTimeout();
                        MessageManager.getInstance().removeSendBridgeMessageCallback(msg_id);
                    }
                }
            };

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
            KeyPair ed25519KeyPair = sign.cryptoSignKeypair();
            byte[] ed25519PrvKey = Arrays.copyOfRange(ed25519KeyPair.getSecretKey().getAsBytes(),0,32);
            currentSession.selfParticipant.ed25519Pubkey = CryptoUtils.bytesToHexString(ed25519KeyPair.getPublicKey().getAsBytes());
            currentSession.selfParticipant.ed25519PrvKey = CryptoUtils.bytesToHexString(ed25519PrvKey);
            Log.i(TAG,"generate25519KeyPair prv_key:"+currentSession.selfParticipant.ed25519PrvKey);
        } catch (SodiumException e) {
            e.printStackTrace();
        }
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


    public void walletBuildAndSaveSession(ConnectRequest connectRequest){
        if(currentSession==null ||currentSession.selfParticipant==null){
            return;
        }
        currentSession.peerTopic = connectRequest.topic;
        if(connectRequest.icons!=null && connectRequest.icons.size()>0){
            currentSession.peerParticipant.iconUrl = connectRequest.icons.get(0);
        }
        currentSession.peerParticipant.website = connectRequest.url;
        currentSession.peerParticipant.redirect = connectRequest.redirect;
        DefaultSPHelper.getInstance().appendSession(currentSession);
    }
}
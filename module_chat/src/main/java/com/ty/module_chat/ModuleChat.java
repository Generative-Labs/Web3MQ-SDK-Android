package com.ty.module_chat;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;

public class ModuleChat {
    private ToNewMessageRequestListener toNewMessageRequestListener;
    private static volatile ModuleChat instance;
    public static synchronized ModuleChat getInstance() {
        if (instance == null) {
            instance = new ModuleChat();
        }
        return instance;
    }
    private ModuleChat(){}

    public void toMessageUI(String chat_type,String chat_id){
        ARouter.getInstance().build(RouterPath.CHAT_MESSAGE).withString(Constants.ROUTER_KEY_CHAT_TYPE,chat_type).withString(Constants.ROUTER_KEY_CHAT_ID,chat_id).navigation();
    }

    public void setToNewMessageRequestListener(ToNewMessageRequestListener toNewMessageRequestListener){
        this.toNewMessageRequestListener= toNewMessageRequestListener;
    }

    public ToNewMessageRequestListener getToNewMessageRequestListener(){
        return this.toNewMessageRequestListener;
    }

    public interface ToNewMessageRequestListener{
        void toRequestFollow(String userid);
    }
}

package com.ty.module_profile;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;

public class ModuleProfile {
    private static volatile ModuleProfile instance;
    private OnLogoutEvent onLogoutEvent;
    private OnChatEvent onChatEvent;
    public static synchronized ModuleProfile getInstance() {
        if (instance == null) {
            instance = new ModuleProfile();
        }
        return instance;
    }
    private ModuleProfile(){}

    public void setOnLogoutEvent(OnLogoutEvent onLogoutEvent){
        this.onLogoutEvent = onLogoutEvent;
    }

    public void setOnChatEvent(OnChatEvent onChatEvent){
        this.onChatEvent = onChatEvent;
    }

    public OnLogoutEvent getOnLogoutEvent(){
        return onLogoutEvent;
    }

    public OnChatEvent getOnChatEvent(){
        return onChatEvent;
    }

    public interface OnLogoutEvent{
        void onLogout();
    }

    public interface OnChatEvent{
        void onChat(String userid);
    }

    public void toOtherProfile(String userid){
        ARouter.getInstance().build(RouterPath.OTHER_PROFILE).withString(Constants.ROUTER_KEY_USER_ID,userid).navigation();
    }
}

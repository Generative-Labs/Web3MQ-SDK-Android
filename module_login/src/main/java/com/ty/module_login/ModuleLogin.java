package com.ty.module_login;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ty.common.config.Constants;
import com.ty.common.config.RouterPath;
import com.ty.module_login.config.LoginConfig;
import com.ty.module_login.config.UIConfigStart;
import com.ty.module_login.interfaces.LoginSuccessCallback;

public class ModuleLogin {
    private static volatile ModuleLogin instance;
    public static synchronized ModuleLogin getInstance() {
        if (instance == null) {
            instance = new ModuleLogin();
        }
        return instance;
    }
    private ModuleLogin(){}

    private LoginConfig loginConfig;

    private LoginSuccessCallback callback;

    public void setLoginConfig(LoginConfig loginConfig){
        this.loginConfig = loginConfig;
    }

    public LoginConfig getLoginConfig(){
        return loginConfig;
    }

    /**
     * 启动
     */
    public void launch(){
        if(loginConfig == null){
            loginConfig = new LoginConfig();
            loginConfig.uiConfigStart = new UIConfigStart();
        }
        ARouter.getInstance().build(RouterPath.LOGIN_START).withObject(Constants.ROUTER_KEY_UI_CONFIG_START,loginConfig.uiConfigStart).navigation();
    }

    public void setOnLoginSuccessCallback(LoginSuccessCallback callback){
        this.callback = callback;
    }

    public LoginSuccessCallback getOnLoginSuccessCallback(){
        return callback;
    }
}

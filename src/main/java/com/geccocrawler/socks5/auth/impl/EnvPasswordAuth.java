package com.geccocrawler.socks5.auth.impl;

import com.geccocrawler.socks5.auth.PasswordAuth;
import com.geccocrawler.utils.CommonUtils;

/**
 * 环境变量密码认证
 *
 * @author smilex
 * @date 2023/5/14/16:43
 */
@SuppressWarnings("unused")
public final class EnvPasswordAuth implements PasswordAuth {

    private static final EnvPasswordAuth ENV_PASSWORD_AUTH = new EnvPasswordAuth();

    public static EnvPasswordAuth getInstance() {
        return ENV_PASSWORD_AUTH;
    }

    private EnvPasswordAuth() {
    }

    private final String userName = CommonUtils.getEnv("userName", v -> v, null);
    private final String passWord = CommonUtils.getEnv("passWord", v -> v, null);

    @Override
    public boolean auth(String userName, String passWord) {
        return this.userName.equals(userName) && this.passWord.equals(passWord);
    }
}

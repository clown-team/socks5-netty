package com.geccocrawler.socks5.auth;

/**
 * 密码认证
 *
 * @author smilex
 */
public interface PasswordAuth {

    /**
     * 认证
     *
     * @param user     用户名
     * @param password 密码
     * @return 结果
     */
    boolean auth(String user, String password);
}

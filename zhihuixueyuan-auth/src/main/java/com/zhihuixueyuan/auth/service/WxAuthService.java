package com.zhihuixueyuan.auth.service;

import com.zhihuixueyuan.ucenter.model.po.XcUser;

/***
 * 微信扫码认证
 */
public interface WxAuthService {
    /***
     * 微信扫码认证，申请令牌并查询用户信息，对于未注册的用户还需将其信息保存至数据库完成自动注册
     */
    public XcUser wxAuth(String code);
}

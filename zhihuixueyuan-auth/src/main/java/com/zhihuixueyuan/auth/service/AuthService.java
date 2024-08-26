package com.zhihuixueyuan.auth.service;

import com.zhihuixueyuan.ucenter.model.dto.AuthParamsDto;
import com.zhihuixueyuan.ucenter.model.dto.XcUserExt;

/***
 * 各种认证模式对应的类的统一实现接口
 */
public interface AuthService {
    public XcUserExt execute(AuthParamsDto authParamsDto);
}

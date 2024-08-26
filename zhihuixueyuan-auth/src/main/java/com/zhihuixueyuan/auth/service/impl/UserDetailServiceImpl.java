package com.zhihuixueyuan.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhihuixueyuan.auth.service.AuthService;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.ucenter.mapper.XcMenuMapper;
import com.zhihuixueyuan.ucenter.mapper.XcUserMapper;
import com.zhihuixueyuan.ucenter.model.dto.AuthParamsDto;
import com.zhihuixueyuan.ucenter.model.dto.XcUserExt;
import com.zhihuixueyuan.ucenter.model.po.XcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/***
 * SpringSecurity中UserDetailsService接口的实现类，在SpringSecurity中会被使用，用于验证用户登录信息
 */
@Slf4j
@Component
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper userMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    XcMenuMapper xcMenuMapper;
    @Override
    //需要重写 loadUserByUsername 方法，返回包装了用户信息的UserDetails对象
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto=null;
        try {
            authParamsDto=JSON.parseObject(s,AuthParamsDto.class);
        } catch (Exception e) {
            throw new ZHXYException("认证请求的数据格式不对！");
        }
        //获取认证类型及对应的认证类
        String authType = authParamsDto.getAuthType();
        AuthService authService = applicationContext.getBean(authType + "_authService", AuthService.class);
        //进行具体的认证逻辑
        XcUserExt userExt = authService.execute(authParamsDto);

        return getUserPrincipal(userExt);
    }

    /***
     * 将用户信息包装成SpringSecurity下的UserDetails对象
     */
    public UserDetails getUserPrincipal(XcUserExt user){
        //获取用户权限
        List<String> permissions = xcMenuMapper.selectPermissionByUserId(user.getId());
        String[] permissionsArray = permissions.toArray(new String[0]);

        String password = user.getPassword();

        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(permissionsArray).build();

        return userDetails;
    }
}

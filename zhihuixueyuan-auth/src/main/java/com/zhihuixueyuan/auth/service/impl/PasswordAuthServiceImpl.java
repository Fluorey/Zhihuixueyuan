package com.zhihuixueyuan.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhihuixueyuan.auth.feignClient.CheckCodeClient;
import com.zhihuixueyuan.auth.service.AuthService;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.ucenter.mapper.XcUserMapper;
import com.zhihuixueyuan.ucenter.model.dto.AuthParamsDto;
import com.zhihuixueyuan.ucenter.model.dto.XcUserExt;
import com.zhihuixueyuan.ucenter.model.po.XcUser;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/***
 * 账号名密码认证类
 */
@Service("password_authService")
public class PasswordAuthServiceImpl implements AuthService {
    @Autowired
    XcUserMapper userMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    CheckCodeClient checkCodeClient;
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        String username=authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> eq = new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, username);
        XcUser user = userMapper.selectOne(eq);

        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        //开发时为了方便，先把验证码的验证屏蔽掉
//        if(StringUtils.isEmpty(checkcode)||StringUtils.isEmpty(checkcodekey)){
//            ZHXYException.cast("验证码不能为空！");
//        }
//        if (!checkCodeClient.verify(checkcodekey,checkcode)) {
//            ZHXYException.cast("验证码错误！");
//        }

        if(user==null)
            ZHXYException.cast("用户名为空或不存在！");
        String passwordForm = authParamsDto.getPassword();
        String passwordDB = user.getPassword();
        if(!passwordEncoder.matches(passwordForm,passwordDB))
            ZHXYException.cast("用户名或密码错误！");
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user,xcUserExt);
        return xcUserExt;
    }
}

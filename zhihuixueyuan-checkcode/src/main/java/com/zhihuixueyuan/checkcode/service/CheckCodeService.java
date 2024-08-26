package com.zhihuixueyuan.checkcode.service;

import com.zhihuixueyuan.checkcode.model.CheckCodeParamsDto;
import com.zhihuixueyuan.checkcode.model.CheckCodeResultDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @description 验证码接口
 */
public interface CheckCodeService {


    /**
     * @description 生成验证码
    */
     CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto);

     /**
      * @description 校验验证码
     */
    public boolean verify(String key, String code);


    /**
     * @description 验证码生成器
    */
    public interface CheckCodeGenerator{
        /**
         * 验证码生成
         * @return 验证码
         */
        String generate(int length);
        

    }

    /**
     * @description key生成器
     */
    public interface KeyGenerator{

        /**
         * key生成
         * @return 验证码
         */
        String generate(String prefix);
    }


    /**
     * @description 验证码存储
     */
    public interface CheckCodeStore{

        /**
         * @description 向缓存设置key
        */
        void set(String key, String value, Integer expire);

        String get(String key);

        void remove(String key);
    }
}

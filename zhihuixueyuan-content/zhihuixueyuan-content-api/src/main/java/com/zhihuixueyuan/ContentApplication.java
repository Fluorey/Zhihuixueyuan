package com.zhihuixueyuan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 课程内容管理服务的启动类
 */
@SpringBootApplication
@EnableFeignClients(basePackages={"com.zhihuixueyuan.content.feignClient"})
public class ContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class,args);
    }
}

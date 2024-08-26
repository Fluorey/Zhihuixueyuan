package com.zhihuixueyuan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages={"com.zhihuixueyuan.*.feignclient"})
@SpringBootApplication
public class LearningApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningApiApplication.class, args);
    }

}

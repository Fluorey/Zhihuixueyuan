package com.zhihuixueyuan.orders.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "pay.alipay")
public class PayConfig {
    public static String APP_ID;
    public static String APP_PRIVATE_KEY;
    public static String ALIPAY_PUBLIC_KEY;
}

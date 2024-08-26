package com.zhihuixueyuan.media.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/***
 * minio配置
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String endpoint;
    private String accessKey;
    private String secretKey;

    @Bean
    public MinioClient minioClient() {

        MinioClient minioClient = MinioClient
                                    .builder()
                                    .endpoint(endpoint)
                                    .credentials(accessKey, secretKey)
                                    .build();
        return minioClient;
    }
}

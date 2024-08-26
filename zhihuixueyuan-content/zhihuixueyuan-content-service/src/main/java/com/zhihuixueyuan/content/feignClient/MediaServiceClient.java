package com.zhihuixueyuan.content.feignClient;

import com.zhihuixueyuan.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;

/***
 * 媒资管理服务远程接口
 */
@FeignClient(value = "media-api",configuration = MultipartSupportConfig.class)
public interface MediaServiceClient {
}

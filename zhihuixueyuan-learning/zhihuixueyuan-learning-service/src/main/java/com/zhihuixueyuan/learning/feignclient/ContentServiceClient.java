package com.zhihuixueyuan.learning.feignclient;

import com.zhihuixueyuan.content.model.po.CoursePublish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description 内容管理远程接口
 */
@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentServiceClient {

    @ResponseBody
    @GetMapping("/content/queryCoursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId);

}

package com.zhihuixueyuan.learning.api;

import com.zhihuixueyuan.base.model.RestResponse;
import com.zhihuixueyuan.content.model.po.CoursePublish;
import com.zhihuixueyuan.learning.feignclient.ContentServiceClient;
import com.zhihuixueyuan.learning.service.LearningService;
import com.zhihuixueyuan.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 我的学习接口
 */
@Api(value = "学习过程管理接口", tags = "学习过程管理接口")
@Slf4j
@RestController
public class MyLearningController {
    @Autowired
    LearningService learningService;
    @Autowired
    ContentServiceClient contentServiceClient;


    @ApiOperation("获取视频")
    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getvideo(@PathVariable("courseId") Long courseId, @PathVariable("courseId") Long teachplanId, @PathVariable("mediaId") String mediaId) {
        //登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if(user != null){
            userId = user.getId();
        }
        //获取视频
        return learningService.getVideo(userId,courseId,teachplanId,mediaId);

    }
    @ApiOperation("远程查询课程发布信息")
    @GetMapping("/open/learn/getCoursePublishInfo/{courseId}")
    public CoursePublish queryCoursePublish(@PathVariable("courseId") Long courseId) {

        return contentServiceClient.getCoursepublish(courseId);

    }

}

package com.zhihuixueyuan.learning.service.impl;

import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.model.RestResponse;
import com.zhihuixueyuan.content.model.po.CoursePublish;
import com.zhihuixueyuan.learning.feignclient.ContentServiceClient;
import com.zhihuixueyuan.learning.feignclient.MediaServiceClient;
import com.zhihuixueyuan.learning.model.dto.XcCourseTablesDto;
import com.zhihuixueyuan.learning.service.LearningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class LearningServiceImpl implements LearningService {
    @Autowired
    ContentServiceClient contentServiceClient;
    @Autowired
    MediaServiceClient mediaServiceClient;
    @Autowired
    MyCourseTablesServiceImpl myCourseTablesService;
    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        //查询课程
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null) {
            RestResponse.validFail("课程不存在");
        }
        //用户id不为空，则进行资格判断
        if(!StringUtils.isEmpty(userId)){
            String learningStatus = myCourseTablesService.getLearningStatus(userId, courseId).getLearnStatus();
            if("702001".equals(learningStatus)){
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }else if ("702002".equals(learningStatus)) {
                return RestResponse.validFail("无法观看，由于没有选课或选课后没有支付");
            } else if ("702003".equals(learningStatus)) {
                return RestResponse.validFail("您的选课已过期需要申请续期或重新支付");
            }
        }
        //如果未登录或者未选课，则判断是否为免费课程
        String charge = coursepublish.getCharge();
        if("201000".equals(charge)){
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }
        return RestResponse.validFail("请登录并购买课程后继续学习");
    }
}



package com.zhihuixueyuan.learning.service;

import com.zhihuixueyuan.base.model.RestResponse;

/***
 * 在线学习相关接口
 */
public interface LearningService {

    /***
     * 根据用户id、课程id、教学计划id和媒资id，先判断是否有学习资格，有的话就返回媒资url
     */
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}

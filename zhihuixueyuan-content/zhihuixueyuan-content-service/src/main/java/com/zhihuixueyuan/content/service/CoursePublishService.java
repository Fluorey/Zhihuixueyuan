package com.zhihuixueyuan.content.service;

import com.zhihuixueyuan.content.model.dto.CoursePreviewDto;
import com.zhihuixueyuan.content.model.po.CoursePublish;

/***
 * 课程预览和发布的管理
 */
public interface CoursePublishService {
    /**
     * @description 获取课程预览信息
     * @param courseId 课程id
     * @return com.zhihuixueyuan.content.model.dto.CoursePreviewDto
     * @author Mr.M
     * @date 2022/9/16 15:36
     */
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    /**
     * @description 提交审核
     * @param courseId  课程id
     * @param companyId 机构Id
     * @return void
     */
    public void commitAudit(Long companyId,Long courseId);

    /***
     * 课程发布接口
     * @param companyId 机构id
     * @param courseId 课程id
     */
    public void publish(Long companyId,Long courseId);

    /***
     * 查询课程发布信息
     */
    public CoursePublish getCoursePublish(Long courseId);

    /**
     * @description 查询缓存中的课程信息
     */
    public CoursePublish getCoursePublishCache(Long courseId);
}

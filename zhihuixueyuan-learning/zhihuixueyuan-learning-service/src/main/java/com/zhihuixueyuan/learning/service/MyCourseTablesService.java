package com.zhihuixueyuan.learning.service;

import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.learning.model.dto.MyCourseTableParams;
import com.zhihuixueyuan.learning.model.dto.XcChooseCourseDto;
import com.zhihuixueyuan.learning.model.dto.XcCourseTablesDto;
import com.zhihuixueyuan.learning.model.po.XcCourseTables;

/***
 * 选课相关的接口
 */
public interface MyCourseTablesService {

    /***
     * 添加选课记录
     */
    public XcChooseCourseDto addChooseCourse(String userId,Long courseId);

    /**
     * @description 判断学习资格
     */
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    /***
     * 更新选课记录（把选课记录的状态字段改为“选课成功”，并且对于付费课程还需向课程表添加记录）
     */
    public boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * @description 我的课程表
     */
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params);
}

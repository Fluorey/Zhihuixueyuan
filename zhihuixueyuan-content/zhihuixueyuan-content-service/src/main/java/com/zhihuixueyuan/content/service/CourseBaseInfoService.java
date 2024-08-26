package com.zhihuixueyuan.content.service;

import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.content.model.dto.AddCourseDto;
import com.zhihuixueyuan.content.model.dto.CourseBaseInfoDto;
import com.zhihuixueyuan.content.model.dto.EditCourseDto;
import com.zhihuixueyuan.content.model.dto.QueryCourseParamsDto;
import com.zhihuixueyuan.content.model.po.CourseBase;

/***
 * 课程信息管理接口
 */
public interface CourseBaseInfoService {
    /***
     * 分页查询课程
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return 返回查询的课程集合
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /***
     * 新增课程
     * @param companyId 机构 id
     * @param addCourseDto 新增课程的信息
     * @return 返回新增课程的信息
     */
    public CourseBaseInfoDto createCourseBase(long companyId, AddCourseDto addCourseDto);

    /***
     * 根据课程ID查找课程详细信息（基本信息+营销信息）
     * @param courseId 课程ID
     * @return 返回课程详细信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(long courseId);
    /***
     * 修改课程信息
     * @param companyId 机构id
     * @param editCourseDto 修改后的课程信息
     * @return 返回修改后的课程信息
     */
    public CourseBaseInfoDto updateCourseBase(long companyId, EditCourseDto editCourseDto);
}

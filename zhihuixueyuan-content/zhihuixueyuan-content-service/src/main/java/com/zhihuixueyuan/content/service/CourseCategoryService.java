package com.zhihuixueyuan.content.service;

import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.content.model.dto.CourseCategoryTreeDto;
import com.zhihuixueyuan.content.model.dto.QueryCourseParamsDto;
import com.zhihuixueyuan.content.model.po.CourseBase;

import java.util.List;

/***
 * 课程分类相关接口
 */
public interface CourseCategoryService {
    //获取某个课程分类的所有子分类
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}

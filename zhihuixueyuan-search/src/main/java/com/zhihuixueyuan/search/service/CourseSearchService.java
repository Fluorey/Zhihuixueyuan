package com.zhihuixueyuan.search.service;

import com.zhihuixueyuan.base.model.CourseIndex;
import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.search.dto.SearchCourseParamDto;
import com.zhihuixueyuan.search.dto.SearchPageResultDto;

/**
 * @description 课程搜索service
 */
public interface CourseSearchService {


    /**
     * @description 搜索课程列表
     * @param pageParams 分页参数
     * @param searchCourseParamDto 搜索条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.search.po.CourseIndex> 课程列表
     * @author Mr.M
     * @date 2022/9/24 22:45
    */
    SearchPageResultDto<CourseIndex> queryCoursePubIndex(PageParams pageParams, SearchCourseParamDto searchCourseParamDto);

 }

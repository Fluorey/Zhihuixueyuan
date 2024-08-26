package com.zhihuixueyuan.content;

import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.content.model.dto.CourseBaseInfoDto;
import com.zhihuixueyuan.content.model.dto.QueryCourseParamsDto;
import com.zhihuixueyuan.content.model.po.CourseBase;
import com.zhihuixueyuan.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class CourseBaseServiceTests {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseService(){
        //分页参数
        PageParams pageParams = new PageParams(2L, 2L);
        //查询条件
        QueryCourseParamsDto queryCourseParamsDto=new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        queryCourseParamsDto.setAuditStatus("202004");

        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        System.out.println(courseBasePageResult);
    }
    @Test
    public void testCourseBaseInfoQuery(){

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(22L);
        System.out.println(courseBaseInfo);

    }
}

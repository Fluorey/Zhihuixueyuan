package com.zhihuixueyuan.content.api;

import com.zhihuixueyuan.base.exception.ValidationGroups;
import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.model.PageParams;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.content.model.dto.AddCourseDto;
import com.zhihuixueyuan.content.model.dto.CourseBaseInfoDto;
import com.zhihuixueyuan.content.model.dto.EditCourseDto;
import com.zhihuixueyuan.content.model.dto.QueryCourseParamsDto;
import com.zhihuixueyuan.content.model.po.CourseBase;
import com.zhihuixueyuan.content.service.CourseBaseInfoService;
import com.zhihuixueyuan.content.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class CourseBaseController {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    /***
     * 查询课程并分页
     * @param pageParams 分页参数
     * @param queryCourseParams 查询条件
     * @return 返回查询出来的课程
     */
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    @RequestMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams){
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(principal);
        return courseBasePageResult;
    }

    /***
     * 新增课程
     * @param addCourseDto 新增课程的信息
     * @return 返回新增的课程的信息
     */
    @PostMapping("/course/create")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Inster.class}) AddCourseDto addCourseDto){

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String companyIdString = user.getCompanyId();
        if(StringUtils.isEmpty(companyIdString))
            ZHXYException.cast("机构为空！");
        long companyId= Long.parseLong(user.getCompanyId());
        ZHXYException.cast("机构id："+companyId+"...");


        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    /***
     * 根据课程id获取课程的详细信息
     * @param courseId 课程id
     * @return 返回课程的详细信息，包装在CourseBaseInfoDto对象中
     */
    @ApiOperation("根据课程id查询课程基础信息")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable long courseId){
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }


    /***
     * 修改课程信息
     * @param editCourseDto 参数为修改后的课程信息
     * @return 返回修改后的课程信息，包装在CourseBaseInfoDto对象中
     */
    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated(ValidationGroups.Update.class) EditCourseDto editCourseDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String companyIdString = user.getCompanyId();
        if(StringUtils.isEmpty(companyIdString))
            ZHXYException.cast("机构为空！");
        long companyId= Long.parseLong(user.getCompanyId());
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDto);
    }
}


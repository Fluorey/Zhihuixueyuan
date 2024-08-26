package com.zhihuixueyuan.learning.api;

import com.zhihuixueyuan.base.exception.ZHXYException;
import com.zhihuixueyuan.base.model.PageResult;
import com.zhihuixueyuan.learning.model.dto.MyCourseTableParams;
import com.zhihuixueyuan.learning.model.dto.XcChooseCourseDto;
import com.zhihuixueyuan.learning.model.dto.XcCourseTablesDto;
import com.zhihuixueyuan.learning.model.po.XcCourseTables;
import com.zhihuixueyuan.learning.service.MyCourseTablesService;
import com.zhihuixueyuan.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 我的课程表接口
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {
    @Autowired
    MyCourseTablesService myCourseTablesService;


    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user==null) {
            ZHXYException.cast("请登录后再选课！");
        }
        //用户id
        String userId = user.getId();
        return myCourseTablesService.addChooseCourse(userId,courseId);
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user==null) {
            ZHXYException.cast("请先登录！");
        }
        //用户id
        String userId = user.getId();
        return myCourseTablesService.getLearningStatus(userId,courseId);
    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {

        return myCourseTablesService.mycoursetables(params);
    }

}

package com.zhihuixueyuan.content.api;

import com.alibaba.fastjson.JSON;
import com.zhihuixueyuan.content.mapper.CourseBaseMapper;
import com.zhihuixueyuan.content.model.dto.CourseBaseInfoDto;
import com.zhihuixueyuan.content.model.dto.CoursePreviewDto;
import com.zhihuixueyuan.content.model.dto.TeachplanDto;
import com.zhihuixueyuan.content.model.po.CourseBase;
import com.zhihuixueyuan.content.model.po.CoursePublish;
import com.zhihuixueyuan.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/***
 * 课程发布接口
 */
@RestController
public class CoursePublishController {
    @Autowired
    CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public CoursePreviewDto preview(@PathVariable("courseId") Long courseId){

        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        return coursePreviewInfo;
    }

    /***
     * 提交课程审核
     * @param courseId 课程id
     */
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);

    }

    /***
     * 课程发布
     * @param courseId 课程id
     */
    @ApiOperation("课程发布")
    @PostMapping ("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId,courseId);

    }

    /***
     * 查询发布课程的信息
     */
    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/queryCoursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId) {
        CoursePublish coursePublish = coursePublishService.getCoursePublishCache(courseId);
        return coursePublish;
    }
    /***
     * 获取已发布课程的课程预览信息
     */
    @ApiOperation("获取已发布课程的课程预览信息")
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublishPre(@PathVariable("courseId") Long courseId){
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //从发布表查数据
        CoursePublish coursePublish=coursePublishService.getCoursePublishCache(courseId);
        if(coursePublish==null){
            return coursePreviewDto;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish,courseBaseInfoDto);
        String teachplan = coursePublish.getTeachplan();
        List<TeachplanDto> teachplanDtos = JSON.parseArray(teachplan, TeachplanDto.class);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachplanDtos);

        return coursePreviewDto;

    }
}

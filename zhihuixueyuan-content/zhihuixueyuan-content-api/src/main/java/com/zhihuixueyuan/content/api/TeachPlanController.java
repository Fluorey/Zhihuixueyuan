package com.zhihuixueyuan.content.api;

import com.zhihuixueyuan.content.model.dto.BindTeachPlanMediaDto;
import com.zhihuixueyuan.content.model.dto.SaveTeachPlanDto;
import com.zhihuixueyuan.content.model.dto.TeachplanDto;
import com.zhihuixueyuan.content.model.po.TeachplanMedia;
import com.zhihuixueyuan.content.service.TeachPlanService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/***
 * 课程计划管理相关的接口
 */

@RestController
public class TeachPlanController {
    @Autowired
    TeachPlanService teachPlanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTeachPlanTreeNodes(@PathVariable long courseId){

        return teachPlanService.queryTeachPlanTreeNodes(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan/save")
    public void saveTeachplan( @RequestBody SaveTeachPlanDto teachplan){

        teachPlanService.saveTeachPlan(teachplan);
    }

    @ApiOperation("课程计划与媒资绑定")
    @PostMapping("teachplan/associate/media")
    public TeachplanMedia associateMedia(@RequestBody BindTeachPlanMediaDto bindTeachPlanMediaDto){
        return teachPlanService.associateMedia(bindTeachPlanMediaDto);
    }
}

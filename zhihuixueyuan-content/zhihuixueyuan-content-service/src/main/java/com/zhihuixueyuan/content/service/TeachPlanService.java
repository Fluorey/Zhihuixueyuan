package com.zhihuixueyuan.content.service;

import com.zhihuixueyuan.content.model.dto.BindTeachPlanMediaDto;
import com.zhihuixueyuan.content.model.dto.SaveTeachPlanDto;
import com.zhihuixueyuan.content.model.dto.TeachplanDto;
import com.zhihuixueyuan.content.model.po.TeachplanMedia;

import java.util.List;

/***
 * 课程计划相关接口
 */
public interface TeachPlanService {
    /***
     * 根据课程id查询课程的教学计划
     * @param courseId 课程id
     * @return 返回对应的课程计划（为TeachplanDto对象的集合）
     */
    public List<TeachplanDto> queryTeachPlanTreeNodes(long courseId);

    /***
     * 新增或修改课程计划
     * @param saveTeachPlanDto 待新增或修改后的课程计划
     */
    public void saveTeachPlan(SaveTeachPlanDto saveTeachPlanDto);

    /***
     * 课程计划与媒资信息进行绑定的接口
     * @param bindTeachPlanMediaDto 所传参数包含课程计划id、媒资id与媒资文件名
     */
    public TeachplanMedia associateMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto);
}

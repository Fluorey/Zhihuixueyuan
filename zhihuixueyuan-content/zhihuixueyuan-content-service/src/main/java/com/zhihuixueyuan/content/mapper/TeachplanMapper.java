package com.zhihuixueyuan.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuixueyuan.content.model.dto.CourseCategoryTreeDto;
import com.zhihuixueyuan.content.model.dto.TeachplanDto;
import com.zhihuixueyuan.content.model.po.Teachplan;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    /***
     * 根据课程id查找对应的课程计划
     * @param courseId 课程id
     * @return 返回TeachPlanDto类型的集合，表示查到的全部课程计划
     */
    public List<TeachplanDto> selectTeachPlanTreeNodes(long courseId);
}

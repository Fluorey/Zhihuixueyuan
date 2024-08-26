package com.zhihuixueyuan.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuixueyuan.content.model.po.CourseTeacher;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 课程-教师关系表 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository
public interface CourseTeacherMapper extends BaseMapper<CourseTeacher> {

}

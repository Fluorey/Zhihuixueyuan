package com.zhihuixueyuan.content.mapper;

import java.util.*;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuixueyuan.content.model.dto.CourseCategoryTreeDto;
import com.zhihuixueyuan.content.model.po.CourseCategory;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Repository
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {

    //根据某个课程分类的id递归查询所有子分类
    public List<CourseCategoryTreeDto> selectTreeNodes(String id);

    //根据某个课程分类的id查询对应的分类名称
    public String queryName(String id);
}

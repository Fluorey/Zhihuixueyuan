package com.zhihuixueyuan.content.service.Impl;

import com.zhihuixueyuan.content.mapper.CourseCategoryMapper;
import com.zhihuixueyuan.content.model.dto.CourseCategoryTreeDto;
import com.zhihuixueyuan.content.model.po.CourseCategory;
import com.zhihuixueyuan.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/***
 * 课程分类相关接口的实现类
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {

        //从数据库获取所有分类节点（包括id自身对应的分类）
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //进行封装处理
        //在map中以分类id为key，以分类节点本身为value进行保存
        Map<String, CourseCategoryTreeDto> mp = courseCategoryTreeDtos.stream().collect(Collectors.toMap(CourseCategory::getId, item -> item));
        //遍历courseCategoryTreeDtos，维护每个节点的子节点
        courseCategoryTreeDtos.forEach(item->{
            CourseCategoryTreeDto parent = mp.get(item.getParentid());
            if(parent!=null){
                if(parent.getChildrenTreeNodes()==null){
                    parent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                parent.getChildrenTreeNodes().add(item);
            }
        });

        return mp.get(id).getChildrenTreeNodes();
    }
}

package com.zhihuixueyuan.content.model.dto;

import com.zhihuixueyuan.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}

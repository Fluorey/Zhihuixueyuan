package com.zhihuixueyuan.content.model.dto;

import com.zhihuixueyuan.content.model.po.Teachplan;
import com.zhihuixueyuan.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/***
 * 课程计划树型结构dto
 */
@Data
public class TeachplanDto extends Teachplan {
    //媒资信息
    TeachplanMedia teachplanMedia;

    //子节点
    List<TeachplanDto> teachPlanTreeNodes;
}

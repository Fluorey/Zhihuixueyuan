package com.zhihuixueyuan.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/***
 * 课程计划绑定媒资时用到的模型类
 */
@Data
@ApiModel(value="BindTeachPlanMediaDto",description = "教学计划-媒资绑定模型类")
public class BindTeachPlanMediaDto {
    @ApiModelProperty(value="媒资Id",required = true)
    private String mediaId;
    @ApiModelProperty(value="媒资文件名",required = true)
    private String mediaFilename;
    @ApiModelProperty(value="课程计划id",required = true)
    private Long teachplanId;
}

package com.zhihuixueyuan.content.model.dto;

import com.zhihuixueyuan.content.model.po.CourseBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程基本信息dto（主要包括继承自CourseBase的课程基本信息以及营销信息）
 */
@Data
public class CourseBaseInfoDto extends CourseBase {


 /**
  * 收费规则，对应数据字典
  */
 private String charge;

 /**
  * 价格
  */
 private Float price;


 /**
  * 原价
  */
 private Float originalPrice;

 /**
  * 咨询qq
  */
 private String qq;

 /**
  * 微信
  */
 private String wechat;

 /**
  * 电话
  */
 private String phone;

 /**
  * 有效期天数
  */
 private Integer validDays;

 /**
  * 大分类名称
  */
 private String mtName;

 /**
  * 小分类名称
  */
 private String stName;

}

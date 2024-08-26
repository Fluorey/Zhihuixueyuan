package com.zhihuixueyuan.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {

    //当前页号
    private long pageNo=1L;
    //每页记录数
    private long pageSize=10L;
}

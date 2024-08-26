package com.zhihuixueyuan.orders.model.dto;

import com.zhihuixueyuan.orders.model.po.XcPayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * @description 支付记录dto
 */
@Data
@ToString
public class PayRecordDto extends XcPayRecord {

    //二维码
    private String qrcode;

}

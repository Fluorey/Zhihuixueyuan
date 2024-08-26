package com.zhihuixueyuan.orders.service;

import com.zhihuixueyuan.messagesdk.model.po.MqMessage;
import com.zhihuixueyuan.orders.model.dto.AddOrderDto;
import com.zhihuixueyuan.orders.model.dto.PayRecordDto;
import com.zhihuixueyuan.orders.model.dto.PayStatusDto;
import com.zhihuixueyuan.orders.model.po.XcPayRecord;

/***
 * 订单相关接口
 */
public interface OrderService {
    /***
     * 创建订单
     */
    public PayRecordDto createOder(String userId, AddOrderDto addOrderDto);

    /**
     * @description 从支付表查询支付记录（在重复扫码支付时需要判断该支付记录是否已经处于“已支付”状态）
     */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果，并且如果支付成功，则需要保存支付结果到订单表和支付记录表
     */
    public PayRecordDto queryPayResult(String payNo);

    /**
     * @description 保存支付宝支付结果
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto) ;

    /**
     * 发送支付完成通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);
}
